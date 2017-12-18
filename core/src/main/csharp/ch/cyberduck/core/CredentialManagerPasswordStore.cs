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
using System.Net;
using ch.cyberduck.core;
using Ch.Cyberduck.Core.CredentialManager;

namespace Ch.Cyberduck.Core
{
    public class CredentialManagerPasswordStore : HostPasswordStore
    {
        public override void addPassword(Scheme scheme, int port, String hostName, String user, String password)
        {
            WinCredentialManager.SaveCredentials(hostName, new NetworkCredential(user, password));
        }

        public override string getPassword(Scheme scheme, int port, String hostName, String user)
        {
            return WinCredentialManager.GetCredentials(hostName)?.Password;
        }

        public override void addPassword(String serviceName, String user, String password)
        {
            WinCredentialManager.SaveCredentials(serviceName, new NetworkCredential(user, password));
        }

        public override string getPassword(String serviceName, String user)
        {
            return WinCredentialManager.GetCredentials(serviceName)?.Password;
        }
    }
}
