using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Reactive.Linq;
using System.Threading;
using System.Threading.Tasks;
using ch.cyberduck.core;
using ch.cyberduck.core.features;
using ch.cyberduck.core.local;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.transfer;
using ch.cyberduck.core.worker;
using ch.cyberduck.ui.quicklook;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using java.util;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Info
{
    public partial class VersionsViewModel(Controller controller, SessionPool session) : ObservableObject
    {
        public delegate void RevertedEventHandler(IList<Path> files);
        public delegate void DeleteConfirmationEventHandler(ConfirmDeleteEventArgs e);

        public event DeleteConfirmationEventHandler OnDeleteConfirmation;
        public event RevertedEventHandler Reverted;

        private readonly Controller controller = controller;
        private readonly Delete delete = session.Feature<Delete>();
        private readonly SessionPool session = session;
        private readonly Versioning versioning = session.Feature<Versioning>();
        [ObservableProperty]
        private bool busy;
        [ObservableProperty]
        [NotifyCanExecuteChangedFor(nameof(DeleteVersionCommand))]
        [NotifyCanExecuteChangedFor(nameof(RevealVersionCommand))]
        [NotifyCanExecuteChangedFor(nameof(RevertVersionCommand))]
        private VersionViewModel selectedVersion;
        [ObservableProperty]
        private Path selection;
        private ReadOnlyCollection<VersionViewModel> versions;
        private TaskNotifier<ReadOnlyCollection<VersionViewModel>> versionsNotifier;

        public ReadOnlyCollection<VersionViewModel> Versions
        {
            get => versions;
            private set => SetProperty(ref versions, value);
        }

        private Task<ReadOnlyCollection<VersionViewModel>> LoadingTask
        {
            get => versionsNotifier;
            set => SetPropertyAndNotifyOnCompletion(ref versionsNotifier, value, OnLoadCompleted);
        }

        public void Load()
        {
            LoadingTask = LoadVersionsAsync(Selection);
        }

        private bool CanDeleteVersion() => SelectedVersion != null && delete.isSupported(SelectedVersion.Model.Path);

        private bool CanRevealVersion() => SelectedVersion != null && SelectedVersion.Model.Path.attributes().getPermission().isReadable();

        private bool CanRevertVersion() => SelectedVersion != null && versioning.isRevertable(SelectedVersion.Model.Path);

        [RelayCommand(CanExecute = nameof(CanDeleteVersion))]
        private async Task OnDeleteVersion()
        {
            var norm = PathNormalizer.normalize(Collections.singletonList(SelectedVersion.Model.Path));
            if (norm.size() == 0)
            {
                return;
            }

            var native = Utils.ConvertFromJavaList<Path>(norm);
            ConfirmDeleteEventArgs e = new(native);
            OnDeleteConfirmation?.Invoke(e);
            if (!e.Result)
            {
                return;
            }

            try
            {
                Busy = true;
                AsyncWorkerBackgroundAction action = new(controller, session,
                        new DeleteWorker(
                            LoginCallbackFactory.get(controller), norm,
                            ProgressListener.noop, false));
                controller.background(action);
                await action.Task;
            }
            finally
            {
                Busy = false;
            }

            await (LoadingTask = LoadVersionsAsync(Selection));
        }

        private void OnLoadCompleted(Task<ReadOnlyCollection<VersionViewModel>> task)
        {
            if (LoadingTask == task && task?.Status == TaskStatus.RanToCompletion)
            {
                Versions = task.Result;
            }
        }

        [RelayCommand(CanExecute = nameof(CanRevealVersion))]
        private void OnRevealVersion()
        {
            var f = SelectedVersion.Model.Path;
            controller.background(new QuicklookTransferBackgroundAction(
                controller, QuickLookFactory.get(), session, TransferQueueFactory.get(), Collections.singletonList(
                    new TransferItem(f, TemporaryFileServiceFactory.get().create(session.getHost().getUuid(), f)))));
        }

        [RelayCommand(CanExecute = nameof(CanRevertVersion))]
        private async Task OnRevertVersion()
        {
            try
            {
                Busy = true;
                var files = Collections.singletonList(SelectedVersion.Model.Path);
                var native = Utils.ConvertFromJavaList<Path>(files);
                AsyncWorkerBackgroundAction action = new(controller, session, new RevertWorker(files));
                controller.background(action);
                await action.Task;
                Reverted?.Invoke(native);
            }
            finally
            {
                Busy = false;
            }

            await (LoadingTask = LoadVersionsAsync(Selection));
        }

        private async Task<ReadOnlyCollection<VersionViewModel>> LoadVersionsAsync(Path path)
        {
            if (path is null)
            {
                return null;
            }

            try
            {
                Busy = true;
                VersionsWorkerImpl worker = new(path, new DisabledListProgressListener());
                controller.background(new WorkerBackgroundAction(controller, session, worker, ProgressListener.noop));
                var result = await worker.Task;
                List<VersionViewModel> viewModels = [];
                foreach (Path version in result)
                {
                    viewModels.Add(new(new(version)));
                }

                return viewModels.AsReadOnly();
            }
            finally
            {
                Busy = false;
            }
        }

        partial void OnSelectionChanged(Path value)
        {
            SelectedVersion = null;
        }

        public sealed class ConfirmDeleteEventArgs(ICollection<Path> files) : EventArgs
        {
            public ICollection<Path> Files { get; } = files;

            public bool Result { get; set; }
        }

        private class AsyncWorkerBackgroundAction(Controller controller, SessionPool session, Worker worker) : WorkerBackgroundAction(controller, session, worker)
        {
            private readonly TaskCompletionSource<object> completionSource = new();

            public Task Task => completionSource.Task;

            public override void cleanup()
            {
                completionSource.SetResult(default);
            }
        }

        private class VersionsWorkerImpl(Path file, ListProgressListener listener) : VersionsWorker(file, listener)
        {
            private readonly TaskCompletionSource<AttributedList> completionSource = new();

            public Task<AttributedList> Task => completionSource.Task;

            public override void cleanup(object result) => completionSource.SetResult((AttributedList)result);
        }
    }
}
