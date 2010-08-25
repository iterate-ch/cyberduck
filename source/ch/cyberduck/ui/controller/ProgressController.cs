// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 
using System;
using System.Collections.Generic;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.ui;
using Ch.Cyberduck.Ui.Controller.Threading;
using Ch.Cyberduck.Ui.Winforms.Serializer;
using ch.cyberduck.core.io;
using java.lang;
using java.util;
using java.util.concurrent;
using org.apache.log4j;
using StructureMap;
using Locale = ch.cyberduck.core.i18n.Locale;
using String = System.String;
using StringBuilder = System.Text.StringBuilder;

namespace Ch.Cyberduck.Ui.Controller
{
    public class ProgressController : WindowController<IProgressView>
    {
        /// <summary>
        /// Keeping track of the current transfer rate
        /// </summary>        
        private readonly Speedometer _meter;

        private readonly Transfer _transfer;

        /// <summary>
        /// The current connection status message
        /// </summary>
        private String _messageText;

        public ProgressController(Transfer transfer)
        {
            _transfer = transfer;
            View = ObjectFactory.GetInstance<IProgressView>();
            _meter = new Speedometer(transfer);
            Init();
        }

        private void Init()
        {
            SetIcon();
            SetProgressText();
            SetMessageText();
            SetStatusText();
            SetRootPaths();

            _transfer.getSession().addProgressListener(new TransferProgressListener(this));
            _transfer.addListener(new TransferAdapter(this));
        }

        private void SetRootPaths()
        {
            List srcRoots = _transfer.getRoots();
            IList<string> roots = new List<string>();
            for (int i = 0; i < srcRoots.size(); i++)
            {
                Path p = (Path) srcRoots.get(i);
                roots.Add(p.getName());
            }
            View.PopulateRoots(roots);
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
            else if (_transfer is SyncTransfer)
            {
                View.TransferDirection = TransferDirection.Sync;
            }
        }

        private void SetMessageText()
        {
            StringBuilder b = new StringBuilder();
            if (null == _messageText)
            {
                // Do not display any progress text when transfer is stopped
                Date timestamp = _transfer.getTimestamp();
                if (null != timestamp)
                {                    
                    _messageText = DateFormatterFactory.instance().getLongFormat(timestamp.getTime());
                }
            }
            if (null != _messageText)
            {
                b.Append(_messageText);
            }
            View.MessageText = b.ToString();
        }

        private void SetStatusText()
        {
            StringBuilder b = new StringBuilder();
            if (!_transfer.isRunning())
            {
                View.TransferStatus = _transfer.isComplete() ? TransferStatus.Complete : TransferStatus.Incomplete;
                if (_transfer is DownloadTransfer)
                {
                    b.Append(_transfer.isComplete()
                                 ? Locale.localizedString("Download complete", "Growl")
                                 : Locale.localizedString("Transfer incomplete", "Status"));
                }
                if (_transfer is UploadTransfer)
                {
                    b.Append(_transfer.isComplete()
                                 ? Locale.localizedString("Upload complete", "Growl")
                                 : Locale.localizedString("Transfer incomplete", "Status"));
                }
                if (_transfer is SyncTransfer)
                {
                    b.Append(_transfer.isComplete()
                                 ? Locale.localizedString("Synchronization complete", "Growl")
                                 : Locale.localizedString("Transfer incomplete", "Status"));
                }
            }
            View.StatusText = b.ToString();
        }

        private void SetProgressText()
        {
            View.ProgressText = _meter.getProgress();
        }

        private class TransferAdapter : ch.cyberduck.core.TransferAdapter
        {
            private const long Delay = 0;
            private const long Period = 500; //in milliseconds
            private static readonly Logger Log = Logger.getLogger(typeof (TransferAdapter).Name);
            private readonly ProgressController _controller;
            private readonly IProgressView _view;

            /// <summary>
            /// Timer to update the progress indicator
            /// </summary>
            private ScheduledFuture _progressTimer;

            public TransferAdapter(ProgressController controller)
            {
                _controller = controller;
                _view = controller.View;
            }

            public override void transferWillStart()
            {
                AsyncDelegate d = delegate
                                      {
                                          _view.TransferStatus = TransferStatus.InProgress;
                                          _view.ProgressIndeterminate = true;
                                          _controller.SetProgressText();
                                          _controller.SetStatusText();
                                      };
                _controller.invoke(new SimpleDefaultMainAction(d));
            }

            public override void transferDidEnd()
            {
                AsyncDelegate d = delegate
                                      {
                                          _view.TransferStatus = TransferStatus.Complete;
                                          _controller._messageText = null;
                                          _controller.SetMessageText();
                                          _controller.SetProgressText();
                                          _controller.SetStatusText();
                                          _view.TransferStatus = _controller._transfer.isComplete()
                                                                     ? TransferStatus.Complete
                                                                     : TransferStatus.Incomplete;
                                          //todo
                                          //filesPopup.itemAtIndex(new NSInteger(0)).setEnabled(transfer.getRoot().getLocal().exists());
                                      };
                _controller.invoke(new SimpleDefaultMainAction(d));
            }

            public override void willTransferPath(Path path)
            {
                _controller._meter.reset();
                _progressTimer = _controller.getTimerPool().scheduleAtFixedRate(new ProgressTimerRunnable(_controller),
                                                                                Delay, Period, TimeUnit.MILLISECONDS);
            }

            public override void didTransferPath(Path path)
            {
                _progressTimer.cancel(false);
                _controller._meter.reset();
            }

            public override void bandwidthChanged(BandwidthThrottle bandwidth)
            {
                _controller._meter.reset();
            }

            private class ProgressTimerRunnable : Runnable
            {
                private readonly ProgressController _controller;

                public ProgressTimerRunnable(ProgressController controller)
                {
                    _controller = controller;
                }

                public void run()
                {
                    AsyncDelegate d = delegate
                                          {
                                              _controller.SetProgressText();
                                              double transferred = _controller._transfer.getTransferred();
                                              double size = _controller._transfer.getSize();
                                              if (transferred > 0 && size > 0)
                                              {
                                                  _controller.View.ProgressIndeterminate = false;
                                                  // normalize double to int if size is too big
                                                  if (size > int.MaxValue)
                                                  {
                                                      _controller.View.ProgressMaximum = int.MaxValue;
                                                      _controller.View.ProgressValue =
                                                          Convert.ToInt32(int.MaxValue*transferred/size);
                                                  }
                                                  else
                                                  {
                                                      _controller.View.ProgressMaximum = Convert.ToInt32(size);
                                                      _controller.View.ProgressValue = Convert.ToInt32(transferred);
                                                  }
                                              }
                                          };
                    _controller.Invoke(new SimpleDefaultMainAction(d));
                }
            }
        }

        private class TransferProgressListener : ProgressListener
        {
            private static readonly Logger Log = Logger.getLogger(typeof (TransferProgressListener).Name);

            private readonly ProgressController _controller;

            public TransferProgressListener(ProgressController controller)
            {
                _controller = controller;
            }

            public void message(string msg)
            {
                _controller._messageText = msg;
                SimpleDefaultMainAction action = new SimpleDefaultMainAction(delegate
                                                                     {
                                                                         Log.info("message() invoked: " + this);
                                                                         _controller.SetMessageText();
                                                                     });
                _controller.invoke(action);
            }
        }
    }
}