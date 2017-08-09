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
using org.apache.log4j;

namespace Ch.Cyberduck.Core
{
    public class DataProtectorPasswordStore : HostPasswordStore
    {
        private static readonly Logger Log = Logger.getLogger(typeof(DataProtectorPasswordStore).FullName);

        // Login Password
        public override void addPassword(Scheme scheme, int port, String hostName, String user, String password)
        {
            Host host = new Host(ProtocolFactory.get().forScheme(scheme), hostName, port);
            host.getCredentials().setUsername(user);
            PreferencesFactory.get().setProperty(new HostUrlProvider().get(host), DataProtector.Encrypt(password));
        }

        // Login Password
        public override string getPassword(Scheme scheme, int port, String hostName, String user)
        {
            Host host = new Host(ProtocolFactory.get().forScheme(scheme), hostName, port);
            host.getCredentials().setUsername(user);
            return getPassword(host);
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
                Protocol ftp = ProtocolFactory.get().forScheme(Scheme.ftp);
                if (null == ftp)
                {
                    return null;
                }
                Host host = new Host(ProtocolFactory.get().forScheme(Scheme.ftp), serviceName);
                host.getCredentials().setUsername(user);
                return getPassword(host);
            }
            return password;
        }

        private string getPassword(Host host)
        {
            string password = PreferencesFactory.get().getProperty(new HostUrlProvider().get(host));
            if (null == password)
            {
                return null;
            }
            return DataProtector.Decrypt(password);
        }
    }
}