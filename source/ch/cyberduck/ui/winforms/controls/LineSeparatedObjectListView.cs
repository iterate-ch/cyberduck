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
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    /// <summary>
    /// ListView with a line between each item
    /// </summary>
    public class LineSeparatedObjectListView : ObjectListView
    {
        protected override void OnDrawSubItem(DrawListViewSubItemEventArgs e)
        {
            base.OnDrawSubItem(e);
            Rectangle bounds = e.Bounds;
            //we need this minus 1 to draw the line within the bounds ot the current item. Otherwise it gets
            //overpainted by the next item
            e.Graphics.DrawLine(_pen, bounds.Left, bounds.Bottom -1 , bounds.Right, bounds.Bottom -1);
        }

        private Pen _pen = new Pen(Color.FromKnownColor(KnownColor.ControlLight), 1);
        public Pen Pen
        {
            get { return _pen; }
            set { _pen = value; }
        }
    }
}