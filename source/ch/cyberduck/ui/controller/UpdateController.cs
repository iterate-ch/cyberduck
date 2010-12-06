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
using ch.cyberduck.core;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class UpdateController : WindowController<IUpdateView>
    {
        private static readonly UpdateController _instance = new UpdateController();

        private UpdateController()
        {
            View = ObjectFactory.GetInstance<IUpdateView>();
        }

        public static UpdateController Instance
        {
            get { return _instance; }
        }

        public override bool Singleton
        {
            get { return true; }
        }

        public bool AboutToInstallUpdate
        {
            get { return View.AboutToInstallUpdate; }
        }

        public void CheckForUpdatesIfNecessary()
        {
            if (Preferences.instance().getBoolean("update.check"))
            {
                DateTime lastCheck = new DateTime(Preferences.instance().getLong("update.check.last"));
                long interval = Preferences.instance().getLong("update.check.interval");

                // see if enough days have elapsed since last check.
                TimeSpan span = DateTime.Now.Subtract(lastCheck);
                if (span.TotalSeconds >= interval)
                {
                    ForceCheckForUpdates(true);
                }
            }
        }

        /// <summary>
        /// Force the update check
        /// </summary>
        /// <param name="background">true if you want to perform the update check in the background.</param>
        public void ForceCheckForUpdates(bool background)
        {
            View.CheckForUpdates(background);
            Preferences.instance().setProperty("update.check.last", DateTime.Now.Ticks);
        }
    }
}