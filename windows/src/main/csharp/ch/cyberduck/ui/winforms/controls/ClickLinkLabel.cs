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
using System.ComponentModel;
using System.Windows.Forms;
using Ch.Cyberduck.Core;
using ch.cyberduck.core;
using ch.cyberduck.core.local;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    internal sealed class ClickLinkLabel : LinkLabel
    {
        public ClickLinkLabel()
        {
            if (!DesignMode)
            {
                ContextMenuStrip contextMenu = new ContextMenuStrip();
                ToolStripItem addItem = contextMenu.Items.Add(LocaleFactory.localizedString("Copy URL", "Browser"));
                addItem.Click += (sender, args) => Clipboard.SetText(Text);
                ContextMenuStrip = contextMenu;
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <see cref="http://stackoverflow.com/questions/34664/designmode-with-controls"/>
        protected Boolean DesignMode
        {
            get { return (LicenseManager.UsageMode == LicenseUsageMode.Designtime); }
        }

        protected override void OnLinkClicked(LinkLabelLinkClickedEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                BrowserLauncherFactory.get().open(Text);
            }
            base.OnLinkClicked(e);
        }
    }
}