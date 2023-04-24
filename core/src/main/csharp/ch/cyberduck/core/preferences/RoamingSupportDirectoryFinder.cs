// 
// Copyright (c) 2010-2019 Yves Langisch. All rights reserved.
// https://cyberduck.io/
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

using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Local;
using Windows.Storage;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Preferences
{
    using Local = ch.cyberduck.core.Local;

    public class RoamingSupportDirectoryFinder : SupportDirectoryFinder
    {
        private static SystemLocal local;

        public static SystemLocal Local
        {
            get
            {
                return local ??= new(Runtime.Packaged.GetValueOrDefault()
                    ? ApplicationData.Current.LocalFolder.Path
                    : Path.Combine(EnvironmentInfo.AppDataPath, Runtime.DataFolderName));
            }
        }

        public Local find()
        {
            return new SystemLocal(Local);
        }
    }
}
