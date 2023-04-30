using ch.cyberduck.core;
using ch.cyberduck.core.features;
using ch.cyberduck.core.local;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.transfer;
using ch.cyberduck.core.worker;
using ch.cyberduck.ui.quicklook;
using Ch.Cyberduck.Core.Refresh.Models;
using DynamicData;
using java.util;
using ReactiveUI;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Reactive.Linq;
using System.Threading.Tasks;
using Observable = System.Reactive.Linq.Observable;
using Unit = System.Reactive.Unit;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Info
{
    public class VersionsViewModel : ReactiveObject
    {
        private readonly ObservableAsPropertyHelper<VersionViewModel> selectedVersionProperty;
        private readonly ReadOnlyObservableCollection<VersionViewModel> versions;
        private readonly IObservableCache<VersionViewModel, VersionModel> viewModelCache;
        private bool busy;
        private Path selection;
        private VersionModel selectedVersionValue;

        public VersionsViewModel(Controller controller, SessionPool session)
        {
            var temporary = TemporaryFileServiceFactory.get();
            var delete = (Delete)session.getFeature(typeof(Delete));
            var versioning = (Versioning)session.getFeature(typeof(Versioning));

            var selectedVersion = this.WhenAnyValue(x => x.SelectedVersionValue).Publish().RefCount();

            /* setup commands */
            Open = ReactiveCommand.Create(() =>
            {
                var f = SelectedVersionValue.Path;
                controller.background(new QuicklookTransferBackgroundAction(
                    controller, QuickLookFactory.get(), session, Collections.singletonList(
                        new TransferItem(f, temporary.create(session.getHost().getUuid(), f)))));
            }, selectedVersion.Select(v => v != null && v.Path.attributes().getPermission().isReadable()));
            Remove = ReactiveCommand.CreateFromTask(async () =>
            {
                var norm = PathNormalizer.normalize(Collections.singletonList(SelectedVersionValue.Path));
                if (norm.size() == 0)
                {
                    return;
                }
                var native = Utils.ConvertFromJavaList<Path>(norm);
                if (!await PromptDelete.Handle(native))
                {
                    return;
                }
                try
                {
                    Busy = true;
                    TaskCompletionSource<object> result = new();
                    controller.background(
                        new AsyncWorkerBackgroundAction(controller, session, result,
                            new DeleteWorker(
                                LoginCallbackFactory.get(controller), norm,
                                new DisabledProgressListener(), false)));
                    await result.Task;
                }
                finally
                {
                    Busy = false;
                }
                await Load.ExecuteIfPossible();
            }, selectedVersion.Select(v => v != null && delete.isSupported(v.Path)));
            Revert = ReactiveCommand.CreateFromTask(async () =>
            {
                try
                {
                    Busy = true;
                    var files = Collections.singletonList(SelectedVersionValue.Path);
                    var native = Utils.ConvertFromJavaList<Path>(files);
                    TaskCompletionSource<object> result = new();
                    controller.background(
                        new AsyncWorkerBackgroundAction(controller, session, result,
                            new RevertWorker(files)));
                    await result.Task;
                    Reverted?.Invoke(native);
                }
                finally
                {
                    Busy = false;
                }
                await Load.ExecuteIfPossible();
            }, selectedVersion.Select(v => v != null && versioning.isRevertable(v.Path)));
            Load = ReactiveCommand.CreateFromTask(async () =>
            {
                try
                {
                    Busy = true;
                    TaskCompletionSource<AttributedList> result = new();
                    controller.background(
                        new WorkerBackgroundAction(
                            controller, session, new VersionsWorkerImpl(Selection, new DisabledListProgressListener(), result)));
                    var versions = await result.Task;

                    return versions.Cast<Path>().Select(p => new VersionModel(p)).AsObservableChangeSet();
                }
                finally
                {
                    Busy = false;
                }
            });

            /* setup tracking */
            viewModelCache = Load.Switch()
                .Transform(x => new VersionViewModel(x))
                .Bind(out versions)
                .AddKey(x => x.Model)
                .AsObservableCache();

            selectedVersionProperty = selectedVersion.Select(x => x switch
            {
                null => Observable.Empty<VersionViewModel>(),
                _ => viewModelCache.WatchValue(x)
            }).Switch().ToProperty(this, nameof(SelectedVersion));
        }

        public delegate void RevertedEventHandler(IList<Path> files);

        public event RevertedEventHandler Reverted;

        public bool Busy
        {
            get => busy;
            set => this.RaiseAndSetIfChanged(ref busy, value);
        }

        public ReactiveCommand<Unit, IObservable<IChangeSet<VersionModel>>> Load { get; }

        public ReactiveCommand<Unit, Unit> Open { get; }

        public Interaction<ICollection<Path>, bool> PromptDelete { get; } = new();

        public ReactiveCommand<Unit, Unit> Remove { get; }

        public ReactiveCommand<Unit, Unit> Revert { get; }

        public VersionViewModel SelectedVersion
        {
            get => selectedVersionProperty.Value;
            set => SelectedVersionValue = value?.Model;
        }

        public Path Selection
        {
            get => selection;
            set => this.RaiseAndSetIfChanged(ref selection, value);
        }

        public ReadOnlyObservableCollection<VersionViewModel> Versions => versions;

        private VersionModel SelectedVersionValue
        {
            get => selectedVersionValue;
            set => this.RaiseAndSetIfChanged(ref selectedVersionValue, value);
        }

        private class AsyncWorkerBackgroundAction : WorkerBackgroundAction
        {
            private readonly TaskCompletionSource<object> completionSource;

            public AsyncWorkerBackgroundAction(Controller controller, SessionPool session, TaskCompletionSource<object> completionSource, Worker worker) : base(controller, session, worker)
            {
                this.completionSource = completionSource;
            }

            public override void cleanup()
            {
                base.cleanup();
                completionSource.SetResult(default);
            }
        }

        private class VersionsWorkerImpl : VersionsWorker
        {
            private readonly TaskCompletionSource<AttributedList> completionSource;

            public VersionsWorkerImpl(Path file, ListProgressListener listener, TaskCompletionSource<AttributedList> completionSource) : base(file, listener)
            {
                this.completionSource = completionSource;
            }

            public override void cleanup(object result) => completionSource.SetResult((AttributedList)result);
        }
    }
}
