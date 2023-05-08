using ch.cyberduck.core;
using ch.cyberduck.core.features;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.worker;
using Ch.Cyberduck.Core.Refresh.Services;
using DynamicData;
using DynamicData.Binding;
using java.util;
using ReactiveMarbles.ObservableEvents;
using ReactiveUI;
using Splat;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Linq;
using System.Reactive;
using System.Reactive.Disposables;
using System.Reactive.Linq;
using System.Threading.Tasks;
using System.Windows.Controls;
using ArrayList = java.util.ArrayList;
using Locale = ch.cyberduck.core.i18n.Locale;
using Observable = System.Reactive.Linq.Observable;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Info
{
    using Preferences = ch.cyberduck.core.preferences.Preferences;

    public class MetadataViewModel : ReactiveObject, Worker.RecursiveCallback
    {
        private readonly ObservableAsPropertyHelper<bool> busy;
        private readonly Controller controller;
        private readonly Metadata feature;
        private readonly ObservableCollectionExtended<Entry> metadata = new();
        private readonly ArrayList pathsJava = new();
        private readonly SessionPool session;
        private bool adding;
        private bool discard;
        private IEnumerable<Path> paths;

        public ReactiveCommand<MetadataTemplateProvider.Template, Unit> AddMetadata { get; }

        public bool Busy => busy.Value;

        public Interaction<Entry, Unit> EditItem { get; } = new();

        public ReactiveCommand<Unit, Map> Load { get; }

        public ObservableCollection<Entry> Metadata => metadata;

        public ReadOnlyCollection<Control> MetadataMenuItems { get; }

        public IEnumerable<Path> Paths
        {
            get => paths;
            set
            {
                paths = value;
                pathsJava.clear();
                foreach (var item in value)
                {
                    pathsJava.add(item);
                }
            }
        }

        public Interaction<RecurseContext, bool> Recurse { get; } = new();

        public ReactiveCommand<Unit, Unit> Save { get; }

        public MetadataViewModel(Metadata feature, Controller controller, SessionPool session)
        {
            (this.feature, this.controller, this.session) = (feature, controller, session);
            var locale = Locator.Current.GetService<Locale>();
            var preferences = Locator.Current.GetService<Preferences>();
            var provider = Locator.Current.GetService<MetadataTemplateProvider>();

            Load = ReactiveCommand.CreateFromTask(OnLoadAsync);
            Save = ReactiveCommand.CreateFromTask(OnSaveAsync);
            AddMetadata = ReactiveCommand.Create<MetadataTemplateProvider.Template>(OnAddMetadata);
            busy = Observable.CombineLatest(Load.IsExecuting, Save.IsExecuting, (l, s) => l | s).ToProperty(this, nameof(Busy));

            Save.InvokeCommand(Load);
            var loadScan = Load.Scan((List: metadata, ChangeSet: metadata.ToObservableChangeSet(), Subscriptions: new CompositeDisposable()), (acc, val) =>
            {
                var (List, ChangeSet, Subscriptions) = acc;
                using (List.SuspendNotifications())
                {
                    Subscriptions.Dispose();

                    List.Load(val.entrySet().AsEnumerable<Map.Entry>().Select(s => new Entry(s)));
                }

                return acc with { Subscriptions = new() };
            });

            var changeNotifications = Observable.Create<EventArgs>(outer =>
            {
                SerialDisposable serialDisposable = new();
                return StableCompositeDisposable.Create(serialDisposable, loadScan.Subscribe(inner =>
                {
                    serialDisposable.Disposable = StableCompositeDisposable.Create(
                        inner.List.ObserveCollectionChanges().Select(v => v.EventArgs).SubscribeSafe(outer),
                        inner.ChangeSet.MergeMany(s => s.Events().EditEnding).SubscribeSafe(outer))
                    .DisposeWith(inner.Subscriptions);
                }));
            }).Subscribe(OnChanged);

            MetadataMenuItems = provider.Templates.Select<MetadataTemplateProvider.Template, Control>(s => s switch
            {
                MetadataTemplateProvider.Template template => new MenuItem()
                {
                    Header = template.Name,
                    Command = AddMetadata,
                    CommandParameter = template
                },
                _ => new Separator(),
            }).ToList().AsReadOnly();
        }

        private void OnAddMetadata(MetadataTemplateProvider.Template template)
        {
            adding = true;
            metadata.Add(new(template.Name, template.Create()));
        }

        private void OnChanged(EventArgs obj)
        {
            // "Atomically" set expected state (assign locals, reset fields).
            (bool adding, bool discard, this.adding, this.discard) = (this.adding, this.discard, false, false);
            switch (obj)
            {
                case NotifyCollectionChangedEventArgs and { Action: NotifyCollectionChangedAction.Add }:
                    // Handles all Add-collection changes
                    // - That is they are added through SplitButton-Menu (AddNewMetadata, adding = true)
                    // - Added through the data grid new item placeholder (adding = false)
                    // If they are added through DataGrid then they are temporary, until Commit is called.
                    this.discard = !adding;
                    if (!adding)
                    {
                        return;
                    }

                    break;

                case NotifyCollectionChangedEventArgs:
                    // DataGrid performs sequence
                    // - NCC: Add
                    // - EditEnding: Cancel
                    // - NCC: Remove
                    // To ensure that regular Deletions are accepted,
                    // and the temporary placeholder is not performing
                    // an unnecessary headers update, do nothing here.
                    if (discard)
                    {
                        return;
                    }

                    break;

                case EntryEditEventArgs and { EditAction: DataGridEditAction.Cancel }:
                    // pass on discard from NCC if added from DataGrid
                    this.discard = discard;
                    return;
            }

            // This is only ever reached, if:
            // - AddNewMetadata (NotifyCollectionChanged (Add))
            // - Commit (DataGrid RowEditEnding)
            // - Delete (NotifyCollectionChanged, & !Discard)

            Observable.RunAsync(Save.Execute(), default);
        }

        private Task<Map> OnLoadAsync()
        {
            ReadMetadataWorkerImpl worker = new(pathsJava);
            controller.background(new WorkerBackgroundAction(controller, session, worker));
            return worker.Result;
        }

        private Task OnSaveAsync()
        {
            return Task.Factory.StartNew(Run).Unwrap();

            Task Run()
            {
                HashMap copy = new(metadata.Count);
                foreach (var item in metadata)
                {
                    copy.put(item.Key, item.Value);
                }

                var worker = new WriteMetadataWorkerImpl(pathsJava, copy, this, new DisabledProgressListener());
                controller.background(new WorkerBackgroundAction(controller, session, worker));
                return worker.Task;
            }
        }

        bool Worker.RecursiveCallback.recurse(Path directory, object value)
        {
            return Recurse.Handle(new(directory, value)).Wait();
        }

        public class Entry : ReactiveObject, IEditableObject
        {
            public event EventHandler<EntryEditEventArgs> EditEnding;

            private Entry backup;
            private string display;
            private string key;
            private string value;

            public string Display
            {
                get => display ??= (value ?? $"({LocaleFactory.localizedString("Multiple files")})");
                set => Value = value;
            }

            public string Key
            {
                get => key;
                set => this.RaiseAndSetIfChanged(ref key, value);
            }

            public string Value
            {
                get => value;
                set
                {
                    this.RaiseAndSetIfChanged(ref this.value, value);
                    this.RaiseAndSetIfChanged(ref display, value, nameof(Display));
                }
            }

            public Entry()
                : this(LocaleFactory.localizedString("Custom Header"), "")
            {
            }

            public Entry(Map.Entry entry)
                : this((string)entry.getKey(), (string)entry.getValue())
            {
            }

            public Entry(string key, string value)
            {
                (this.key, this.value) = (key, value);
            }

            protected Entry(Entry original)
                : this(original.key, original.value)
            {
            }

            public void Deconstruct(out string Key, out string Value) => (Key, Value) = (key, value);

            void IEditableObject.BeginEdit()
            {
                backup ??= new(this);
            }

            void IEditableObject.CancelEdit()
            {
                if (backup is not null)
                {
                    ((Key, Value), display) = (backup, null);
                }
                OnEndEdit(EntryEditEventArgs.Cancel);
            }

            void IEditableObject.EndEdit() => OnEndEdit(EntryEditEventArgs.Commit);

            private void OnEndEdit(EntryEditEventArgs args)
            {
                (Entry copy, backup) = (backup, null);
                if (copy is not null)
                {
                    EditEnding?.Invoke(this, args);
                }
            }
        }

        public class EntryEditEventArgs : EventArgs
        {
            public static readonly EntryEditEventArgs Cancel = new(DataGridEditAction.Cancel);

            public static readonly EntryEditEventArgs Commit = new(DataGridEditAction.Commit);

            public DataGridEditAction EditAction { get; }

            public EntryEditEventArgs(DataGridEditAction editAction)
            {
                this.EditAction = editAction;
            }
        }

        public readonly record struct RecurseContext(Path Directory, object Value)
        {
            public readonly Path Directory = Directory;
            public readonly object Value = Value;
        }

        private class ReadMetadataWorkerImpl : ReadMetadataWorker
        {
            private readonly TaskCompletionSource<Map> result = new();

            public Task<Map> Result => result.Task;

            public ReadMetadataWorkerImpl(List files) : base(files)
            {
            }

            public override void cleanup(object result) => this.result.SetResult((Map)result);
        }

        private class WriteMetadataWorkerImpl : WriteMetadataWorker
        {
            private readonly TaskCompletionSource<object> result = new();

            public Task Task => result.Task;

            public WriteMetadataWorkerImpl(List files, Map metadata, RecursiveCallback callback, ProgressListener listener)
                : base(files, metadata, callback, listener)
            {
            }

            public override void cleanup(object result) => this.result.SetResult(default);
        }
    }
}
