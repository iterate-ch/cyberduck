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
using static Windows.Win32.UI.WindowsAndMessaging.MESSAGEBOX_RESULT;
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

            var dialog = TaskDialog.Create()
                .AllowCancellation()
                .Title(Localize("Please Donate") + "(" + uses + ")")
                .Instruction(Localize("Thank you for using Cyberduck!"))
                .Content($@"{Localize("This is free software, but it still costs money to write, support, and distribute it. If you enjoy using it, please consider a donation to the authors of this software. It will help to make Cyberduck even better!")} {Localize("As a contributor to Cyberduck, you receive a registration key that disables this prompt.")}")
                .CommandLinks(add =>
                {
                    add(IDOK, Localize("Donate"), true);
                    add(IDIGNORE, Localize("Later"), false);
                    add(IDCONTINUE, Localize("Buy in Windows Store"), false);
                });
            if (PreferencesFactory.get().getBoolean("donate.reminder.suppress.enable"))
            {
                dialog.VerificationText(Localize("Don't show again for this version"), false);
            }
            var result = dialog.Show();
            if (result.VerificationChecked == true)
            {
                PreferencesFactory.get().setProperty("donate.reminder", Assembly.GetExecutingAssembly().GetName().Version.ToString());
            }

            if (result.Button == IDOK)
            {
                BrowserLauncherFactory.get().open(PreferencesFactory.get().getProperty("website.donate"));
            }
            if (result.Button == IDCONTINUE)
            {
                BrowserLauncherFactory.get().open(PreferencesFactory.get().getProperty("website.store"));
            }

            PreferencesFactory.get().setProperty("donate.reminder.date", DateTime.Now.Ticks);
        }
    }
}
