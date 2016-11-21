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
using System.Runtime.InteropServices;
using System.Security.Principal;
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

        [DllImport("advapi32.dll", SetLastError = true)]
        private static extern bool GetTokenInformation(IntPtr tokenHandle, TokenInformationClass tokenInformationClass,
            IntPtr tokenInformation, int tokenInformationLength, out int returnLength);

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
            if (Utils.IsUWPSupported)
            {
                Log.debug("Tried Update Privileges. Is UWP, won't continue.");
                return false;
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

            int tokenInfLength = Marshal.SizeOf(typeof (int));
            IntPtr tokenInformation = Marshal.AllocHGlobal(tokenInfLength);

            try
            {
                var result = GetTokenInformation(identity.Token, TokenInformationClass.TokenElevationType,
                    tokenInformation, tokenInfLength, out tokenInfLength);

                if (!result)
                {
                    var exception = Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error());
                    Log.warn("Exception while retrieving token information", exception);
                    return false;
                }

                var elevationType = (TokenElevationType) Marshal.ReadInt32(tokenInformation);

                switch (elevationType)
                {
                    case TokenElevationType.TokenElevationTypeDefault:
                        // TokenElevationTypeDefault - User is not using a split token, so they cannot elevate.
                        return false;
                    case TokenElevationType.TokenElevationTypeFull:
                        // TokenElevationTypeFull - User has a split token, and the process is running elevated. Assuming they're an administrator.
                        return true;
                    case TokenElevationType.TokenElevationTypeLimited:
                        // TokenElevationTypeLimited - User has a split token, but the process is not running elevated. Assuming they're an administrator.
                        return true;
                    default:
                        Log.warn($"Unknown token elevation type: {elevationType}");
                        return false;
                }
            }
            finally
            {
                if (tokenInformation != IntPtr.Zero) Marshal.FreeHGlobal(tokenInformation);
            }
        }

        private enum TokenInformationClass
        {
            TokenUser = 1,
            TokenGroups,
            TokenPrivileges,
            TokenOwner,
            TokenPrimaryGroup,
            TokenDefaultDacl,
            TokenSource,
            TokenType,
            TokenImpersonationLevel,
            TokenStatistics,
            TokenRestrictedSids,
            TokenSessionId,
            TokenGroupsAndPrivileges,
            TokenSessionReference,
            TokenSandBoxInert,
            TokenAuditPolicy,
            TokenOrigin,
            TokenElevationType,
            TokenLinkedToken,
            TokenElevation,
            TokenHasRestrictions,
            TokenAccessInformation,
            TokenVirtualizationAllowed,
            TokenVirtualizationEnabled,
            TokenIntegrityLevel,
            TokenUiAccess,
            TokenMandatoryPolicy,
            TokenLogonSid,
            MaxTokenInfoClass
        }

        /// <summary>
        /// The elevation type for a user token.
        /// </summary>
        private enum TokenElevationType
        {
            TokenElevationTypeDefault = 1,
            TokenElevationTypeFull,
            TokenElevationTypeLimited
        }
    }
}