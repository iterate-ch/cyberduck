// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Controller.Threading;
using StructureMap;
using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.io;
using ch.cyberduck.core.local;
using ch.cyberduck.core.transfer;
using ch.cyberduck.core.transfer.synchronisation;
using java.util;
using org.apache.log4j;
using Locale = ch.cyberduck.core.i18n.Locale;
using Queue = ch.cyberduck.core.transfer.Queue;

namespace Ch.Cyberduck.Ui.Controller
{
    public class TransferController : WindowController<ITransferView>, TranscriptListener, CollectionListener
    {
        private static readonly Logger Log = Logger.getLogger(typeof (TransferController).FullName);

        private static readonly object SyncRoot = new Object();
        private static volatile TransferController _instance;

        private readonly IDictionary<Transfer, ProgressController> _transferMap =
            new Dictionary<Transfer, ProgressController>();

        private TransferController()
        {
            View = ObjectFactory.GetInstance<ITransferView>();
            Init();
        }

        public override bool Singleton
        {
            get { return true; }
        }

        public static TransferController Instance
        {
            get
            {
                if (_instance == null)
                {
                    lock (SyncRoot)
                    {
                        if (_instance == null)
                            _instance = new TransferController();
                    }
                }
                return _instance;
            }
        }

        public void collectionLoaded()
        {
            Invoke(delegate
                {
                    IList<IProgressView> model = new List<IProgressView>();
                    foreach (Transfer transfer in TransferCollection.defaultCollection())
                    {
                        ProgressController progressController = new ProgressController(transfer);
                        model.Add(progressController.View);
                        _transferMap.Add(new KeyValuePair<Transfer, ProgressController>(transfer,
                                                                                        progressController));
                    }
                    View.SetModel(model);
                }
                );
        }

        public void collectionItemAdded(object obj)
        {
            Invoke(delegate
                {
                    Transfer transfer = obj as Transfer;
                    ProgressController progressController = new ProgressController(transfer);
                    _transferMap.Add(new KeyValuePair<Transfer, ProgressController>(transfer, progressController));
                    IProgressView progressView = progressController.View;
                    View.AddTransfer(progressView);
                    View.SelectTransfer(progressView);
                });
        }

        public void collectionItemRemoved(object obj)
        {
            Invoke(delegate
                {
                    Transfer transfer = obj as Transfer;
                    if (null != transfer)
                    {
                        ProgressController progressController;
                        if (_transferMap.TryGetValue(transfer, out progressController))
                        {
                            View.RemoveTransfer(progressController.View);
                        }
                    }
                });
        }

        public void collectionItemChanged(object obj)
        {
            ;
        }

        public void log(bool request, string transcript)
        {
            if (View.TranscriptVisible)
            {
                invoke(new LogAction(this, request, transcript));
            }
        }

        public static bool ApplicationShouldTerminate()
        {
            if (null != _instance)
            {
                //Saving state of transfer window
                Preferences.instance().setProperty("queue.openByDefault", _instance.Visible);
                if (TransferCollection.defaultCollection().numberOfRunningTransfers() > 0)
                {
                    DialogResult result = _instance.QuestionBox(Locale.localizedString("Transfer in progress"),
                                                                Locale.localizedString(
                                                                    "There are files currently being transferred. Quit anyway?"),
                                                                null,
                                                                String.Format("{0}", Locale.localizedString("Exit")),
                                                                true //Cancel
                        );
                    if (DialogResult.OK == result)
                    {
                        // Quit
                        for (int i = 0; i < TransferCollection.defaultCollection().size(); i++)
                        {
                            Transfer transfer = (Transfer) TransferCollection.defaultCollection().get(i);
                            if (transfer.isRunning())
                            {
                                transfer.interrupt();
                            }
                        }
                        return true;
                    }
                    // Cancel
                    return false;
                }
            }
            return true;
        }

        private void Init()
        {
            collectionLoaded();
            TransferCollection.defaultCollection().addListener(this);

            PopulateBandwithList();

            View.PositionSizeRestoredEvent += delegate
                {
                    View.TranscriptVisible = Preferences.instance().getBoolean("queue.logDrawer.isOpen");
                    View.TranscriptHeight = Preferences.instance().getInteger("queue.logDrawer.size.height");

                    View.ToggleTranscriptEvent += View_ToggleTranscriptEvent;
                    View.TranscriptHeightChangedEvent += View_TranscriptHeightChangedEvent;
                };
            View.QueueSize = Preferences.instance().getInteger("queue.maxtransfers");
            View.BandwidthEnabled = false;

            View.ResumeEvent += View_ResumeEvent;
            View.ReloadEvent += View_ReloadEvent;
            View.StopEvent += View_StopEvent;
            View.RemoveEvent += View_RemoveEvent;
            View.CleanEvent += View_CleanEvent;
            View.OpenEvent += View_OpenEvent;
            View.ShowEvent += View_ShowEvent;
            View.SelectionChangedEvent += View_SelectionChangedEvent;
            View.BandwidthChangedEvent += View_BandwidthChangedEvent;
            View.QueueSizeChangedEvent += View_QueueSizeChangedEvent;

            View.ValidateResumeEvent += View_ValidateResumeEvent;
            View.ValidateReloadEvent += View_ValidateReloadEvent;
            View.ValidateStopEvent += View_ValidateStopEvent;
            View.ValidateRemoveEvent += View_ValidateRemoveEvent;
            View.ValidateCleanEvent += View_ValidateCleanEvent;
            View.ValidateOpenEvent += View_ValidateOpenEvent;
            View.ValidateShowEvent += View_ValidateShowEvent;
        }

        private void View_TranscriptHeightChangedEvent()
        {
            Preferences.instance().setProperty("queue.logDrawer.size.height", View.TranscriptHeight);
        }

        private void View_ToggleTranscriptEvent()
        {
            View.TranscriptVisible = !View.TranscriptVisible;
            Preferences.instance().setProperty("queue.logDrawer.isOpen", View.TranscriptVisible);
        }

        private bool View_ValidateShowEvent()
        {
            return ValidateToolbarItem(delegate(Transfer transfer)
                {
                    if (transfer.getLocal() != null)
                    {
                        for (int i = 0; i < transfer.getRoots().size(); i++)
                        {
                            Path p = (Path) transfer.getRoots().get(i);
                            if (p.getLocal().exists())
                            {
                                return true;
                            }
                        }
                    }
                    return false;
                });
        }

        private bool View_ValidateOpenEvent()
        {
            return ValidateToolbarItem(delegate(Transfer transfer)
                {
                    if (transfer.getLocal() != null)
                    {
                        if (!transfer.isComplete())
                        {
                            return false;
                        }
                        if (!transfer.isRunning())
                        {
                            for (int i = 0; i < transfer.getRoots().size(); i++)
                            {
                                Path p = (Path) transfer.getRoots().get(i);
                                if (p.getLocal().exists())
                                {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                });
        }

        private bool View_ValidateCleanEvent()
        {
            return _transferMap.Count > 0;
        }

        private void View_CleanEvent()
        {
            IList<Transfer> toRemove = new List<Transfer>();
            foreach (KeyValuePair<Transfer, ProgressController> pair in _transferMap)
            {
                Transfer t = pair.Key;
                if (!t.isRunning() && t.isComplete())
                {
                    TransferCollection.defaultCollection().remove(t);
                    toRemove.Add(t);
                }
            }
            foreach (Transfer t in toRemove)
            {
                _transferMap.Remove(t);
            }
            TransferCollection.defaultCollection().save();
        }

        private bool View_ValidateRemoveEvent()
        {
            return View.SelectedTransfers.Count > 0;
        }

        private bool View_ValidateStopEvent()
        {
            return ValidateToolbarItem(transfer => transfer.isRunning());
        }

        private bool View_ValidateReloadEvent()
        {
            return ValidateToolbarItem(transfer => (transfer.isReloadable() && !transfer.isRunning()));
        }

        /// <summary>
        /// Validates the selected items in the transfer window against the toolbar validator
        /// </summary>
        /// <param name="validate"></param>
        /// <returns>True if one or more of the selected items passes the validation test</returns>
        private bool ValidateToolbarItem(TransferToolbarValidator validate)
        {
            foreach (IProgressView selectedTransfer in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(selectedTransfer);
                if (validate(transfer))
                {
                    return true;
                }
            }
            return false;
        }

        private bool View_ValidateResumeEvent()
        {
            return ValidateToolbarItem(delegate(Transfer transfer)
                {
                    if (transfer.isRunning())
                    {
                        return false;
                    }
                    return transfer.isResumable() && !transfer.isComplete();
                });
        }

        private void PopulateBandwithList()
        {
            IList<KeyValuePair<float, string>> list = new List<KeyValuePair<float, string>>();
            list.Add(new KeyValuePair<float, string>(BandwidthThrottle.UNLIMITED,
                                                     Locale.localizedString("Unlimited Bandwidth", "Preferences")));
            foreach (
                String option in
                    Preferences.instance().getProperty("queue.bandwidth.options").Split(new[] {','},
                                                                                        StringSplitOptions.
                                                                                            RemoveEmptyEntries))
            {
                list.Add(new KeyValuePair<float, string>(Convert.ToInt32(option.Trim()),
                                                         (SizeFormatterFactory.get().format(
                                                             Convert.ToInt32(option.Trim())) + "/s")));
            }
            View.PopulateBandwidthList(list);
        }

        private void View_QueueSizeChangedEvent()
        {
            Preferences.instance().setProperty("queue.maxtransfers", View.QueueSize);
            Queue.instance().resize();
        }

        private void View_BandwidthChangedEvent()
        {
            foreach (IProgressView progressView in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(progressView);
                transfer.setBandwidth(View.Bandwidth);
            }
            UpdateBandwidthPopup();
        }

        private void View_SelectionChangedEvent()
        {
            Log.debug("SelectionChanged");
            UpdateLabels();
            UpdateIcon();
            UpdateBandwidthPopup();
        }

        private void UpdateBandwidthPopup()
        {
            Log.debug("UpdateBandwidthPopup");
            IList<IProgressView> selectedTransfers = View.SelectedTransfers;
            View.BandwidthEnabled = selectedTransfers.Count > 0;

            foreach (IProgressView progressView in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(progressView);
                if (transfer is SyncTransfer)
                {
                    // Currently we do not support bandwidth throtling for sync transfers due to
                    // the problem of mapping both download and upload rate in the GUI
                    View.BandwidthEnabled = false;
                    // Break through and set the standard icon below
                }
                if (transfer.getBandwidth().getRate() != BandwidthThrottle.UNLIMITED)
                {
                    View.Bandwidth = transfer.getBandwidth().getRate();
                }
                else
                {
                    View.Bandwidth = BandwidthThrottle.UNLIMITED;
                }
                return;
            }
        }

        private void UpdateIcon()
        {
            IList<IProgressView> selectedTransfers = View.SelectedTransfers;
            if (1 == selectedTransfers.Count)
            {
                Transfer transfer = GetTransferFromView(selectedTransfers[0]);
                if (transfer.numberOfRoots() == 1)
                {
                    if (transfer.getLocal() != null)
                    {
                        View.FileIcon = IconCache.Instance.IconForFilename(transfer.getRoot().getLocal().getAbsolute(),
                                                                           IconCache.IconSize.Large);
                    }
                    else
                    {
                        View.FileIcon = IconCache.Instance.IconForPath(transfer.getRoot(),
                                                                       IconCache.IconSize.Large);
                    }
                }
                else
                {
                    View.FileIcon = ResourcesBundle.multiple.ToBitmap();
                }
            }
            else
            {
                View.FileIcon = null;
            }
        }

        private void UpdateLabels()
        {
            IList<IProgressView> selectedTransfers = View.SelectedTransfers;
            if (1 == selectedTransfers.Count)
            {
                Transfer transfer = GetTransferFromView(selectedTransfers[0]);
                if (transfer.numberOfRoots() == 1)
                {
                    View.Url = transfer.getRemote();
                    View.Local = transfer.getLocal();
                }
                else
                {
                    View.Url = Locale.localizedString("Multiple files");
                    View.Local = Locale.localizedString("Multiple files");
                }
            }
            else
            {
                View.Url = string.Empty;
                View.Local = string.Empty;
            }
        }

        private void View_ShowEvent()
        {
            foreach (IProgressView progressView in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(progressView);
                for (int i = 0; i < transfer.getRoots().size(); i++)
                {
                    Path path = (Path) transfer.getRoots().get(i);
                    RevealServiceFactory.get().reveal(path.getLocal());
                }
            }
        }

        private void View_OpenEvent()
        {
            IList<IProgressView> selected = View.SelectedTransfers;
            if (selected.Count == 1)
            {
                Transfer transfer = GetTransferFromView(selected[0]);

                for (int i = 0; i < transfer.getRoots().size(); i++)
                {
                    Path path = (Path) transfer.getRoots().get(i);
                    Local l = path.getLocal();
                    if (ApplicationLauncherFactory.get().open(l))
                    {
                        break;
                    }
                }
            }
        }

        private void View_RemoveEvent()
        {
            foreach (IProgressView progressView in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(progressView);
                if (!transfer.isRunning())
                {
                    TransferCollection.defaultCollection().remove(transfer);
                }
            }
            TransferCollection.defaultCollection().save();
        }

        private void View_StopEvent()
        {
            foreach (IProgressView progressView in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(progressView);

                AsyncDelegate run = transfer.cancel;
                AsyncDelegate cleanup = delegate { ; };

                if (transfer.isRunning())
                {
                    Background(run, cleanup);
                }
            }
        }

        private void View_ReloadEvent()
        {
            foreach (IProgressView progressView in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(progressView);
                if (!transfer.isRunning())
                {
                    StartTransfer(transfer, false, true);
                }
            }
        }

        private Transfer GetTransferFromView(IProgressView view)
        {
            foreach (KeyValuePair<Transfer, ProgressController> pair in _transferMap)
            {
                if (pair.Value.View == view) return pair.Key;
            }
            return null;
        }

        private void View_ResumeEvent()
        {
            foreach (IProgressView progressView in View.SelectedTransfers)
            {
                Transfer transfer = GetTransferFromView(progressView);
                if (!transfer.isRunning())
                {
                    StartTransfer(transfer, true, false);
                }
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="transfer"></param>
        public void StartTransfer(Transfer transfer)
        {
            StartTransfer(transfer, false, false);
        }

        private void StartTransfer(Transfer transfer, bool resumeRequested, bool reloadRequested)
        {
            if (!TransferCollection.defaultCollection().contains(transfer))
            {
                TransferCollection.defaultCollection().add(transfer);
            }
            if (Preferences.instance().getBoolean("queue.orderFrontOnStart"))
            {
                View.Show();
            }
            background(new TransferBackgroundAction(this, transfer, resumeRequested, reloadRequested));
        }

        public void TaskbarOverlayIcon(Icon icon, string description)
        {
            Invoke(delegate { View.TaskbarOverlayIcon(icon, description); });
        }

        private class LogAction : WindowMainAction
        {
            private readonly string _msg;
            private readonly bool _request;

            public LogAction(TransferController c, bool request, string msg)
                : base(c)
            {
                _request = request;
                _msg = msg;
            }

            public override void run()
            {
                ((TransferController) Controller).View.AddTranscriptEntry(_request, _msg);
            }
        }

        private class TransferBackgroundAction : AlertRepeatableBackgroundAction
        {
            private readonly TransferController _controller;
            private readonly object _lock = new object();
            private readonly Transfer _transfer;
            private TransferListener _listener;

            private bool _reload;
            private bool _resume;

            public TransferBackgroundAction(TransferController controller, Transfer transfer, bool resumeRequested,
                                            bool reloadRequested) : base(controller)
            {
                _transfer = transfer;
                _controller = controller;

                _resume = resumeRequested;
                _reload = reloadRequested;
            }

            public override void run()
            {
                TransferOptions options = new TransferOptions();
                options.reloadRequested = _reload;
                options.resumeRequested = _resume;
                _transfer.start(new LazyTransferPrompt(_controller, _transfer), options);
            }

            public override void finish()
            {
                base.finish();
                _transfer.removeListener(_listener);
            }

            public override void cleanup()
            {
                if (_transfer.isComplete() && !_transfer.isCanceled() && _transfer.isReset())
                {
                    if (Preferences.instance().getBoolean("queue.removeItemWhenComplete"))
                    {
                        TransferCollection.defaultCollection().remove(_transfer);
                    }
                    if (Preferences.instance().getBoolean("queue.orderBackOnStop"))
                    {
                        if (!(TransferCollection.defaultCollection().numberOfRunningTransfers() > 0))
                        {
                            _controller.View.Close();
                        }
                    }
                }
                TransferCollection.defaultCollection().save();
            }

            protected override List getSessions()
            {
                return _transfer.getSessions();
            }

            public override String getActivity()
            {
                return _transfer.getName();
            }

            public override void pause()
            {
                _transfer.fireTransferQueued();
                // Upon retry do not suggest to overwrite already completed items from the transfer
                _reload = false;
                _resume = true;
                base.pause();
                _transfer.fireTransferResumed();
            }

            public override bool isCanceled()
            {
                return _transfer.isCanceled();
            }

            public override void log(bool request, string message)
            {
                _controller.log(request, message);
                base.log(request, message);
            }

            public override object @lock()
            {
                // No synchronization with other tasks
                return _lock;
            }

            private class LazyTransferPrompt : TransferPrompt
            {
                private readonly TransferController _controller;
                private readonly Transfer _transfer;

                public LazyTransferPrompt(TransferController controller, Transfer transfer)
                {
                    _transfer = transfer;
                    _controller = controller;
                }

                public TransferAction prompt()
                {
                    return TransferPromptController.Create(_controller, _transfer).prompt();
                }
            }
        }

        private delegate bool TransferToolbarValidator(Transfer transfer);
    }
}