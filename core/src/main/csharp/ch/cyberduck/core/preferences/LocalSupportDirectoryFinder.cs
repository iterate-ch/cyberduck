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

namespace Ch.Cyberduck.Core.Preferences
{
    using Local = ch.cyberduck.core.Local;

    public class LocalSupportDirectoryFinder : SupportDirectoryFinder
    {
        private static Local local;

        public static Local Local
        {
            get
            {
                if (local is null)
                {
                    local ??= new(EnvironmentInfo.LocalAppDataPath, EnvironmentInfo.DataFolderName);
                }

                return local;
            }
        }

        public Local find() => Local;

        public SupportDirectoryFinder setup()
        {
            return this;
        }
    }
}
