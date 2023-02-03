// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.Drawing.Drawing2D;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Core
{
    public sealed class TaskbarApplicationBadgeLabeler : ApplicationBadgeLabeler
    {
        public void badge(string text)
        {
            if (Cyberduck.Core.Utils.IsBlank(text))
            {
                clear();
            }
            else
            {
                using (Bitmap bm = new Bitmap(16, 16))
                using (Graphics g = Graphics.FromImage(bm))
                {
                    g.SmoothingMode = SmoothingMode.AntiAlias;
                    g.FillEllipse(Brushes.Navy, new Rectangle(0, 0, 15, 15));

                    if (text.Length == 1)
                    {
                        Font f = new Font("Segoe UI", 8, FontStyle.Bold);
                        g.DrawString(text, f, new SolidBrush(Color.White), 3, 1);
                    }
                    else
                    {
                        Font f = new Font("Segoe UI", 7, FontStyle.Bold);
                        g.DrawString(text, f, new SolidBrush(Color.White), 1, 1);
                    }
                    TransferController.Instance.TaskbarOverlayIcon(Icon.FromHandle(bm.GetHicon()), text);
                }
            }
        }

        public void clear()
        {
            TransferController.Instance.TaskbarOverlayIcon(null, String.Empty);
        }
    }
}