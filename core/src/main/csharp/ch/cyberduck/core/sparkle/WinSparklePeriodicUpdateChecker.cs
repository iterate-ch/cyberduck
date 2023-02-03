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

using ch.cyberduck.core;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.updater;
using Microsoft.Win32.SafeHandles;
using org.apache.logging.log4j;
using System;
using System.Runtime.InteropServices;
using System.Security.Principal;
using Windows.Win32.Security;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.Security.TOKEN_INFORMATION_CLASS;

namespace Ch.Cyberduck.Core.Sparkle
{
    public class WinSparklePeriodicUpdateChecker : AbstractPeriodicUpdateChecker, IDisposable
    {
        private static readonly Logger Log = LogManager.getLogger(typeof (WinSparklePeriodicUpdateChecker).Name);
        private readonly ch.cyberduck.core.preferences.Preferences _preferences = PreferencesFactory.get();

        public WinSparklePeriodicUpdateChecker(Controller controller)
            : base(controller)
        {
            // set app details
            WinSparkle.SetAppDetails(
                companyName: string.Empty, // auto read from assembly
                appName: _preferences.getProperty("application.name"),
                appVersion: _preferences.getProperty("application.version"));
            WinSparkle.SetAppBuildVersion(_preferences.getProperty("application.revision"));
            WinSparkle.SetAutomaticCheckForUpdates(false);
            WinSparkle.Initialize();
        }

        public static void SetCanShutdownCallback(WinSparkle.win_sparkle_can_shutdown_callback_t callback)
        {
            WinSparkle.SetCanShutdownCallback(callback);
        }

        public static void SetShutdownRequestCallback(WinSparkle.win_sparkle_shutdown_request_callback_t callback)
        {
            WinSparkle.SetShutdownRequestCallback(callback);
        }

        public override void check(bool background)
        {
            Log.debug($"Checking for updates, background= {background}");
            WinSparkle.SetAppcastUrl(this.getFeedUrl());
            if (background)
            {
                WinSparkle.CheckUpdateWithoutUi();
            }
            else
            {
                WinSparkle.CheckUpdateWithUi();
            }
        }

        public unsafe override bool hasUpdatePrivileges()
        {
            var privilegeCheck = PreferencesFactory.get().getBoolean("update.check.privilege");
            if (!privilegeCheck)
            {
                return true;
            }

            var identity = WindowsIdentity.GetCurrent();
            if (identity == null)
            {
                Log.warn("Couldn't get the current user identity");
                return false;
            }
            var principal = new WindowsPrincipal(identity);

            // Check if this user has the Administrator role. If they do, return immediately.
            // If UAC is on, and the process is not elevated, then this will actually return false.
            if (principal.IsInRole(WindowsBuiltInRole.Administrator)) return true;

            // If we're not running in Vista onwards, we don't have to worry about checking for UAC.
            if (Environment.OSVersion.Platform != PlatformID.Win32NT || Environment.OSVersion.Version.Major < 6)
            {
                // Operating system does not support UAC; skipping elevation check.
                return false;
            }

            TOKEN_ELEVATION_TYPE elevationType;
            using var identityHandle = new SafeFileHandle(identity.Token, false);
            var result = GetTokenInformation(identityHandle, TokenElevationType, &elevationType, sizeof(TOKEN_ELEVATION_TYPE), out var length);
            if (!result)
            {
                var exception = Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error());
                Log.warn("Exception while retrieving token information", exception);
                return false;
            }

            switch (elevationType)
            {
                case TOKEN_ELEVATION_TYPE.TokenElevationTypeDefault:
                    // TokenElevationTypeDefault - User is not using a split token, so they cannot elevate.
                    return false;

                case TOKEN_ELEVATION_TYPE.TokenElevationTypeFull:
                    // TokenElevationTypeFull - User has a split token, and the process is running elevated. Assuming they're an administrator.
                    return true;

                case TOKEN_ELEVATION_TYPE.TokenElevationTypeLimited:
                    // TokenElevationTypeLimited - User has a split token, but the process is not running elevated. Assuming they're an administrator.
                    return true;

                default:
                    Log.warn($"Unknown token elevation type: {elevationType}");
                    return false;
            }
        }

        #region IDisposable Support

        private bool disposedValue = false;

        ~WinSparklePeriodicUpdateChecker()
        {
            Dispose(false);
        }

        void IDisposable.Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!disposedValue)
            {
                WinSparkle.Cleanup();

                disposedValue = true;
            }
        }

        #endregion IDisposable Support
    }
}
