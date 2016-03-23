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
using ch.cyberduck.core.preferences;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class UpdateController : WindowController<IUpdateView>
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

        /// <summary>
        /// Force the update check
        /// </summary>
        /// <param name="background">true if you want to perform the update check in the background.</param>
        public void ForceCheckForUpdates(bool background)
        {
            View.CheckForUpdates(background);
            PreferencesFactory.get().setProperty("update.check.last", DateTime.Now.Ticks);
        }
    }
}