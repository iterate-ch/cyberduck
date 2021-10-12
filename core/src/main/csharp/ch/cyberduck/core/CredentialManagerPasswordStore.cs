//
// Copyright (c) 2021 iterate GmbH. All rights reserved.
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
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.CredentialManager;
using org.apache.logging.log4j;
using System;
using System.Collections.Generic;
using static Windows.Win32.Security.Credentials.CRED_PERSIST;
using static Windows.Win32.Security.Credentials.CRED_TYPE;
using System.Text;

namespace Ch.Cyberduck.Core
{

    public class CredentialManagerPasswordStore : HostPasswordStore
    {
        private static Logger logger = LogManager.getLogger(typeof(CredentialManagerPasswordStore).FullName);

        private readonly HostUrlProvider hostUrlProvider = new HostUrlProvider();

        void PasswordStore.addPassword(string serviceName, string accountName, string password)
        {
            throw new NotImplementedException();
        }

        void PasswordStore.addPassword(Scheme scheme, int port, string hostname, string user, string password)
        {
            throw new NotImplementedException();
        }

        void HostPasswordStore.delete(Host bookmark)
        {
            if (log.isInfoEnabled())
            {
                log.info(string.Format("Delete password for bookmark {0}", bookmark));
            }
            var target = ToUri(bookmark);
            WinCredentialManager.RemoveCredentials(target.AbsoluteUri);
        }

        void PasswordStore.deletePassword(string serviceName, string user)
        {
            throw new NotImplementedException();
        }

        void PasswordStore.deletePassword(Scheme scheme, int port, string hostname, string user)
        {
            throw new NotImplementedException();
        }

        string HostPasswordStore.findLoginPassword(Host bookmark)
        {
            var target = ToUri(bookmark);
            var cred = WinCredentialManager.GetCredentials(target.AbsoluteUri);
            if (cred is null)
            {
                return default;
            }
            return cred.Password;
        }

        string HostPasswordStore.findLoginToken(Host bookmark)
        {
            if (log.isInfoEnabled())
            {
                log.info(string.Format("Fetching login token from keychain for {0}", bookmark));
            }
            var target = ToUri(bookmark);
            var cred = WinCredentialManager.GetCredentials(target.AbsoluteUri);
            if (cred is null)
            {
                return default;
            }
            return cred.Attributes.TryGetValue("Token", out var token) ? token : default;
        }

        OAuthTokens HostPasswordStore.findOAuthTokens(Host bookmark)
        {
            if (log.isInfoEnabled())
            {
                log.info(string.Format("Fetching OAuth tokens from keychain for {0}", bookmark));
            }
            var target = ToUri(bookmark);
            var cred = WinCredentialManager.GetCredentials(target.AbsoluteUri);
            if (cred is null)
            {
                return OAuthTokens.EMPTY;
            }
            cred.Attributes.TryGetValue("OAuth Access Token", out var accessToken);
            cred.Attributes.TryGetValue("OAuth Refresh Token", out var refreshToken);
            long expiry = default;
            if (cred.Attributes.TryGetValue("OAuth Expiry", out var expiryValue))
            {
                long.TryParse(expiryValue, out expiry);
            }
            return new(accessToken, refreshToken, new(expiry));
        }

        string HostPasswordStore.findPrivateKeyPassphrase(Host bookmark)
        {
            if (log.isInfoEnabled())
            {
                log.info(string.Format("Fetching private key passphrase from keychain for {0}", bookmark));
            }
            var target = ToUri(bookmark);
            var cred = WinCredentialManager.GetCredentials(target.AbsoluteUri);
            if (cred is null)
            {
                return default;
            }
            cred.Attributes.TryGetValue("Private Key Passphrase", out var passphrase);
            return passphrase;
        }

        string PasswordStore.getPassword(string serviceName, string accountName)
        {
            throw new NotImplementedException();
        }

        string PasswordStore.getPassword(Scheme scheme, int port, string hostname, string user)
        {
            throw new NotImplementedException();
        }

        void HostPasswordStore.save(Host bookmark)
        {
            if (string.IsNullOrWhiteSpace(bookmark.getHostname()))
            {
                log.warn("No hostname given.");
                return;
            }
            if (log.isInfoEnabled())
            {
                log.info(string.Format("Add password for bookmark {0}", bookmark));
            }
            var target = ToUri(bookmark);
            var protocol = bookmark.getProtocol();
            var credential = bookmark.getCredentials();
            var additionalInfo = new Dictionary<string, string>();
            if (protocol.isTokenConfigurable())
            {
                additionalInfo["Token"] = credential.getToken();
            }
            if (protocol.isOAuthConfigurable())
            {
                additionalInfo["OAuth Access Token"] = credential.getOauth().getAccessToken();
                additionalInfo["OAuth Refresh Token"] = credential.getOauth().getRefreshToken();
                if (credential.getOauth().getExpiryInMilliseconds() != null)
                {
                    additionalInfo["OAuth Expiry"] = credential.getOauth().getExpiryInMilliseconds().longValue().ToString();
                }
            }
            if (protocol.isPrivateKeyConfigurable())
            {
                additionalInfo["Private Key Passphrase"] = credential.getIdentityPassphrase();
            }
            if (credential.isPasswordAuthentication())
            {
                if (string.IsNullOrWhiteSpace(credential.getUsername()))
                {
                    log.warn(string.Format("No username in credentials for bookmark {0}", bookmark.getHostname()));
                    return;
                }
                if (string.IsNullOrWhiteSpace(credential.getPassword()))
                {
                    log.warn(string.Format("No password in credentials for bookmark {0}", bookmark.getHostname()));
                    return;
                }
            }
            var winCred = new WindowsCredentialManagerCredential(
                credential.getUsername(), credential.getPassword(),
                CRED_TYPE_GENERIC, 0, CRED_PERSIST_ENTERPRISE,
                additionalInfo);
            WinCredentialManager.SaveCredentials(target.AbsoluteUri, winCred);
        }

        private static Uri ToUri(Host bookmark)
        {
            var protocol = bookmark.getProtocol();
            var credentials = bookmark.getCredentials();

            var targetBuilder = new UriBuilder(PreferencesFactory.get().getProperty("application.container.name"), string.Empty);
            var pathBuilder = new StringBuilder();
            pathBuilder.Append(protocol.getIdentifier());
            if (protocol.isHostnameConfigurable() || !(protocol.isTokenConfigurable() || protocol.isOAuthConfigurable()))
            {
                pathBuilder.Append(":" + bookmark.getHostname());
                if (protocol.isPortConfigurable() && !Equals(protocol.getDefaultPort(), bookmark.getPort()))
                {
                    pathBuilder.Append(":" + bookmark.getPort());
                }
            }
            targetBuilder.Path = pathBuilder.ToString();
            targetBuilder.Query = "user=" + credentials.getUsername();

            return targetBuilder.Uri;
        }
    }
}
