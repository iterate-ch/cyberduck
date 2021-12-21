//
// Copyright (c) 2021 iterate GmbH. All rights reserved.
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
using ch.cyberduck.core.exception;
using Ch.Cyberduck.Core.TaskDialog;
using java.util.concurrent;
using System;
using System.Drawing;
using System.Threading;
using System.Threading.Tasks;

namespace Ch.Cyberduck.Core.Native
{
    public static class Dialogs
    {
        public static void AwaitBackgroundAction(CountDownLatch signal, Host bookmark, string title, string message, Icon icon)
        {
            IActiveTaskDialog taskDialog = default;

            CancellationTokenSource cancelToken = new CancellationTokenSource();
            Task task = Wrap(signal, cancelToken.Token).ContinueWith(_ =>
            {
                taskDialog.ClickButton((int)TaskDialogSimpleResult.Ok);
            });

            var result = TaskDialog.TaskDialog.Show(allowDialogCancellation: true,
                customMainIcon: icon,
                commonButtons: TaskDialogCommonButtons.Cancel,
                content: message,
                mainInstruction: title,
                title: BookmarkNameProvider.toString(bookmark),
                showMarqueeProgressBar: true,
                callback: (d, a, o) =>
                {
                    switch (a.Notification)
                    {
                        case TaskDialogNotification.DialogConstructed:
                            taskDialog = d;

                            d.SetProgressBarMarquee(true, 0);
                            break;

                        case TaskDialogNotification.ButtonClicked:
                            return false;
                    }

                    return true;
                });
            cancelToken.Cancel();

            if (result.Result == TaskDialogSimpleResult.Cancel)
            {
                throw new ConnectionCanceledException();
            }
        }

        private static Task Wrap(CountDownLatch signal, CancellationToken cancelToken)
        {
            TaskCompletionSource<bool> completionSource = new TaskCompletionSource<bool>();
            ThreadPool.QueueUserWorkItem(_ =>
            {
                try
                {
                    // This is bad.
                    while (!signal.await(500, TimeUnit.MILLISECONDS))
                    {
                        if (cancelToken.IsCancellationRequested)
                        {
                            completionSource.SetCanceled();
                            return;
                        }
                    }
                    completionSource.SetResult(true);
                }
                catch (Exception e)
                {
                    completionSource.SetException(e);
                }
            });
            return completionSource.Task;
        }
    }
}
