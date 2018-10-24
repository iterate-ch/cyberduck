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
using System.Reflection;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using ch.cyberduck.core.notification;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.TaskDialog;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class DonationController : IDonationController
    {
        private static string Localize(string text) => LocaleFactory.localizedString(text, "Donate");

        public DonationController()
        {
        }

        public void Show()
        {
            int uses = PreferencesFactory.get().getInteger("uses");

            var result = TaskDialog.Show(
                owner: IntPtr.Zero,
                allowDialogCancellation: true,
                title: Localize("Please Donate") + " (" + uses + ")",
                verificationText: PreferencesFactory.getBoolean("donate.reminder.supress.enable") ? Localize("Don't show again for this version") : null,
                mainInstruction: Localize("Thank you for using Cyberduck!"),
                content: $@"{Localize("This is free software, but it still costs money to write, support, and distribute it. If you enjoy using it, please consider a donation to the authors of this software. It will help to make Cyberduck even better!")} {Localize("As a contributor to Cyberduck, you receive a registration key that disables this prompt.")}",
                commandLinks: new[] { Localize("Donate"), Localize("Later"), Localize("Buy in Windows Store") },
                verificationByDefault: false);
            if (result.VerificationChecked == true)
            {
                PreferencesFactory.get().setProperty("donate.reminder", Assembly.GetExecutingAssembly().GetName().Version.ToString());
            }

            if (result.CommandButtonResult == 0)
            {
                BrowserLauncherFactory.get().open(PreferencesFactory.get().getProperty("website.donate"));
            }
            if (result.CommandButtonResult == 2)
            {
                BrowserLauncherFactory.get().open(PreferencesFactory.get().getProperty("website.store"));
            }

            PreferencesFactory.get().setProperty("donate.reminder.date", DateTime.Now.Ticks);
        }
    }
}
