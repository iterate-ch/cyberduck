// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 

using System;
using ch.cyberduck.core;
using ch.cyberduck.core.preferences;
using org.apache.log4j;

namespace Ch.Cyberduck.Core
{
    public class DataProtectorPasswordStore : HostPasswordStore
    {
        private static readonly Logger Log = Logger.getLogger(typeof (DataProtectorPasswordStore).FullName);

        public override string getPassword(Scheme scheme, int port, String hostName, String user)
        {
            Host host = new Host(ProtocolFactory.forScheme(scheme.name()), hostName, port);
            host.getCredentials().setUsername(user);
            return getPassword(host);
        }

        public override string getPassword(String hostName, String user)
        {
            Host host = new Host(ProtocolFactory.forScheme(Scheme.ftp.name()), hostName);
            host.getCredentials().setUsername(user);
            return getPassword(host);
        }

        public override void addPassword(String hostName, String user, String password)
        {
            Host host = new Host(ProtocolFactory.forScheme(Scheme.ftp.name()), hostName);
            host.getCredentials().setUsername(user);
            PreferencesFactory.get().setProperty(new HostUrlProvider().get(host), DataProtector.Encrypt(password));
        }

        public override void addPassword(Scheme scheme, int port, String hostName, String user, String password)
        {
            Host host = new Host(ProtocolFactory.forScheme(scheme.name()), hostName, port);
            host.getCredentials().setUsername(user);
            PreferencesFactory.get().setProperty(new HostUrlProvider().get(host), DataProtector.Encrypt(password));
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