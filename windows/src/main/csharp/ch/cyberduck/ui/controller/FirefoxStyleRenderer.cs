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

using System.Drawing;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class FirefoxStyleRenderer : ToolStripSystemRenderer
    {
        protected override void OnRenderToolStripBackground(ToolStripRenderEventArgs e)
        {
            Rectangle rect = e.AffectedBounds;
            using (SolidBrush brush = new SolidBrush(Color.White))
            {
                e.Graphics.FillRectangle(brush, rect);
            }
        }

        protected override void OnRenderToolStripBorder(ToolStripRenderEventArgs e)
        {
            base.OnRenderToolStripBorder(e);
            Rectangle rect = e.AffectedBounds;
            e.Graphics.DrawLine(new Pen(Color.FromKnownColor(KnownColor.ControlDark)),
                                rect.Left, rect.Bottom,
                                rect.Right, rect.Bottom);
            return;
        }

        protected override void OnRenderButtonBackground(ToolStripItemRenderEventArgs e)
        {
            if (e.Item is ToolStripButton)
            {
                ToolStripButton button = (ToolStripButton) e.Item;
                SolidBrush solidBrush = null;
                using (solidBrush)
                {
                    if (button.Pressed || button.Checked)
                    {
                        solidBrush = new SolidBrush(Color.FromArgb(193, 210, 238));
                    }
                    else if (button.Selected)
                    {
                        solidBrush = new SolidBrush(Color.FromArgb(224, 232, 246));
                    }
                    else
                    {
                        base.OnRenderButtonBackground(e);
                        return;
                    }
                    e.Graphics.FillRectangle(solidBrush, 0, 0, e.Item.Width, e.Item.Height);
                }
            }
            else
            {
                base.OnRenderButtonBackground(e);
            }
        }
    }
}