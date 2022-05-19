using ch.cyberduck.core;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.worker;
using Ch.Cyberduck.Core.Refresh.Models;
using DynamicData;
using java.util;
using ReactiveUI;
using ReactiveUI.Fody.Helpers;
using System;
using System.ComponentModel;
using System.Reactive.Linq;
using System.Threading.Tasks;
using Observable = System.Reactive.Linq.Observable;
using Unit = System.Reactive.Unit;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Info
{
    public class VersionsViewModel : ReactiveObject
    {
        private readonly ObservableAsPropertyHelper<VersionViewModel> selectedVersionProperty;
        private readonly SourceList<VersionModel> versions = new();
        private readonly IObservableCache<VersionViewModel, VersionModel> viewModelCache;

        public VersionsViewModel(Controller controller, SessionPool session)
        {
            /* setup tracking */
            viewModelCache = versions.Connect()
                .AddKey(x => x)
                .Transform(x => new VersionViewModel(x))
                .Bind(Versions)
                .AsObservableCache();

            this.WhenAnyValue(x => x.SelectedVersionValue)
                .Select(x => x switch
                {
                    null => Observable.Return(default(VersionViewModel)),
                    _ => viewModelCache.WatchValue(x)
                })
                .Switch().ToProperty(this, nameof(SelectedVersion), out selectedVersionProperty);

            /* setup commands */
            Select = ReactiveCommand.CreateFromTask(async (Path file) =>
            {
                // handle multiple selection
                if (file == null)
                {
                    Enabled = false;
                    return;
                }

                TaskCompletionSource<AttributedList> result = new();
                controller.background(
                    new WorkerBackgroundAction(
                        controller, session, new VersionsWorkerImpl(file, new DisabledListProgressListener(), result)));
                try
                {
                    Busy = true;
                    var versions = await result.Task;
                    this.versions.Edit(u =>
                    {
                        u.Clear();
                        Iterator versionIterator = versions.iterator();
                        try
                        {
                            while (versionIterator.hasNext())
                            {
                                Path path;
                                try
                                {
                                    path = (Path)versionIterator.next();
                                }
                                catch (Exception)
                                {
                                    // Catch next
                                    continue;
                                }

                                u.Add(new VersionModel(path));
                            }
                        }
                        catch (Exception)
                        {
                            // Catch hasNext
                        }
                    });
                    Enabled = true;
                }
                finally
                {
                    Busy = false;
                }
            });
        }

        [Reactive]
        public bool Busy { get; set; }

        [Reactive]
        public bool Enabled { get; private set; }

        public ReactiveCommand<Path, Unit> Select { get; }

        public VersionViewModel SelectedVersion
        {
            get => selectedVersionProperty.Value;
            set => SelectedVersionValue = value?.Model;
        }

        public BindingList<VersionViewModel> Versions { get; } = new();

        [Reactive]
        private VersionModel SelectedVersionValue { get; set; }

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
