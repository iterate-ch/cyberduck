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
using ch.cyberduck.core.exception;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.CredentialManager;
using org.apache.logging.log4j;
using System;
using System.Collections.Generic;
using System.Net;
using System.Text;
using static Windows.Win32.Security.Credentials.CRED_PERSIST;
using static Windows.Win32.Security.Credentials.CRED_TYPE;

namespace Ch.Cyberduck.Core
{
    public class CredentialManagerPasswordStore : DefaultHostPasswordStore
    {
        private static Logger logger = LogManager.getLogger(typeof(CredentialManagerPasswordStore).FullName);

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

        public override void delete(Host bookmark)
        {
            if (logger.isInfoEnabled())
            {
                logger.info(string.Format("Delete password for bookmark {0}", bookmark));
            }
            var target = ToUri(bookmark);
            if (!WinCredentialManager.RemoveCredentials(target.AbsoluteUri))
            {
                base.delete(bookmark);
            }
        }

        public override void deletePassword(string serviceName, string user)
        {
            if (!WinCredentialManager.RemoveCredentials($"{serviceName} - {user}"))
            {
                throw new LocalAccessDeniedException($"Cannot delete {serviceName} - {user}");
            }
        }

        public override void deletePassword(Scheme scheme, int port, string hostName, string user)
        {
            var hostUrl = hostUrlProvider.get(scheme, port, user, hostName, string.Empty);
            if (!WinCredentialManager.RemoveCredentials(hostUrl))
            {
                throw new LocalAccessDeniedException($"Cannot delete {hostUrl}");
            }
        }

        public override string findLoginPassword(Host bookmark)
        {
            var cred = FindCredentials(bookmark);
            if (!string.IsNullOrWhiteSpace(cred.Password))
            {
                return cred.Password;
            }

            return base.findLoginPassword(bookmark);
        }

        public override string findLoginToken(Host bookmark)
        {
            if (logger.isInfoEnabled())
            {
                logger.info(string.Format("Fetching login token from keychain for {0}", bookmark));
            }
            var cred = FindCredentials(bookmark);
            if (cred.Attributes is Dictionary<string, string> attrs
                && attrs.TryGetValue("Token", out var token)
                && !string.IsNullOrWhiteSpace(token))
            {
                return token;
            }

            return base.findLoginToken(bookmark);
        }

        public override OAuthTokens findOAuthTokens(Host bookmark)
        {
            if (logger.isInfoEnabled())
            {
                logger.info(string.Format("Fetching OAuth tokens from keychain for {0}", bookmark));
            }

            var cred = FindCredentials(bookmark);
            if (cred.Attributes is Dictionary<string, string> attrs
                && attrs.TryGetValue("OAuth Access Token", out var accessToken)
                && !string.IsNullOrWhiteSpace(accessToken))
            {
                attrs.TryGetValue("OAuth Refresh Token", out var refreshToken);
                attrs.TryGetValue("OIDC Id Token", out var idToken);

                long? expiry = default;
                if (attrs.TryGetValue("OAuth Expiry", out var expiryAttribute) && long.TryParse(expiryAttribute, out var expiryValue))
                {
                    expiry = expiryValue;
                }

                return new(accessToken, refreshToken, expiry.HasValue ? new(expiry.Value) : null, idToken);
            }

            return base.findOAuthTokens(bookmark);
        }

        public override string findPrivateKeyPassphrase(Host bookmark)
        {
            if (logger.isInfoEnabled())
            {
                logger.info(string.Format("Fetching private key passphrase from keychain for {0}", bookmark));
            }

            var cred = FindCredentials(bookmark);
            if (cred.Attributes is Dictionary<string, string> attrs
                && attrs.TryGetValue("Private Key Passphrase", out var passphrase)
                && !string.IsNullOrWhiteSpace(passphrase))
            {
                return passphrase;
            }

            return base.findPrivateKeyPassphrase(bookmark);
        }

        public override string getPassword(string serviceName, string user) => WinCredentialManager.GetCredentials($"{serviceName} - {user}").Password;

        public override string getPassword(Scheme scheme, int port, string hostName, string user)
        {
            var hostUrl = hostUrlProvider.get(scheme, port, user, hostName, string.Empty);
            return WinCredentialManager.GetCredentials(hostUrl).Password;
        }

        public override void save(Host bookmark)
        {
            if (string.IsNullOrWhiteSpace(bookmark.getHostname()))
            {
                logger.warn("No hostname given.");
                return;
            }

            if (logger.isInfoEnabled())
            {
                logger.info(string.Format("Add password for bookmark {0}", bookmark));
            }

            var credential = bookmark.getCredentials();
            var target = ToUri(bookmark, credential.isOAuthAuthentication(), out var isOAuth);

            var winCred = new WindowsCredentialManagerCredential(
                credential.getUsername(), credential.getPassword(),
                CRED_TYPE_GENERIC, 0, CRED_PERSIST_ENTERPRISE);

            if (credential.isPasswordAuthentication() &&
                string.IsNullOrWhiteSpace(credential.getPassword()))
            {
                logger.warn(string.Format("No password in credentials for bookmark {0}", bookmark.getHostname()));
            }

            if (credential.isTokenAuthentication())
            {
                winCred.Attributes["Token"] = credential.getToken();
            }

            bool migrateOAuth = false;
            if (credential.isOAuthAuthentication())
            {
                migrateOAuth = isOAuth;

                var oauth = credential.getOauth();
                winCred.Attributes["OAuth Access Token"] = oauth.getAccessToken();
                winCred.Attributes["OAuth Refresh Token"] = oauth.getRefreshToken();
                winCred.Attributes["OIDC Id Token"] = oauth.getIdToken();
                if (oauth.getExpiryInMilliseconds()?.longValue() is long oAuthExpiry)
                {
                    winCred.Attributes["OAuth Expiry"] = oAuthExpiry.ToString();
                }
            }

            if (credential.isPublicKeyAuthentication())
            {
                winCred.Attributes["Private Key Passphrase"] = credential.getIdentityPassphrase();
            }

            if (!WinCredentialManager.SaveCredentials(target.AbsoluteUri, winCred))
            {
                base.save(bookmark);
            }
            else if (migrateOAuth)
            {
                // remove previous entry, if saved successfully
                WinCredentialManager.RemoveCredentials(ToUri(bookmark, false, out _).AbsoluteUri);
            }
        }

        private static Uri ToUri(Host bookmark) => ToUri(bookmark, true, out _);

        private static Uri ToUri(Host bookmark, bool withOAuth, out bool isOAuth)
        {
            var protocol = bookmark.getProtocol();
            var credentials = bookmark.getCredentials();
            var targetBuilder = new UriBuilder(PreferencesFactory.get().getProperty("application.container.name"), string.Empty);
            var pathBuilder = new StringBuilder();

            string hostname = default;
            int? port = bookmark.getPort();
            if (withOAuth && protocol.isOAuthConfigurable() && GetOAuthUri(protocol, out var oAuthUri))
            {
                isOAuth = true;
                pathBuilder.Append("oauth");
                hostname = oAuthUri.Host;
                if (oAuthUri.Port != -1)
                {
                    port = oAuthUri.IsDefaultPort
                        ? null
                        : oAuthUri.Port;
                }
            }
            else
            {
                isOAuth = false;
                pathBuilder.Append(protocol.getIdentifier());

                if (protocol.isHostnameConfigurable() || !(protocol.isTokenConfigurable() || protocol.isOAuthConfigurable()))
                {
                    hostname = bookmark.getHostname();
                }
            }

            if (!string.IsNullOrWhiteSpace(hostname))
            {
                pathBuilder.AppendFormat(":{0}", hostname);

                if (port is int portValue && !Equals(protocol.getDefaultPort(), portValue))
                {
                    pathBuilder.AppendFormat(":{0}", portValue);
                }
            }

            targetBuilder.Path = pathBuilder.ToString();
            if (!string.IsNullOrWhiteSpace(credentials.getUsername()))
            {
                targetBuilder.Query = "user=" + credentials.getUsername();
            }

            return targetBuilder.Uri;

            static bool GetOAuthUri(Protocol protocol, out Uri uri)
            {
                var tokenUrl = protocol.getOAuthTokenUrl();
                bool result = Uri.TryCreate(tokenUrl, UriKind.Absolute, out uri);
                if (!result && logger.isDebugEnabled())
                {
                    logger.debug($"Failure creating absolute Uri from \"{tokenUrl}\"");
                }

                return result;
            }
        }

        private WindowsCredentialManagerCredential FindCredentials(Host host)
        {
            // Try find new oauth format, or default format
            if (WinCredentialManager.TryGetCredentials(ToUri(host, true, out var isOAuth).AbsoluteUri, out var result))
            {
                return result;
            }

            // new OAuth format not found and not a OAuth protocol, return default
            if (!isOAuth)
            {
                return default;
            }

            // is OAuth protocol, but wasn't migrated to new format yet
            return WinCredentialManager.GetCredentials(ToUri(host, false, out _).AbsoluteUri);
        }

    }
}
