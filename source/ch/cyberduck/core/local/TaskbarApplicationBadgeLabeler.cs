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
using System.Drawing;
using System.Drawing.Drawing2D;
using Ch.Cyberduck.Ui.Controller;
using ch.cyberduck.core.local;

namespace Ch.Cyberduck.Core.Local
{
    internal class TaskbarApplicationBadgeLabeler : ApplicationBadgeLabeler
    {
        public void badge(string text)
        {
            if (Utils.IsBlank(text))
            {
                TransferController.Instance.TaskbarOverlayIcon(null, String.Empty);
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

        public static void Register()
        {
            if (Utils.IsWin7OrLater)
            {
                ApplicationBadgeLabelerFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
            }
        }

        private class Factory : ApplicationBadgeLabelerFactory
        {
            protected override object create()
            {
                return new TaskbarApplicationBadgeLabeler();
            }
        }
    }
}