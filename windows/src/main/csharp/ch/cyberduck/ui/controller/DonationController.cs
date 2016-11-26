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
using ch.cyberduck.core.preferences;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class DonationController : WindowController<IDonationView>, IDonationController
    {
        public DonationController() : this(ObjectFactory.GetInstance<IDonationView>())
        {
        }

        public DonationController(IDonationView view)
        {
            View = view;
        }

        public void Show()
        {
            int uses = PreferencesFactory.get().getInteger("uses");
            View.Title = LocaleFactory.localizedString("Please Donate", "Donate") + " (" + uses + ")";
            View.NeverShowDonation =
                Assembly.GetExecutingAssembly()
                    .GetName()
                    .Version.ToString()
                    .Equals(PreferencesFactory.get().getProperty("donate.reminder"));
            if (DialogResult.OK == View.ShowDialog())
            {
                BrowserLauncherFactory.get().open(PreferencesFactory.get().getProperty("website.donate"));
            }
            if (View.NeverShowDonation)
            {
                PreferencesFactory.get()
                    .setProperty("donate.reminder", Assembly.GetExecutingAssembly().GetName().Version.ToString());
            }
            // Remeber this reminder date
            PreferencesFactory.get().setProperty("donate.reminder.date", DateTime.Now.Ticks);
        }
    }
}