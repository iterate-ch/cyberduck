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

using System;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.aquaticprime;
using ch.cyberduck.core.local;
using ch.cyberduck.core.notification;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.updater;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Core;
using Application = System.Windows.Forms.Application;

namespace Ch.Cyberduck.Ui.Growl
{
    public class ToolstripNotificationService : NotificationService
    {
        private readonly NotifyIcon _icon = new NotifyIcon();

        public void notify(string title, string description)
        {
            _icon.ShowBalloonTip(PreferencesFactory.get().getInteger("notifications.timeout.milliseconds"), title,
                description, ToolTipIcon.Info);
        }

        public void setup()
        {
            ContextMenuStrip rightMenu = new ContextMenuStrip();
            ToolStripMenuItem itemUpdate = new ToolStripMenuItem
            {
                Text = LocaleFactory.get().localize("Check for Update…", "Main")
            };
            PeriodicUpdateChecker updater = new WindowsPeriodicUpdateChecker();
            itemUpdate.Enabled = updater.hasUpdatePrivileges();
            itemUpdate.Click += delegate { updater.check(false); };
            ToolStripMenuItem itemDonate = new ToolStripMenuItem
            {
                Text = LocaleFactory.get().localize("Donate…", "Main")
            };
            itemDonate.Click +=
                delegate { BrowserLauncherFactory.get().open(PreferencesFactory.get().getProperty("website.donate")); };
            ToolStripMenuItem itemKey = new ToolStripMenuItem {Text = LicenseFactory.find().ToString(), Enabled = false};
            ToolStripMenuItem itemExit = new ToolStripMenuItem
            {
                Text = LocaleFactory.get().localize("Exit", "Localizable")
            };
            itemExit.Click += delegate { MainController.Exit(false); };
            rightMenu.Items.AddRange(new ToolStripItem[]
            {itemUpdate, new ToolStripSeparator(), itemDonate, itemKey, new ToolStripSeparator(), itemExit});

            try
            {
                _icon.Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);
            }
            catch (ArgumentException)
            {
            }
            _icon.Visible = true;
            _icon.ContextMenuStrip = rightMenu;

            _icon.MouseClick += delegate(object sender, MouseEventArgs args)
            {
                if (args.Button == MouseButtons.Left)
                {
                    foreach (BrowserController browser in MainController.Browsers)
                    {
                        browser.View.Activate();
                        browser.View.BringToFront();
                    }
                }
            };
        }

        public void unregister()
        {
            _icon.Dispose();
        }

        public void notifyWithImage(string title, string description, string image)
        {
            notify(title, description);
        }
    }
}