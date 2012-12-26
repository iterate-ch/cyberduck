// 
// Copyright (c) 2010-2012 Yves Langisch. All rights reserved.
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
using System.Text;
using Ch.Cyberduck.Core.Local;
using NUnit.Framework;
using ch.cyberduck.core;
using java.io;
using java.security.cert;

namespace Ch.Cyberduck.Ui.Controller
{
    [TestFixture]
    public class KeychainTest
    {
        [SetUp]
        public void Init()
        {
            UserPreferences.Register();
            LocaleImpl.Register();
            LocalImpl.Register();
            Keychain.Register();
        }

        private const string ExpiredSelfSigned =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIICnzCCAggCCQD/H0dWJzmTsjANBgkqhkiG9w0BAQUFADCBkzELMAkGA1UEBhMC\n" +
            "Q0gxDTALBgNVBAgMBEJlcm4xDTALBgNVBAcMBEJlcm4xFTATBgNVBAoMDGl0ZXJh\n" +
            "dGUgR21iSDELMAkGA1UECwwCSVQxIDAeBgNVBAMMF3Rlc3Quc2VjdXJlLmV4YW1w\n" +
            "bGUuY29tMSAwHgYJKoZIhvcNAQkBFhF5dmVzQGN5YmVyZHVjay5jaDAeFw0xMjEy\n" +
            "MTkwNjU3MTZaFw0xMjEyMjAwNjU3MTZaMIGTMQswCQYDVQQGEwJDSDENMAsGA1UE\n" +
            "CAwEQmVybjENMAsGA1UEBwwEQmVybjEVMBMGA1UECgwMaXRlcmF0ZSBHbWJIMQsw\n" +
            "CQYDVQQLDAJJVDEgMB4GA1UEAwwXdGVzdC5zZWN1cmUuZXhhbXBsZS5jb20xIDAe\n" +
            "BgkqhkiG9w0BCQEWEXl2ZXNAY3liZXJkdWNrLmNoMIGfMA0GCSqGSIb3DQEBAQUA\n" +
            "A4GNADCBiQKBgQCnHajku6ggmIzMHylTBrGRLZrlGR76wHOHvdGxHlZusAeh0wBV\n" +
            "k26qtRiU4cajTy4o5QavIsqtEkm0FZ1GXHdCEqOSE3ms4d22QEDfypE+6gorNp1G\n" +
            "XNnZ8ecYus68J7LyEtLvu9QADCsIXhN+485gWcjVcGtkCgSE35yNa/LlMwIDAQAB\n" +
            "MA0GCSqGSIb3DQEBBQUAA4GBAHU/rdAcd8PyouChNOMo/7hB0hdKVPbXFC6iQKxF\n" +
            "RgDGXYev9EDCSVC6ywhOU9t4Sr6bY8qvBoxc2CEjmAKcWZ+mjCTqtvpVLV7GDUlE\n" +
            "3ut1tSLWoFdgttiWv3pIAgdRxg+SSwea1S0rWF3vba3kB3IrkEYhZRYneqY8hczD\n" +
            "7T5f\n" +
            "-----END CERTIFICATE-----\n";

        [Test]
        public void ExpiredSelfSignedCertificate()
        {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(Encoding.ASCII.GetBytes(ExpiredSelfSigned));
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);
            const string hostName = "foo.secure.example.com";
            try
            {
                //no exception registered yet
                KeychainFactory.get().isTrusted(hostName, new[] {cert});
                Assert.Fail();
            }
            catch (EntryPointNotFoundException exception)
            {
            }
            //register exception
            Preferences.instance()
                       .setProperty(hostName + ".certificate.accept", Keychain.ConvertCertificate(cert).SubjectName.Name);
            Assert.IsTrue(KeychainFactory.get().isTrusted(hostName, new[] {cert}));
        }
    }
}