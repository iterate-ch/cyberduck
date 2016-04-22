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

namespace Ch.Cyberduck.Ui.Sparkle
{
    public class WinSparkle
    {
        /// <summary>
        /// Delegate for <see cref="SetCanShutdownCallback"/>
        /// </summary>
        [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
        public delegate int win_sparkle_can_shutdown_callback_t();

        /// <summary>
        /// Delegate for <see cref="SetShutdownRequestCallback"/>
        /// </summary>
        [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
        public delegate void win_sparkle_shutdown_request_callback_t();

        /// <summary>
        /// <para>
        /// Starts WinSparkle.
        /// </para>
        /// <para>
        /// If WinSparkle is configured to check for updates on startup, proceeds
        /// to perform the check. You should only call this function when your app
        /// is initialized and shows its main window.
        /// </para>
        /// <para>
        /// Note This call doesn't block and returns almost immediately. If an 
        /// update is available, the respective UI is shown later from a separate
        /// thread.
        /// </para>
        /// <para>
        /// See <see cref="Cleanup"/>
        /// </para>
        /// </summary>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_init", CallingConvention = CallingConvention.Cdecl)]
        public static extern void Initialize();

        /// <summary>
        /// <para>
        /// Cleans up after WinSparkle.
        /// </para>
        /// <para>
        /// Should be called by the app when it's shutting down. Cancels any pending Sparkle operations and shuts down its helper threads.
        /// </para>
        /// </summary>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_cleanup", CallingConvention = CallingConvention.Cdecl)]
        public static extern void Cleanup();

        /// <summary>
        /// <para>
        /// Sets URL for the app's appcast.
        /// </para>
        /// <para>
        /// Only http and https schemes are supported.
        /// </para>
        /// <para>  
        /// If this function isn't called by the app, the URL is obtained from Windows resource named "FeedURL" of type "APPCAST".
        /// </para>
        /// <para>
        /// Can only be called @em before the first call to <see cref="Initialize()"/>
        /// </para>
        /// </summary>
        /// <param name="url">URL of the appcast.</param>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_appcast_url", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void SetAppcastUrl([MarshalAs(UnmanagedType.AnsiBStr)] string url);

        /// <summary>
        /// <para>
        /// Sets application metadata.
        /// </para>
        /// <para>
        /// Normally, these are taken from VERSIONINFO/StringFileInfo resources, but if 
        /// your application doesn't use them for some reason, using this function is an alternative.
        /// </para>
        /// <para>
        /// Can only be called @em before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.3</remarks>
        /// <param name="companyName">Company name of the vendor.</param>
        /// <param name="appName">Application name. This is both shown to the user and used in HTTP User-Agent header.</param>
        /// <param name="appVersion">Version of the app, as string (e.g. "1.2" or "1.2rc1").</param>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_app_details", CharSet = CharSet.Unicode,
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void SetAppDetails([MarshalAs(UnmanagedType.LPWStr)] string companyName,
            [MarshalAs(UnmanagedType.LPWStr)] string appName, [MarshalAs(UnmanagedType.LPWStr)] string appVersion);

        /// <summary>
        /// <para>
        /// Sets application build version number.
        /// </para>
        /// <para>
        /// This is the internal version number that is not normally shown to the user. It can be used for finer granularity that official 
        /// release versions, e.g. for interim builds. 
        /// </para>
        /// <para>
        /// If this function is called, then the provided *build* number is used for comparing versions; it is compared to the "version" 
        /// attribute in the appcast and corresponds to OS X Sparkle's CFBundleVersion handling. If used, then the appcast must also contain 
        /// the "shortVersionString" attribute with human-readable display version string. The version passed to <see cref="SetAppDetails"/> 
        /// corresponds to this and is used for display.
        /// </para>
        /// <para>
        /// Can only be called before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <param name="buildVersion">The version number</param>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_app_build_version", CharSet = CharSet.Unicode,
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void SetAppBuildVersion([MarshalAs(UnmanagedType.LPWStr)] string buildVersion);

        /// <summary>
        /// <para>
        /// Set the registry path where settings will be stored.
        /// </para>
        /// <para>
        /// Normally, these are stored in "HKCU\Software\&lt;company_name>\&lt;app_name>\WinSparkle" but if your application needs to 
        /// store the data elsewhere for some reason, using this function is an alternative.
        /// </para>
        /// <para>
        /// Note that the path is relative to HKCU/HKLM root and the root is not part of it. For example:
        /// </para>
        /// <example>
        /// SetRegistryPath(@"Software\My App\Updates");
        /// </example>
        /// <para>
        /// Can only be called before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.3</remarks>
        /// <param name="path">Registry path where settings will be stored.</param>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_registry_path", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void SetRegistryPath([MarshalAs(UnmanagedType.LPWStr)] string path);

        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_automatic_check_for_updates", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        private static extern void SetAutomaticCheckForUpdatesInternal(
            [MarshalAs(UnmanagedType.I4)] int enableAutomaticUpdates);

        /// <summary>
        /// <para>
        /// Sets whether updates are checked automatically or only through a manual call.
        /// </para>
        /// <para>
        /// If disabled, <see cref="CheckUpdateWithUi"/> must be used explicitly.
        /// </para>
        /// <para>
        /// Can only be called before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <param name="enableAutomaticUpdates"><c>True</c> to enable automatic check, <c>False</c> to disable automatic check</param>
        public static void SetAutomaticCheckForUpdates(bool enableAutomaticUpdates)
        {
            SetAutomaticCheckForUpdatesInternal(enableAutomaticUpdates ? 1 : 0);
        }

        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_get_automatic_check_for_updates", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        [return: MarshalAs(UnmanagedType.I4)]
        private static extern int GetAutomaticCheckForUpdatesInternal();

        /// <summary>
        /// <para>
        /// Gets the automatic update checking state. Defaults to 0 when not yet configured (as happens on first start).
        /// </para>
        /// <para>
        /// Can only be called before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <returns><c>True</c> if automatic update checks are enabled, <c>False</c> if automatic update checks are disabled</returns>
        public static bool GetAutomaticCheckForUpdates()
        {
            return GetAutomaticCheckForUpdatesInternal() == 1;
        }

        /// <summary>
        /// <para>
        /// Sets the automatic update interval.
        /// </para>
        /// <para>
        /// Can only be called before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <param name="intervalSeconds">The interval in seconds between checks for updates. The minimum update interval is 3600 seconds (1 hour).</param>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_update_check_interval", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void SetUpdateCheckInterval([MarshalAs(UnmanagedType.I4)] int intervalSeconds);

        /// <summary>
        /// <para>
        /// Gets the automatic update interval in seconds. Default value is one day.
        /// </para>
        /// <para>
        /// Can only be called before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <returns>The interval in seconds</returns>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_get_update_check_interval", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        [return: MarshalAs(UnmanagedType.I4)]
        public static extern int GetUpdateCheckInterval();


        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_get_last_check_time", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        [return: MarshalAs(UnmanagedType.I4)]
        private static extern int GetLastCheckTimeInternal();

        /// <summary>
        /// Gets the time for the last update check. Default value is -1, indicating that the update check has never run.
        /// <para>
        /// Can only be called before the first call to <see cref="Initialize"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <returns>The DateTime of the last check</returns>
        public static DateTime GetLastCheckTime()
        {
            var seconds = GetLastCheckTimeInternal();
            return seconds > 0 ? new DateTime(1970, 1, 1).AddSeconds(seconds).ToLocalTime() : DateTime.MinValue;
        }

        /// <summary>
        /// <para>
        /// Set callback for querying the application if it can be closed.
        /// </para>
        /// <para>
        /// This callback will be called to ask the host if it's ready to shut down, before attempting to launch the installer. 
        /// The callback returns <c>True</c> if the host application can be safely shut down or <c>False</c> if not (e.g. because the user has unsaved documents).
        /// </para>
        /// <para>
        /// There's no guarantee about the thread from which the callback is called, except that it certainly *won't* be called from the app's main thread. Make sure the callback is thread-safe.
        /// </para>
        /// <para>
        /// See <see cref="SetShutdownRequestCallback"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <param name="callback"></param>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_can_shutdown_callback", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void SetCanShutdownCallback(win_sparkle_can_shutdown_callback_t callback);

        /// <summary>
        /// <para>
        /// Set callback for shutting down the application.
        /// </para>
        /// <para>
        /// This callback will be called to ask the host to shut down immediately after launching the installer. 
        /// Its implementation should gracefully terminate the application.
        /// </para>
        /// <para>
        /// It will only be called if the call to the callback set with <see cref="SetCanShutdownCallback"/> returns <c>True</c>.
        /// </para>
        /// <para>
        /// See <see cref="SetCanShutdownCallback"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        /// <param name="callback"></param>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_set_shutdown_request_callback", CharSet = CharSet.Ansi,
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void SetShutdownRequestCallback(win_sparkle_shutdown_request_callback_t callback);

        /// <summary>
        /// <para>
        /// Checks if an update is available, showing progress UI to the user.
        /// </para>
        /// <para>
        /// Normally, WinSparkle checks for updates on startup and only shows its UI when it finds an update. 
        /// If the application disables this behavior, it can hook this function to "Check for updates..." menu item.
        /// </para>
        /// <para>
        /// When called, background thread is started to check for updates. A small window is shown to let the user know the progress. 
        /// If no update is found, the user is told so. If there is an update, the usual "update available" window is shown.
        /// </para>
        /// <para>
        /// This function returns immediately.
        /// </para>
        /// <para>
        /// See <see cref="CheckUpdateWithoutUi"/>
        /// </para>
        /// </summary>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_check_update_with_ui",
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void CheckUpdateWithUi();

        /// <summary>
        /// <para>
        /// Checks if an update is available.
        /// </para>
        /// <para>
        /// No progress UI is shown to the user when checking. If an update is available, the usual "update available" 
        /// window is shown; this function is *not* completely UI-less.
        /// </para>
        /// <para>
        /// Use with caution, it usually makes more sense to use the automatic update checks on interval option or manual check with visible UI. 
        /// </para>
        /// <para>
        /// This function returns immediately.
        /// </para>
        /// <para>
        /// See <see cref="CheckUpdateWithUi"/>
        /// </para>
        /// </summary>
        /// <remarks>Since 0.4</remarks>
        [DllImport("WinSparkle.dll", EntryPoint = "win_sparkle_check_update_without_ui",
            CallingConvention = CallingConvention.Cdecl)]
        public static extern void CheckUpdateWithoutUi();
    }
}