﻿//
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
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
using ch.cyberduck.ui;
using ch.cyberduck.ui.controller;
using org.apache.log4j;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    internal abstract class TransferPromptController : WindowController<ITransferPromptView>, TransferPrompt
    {
        private static readonly Logger Log = Logger.getLogger(typeof (TransferPromptController));
        private static readonly string UnknownString = Locale.localizedString("Unknown");

        protected readonly Transfer Transfer;
        private readonly WindowController _parent;

        protected internal TransferAction Action =
            TransferAction.forName(Preferences.instance().getProperty("queue.prompt.action.default"));

        protected TransferPromptModel TransferPromptModel;
        private StatusLabelProgressListener _progressListener;

        protected TransferPromptController(WindowController parent, Transfer transfer)
        {
            View = ObjectFactory.GetInstance<ITransferPromptView>();
            _parent = parent;
            Transfer = transfer;
            View.Title = Locale.localizedString(TransferName);

            PopulateActions();
        }

        protected abstract string TransferName { get; }

        public virtual TransferAction prompt()
        {
            Log.debug("prompt:" + Transfer);
            for (int i = 0; i < Transfer.getRoots().size(); i++)
            {
                Path next = (Path) Transfer.getRoots().get(i);
                if (TransferPromptModel.Filter().accept(next))
                {
                    TransferPromptModel.Add(next);
                }
            }

            AsyncDelegate wireAction = delegate
                                           {
                                               Transfer.getSession().addProgressListener(
                                                   _progressListener = new StatusLabelProgressListener(this));

                                               View.ToggleDetailsEvent += View_ToggleDetailsEvent;
                                               View.DetailsVisible = Preferences.instance().getBoolean(
                                                   "transfer.toggle.details");

                                               View.ChangedActionEvent += View_ChangedActionEvent;
                                               View.ChangedSelectionEvent += View_ChangedSelectionEvent;

                                               View.ModelCanExpandDelegate = TransferPromptModel.CanExpand;
                                               View.ModelChildrenGetterDelegate = TransferPromptModel.ChildrenGetter;
                                               View.ModelCheckStateGetter = TransferPromptModel.GetCheckState;
                                               View.ModelCheckStateSetter = TransferPromptModel.SetCheckState;
                                               View.ModelSizeGetter = TransferPromptModel.GetSize;
                                               View.ModelSizeAsStringGetter = TransferPromptModel.GetSizeAsString;
                                               View.ModelFilenameGetter = TransferPromptModel.GetName;
                                               View.ModelIconGetter = TransferPromptModel.GetIcon;
                                               View.ModelWarningGetter = TransferPromptModel.GetWarningImage;
                                               View.ModelCreateGetter = TransferPromptModel.GetCreateImage;
                                               View.ModelSyncGetter = TransferPromptModel.GetSyncGetter;
                                               View.ModelActiveGetter = TransferPromptModel.IsActive;

                                               View.ItemsChanged += UpdateStatusLabel;
                                               View.SetModel(TransferPromptModel.GetEnumerator());

                                               //select first one if there is any
                                               IEnumerator<TreePathReference> en =
                                                   TransferPromptModel.GetEnumerator().GetEnumerator();
                                               if (en.MoveNext())
                                               {
                                                   View.SelectedPath = en.Current;
                                               }

                                               DialogResult result = View.ShowDialog(_parent.View);

                                               if (result == DialogResult.Cancel)
                                               {
                                                   Action = TransferAction.ACTION_CANCEL;
                                               }
                                           };
            _parent.Invoke(wireAction, true);
            return Action;
        }

        protected override void Invalidate()
        {
            Transfer.getSession().removeProgressListener(_progressListener);
        }

        public void UpdateStatusLabel()
        {
            View.StatusLabel = View.NumberOfFiles + " " + Locale.localizedString("Files");
        }

        private void View_ToggleDetailsEvent()
        {
            View.DetailsVisible = !View.DetailsVisible;
            Preferences.instance().setProperty("transfer.toggle.details", View.DetailsVisible);
        }

        private void View_ChangedSelectionEvent()
        {
            if (View.SelectedPath != null)
            {
                Path selected = View.SelectedPath.Unique;
                if (null != selected)
                {
                    //todo wieso die ganze hidden geschichte?
                    View.LocalFileUrl = selected.getLocal().getAbsolute();
                    if (selected.getLocal().exists())
                    {
                        if (selected.getLocal().attributes().getSize() == -1)
                        {
                            View.LocalFileSize = UnknownString;
                        }
                        else
                        {
                            View.LocalFileSize = Status.getSizeAsString(selected.getLocal().attributes().getSize());
                        }
                        if (selected.getLocal().attributes().getModificationDate() == -1)
                        {
                            View.LocalFileModificationDate = UnknownString;
                        }
                        else
                        {
                            View.LocalFileModificationDate =
                                DateFormatterFactory.instance().getLongFormat(
                                    selected.getLocal().attributes().getModificationDate());
                        }
                    }
                    else
                    {
                        View.LocalFileSize = String.Empty;
                        View.LocalFileModificationDate = String.Empty;
                    }

                    View.RemoteFileUrl = selected.getHost().toURL() + selected.getAbsolute();
                    if (selected.exists())
                    {
                        if (selected.attributes().getSize() == -1)
                        {
                            View.RemoteFileSize = UnknownString;
                        }
                        else
                        {
                            View.RemoteFileSize = Status.getSizeAsString(selected.attributes().getSize());
                        }
                        if (selected.attributes().getModificationDate() == -1)
                        {
                            View.RemoteFileModificationDate = UnknownString;
                        }
                        else
                        {
                            View.RemoteFileModificationDate =
                                DateFormatterFactory.instance().getLongFormat(
                                    selected.attributes().getModificationDate());
                        }
                    }
                    else
                    {
                        View.RemoteFileSize = String.Empty;
                        View.RemoteFileModificationDate = String.Empty;
                    }
                }
                else
                {
                    View.LocalFileUrl = String.Empty;
                    View.LocalFileSize = String.Empty;
                    View.LocalFileModificationDate = String.Empty;
                }
            }
        }

        protected virtual void View_ChangedActionEvent()
        {
            TransferAction selected = View.SelectedAction;
            if (Action.equals(selected))
            {
                return;
            }
            Preferences.instance().setProperty("queue.prompt.action.default", selected.toString());
            Action = selected;
            ReloadData();
        }

        public void ReloadData()
        {
            View.SetModel(TransferPromptModel.GetEnumerator());
            UpdateStatusLabel();
        }

        protected virtual void PopulateActions()
        {
            IDictionary<TransferAction, string> actions = new Dictionary<TransferAction, string>();
            if (Transfer.isResumable())
            {
                actions.Add(TransferAction.ACTION_RESUME, TransferAction.ACTION_RESUME.getLocalizableString());
            }
            actions.Add(TransferAction.ACTION_OVERWRITE, TransferAction.ACTION_OVERWRITE.getLocalizableString());
            actions.Add(TransferAction.ACTION_RENAME, TransferAction.ACTION_RENAME.getLocalizableString());
            if (Transfer.getSession().isRenameSupported(Transfer.getRoot())())
            {
                actions.Add(TransferAction.ACTION_RENAME_EXISTING, TransferAction.ACTION_RENAME_EXISTING.getLocalizableString());
            }
            View.PopulateActions(actions);

            TransferAction defaultAction = TransferAction.forName(
                Preferences.instance().getProperty("queue.prompt.action.default"));
            View.SelectedAction = defaultAction;
            Action = defaultAction;
        }

        public static TransferPromptController Create(WindowController parent, Transfer transfer)
        {
            // create the controller in the thread that has created the view of the TransferController (parent)
            TransferPromptController promptController = null;

            if (transfer is DownloadTransfer)
            {
                parent.Invoke(delegate { promptController = new DownloadPromptController(parent, transfer); }, true);
                return promptController;
            }

            if (transfer is UploadTransfer)
            {
                parent.Invoke(delegate { promptController = new UploadPromptController(parent, transfer); }, true);
                return promptController;
            }
            if (transfer is SyncTransfer)
            {
                parent.Invoke(delegate { promptController = new SyncPromptController(parent, transfer); }, true);
                return promptController;
            }
            throw new ArgumentException(transfer.toString());
        }

        public void RefreshObject(AbstractPath path)
        {
            AsyncDelegate refreshAction = () => View.RefreshBrowserObject(new TreePathReference(path));
            Invoke(refreshAction);
        }

        private class StatusLabelProgressListener : ProgressListener
        {
            private readonly TransferPromptController _controller;

            public StatusLabelProgressListener(TransferPromptController controller)
            {
                _controller = controller;
            }

            public void message(string msg)
            {
                _controller.Invoke(new AsyncDelegate(delegate { _controller.View.StatusLabel = msg; }));
            }
        }
    }
}