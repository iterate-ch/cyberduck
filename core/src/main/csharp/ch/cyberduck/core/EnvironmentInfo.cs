//
// Copyright (c) 2023 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 

using System;
using System.IO;
using Windows.Win32.UI.Shell;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core
{
    public static class EnvironmentInfo
    {
        public static string AppDataPath { get; }

        public static string CommonAppDataPath { get; }

        public static string DownloadsPath { get; }

        public static string LocalAppDataPath { get; }

        public static string UserProfilePath { get; }

        static EnvironmentInfo()
        {
            AppDataPath = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            CommonAppDataPath = Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData);
            LocalAppDataPath = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            UserProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
            try
            {
                DownloadsPath = SHGetKnownFolderPath(FOLDERID_Downloads, KNOWN_FOLDER_FLAG.KF_FLAG_DONT_VERIFY, default);
            }
            catch
            {
                DownloadsPath = Path.Combine(UserProfilePath, "Downloads");
            }
        }
    }
}
