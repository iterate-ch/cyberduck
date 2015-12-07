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

using System.Collections.Generic;
using BrightIdeasSoftware;
using Ch.Cyberduck.Ui.Winforms.Controls;
using ch.cyberduck.core.transfer;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface ITransferPromptView : IView
    {
        string Title { set; }
        TransferAction SelectedAction { get; set; }
        TransferItem SelectedItem { get; set; }
        string LocalFileUrl { set; }
        string LocalFileSize { set; }
        string LocalFileModificationDate { set; }
        string RemoteFileUrl { set; }
        string RemoteFileSize { set; }
        string RemoteFileModificationDate { set; }
        bool DetailsVisible { set; get; }
        int NumberOfFiles { get; }

        CheckStateGetterDelegate ModelCheckStateGetter { set; }
        CheckStatePutterDelegate ModelCheckStateSetter { set; }

        TreeListView.CanExpandGetterDelegate ModelCanExpandDelegate { set; }
        TreeListView.ChildrenGetterDelegate ModelChildrenGetterDelegate { set; }

        TypedColumn<TransferItem>.TypedImageGetterDelegate ModelIconGetter { set; }
        TypedColumn<TransferItem>.TypedAspectGetterDelegate ModelFilenameGetter { set; }
        TypedColumn<TransferItem>.TypedAspectGetterDelegate ModelSizeGetter { set; }
        AspectToStringConverterDelegate ModelSizeAsStringGetter { set; }
        TypedColumn<TransferItem>.TypedImageGetterDelegate ModelWarningGetter { set; }
        TypedColumn<TransferItem>.TypedImageGetterDelegate ModelCreateGetter { set; }
        TypedColumn<TransferItem>.TypedImageGetterDelegate ModelSyncGetter { set; }
        MulticolorTreeListView.ActiveGetterTransferItemDelegate ModelActiveGetter { set; }
        string StatusLabel { set; }
        IList<TransferItem> VisibleItems { get; }
        void SetModel(IEnumerable<TransferItem> model);
        void RefreshBrowserObject(TransferItem item);
        void PopulateActions(IDictionary<TransferAction, string> actions);

        event VoidHandler ChangedActionEvent;
        event VoidHandler ChangedSelectionEvent;
        event VoidHandler ToggleDetailsEvent;

        void StartActivityAnimation();
        void StopActivityAnimation();

        // mainly used to detect a change of displayed items to update the status label
        event VoidHandler ItemsChanged;
    }
}