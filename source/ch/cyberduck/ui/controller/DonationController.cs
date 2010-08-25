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
using System.Reflection;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class DonationController : WindowController<IDonationView>
    {
        public DonationController()
            : this(ObjectFactory.GetInstance<IDonationView>())
        {
        }

        public DonationController(IDonationView view)
        {
            View = view;
        }

        public void Show()
        {
            int uses = Preferences.instance().getInteger("uses");
            View.Title = Locale.localizedString("Please Donate", "Donate") + " (" + uses + ")";
            View.NeverShowDonation =
                Assembly.GetExecutingAssembly().GetName().Version.ToString().Equals(
                    Preferences.instance().getProperty("donate.reminder"));
            if (DialogResult.OK == View.ShowDialog())
            {
                Utils.StartProcess(Preferences.instance().getProperty("website.donate"));
            }
            if (View.NeverShowDonation)
            {
                Preferences.instance().setProperty("donate.reminder",
                                                   Assembly.GetExecutingAssembly().GetName().Version.ToString());
            }
            // Remeber this reminder date
            Preferences.instance().setProperty("donate.reminder.date", DateTime.Now.Ticks);
        }
    }
}