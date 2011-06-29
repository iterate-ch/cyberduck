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

using System.Windows.Forms;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    internal sealed class ClickLinkLabel : LinkLabel
    {
        public ClickLinkLabel()
        {
            ContextMenuStrip contextMenu = new ContextMenuStrip();
            ToolStripItem addItem = contextMenu.Items.Add(Locale.localizedString("Copy URL", "Browser"));
            addItem.Click += (sender, args) => Clipboard.SetText(Text);
            ContextMenuStrip = contextMenu;
        }

        protected override void OnLinkClicked(LinkLabelLinkClickedEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                Utils.StartProcess(Text);
            }
            base.OnLinkClicked(e);
        }
    }
}