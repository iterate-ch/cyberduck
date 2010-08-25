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
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;
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

        private class Comp : IComparer<TreePathReference>
        {
            private readonly Tree _treeModel;

            public Comp(Tree treeModel)
            {
                _treeModel = treeModel;
            }

            public int Compare(TreePathReference x, TreePathReference y)
            {
                //sort descending
                return _treeModel.GetBranch(y).Level - _treeModel.GetBranch(x).Level;
            }
        }
    }
}