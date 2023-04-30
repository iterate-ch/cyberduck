﻿// 
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

using System;
using System.Collections.Generic;
using System.Globalization;
using System.Text;
using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.transfer;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Controller.Threading;
using Ch.Cyberduck.Ui.Winforms.Controls;
using java.util;
using StructureMap;
using TransferStatus = Ch.Cyberduck.Ui.Winforms.Controls.TransferStatus;

namespace Ch.Cyberduck.Ui.Controller
{
    public class ProgressController : WindowController<IProgressView>, TransferListener, ProgressListener
    {
        /**
         * Formatter for file size
        */
        private readonly SizeFormatter _sizeFormatter = SizeFormatterFactory.get();
        private readonly Transfer _transfer;

        public ProgressController(Transfer transfer)
        {
            _transfer = transfer;
            View = ObjectFactory.GetInstance<IProgressView>();
            Init();
        }

        public override void message(string message)
        {
            string text;
            if (Utils.IsBlank(message))
            {
                // Do not display any progress text when transfer is stopped
                Date timestamp = _transfer.getTimestamp();
                if (null != timestamp)
                {
                    text = UserDateFormatterFactory.get().getLongFormat(timestamp.getTime());
                }
                else
                {
                    text = String.Empty;
                }
            }
            else
            {
                text = message;
            }
            AsyncDelegate d = () => View.MessageText = text;
            invoke(new SimpleDefaultMainAction(this, d));
        }

        public void transferDidStart(Transfer t)
        {
            AsyncDelegate d = delegate
            {
                View.TransferStatus = TransferStatus.InProgress;
                View.ProgressIndeterminate = true;
                Progress(String.Empty);
                View.StatusText = String.Empty;
            };
            invoke(new SimpleDefaultMainAction(this, d));
        }

        public void transferDidStop(Transfer t)
        {
            AsyncDelegate d = delegate
            {
                View.ProgressIndeterminate = true;
                message(String.Empty);
                Progress(String.Format(LocaleFactory.localizedString("{0} of {1}"),
                    _sizeFormatter.format(_transfer.getTransferred().longValue()),
                    _sizeFormatter.format(_transfer.getSize().longValue())));
                View.StatusText =
                    LocaleFactory.localizedString(
                        LocaleFactory.localizedString(
                            _transfer.isComplete()
                                ? String.Format("{0} complete",
                                    CultureInfo.CurrentCulture.TextInfo.ToTitleCase(
                                        _transfer.getType().name()))
                                : "Transfer incomplete", "Status"), "Status");
                View.TransferStatus = t.isComplete() ? TransferStatus.Complete : TransferStatus.Incomplete;
                UpdateOverallProgress();
            };
            invoke(new SimpleDefaultMainAction(this, d));
        }

        public void transferDidProgress(Transfer t, TransferProgress tp)
        {
            Progress(tp.getProgress());
            AsyncDelegate d = delegate
            {
                double transferred = _transfer.getTransferred().longValue();
                double size = _transfer.getSize().longValue();
                if (transferred > size)
                {
                    // clamp transferred to size
                    transferred = size;
                }
                if (transferred > 0 && size > 0)
                {
                    View.ProgressIndeterminate = false;
                    View.ProgressMaximum = 100;
                    // Normalize Value to [0;100]
                    View.ProgressValue = Convert.ToInt32(transferred / size * 100);
                }
                else
                {
                    View.ProgressIndeterminate = true;
                }
                UpdateOverallProgress();
            };
            invoke(new SimpleDefaultMainAction(this, d));
        }

        private void UpdateOverallProgress()
        {
            TransferProgress progress = TransferCollection.defaultCollection().getProgress();
            TransferController.Instance.View.UpdateOverallProgressState(
                TransferCollection.defaultCollection().numberOfRunningTransfers() == 0
                    ? 0
                    : progress.getTransferred().longValue(), progress.getSize().longValue());
        }

        private void Init()
        {
            Progress(String.Format(LocaleFactory.localizedString("{0} of {1}"),
                _sizeFormatter.format(_transfer.getTransferred().longValue()),
                _sizeFormatter.format(_transfer.getSize().longValue())));

            SetIcon();
            SetMessageText();
            SetRootPaths();
            SetTransferStatus();
            View.StatusText =
                LocaleFactory.localizedString(
                    LocaleFactory.localizedString(
                        _transfer.isComplete()
                            ? String.Format("{0} complete",
                                CultureInfo.CurrentCulture.TextInfo.ToTitleCase(_transfer.getType().name()))
                            : "Transfer incomplete", "Status"), "Status");
        }

        private void Progress(String message)
        {
            invoke(new SimpleDefaultMainAction(this, () => View.ProgressText = message));
        }

        private void SetRootPaths()
        {
            List items = _transfer.getRoots();
            IList<string> roots = new List<string>();
            for (int i = 0; i < items.size(); i++)
            {
                TransferItem item = (TransferItem) items.get(i);
                if (i == 0)
                {
                    if (items.size() > 1)
                    {
                        roots.Add(String.Format("{0} ({1} more)", item.remote.getName(), items.size() - 1));
                    }
                    else
                    {
                        roots.Add(item.remote.getName());
                    }
                }
                else
                {
                    roots.Add(item.remote.getName());
                }
            }
            View.PopulateRoots(roots);
        }

        private void SetTransferStatus()
        {
            if (_transfer.isRunning())
            {
                View.TransferStatus = TransferStatus.InProgress;
            }
            else
            {
                View.TransferStatus = _transfer.isComplete() ? TransferStatus.Complete : TransferStatus.Incomplete;
            }
        }

        private void SetMessageText()
        {
            message(String.Empty);
        }

        private void SetIcon()
        {
            if (_transfer is DownloadTransfer)
            {
                View.TransferDirection = TransferDirection.Download;
            }
            else if (_transfer is UploadTransfer)
            {
                View.TransferDirection = TransferDirection.Upload;
            }
            else if (_transfer is CopyTransfer)
            {
                View.TransferDirection = TransferDirection.Upload;
            }
            else if (_transfer is SyncTransfer)
            {
                View.TransferDirection = TransferDirection.Sync;
            }
        }
    }
}
