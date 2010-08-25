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
using System.Collections;
using System.Collections.Generic;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface ITransferPromptView : IView
    {
        TransferAction SelectedAction { get; set; }
        TreePathReference SelectedPath { get; }
        string LocalFileUrl { set; }
        string LocalFileSize { set; }
        string LocalFileModificationDate { set; }
        string RemoteFileUrl { set; }
        string RemoteFileSize { set; }
        string RemoteFileModificationDate { set; }
        void SetModel(IEnumerable<TreePathReference> model);
        void RefreshBrowserObject(TreePathReference reference);
        bool DetailsVisible { set; get; }
        int NumberOfFiles { get; }

        void PopulateActions(IDictionary<TransferAction, string> actions);

        void ModelCanExpandDelegate(TreeListView.CanExpandGetterDelegate canExpandDelegate);
        void ModelChildrenGetterDelegate(TreeListView.ChildrenGetterDelegate childrenGetterDelegate);

        CheckStateGetterDelegate ModelCheckStateGetter { set; }
        CheckStatePutterDelegate ModelCheckStateSetter { set; }
        ImageGetterDelegate ModelIconGetter { set; }
        AspectGetterDelegate ModelFilenameGetter { set; }
        AspectGetterDelegate ModelSizeGetter { set; }
        AspectToStringConverterDelegate ModelSizeAsStringGetter { set; }
        ImageGetterDelegate ModelWarningGetter { set; }
        ImageGetterDelegate ModelCreateGetter { set; }
        ImageGetterDelegate ModelSyncGetter { set; }
        MulticolorTreeListView.ActiveGetterDelegate ModelActiveGetter { set; }

        event VoidHandler ChangedActionEvent;
        event VoidHandler ChangedSelectionEvent;
        event VoidHandler ToggleDetailsEvent;

        //todo might be pulled out into a separate interface. same for toggledetails.
        void StartActivityAnimation();
        void StopActivityAnimation();

        string StatusLabel { set; }

        // mainly used to detect a change of displayed items to update the status label
        event VoidHandler ItemsChanged;
    }
}