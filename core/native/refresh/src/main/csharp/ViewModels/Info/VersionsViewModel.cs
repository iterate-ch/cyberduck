using ch.cyberduck.core;
using ch.cyberduck.core.features;
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
            var versioning = (Versioning)session.getFeature(typeof(Versioning));

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
            Revert = ReactiveCommand.CreateFromTask(async () =>
            {
                TaskCompletionSource<object> result = new();
                controller.background(
                    new AsyncWorkerBackgroundAction(controller, session, result,
                        new RevertWorker(Collections.singletonList(SelectedVersionValue.Path))));
                await result.Task;
                await Load.ExecuteIfPossible();
            }, Observable.CombineLatest(
                this.WhenAnyValue(v => v.Selection),
                this.WhenAnyValue(v => v.SelectedVersionValue),
                (s, v) => v != null && versioning.isRevertable(s)));
            Load = ReactiveCommand.CreateFromTask(async () =>
            {
                TaskCompletionSource<AttributedList> result = new();
                controller.background(
                    new WorkerBackgroundAction(
                        controller, session, new VersionsWorkerImpl(Selection, new DisabledListProgressListener(), result)));
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
                                // Log exception
                                continue;
                            }

                            u.Add(new VersionModel(path));
                        }
                    }
                    catch (Exception)
                    {
                        // Log exception
                    }
                });
            });
            Load.IsExecuting.ToPropertyEx(this, x => x.Busy);
        }

        [ObservableAsProperty]
        public bool Busy { get; }

        public ReactiveCommand<Unit, Unit> Help { get; }

        public ReactiveCommand<Unit, Unit> Load { get; }

        public ReactiveCommand<Unit, Unit> Open { get; }

        public ReactiveCommand<Unit, Unit> Remove { get; }

        public ReactiveCommand<Unit, Unit> Revert { get; }

        public VersionViewModel SelectedVersion
        {
            get => selectedVersionProperty.Value;
            set => SelectedVersionValue = value?.Model;
        }

        [Reactive]
        public Path Selection { get; set; }

        public BindingList<VersionViewModel> Versions { get; } = new();

        [Reactive]
        private VersionModel SelectedVersionValue { get; set; }

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
