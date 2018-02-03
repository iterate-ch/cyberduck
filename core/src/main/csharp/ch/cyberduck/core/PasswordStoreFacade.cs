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

using ch.cyberduck.core;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Ch.Cyberduck.Core
{
    public class PasswordStoreFacade : HostPasswordStore
    {
        private readonly CredentialManagerPasswordStore credentialManagerPasswordStore = new CredentialManagerPasswordStore();
        private readonly DataProtectorPasswordStore dataProtectorPasswordStore = new DataProtectorPasswordStore();

        public override void addPassword(string serviceName, string user, string password)
        {
            credentialManagerPasswordStore.addPassword(serviceName, user, password);
        }

        public override void addPassword(Scheme scheme, int port, string hostName, string user, string password)
        {
            credentialManagerPasswordStore.addPassword(scheme, port, hostName, user, password);
        }

        public override string getPassword(Scheme scheme, int port, string hostName, string user)
        {
            var password = credentialManagerPasswordStore.getPassword(scheme, port, hostName, user);
            if (string.IsNullOrWhiteSpace(password))
            {
                password = dataProtectorPasswordStore.getPassword(scheme, port, hostName, user);
            }
            return password;
        }

        public override string getPassword(string serviceName, string user)
        {
            var password = credentialManagerPasswordStore.getPassword(serviceName, user);
            if (string.IsNullOrWhiteSpace(password))
            {
                password = dataProtectorPasswordStore.getPassword(serviceName, user);
            }
            return password;
        }
    }
}
