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
using ch.cyberduck.core;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.updater;
using java.time;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Sparkle
{
    public class WinSparklePeriodicUpdateChecker : AbstractPeriodicUpdateChecker
    {
        private static readonly Logger Log = Logger.getLogger(typeof (WinSparklePeriodicUpdateChecker).Name);
        private readonly ch.cyberduck.core.preferences.Preferences _preferences = PreferencesFactory.get();
        
        public WinSparklePeriodicUpdateChecker(Controller controller)
            : base(controller)
        {
        }

        public override void unregister()
        {
            base.unregister();
            WinSparkle.Cleanup();
        }

        public static void SetCanShutdownCallback(WinSparkle.win_sparkle_can_shutdown_callback_t callback)
        {
            WinSparkle.SetCanShutdownCallback(callback);
        }

        public static void SetShutdownRequestCallback(WinSparkle.win_sparkle_shutdown_request_callback_t callback)
        {
            WinSparkle.SetShutdownRequestCallback(callback);
        }

        public override Duration register()
        {
            WinSparkle.SetAutomaticCheckForUpdates(false);
            WinSparkle.Initialize();
            return base.register();
        }

        public override void check(bool background)
        {
            Log.debug($"Checking for updates, background= {background}");
            SetAppcastURL();
            if (background)
            {
                WinSparkle.CheckUpdateWithoutUi();
            }
            else
            {
                WinSparkle.CheckUpdateWithUi();
            }
            _preferences.setProperty("update.check.last", DateTime.Now.Ticks);
        }

        private void SetAppcastURL()
        {
            String currentFeed = _preferences.getProperty("update.feed");
            String feedUrl = _preferences.getProperty("update.feed." + currentFeed);
            Log.debug("Setting feed URL to " + feedUrl);
            WinSparkle.SetAppcastUrl(feedUrl);
        }

        public override bool hasUpdatePrivileges()
        {
            return true;
        }
    }
}
