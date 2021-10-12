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
using ch.cyberduck.core.exception;
using Ch.Cyberduck.Core.CredentialManager;
using org.apache.log4j;
using System;
using System.Net;

namespace Ch.Cyberduck.Core
{
    public class CredentialManagerDefaultFallbackPasswordStore : DefaultHostPasswordStore
    {
        private static Logger logger = Logger.getLogger(typeof(CredentialManagerDefaultFallbackPasswordStore).FullName);

        private readonly HostUrlProvider hostUrlProvider = new HostUrlProvider();

        public override void addPassword(string serviceName, string user, string password)
        {
            var hostUrl = $"{serviceName} - {user}";
            if (!WinCredentialManager.SaveCredentials(hostUrl, new NetworkCredential(user, password)))
            {
                throw new LocalAccessDeniedException($"Could not save credentials for \"{hostUrl}\" to Windows Credential Manager.");
            }
        }

        public override void addPassword(Scheme scheme, int port, string hostName, string user, string password)
        {
            var hostUrl = hostUrlProvider.get(scheme, port, user, hostName, string.Empty);
            if (!WinCredentialManager.SaveCredentials(hostUrl, new NetworkCredential(user, password)))
            {
                throw new LocalAccessDeniedException($"Could not save credentials for \"{hostUrl}\" to Windows Credential Manager.");
            }
        }

        public override string getPassword(string serviceName, string user)
        {
            return WinCredentialManager.GetCredentials($"{serviceName} - {user}").Password;
        }

        public override string getPassword(Scheme scheme, int port, string hostName, string user)
        {
            var hostUrl = hostUrlProvider.get(scheme, port, user, hostName, string.Empty);
            return WinCredentialManager.GetCredentials(hostUrl).Password;
        }

        public override void deletePassword(String serviceName, String user)
        {
            WinCredentialManager.RemoveCredentials($"{serviceName} - {user}");
        }

        public override void deletePassword(Scheme scheme, int port, string hostName, string user)
        {
            var hostUrl = hostUrlProvider.get(scheme, port, user, hostName, string.Empty);
            WinCredentialManager.RemoveCredentials(hostUrl);
        }
    }
}
