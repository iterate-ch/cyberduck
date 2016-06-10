// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
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
using System.Security.Cryptography.X509Certificates;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Ssl;
using Ch.Cyberduck.Core.TaskDialog;
using java.io;
using java.security;
using java.security.cert;
using java.util;
using org.apache.log4j;
using X509Certificate = java.security.cert.X509Certificate;

namespace Ch.Cyberduck.Core
{
    public class SystemCertificateStore : CertificateStore
    {
        private static readonly Logger Log = Logger.getLogger(typeof (SystemCertificateStore).FullName);

        public bool isTrusted(String hostName, List certs)
        {
            X509Certificate2 serverCert = ConvertCertificate(certs.iterator().next() as X509Certificate);
            X509Chain chain = new X509Chain();
            chain.ChainPolicy.RevocationMode =
                PreferencesFactory.get().getBoolean("connection.ssl.x509.revocation.online")
                    ? X509RevocationMode.Online
                    : X509RevocationMode.Offline;
            chain.ChainPolicy.UrlRetrievalTimeout = new TimeSpan(0, 0, 0, 10); // set timeout to 10 seconds
            chain.ChainPolicy.VerificationFlags = X509VerificationFlags.NoFlag;

            for (int index = 1; index < certs.size(); index++)
            {
                chain.ChainPolicy.ExtraStore.Add(ConvertCertificate(certs.get(index) as X509Certificate));
            }
            chain.Build(serverCert);

            bool isException = CheckForException(hostName, serverCert);
            if (isException)
            {
                // Exceptions always have precendence
                return true;
            }

            string errorFromChainStatus = GetErrorFromChainStatus(chain, hostName);
            bool certError = null != errorFromChainStatus;
            bool hostnameMismatch = hostName != null &&
                                    !HostnameVerifier.CheckServerIdentity(certs.iterator().next() as X509Certificate,
                                        serverCert, hostName);

            // check if host name matches
            if (null == errorFromChainStatus && hostnameMismatch)
            {
                errorFromChainStatus =
                    LocaleFactory.localizedString(
                        "The certificate for this server is invalid. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                        "Keychain").Replace("%@", hostName);
            }

            if (null != errorFromChainStatus)
            {
                while (true)
                {
                    TaskDialogResult r =
                        TaskDialog.TaskDialog.Show(
                            title: LocaleFactory.localizedString("This certificate is not valid", "Keychain"),
                            mainInstruction: LocaleFactory.localizedString("This certificate is not valid", "Keychain"),
                            content: errorFromChainStatus,
                            commandLinks:
                                new string[]
                                {
                                    LocaleFactory.localizedString("Continue", "Credentials"),
                                    LocaleFactory.localizedString("Disconnect"),
                                    LocaleFactory.localizedString("Show Certificate", "Keychain")
                                },
                            mainIcon: TaskDialogIcon.Warning,
                            verificationText: LocaleFactory.localizedString("Always Trust", "Keychain"),
                            footerIcon: TaskDialogIcon.Information);
                    if (r.CommandButtonResult == 0)
                    {
                        if (r.VerificationChecked.Value)
                        {
                            if (certError)
                            {
                                //todo can we use the Trusted People and Third Party Certificate Authority Store? Currently X509Chain is the problem.
                                AddCertificate(serverCert, StoreName.Root);
                            }
                            PreferencesFactory.get()
                                .setProperty(hostName + ".certificate.accept", serverCert.SubjectName.Name);
                        }
                        return true;
                    }
                    if (r.CommandButtonResult == 1)
                    {
                        return false;
                    }
                    if (r.CommandButtonResult == 2)
                    {
                        X509Certificate2UI.DisplayCertificate(serverCert);
                    }
                }
            }
            return true;
        }

        public bool display(List certificates)
        {
            if (certificates.isEmpty())
            {
                return false;
            }
            X509Certificate2 cert = ConvertCertificate(certificates.iterator().next() as X509Certificate);
            X509Certificate2UI.DisplayCertificate(cert);
            return true;
        }

        public X509Certificate choose(string[] keyTypes, Principal[] issuers, string hostname, string prompt)
        {
            X509Store store = new X509Store(StoreName.My, StoreLocation.CurrentUser);
            try
            {
                store.Open(OpenFlags.ReadOnly);
                X509Certificate2Collection found = new X509Certificate2Collection();
                foreach (Principal issuer in issuers)
                {
                    // JBA 20141028, windows is expecting EMAILADDRESS in issuer name, but the rfc1779 emmits it as an OID, which makes it not match
                    // this is not the best way to fix the issue, but I can't find anyway to get an X500Principal to not emit EMAILADDRESS as an OID
                    string rfc1779 = issuer.toString()
                        .Replace("EMAILADDRESS=", "E=")
                        .Replace("ST=", "S=")
                        .Replace("SP=", "S=");
                    Log.debug("Query certificate store for issuer name " + rfc1779);

                    X509Certificate2Collection certificates =
                        store.Certificates.Find(X509FindType.FindByIssuerDistinguishedName, rfc1779, true);
                    found.AddRange(certificates);
                    foreach (X509Certificate2 certificate in certificates)
                    {
                        Log.debug("Found certificate with DN " + certificate.IssuerName.Name);
                    }
                }
                X509Certificate2Collection selected = X509Certificate2UI.SelectFromCollection(found,
                    LocaleFactory.localizedString("Choose"), prompt, X509SelectionFlag.SingleSelection);
                foreach (X509Certificate2 c in selected)
                {
                    return ConvertCertificate(c);
                }
                throw new ConnectionCanceledException();
            }
            finally
            {
                store.Close();
            }
        }

        public static X509Certificate2 ConvertCertificate(X509Certificate certificate)
        {
            return new X509Certificate2(certificate.getEncoded());
        }

        public static X509Certificate ConvertCertificate(X509Certificate2 certificate)
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificate.RawData));
        }

        private string GetErrorFromChainStatus(X509Chain chain, string hostName)
        {
            string error = null;
            foreach (X509ChainStatus status in chain.ChainStatus)
            {
                if ((status.Status & X509ChainStatusFlags.RevocationStatusUnknown) ==
                    X509ChainStatusFlags.RevocationStatusUnknown ||
                    ((status.Status & X509ChainStatusFlags.OfflineRevocation) == X509ChainStatusFlags.OfflineRevocation))
                {
                    //due to the offline revocation check
                    continue;
                }
                if ((status.Status & X509ChainStatusFlags.NotTimeValid) == X509ChainStatusFlags.NotTimeValid)
                {
                    //certificate is expired, CSSM_CERT_STATUS_EXPIRED
                    error =
                        LocaleFactory.localizedString(
                            "The certificate for this server has expired. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                            "Keychain").Replace("%@", hostName);
                    return error;
                }
                if (((status.Status & X509ChainStatusFlags.UntrustedRoot) == X509ChainStatusFlags.UntrustedRoot) ||
                    (status.Status & X509ChainStatusFlags.PartialChain) == X509ChainStatusFlags.PartialChain)
                {
                    // untrusted self-signed, !CSSM_CERT_STATUS_IS_IN_ANCHORS && CSSM_CERT_STATUS_IS_ROOT
                    error =
                        LocaleFactory.localizedString(
                            "The certificate for this server was signed by an unknown certifying authority. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                            "Keychain").Replace("%@", hostName);
                    return error;
                }

                //all other errors we map to !CSSM_CERT_STATUS_IS_IN_ANCHORS
                Log.debug("Certificate error" + status.StatusInformation);
                error =
                    LocaleFactory.localizedString(
                        "The certificate for this server is invalid. You might be connecting to a server that is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?",
                        "Keychain").Replace("%@", hostName);
            }
            return error;
        }

        private bool CheckForException(string hostname, X509Certificate2 cert)
        {
            string accCert = PreferencesFactory.get().getProperty(hostname + ".certificate.accept");
            if (Utils.IsNotBlank(accCert))
            {
                return accCert.Equals(cert.SubjectName.Name);
            }
            return false;
        }

        private void AddCertificate(X509Certificate2 cert, StoreName storeName)
        {
            Log.debug("Add certificate:" + cert.SubjectName.Name);
            X509Store store = new X509Store(storeName, StoreLocation.CurrentUser);
            try
            {
                store.Open(OpenFlags.ReadWrite);
                store.Add(cert);
            }
            finally
            {
                store.Close();
            }
        }
    }
}