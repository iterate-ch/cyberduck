// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
// http://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
// 

using ch.cyberduck.core;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui.core;
using ch.cyberduck.ui.Model;
using ch.cyberduck.ui.ViewModels;
using ch.cyberduck.ui.Views;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Controller.Threading;
using DynamicData;
using DynamicData.Kernel;
using StructureMap;
using System.Windows.Forms;
using System.Windows.Interop;
using System.Windows.Threading;
using static Ch.Cyberduck.Ui.Controller.AsyncController;
using CoreController = ch.cyberduck.core.Controller;
using IWin32Window = System.Windows.Forms.IWin32Window;
using TransferProgress = ch.cyberduck.core.transfer.TransferProgress;

namespace Ch.Cyberduck.Ui.Controller
{
    public class TransferController : AbstractController, TransferListener, IWindowController
    {
        private static TransferController instance;
        private readonly Dispatcher dispatcher;
        private readonly Preferences preferences = PreferencesFactory.get();
        private readonly SourceCache<TransferProgressModel, Transfer> progressCache = new(m => m.Transfer);
        private readonly NativeWindow nativeTransfersWindow = new();
        private readonly TransferCollection transfers = TransferCollection.defaultCollection();
        private TransfersWindow transfersWindow;

        public static TransferController Instance => instance ??= ObjectFactory.GetInstance<TransferController>();

        public IObservableCache<TransferProgressModel, Transfer> Progress => progressCache;

        public TransfersWindow Window => transfersWindow;

        public IWin32Window WindowInterop => nativeTransfersWindow;

        IWin32Window IWindowController.Window => nativeTransfersWindow;

        public TransferController()
        {
            dispatcher = MainController.Application.Dispatcher;
        }

        public static bool ApplicationShouldTerminate()
        {
            if (instance == null)
            {
                return true;
            }

            //Saving state of transfer window
            PreferencesFactory.get().setProperty("queue.window.open.default", instance.transfersWindow is not null);
            if (instance.transfers.numberOfRunningTransfers() == 0)
            {
                return true;
            }

            TaskDialogResult result = default;
            if (result.Button != 0)
            {
                return false;
            }

            // Quit
            foreach (BackgroundAction action in instance.getRegistry())
            {
                action.cancel();
            }

            return true;
        }

        public void CloseWindow()
        {
            if (Window is { } window)
            {
                window.Dispatcher.Invoke(window.Close);
            }
        }

        public override void invoke(MainAction action, bool wait)
        {
            if (!action.isValid())
            {
                return;
            }

            if (wait)
            {
                MainController.Application.SynchronizationContext.Send(
                    d => ((MainAction)d).run(),
                    action);
            }
            else
            {
                MainController.Application.SynchronizationContext.Post(
                    d => ((MainAction)d).run(),
                    action);
            }
        }

        public void Invoke(AsyncDelegate action) => Invoke(action, true);

        public void Invoke(AsyncDelegate action, bool wait)
        {
            invoke(new SimpleDefaultMainAction(this, action), wait);
        }

        public TransferProgressModel Lookup(Transfer transfer)
        {
            return progressCache.Lookup(transfer).ValueOrDefault();
        }

        public void RemoveTransfer(Transfer transfer)
        {
            transfers.remove(transfer);
        }

        public void StartTransfer(Transfer transfer)
        {
            StartTransfer(transfer, new TransferOptions());
        }

        public void StartTransfer(Transfer transfer, TransferOptions options)
        {
            StartTransfer(transfer, options, null);
        }

        public void StartTransfer(Transfer transfer, TransferOptions options, TransferCallback callback)
        {
            if (progressCache.Lookup(transfer) is not
                {
                    HasValue: true,
                    Value: { } progress
                })
            {
                progress = new(transfer);
                progressCache.AddOrUpdate(progress);
            }

            if (!transfers.contains(transfer))
            {
                /* TODO: Queue Size Warn */
                transfers.add(transfer);
            }

            PathCache cache = new(preferences.getInteger("transfer.cache.size"));
            TransferBackgroundAction action = new(
                this,
                transfer.withCache(cache),
                progress,
                options,
                callback);
            progress.Action = action;
            background(action);
        }

        public void ShowWindow()
        {
            dispatcher.Invoke((TransferController controller) =>
            {
                if (controller.transfersWindow is not { } window)
                {
                    controller.transfersWindow = window = new()
                    {
                        DataContext = ObjectFactory.GetInstance<TransfersViewModel>()
                    };

                    window.Closed += (s, e) =>
                    {
                        try
                        {
                            controller.nativeTransfersWindow.ReleaseHandle();
                        }
                        catch { }
                        finally
                        {
                            controller.transfersWindow = null;
                        }
                    };

                    controller.nativeTransfersWindow.AssignHandle(new WindowInteropHelper(window).EnsureHandle());
                }

                window.Show();
                window.Activate();
            }, this);
        }

        void TransferListener.transferDidProgress(Transfer t, TransferProgress tp)
        {
            if (progressCache.Lookup(t) is not
                {
                    HasValue: true,
                    Value: { } progress
                })
            {
                return;
            }

            progress.Refresh(tp);
        }

        void TransferListener.transferDidStart(Transfer t)
        {
            if (progressCache.Lookup(t) is not
                {
                    HasValue: true,
                    Value: { } progress
                })
            {
                progressCache.AddOrUpdate(progress = new TransferProgressModel(t)
                {
                    Action = FindTransferAction(t)
                });
            }

            progress.Refresh(null);
        }

        void TransferListener.transferDidStop(Transfer t)
        {
            if (progressCache.Lookup(t) is not
                {
                    HasValue: true
                })
            {
                return;
            }

            progressCache.Remove(t);
        }

        private TransferBackgroundAction FindTransferAction(Transfer transfer)
        {
            foreach (BackgroundAction action in getRegistry())
            {
                if (action is not TransferBackgroundAction transferAction)
                {
                    continue;
                }

                if (transferAction.getTransfer() != transfer)
                {
                    continue;
                }

                return transferAction;
            }

            return null;
        }

        private class TransferBackgroundAction(
            TransferController controller,
            Transfer transfer,
            ProgressListener listener,
            TransferOptions options,
            TransferCallback callback
        ) : TransferCollectionBackgroundAction(
            controller,
            source: CreateSessionPool(
                controller,
                transfer.getSource(),
                listener),
            destination: CreateSessionPool(
                controller,
                transfer.getDestination(),
                listener),
            transferListener: controller,
            listener: listener,
            transfer, options
        )
        {
            private readonly Preferences preferences = controller.preferences;
            private readonly Transfer transfer = transfer;

            public override void cleanup()
            {
                base.cleanup();
                if (transfer.isComplete()
                    && transfer.isReset()
                    && preferences.getBoolean("queue.window.open.transfer.stop"))
                {
                    if (controller.transfers.numberOfRunningTransfers() == 0)
                    {
                        controller.CloseWindow();
                    }
                }
            }

            public override void init()
            {
                base.init();
                if (preferences.getBoolean("queue.window.open.transfer.start"))
                {
                    controller.ShowWindow();
                }
            }

            public override void finish()
            {
                base.finish();
                if (transfer.isComplete())
                {
                    callback?.complete(transfer);
                }
            }

            private static SessionPool CreateSessionPool(CoreController controller, Host host, ProgressListener listener)
            {
                if (host == null)
                {
                    return SessionPool.DISCONNECTED;
                }

                return SessionPoolFactory.create(controller, host, listener);
            }
        }
    }
}
