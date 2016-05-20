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

using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui.comparator;
using org.apache.commons.io;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    /// <summary>
    /// TreeListView that offers a different row text color based on the linked model object
    /// </summary>
    public class MulticolorTreeListView : TreeListView
    {
        public delegate bool ActiveGetterPathDelegate(Path path);

        public delegate bool ActiveGetterTransferItemDelegate(TransferItem path);

        private const int IconSize = 16;
        private Color _activeForegroudColor = DefaultForeColor;
        private ActiveGetterPathDelegate _activeGetterPath = reference => true;
        private ActiveGetterTransferItemDelegate _activeGetterTransferItem = reference => true;
        private Color _inactiveForegroudColor = Color.Gray;

        public ActiveGetterTransferItemDelegate ActiveGetterTransferItem
        {
            get { return _activeGetterTransferItem; }
            set { _activeGetterTransferItem = value; }
        }

        public ActiveGetterPathDelegate ActiveGetterPath
        {
            get { return _activeGetterPath; }
            set { _activeGetterPath = value; }
        }

        public Color ActiveForegroudColor
        {
            get { return _activeForegroudColor; }
            set { _activeForegroudColor = value; }
        }

        public Color InactiveForegroudColor
        {
            get { return _inactiveForegroudColor; }
            set { _inactiveForegroudColor = value; }
        }

        /// <summary>
        /// Make OnExpanding accessible for non subclasses
        /// </summary>
        /// <param name="model"></param>
        public void OnExpanding(TreeBranchExpandingEventArgs args)
        {
            base.OnExpanding(args);
        }

        protected override void OnDrawSubItem(DrawListViewSubItemEventArgs e)
        {
            object o = ((OLVListItem) e.Item).RowObject;
            if (o is TransferItem)
            {
                TransferItem item = (TransferItem) o;
                e.Item.ForeColor = ActiveGetterTransferItem(item) ? ActiveForegroudColor : InactiveForegroudColor;
            }
            if (o is Path)
            {
                Path path = (Path) o;
                e.Item.ForeColor = ActiveGetterPath(path) ? ActiveForegroudColor : InactiveForegroudColor;
            }
            base.OnDrawSubItem(e);
        }

        protected override void ShowColumnSelectMenu(Point pt)
        {
            ToolStripDropDown m = MakeColumnSelectMenu(new ContextMenuStrip());
            ContextMenu cm = new ContextMenu();
            foreach (ToolStripMenuItem item in m.Items)
            {
                ToolStripMenuItem item1 = item;
                MenuItem nItem = new MenuItem(LocaleFactory.localizedString(item.Text, "Localizable"),
                    delegate { item1.PerformClick(); }); //forward click event
                nItem.Checked = item.Checked;
                cm.MenuItems.Add(nItem);
            }
            cm.Show(this, PointToClient(pt)); //transform coordinates
        }

        protected override void OnCellEditStarting(CellEditEventArgs e)
        {
            e.Control.AutoSize = false;
            e.Control.Bounds = new Rectangle(e.Control.Bounds.X + IconSize, e.Control.Bounds.Y,
                e.Control.Bounds.Width - IconSize, e.Control.Bounds.Height);
            if (e.Control is TextBox)
            {
                //Only select filename part w/o extension (Explorer like behavior)
                TextBox tb = e.Control as TextBox;
                int extensionIndex = FilenameUtils.indexOfExtension((string) e.Value);
                if (extensionIndex > -1)
                {
                    tb.Select(0, extensionIndex);
                }
            }
            base.OnCellEditStarting(e);
        }

        protected override void OnKeyDown(KeyEventArgs e)
        {
            OLVListItem focused = FocusedItem as OLVListItem;
            if (focused == null)
            {
                base.OnKeyDown(e);
                return;
            }
            switch (e.KeyCode)
            {
                case Keys.Left:
                    if (SelectedObjects.Count > 1)
                    {
                        foreach (var o in SelectedObjects)
                        {
                            Branch br = TreeModel.GetBranch(o);
                            if (br.IsExpanded)
                                Collapse(o);
                        }
                    }
                    else
                    {
                        // If the branch is expanded, collapse it. If it's collapsed,
                        // select the parent of the branch.
                        Branch br = TreeModel.GetBranch(focused.RowObject);
                        if (br.IsExpanded)
                            Collapse(focused.RowObject);
                        else
                        {
                            if (br.ParentBranch != null && br.ParentBranch.Model != null)
                                SelectObject(br.ParentBranch.Model, true);
                        }
                    }
                    e.Handled = true;
                    break;

                case Keys.Right:
                    foreach (var o in SelectedObjects)
                    {
                        Branch br = TreeModel.GetBranch(o);
                        if (br.IsExpanded)
                        {
                            List<Branch> filtered = br.FilteredChildBranches;
                            if (filtered.Count > 0)
                                SelectObject(filtered[0].Model, true);
                        }
                        else
                        {
                            if (br.CanExpand)
                            {
                                OnExpanding(new TreeBranchExpandingEventArgs(o, null));
                                Expand(o);
                            }
                        }
                    }
                    e.Handled = true;
                    break;
            }
            base.OnKeyDown(e);
        }
    }

    public class SortComparatorOLVColumn : OLVColumn
    {
        public delegate BrowserComparator SortComparatorDelegate(SortOrder order);

        public SortComparatorDelegate ComparatorGetter;
    }
}