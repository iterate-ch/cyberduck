// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using System.Security.Cryptography.X509Certificates;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using org.apache.log4j;
using X509Certificate = java.security.cert.X509Certificate;

namespace Ch.Cyberduck.Ui.Controller
{
    public class Keychain : AbstractKeychain
    {
        private static readonly Logger Log = Logger.getLogger(typeof (Keychain).FullName);

        public override string getPassword(String protocol, int port, String hostName, String user)
        {
            Host host = new Host(Protocol.forScheme(protocol), hostName);
            host.getCredentials().setUsername(user);
            return getPassword(host);
        }

        private string getPassword(Host host)
        {
            string password = Preferences.instance().getProperty(host.toURL());
            if (null == password)
            {
                return null;
            }
            return DataProtector.Decrypt(password);
        }

        public override string getPassword(String hostName, String user)
        {
            Host host = new Host(hostName);
            host.getCredentials().setUsername(user);
            return getPassword(host);
        }

        public override void addPassword(String hostName, String user, String password)
        {
            Host host = new Host(hostName);
            host.getCredentials().setUsername(user);
            Preferences.instance().setProperty(host.toURL(), DataProtector.Encrypt(password));
        }

        public override void addPassword(String protocol, int port, String hostName, String user, String password)
        {
            Host host = new Host(Protocol.forScheme(protocol), hostName, port);
            host.getCredentials().setUsername(user);
            Preferences.instance().setProperty(host.toURL(), DataProtector.Encrypt(password));
        }

        public override bool isTrusted(String hostName, X509Certificate[] certs)
        {
            X509Certificate2 serverCert = ConvertCertificate(certs[0]);
            X509Chain chain = new X509Chain();
            //todo Online revocation check. Preference.
            chain.ChainPolicy.RevocationMode = X509RevocationMode.Offline; // | X509RevocationMode.Online
            chain.ChainPolicy.UrlRetrievalTimeout = new TimeSpan(0, 0, 0, 10); // set timeout to 10 seconds
            chain.ChainPolicy.VerificationFlags = X509VerificationFlags.NoFlag;

            for (int index = 1; index < certs.Length; index++)
            {
                chain.ChainPolicy.ExtraStore.Add(ConvertCertificate(certs[index]));
            }
            chain.Build(serverCert);

            string errorFromChainStatus = GetErrorFromChainStatus(chain, hostName);
            bool certError = null != errorFromChainStatus;
            bool hostnameMismatch = !HostnameVerifier.CheckServerIdentity(certs[0], serverCert, hostName) &&
                                    !CheckForException(hostName, serverCert);

            // check if host name matches
            if (null == errorFromChainStatus && hostnameMismatch)
            {
                errorFromChainStatus = Locale.localizedString(
                    "The certificate for this server is invalid. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                    "Keychain").Replace("%@", hostName);
            }

            if (null != errorFromChainStatus)
            {
                while (true)
                {
                    cTaskDialog.ForceEmulationMode = true;
                    int r =
                        cTaskDialog.ShowCommandBox(Locale.localizedString("This certificate is not valid", "Keychain"),
                                                   null,
                                                   errorFromChainStatus,
                                                   null,
                                                   null,
                                                   Locale.localizedString("Always Trust", "Keychain"),
                                                   Locale.localizedString("Continue", "Credentials") + "|" +
                                                   Locale.localizedString("Disconnect") + "|" +
                                                   Locale.localizedString("Show Certificate", "Keychain"),
                                                   false,
                                                   eSysIcons.Warning, eSysIcons.Information);
                    if (r == 0)
                    {
                        if (cTaskDialog.VerificationChecked)
                        {
                            if (certError)
                            {
                                //todo can we use the Trusted People and Third Party Certificate Authority Store? Currently X509Chain is the problem.
                                AddCertificate(serverCert, StoreName.Root);
                            }
                            if (hostnameMismatch)
                            {
                                Preferences.instance().setProperty(hostName + ".certificate.accept",
                                                                   serverCert.SubjectName.Name);
                            }
                        }
                        return true;
                    }
                    if (r == 1)
                    {
                        return false;
                    }
                    if (r == 2)
                    {
                        X509Certificate2UI.DisplayCertificate(serverCert);
                    }
                }
            }
            return true;
        }

        private bool CheckForException(string hostname, X509Certificate2 cert)
        {
            string accCert = Preferences.instance().getProperty(hostname + ".certificate.accept");
            if (Utils.IsNotBlank(accCert))
            {
                return accCert.Equals(cert.SubjectName.Name);
            }
            return false;
        }

        private void AddCertificate(X509Certificate2 cert, StoreName storeName)
        {
            Log.debug("AddCertificate:" + cert.SubjectName.Name);
            X509Store store = new X509Store(storeName, StoreLocation.CurrentUser);
            store.Open(OpenFlags.ReadWrite);
            store.Add(cert);
            store.Close();
        }

        private string GetErrorFromChainStatus(X509Chain chain, string hostName)
        {
            string error = null;
            foreach (X509ChainElement element in chain.ChainElements)
            {
                if (element.ChainElementStatus.Length > 0)
                {
                    foreach (X509ChainStatus status in element.ChainElementStatus)
                    {
                        if ((status.Status & X509ChainStatusFlags.RevocationStatusUnknown) ==
                            X509ChainStatusFlags.RevocationStatusUnknown ||
                            ((status.Status & X509ChainStatusFlags.OfflineRevocation) ==
                             X509ChainStatusFlags.OfflineRevocation))
                        {
                            //due to the offline revocation check
                            continue;
                        }
                        if ((status.Status & X509ChainStatusFlags.NotTimeValid) == X509ChainStatusFlags.NotTimeValid)
                        {
                            if (DateTime.Compare(DateTime.Now, element.Certificate.NotAfter) > 0)
                            {
                                //certificate is expired, CSSM_CERT_STATUS_EXPIRED
                                error = Locale.localizedString(
                                    "The certificate for this server has expired. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                                    "Keychain").Replace("%@", hostName);
                                return error;
                            }
                            if (DateTime.Compare(DateTime.Now, element.Certificate.NotBefore) > 0)
                            {
                                //certificate is not valid yet, CSSM_CERT_STATUS_NOT_VALID_YET
                                error = Locale.localizedString(
                                    "The certificate for this server is not yet valid. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                                    "Keychain").Replace("%@", hostName);
                                return error;
                            }
                        }
                        if ((status.Status & X509ChainStatusFlags.UntrustedRoot) == X509ChainStatusFlags.UntrustedRoot)
                        {
                            if (chain.ChainElements.Count == 1)
                            {
                                // untrusted self-signed, !CSSM_CERT_STATUS_IS_IN_ANCHORS && CSSM_CERT_STATUS_IS_ROOT
                                error = Locale.localizedString(
                                    "The certificate for this server was signed by an unknown certifying authority. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                                    "Keychain").Replace("%@", hostName);
                                return error;
                            }
                        }

                        //all other errors we map to !CSSM_CERT_STATUS_IS_IN_ANCHORS
                        Log.debug("Certificate error" + status.StatusInformation);
                        error = Locale.localizedString(
                            "The certificate for this server is invalid. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                            "Keychain").Replace("%@", hostName);
                        return error;
                    }
                }
            }
            return error;
        }

        public override bool displayCertificates(X509Certificate[] certificates)
        {
            //todo did not find a way to show the chain in the case of self signed certs
            X509Certificate2 cert = ConvertCertificate(certificates[0]);
            X509Certificate2UI.DisplayCertificate(cert);

            return true;
        }

        public static X509Certificate2 ConvertCertificate(X509Certificate certificate)
        {
            return new X509Certificate2(certificate.getEncoded());
        }

        public static void Register()
        {
            KeychainFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : KeychainFactory
        {
            protected override object create()
            {
                return new Keychain();
            }
        }
    }
}