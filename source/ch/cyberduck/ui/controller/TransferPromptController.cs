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
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Winforms;
using StructureMap;
using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.transfer;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    internal abstract class TransferPromptController : WindowController<ITransferPromptView>, TransferPrompt,
                                                       ProgressListener, TranscriptListener
    {
        private static readonly Logger Log = Logger.getLogger(typeof (TransferPromptController));
        private static readonly string UnknownString = LocaleFactory.localizedString("Unknown");
        protected readonly Session Session;

        protected readonly Transfer Transfer;
        private readonly WindowController _parent;

        protected internal TransferAction Action =
            TransferAction.forName(Preferences.instance().getProperty("queue.prompt.action.default"));

        protected TransferPromptModel TransferPromptModel;

        protected TransferPromptController(WindowController parent, Transfer transfer, Session session)
        {
            View = ObjectFactory.GetInstance<ITransferPromptView>();
            _parent = parent;
            Transfer = transfer;
            Session = session;
            View.Title = LocaleFactory.localizedString(TransferName);

            PopulateActions();
        }

        protected abstract string TransferName { get; }

        public override void start(BackgroundAction action)
        {
            Invoke(delegate { View.StartActivityAnimation(); });
        }

        public override void stop(BackgroundAction action)
        {
            Invoke(delegate { View.StopActivityAnimation(); });            
        }

        public override void message(string msg)
        {
            Invoke(delegate { View.StatusLabel = msg; });
        }

        public override void log(bool request, string message)
        {
            //
        }

        public virtual TransferAction prompt()
        {
            if (Log.isDebugEnabled())
            {
                Log.debug(String.Format("Prompt for transfer action of {0}", Transfer));
            }
            for (int i = 0; i < Transfer.getRoots().size(); i++)
            {
                Path next = (Path) Transfer.getRoots().get(i);
                TransferPromptModel.Add(next);
            }

            AsyncDelegate wireAction = delegate
                {
                    View.ToggleDetailsEvent += View_ToggleDetailsEvent;
                    View.DetailsVisible = Preferences.instance().getBoolean("transfer.toggle.details");

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

                    View.ViewShownEvent += delegate
                        {
                            View.SetModel(TransferPromptModel.ChildrenGetter(null));
                            //select first one if there is any
                            IEnumerator<Path> en = TransferPromptModel.ChildrenGetter(null).GetEnumerator();
                            if (en.MoveNext())
                            {
                                View.SelectedPath = en.Current;
                            }
                        };
                    DialogResult result = View.ShowDialog(_parent.View);

                    if (result == DialogResult.Cancel)
                    {
                        Action = TransferAction.cancel;
                    }
                };
            _parent.Invoke(wireAction, true);
            return Action;
        }

        public bool isSelected(Path p)
        {
            return TransferPromptModel.IsSelected(p);
        }

        public void UpdateStatusLabel()
        {
            View.StatusLabel = String.Format(LocaleFactory.localizedString("{0} Files"), View.NumberOfFiles);
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
                Path selected = View.SelectedPath;
                if (null != selected)
                {
                    View.LocalFileUrl = selected.getLocal().getAbsolute();
                    if (selected.getLocal().attributes().getSize() == -1)
                    {
                        View.LocalFileSize = UnknownString;
                    }
                    else
                    {
                        View.LocalFileSize =
                            SizeFormatterFactory.get().format(selected.getLocal().attributes().getSize());
                    }
                    if (selected.getLocal().attributes().getModificationDate() == -1)
                    {
                        View.LocalFileModificationDate = UnknownString;
                    }
                    else
                    {
                        View.LocalFileModificationDate =
                            UserDateFormatterFactory.get()
                                                    .getLongFormat(
                                                        UserDefaultsDateFormatter.ConvertJavaMillisecondsToDotNetMillis(
                                                            selected.getLocal().attributes().getModificationDate()));
                    }
                    View.RemoteFileUrl =
                        ((UrlProvider) Session.getFeature(typeof (UrlProvider))).toUrl(selected)
                                                                                .find(DescriptiveUrl.Type.provider)
                                                                                .getUrl();
                    if (selected.attributes().getSize() == -1)
                    {
                        View.RemoteFileSize = UnknownString;
                    }
                    else
                    {
                        View.RemoteFileSize = SizeFormatterFactory.get().format(selected.attributes().getSize());
                    }
                    if (selected.attributes().getModificationDate() == -1)
                    {
                        View.RemoteFileModificationDate = UnknownString;
                    }
                    else
                    {
                        View.RemoteFileModificationDate =
                            UserDateFormatterFactory.get().getLongFormat(selected.attributes().getModificationDate());
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
            Preferences.instance()
                       .setProperty(String.Format("queue.prompt.{0}.action.default", Transfer.getType().name()),
                                    selected.toString());
            Action = selected;
            TransferPromptModel.SetAction(selected);
            ReloadData();
        }

        public void ReloadData()
        {
            View.SetModel(TransferPromptModel.ChildrenGetter(null));
            UpdateStatusLabel();
        }

        public void ReloadData(List<Path> roots)
        {
            foreach (Path p in roots)
            {
                View.RefreshBrowserObject(p);
            }
            UpdateStatusLabel();
        }

        private void PopulateActions()
        {
            View.PopulateActions(GetTransferActions());
            TransferAction defaultAction =
                TransferAction.forName(
                    Preferences.instance()
                               .getProperty(String.Format("queue.prompt.{0}.action.default", Transfer.getType().name())));
            View.SelectedAction = defaultAction;
            Action = defaultAction;
        }

        protected virtual IDictionary<TransferAction, string> GetTransferActions()
        {
            IDictionary<TransferAction, string> actions = new Dictionary<TransferAction, string>();
            actions.Add(TransferAction.resume, TransferAction.resume.getTitle());
            actions.Add(TransferAction.overwrite, TransferAction.overwrite.getTitle());
            actions.Add(TransferAction.rename, TransferAction.rename.getTitle());
            actions.Add(TransferAction.renameexisting, TransferAction.renameexisting.getTitle());
            actions.Add(TransferAction.skip, TransferAction.skip.getTitle());
            actions.Add(TransferAction.comparison, TransferAction.comparison.getTitle());
            return actions;
        }

        public void RefreshObject(Path path)
        {
            AsyncDelegate refreshAction = () => View.RefreshBrowserObject(path);
            Invoke(refreshAction);
        }
    }
}