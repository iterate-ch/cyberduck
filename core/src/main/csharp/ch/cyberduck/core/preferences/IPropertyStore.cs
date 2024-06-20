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

namespace Ch.Cyberduck.Core.Preferences
{
    public interface IPropertyStore
    {
        string this[string property] { get; set; }

        void DeleteProperty(string property);

        void Load();

        void Save();
    }

    public interface IPropertyStoreFactory
    {
        IPropertyStore New();
    }
}
