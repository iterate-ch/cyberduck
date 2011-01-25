// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck.core.i18n;
using ch.cyberduck.ui;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    /// <summary>
    /// TreeListView that offers a different row text color based on the linked model object
    /// </summary>
    public class MulticolorTreeListView : TreeListView
    {
        public delegate bool ActiveGetterDelegate(TreePathReference reference);

        private Color _activeForegroudColor = DefaultForeColor;

        private ActiveGetterDelegate _activeGetter = reference => true;
        private Color _inactiveForegroudColor = Color.Gray;

        public ActiveGetterDelegate ActiveGetter
        {
            get { return _activeGetter; }
            set { _activeGetter = value; }
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

        protected override void OnDrawSubItem(DrawListViewSubItemEventArgs e)
        {
            object o = ((OLVListItem) e.Item).RowObject;
            if (o is TreePathReference)
            {
                TreePathReference r = (TreePathReference) o;
                e.Item.ForeColor = ActiveGetter(r) ? ActiveForegroudColor : InactiveForegroudColor;
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
                MenuItem nItem = new MenuItem(Locale.localizedString(item.Text, "Localizable"),
                                              delegate { item1.PerformClick(); }); //forward click event
                nItem.Checked = item.Checked;
                cm.MenuItems.Add(nItem);
            }
            cm.Show(this, PointToClient(pt)); //transform coordinates
        }
    }

    public class SortComparatorOLVColumn : OLVColumn
    {
        public delegate BrowserComparator SortComparatorDelegate(SortOrder order);

        public SortComparatorDelegate ComparatorGetter;
    }
}