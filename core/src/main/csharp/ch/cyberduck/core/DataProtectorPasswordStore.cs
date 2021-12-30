// 
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
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
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Core
{
    public class DataProtectorPasswordStore : DefaultHostPasswordStore
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(DataProtectorPasswordStore).FullName);

        // Login Password
        public override void addPassword(Scheme scheme, int port, String hostname, String user, String password)
        {
            string url = new HostUrlProvider().withPath(false).get(scheme, port, user, hostname, null);
            PreferencesFactory.get().setProperty(url, DataProtector.Encrypt(password));
        }

        // Login Password
        public override string getPassword(Scheme scheme, int port, String hostname, String user)
        {
            string password = PreferencesFactory.get().getProperty(new HostUrlProvider().withPath(false).get(scheme, port, user, hostname, null));
            if (null == password)
            {
                return null;
            }
            return DataProtector.Decrypt(password);
        }

        // Generic Password
        public override void addPassword(String serviceName, String user, String password)
        {
            PreferencesFactory.get().setProperty($"{serviceName} - {user}", DataProtector.Encrypt(password));
        }

        // Generic Password
        public override string getPassword(String serviceName, String user)
        {
            String password = PreferencesFactory.get().getProperty($"{serviceName} - {user}");
            if (null == password)
            {
                // Legacy implementation
                return getPassword(Scheme.ftp, Scheme.ftp.getPort(), serviceName, user);
            }
            return DataProtector.Decrypt(password);
        }

        public override void deletePassword(String serviceName, String user)
        {
            PreferencesFactory.get().deleteProperty($"{serviceName} - {user}");
        }

        public override void deletePassword(Scheme scheme, int port, string hostName, string user)
        {
            string url = new HostUrlProvider().withPath(false).get(scheme, port, user, hostName, null);
            PreferencesFactory.get().deleteProperty(url);
        }
    }
}
