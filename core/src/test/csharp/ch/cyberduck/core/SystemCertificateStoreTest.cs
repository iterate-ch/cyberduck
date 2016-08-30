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
using System.Text;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core;
using java.io;
using java.security.cert;
using java.util;
using NUnit.Framework;
using Console = System.Console;
using List = java.util.List;

namespace Ch.Cyberduck.Ui.Controller
{
    [TestFixture]
    public class SystemCertificateStoreTest
    {
        private const string ExpiredSelfSigned =
            "-----BEGIN CERTIFICATE-----\n" + "MIICnzCCAggCCQD/H0dWJzmTsjANBgkqhkiG9w0BAQUFADCBkzELMAkGA1UEBhMC\n" +
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
            "3ut1tSLWoFdgttiWv3pIAgdRxg+SSwea1S0rWF3vba3kB3IrkEYhZRYneqY8hczD\n" + "7T5f\n" +
            "-----END CERTIFICATE-----\n";

        [Test]
        public void ExpiredSelfSignedCertificate()
        {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(Encoding.ASCII.GetBytes(ExpiredSelfSigned));
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(inputStream);
            const string hostName = "foo.secure.example.com";
            List certs = new ArrayList();
            certs.add(cert);
            try
            {
                //no exception registered yet
                new SystemCertificateStore().isTrusted(hostName, certs);
                Assert.Fail();
            }
            catch (EntryPointNotFoundException exception)
            {
            }
            //register exception
            PreferencesFactory.get()
                .setProperty(hostName + ".certificate.accept",
                    SystemCertificateStore.ConvertCertificate(cert).SubjectName.Name);
            Assert.IsTrue(new SystemCertificateStore().isTrusted(hostName, certs));
        }

        [Test]
        public void UntrustedSelfSignedCertificate()
        {
            String host = "-----BEGIN CERTIFICATE-----\n" +
                          "MIIEcDCCAligAwIBAgIEAP+quzANBgkqhkiG9w0BAQUFADA2MQswCQYDVQQGEwJE\n" +
                          "RTELMAkGA1UECAwCQlcxDTALBgNVBAoMBFNZU1MxCzAJBgNVBAMMAkNBMB4XDTE0\n" +
                          "MDQxMDA3Mzg0MFoXDTE1MDQxMDA3Mzg0MFowQTELMAkGA1UEBhMCREUxCzAJBgNV\n" +
                          "BAgMAkJXMQ0wCwYDVQQKDARTWVNTMRYwFAYDVQQDDA13d3cuZ29vZ2xlLmNoMIIB\n" +
                          "IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8P4nPfLKOXtl+uOOUF7YO+mH\n" +
                          "ee9hYHuDLusgyDpLEQAqnBHtBi6RCP5XmArpunNfTF8yls5QdyjKogJ6nXafzlGa\n" +
                          "If1fe6iI/OMp9oUBdqJh2mU9OfZm5he9sLobunrVWqlm5fIbSRZkhZe5o8Dutcsa\n" +
                          "p74TClaHbXTcsVbw6/aScXibj5ARIK71JgtPFUNp1QanF78GmXUu2MOROaz2duUF\n" +
                          "LxzJJCxnNElNkt663LUjtgfbcEgKQDZ0k0uNchAyDHDIkNr6FmilgBmt1LI0sjdH\n" +
                          "llY6Z/r8waH9ztTqlf78jG1AhmUSTbBNtYU92rqdRqPa21WBbhaEhNtFg8EZywID\n" +
                          "AQABo3sweTAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVy\n" +
                          "YXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU48hgd+eZiIeWwiDJ3XPIb6PI+0gw\n" +
                          "HwYDVR0jBBgwFoAUWc4WPc0maSLX2fTyx9YLvqqPs8YwDQYJKoZIhvcNAQEFBQAD\n" +
                          "ggIBAA8T4LM8FKvRQh+/UWmKkMFFMVcSav2UYYHwbDQ1G+38lKT9G1pddslA450H\n" +
                          "XkaOZnRIb31+bgIK/WRt16sowt/wxK34p0kphKS7ubszF6SQI/rUF7Z/44MBnyng\n" +
                          "CsJhO+hu9vk7qTBSx7wL5MI9lsmalzwmQhEnNBKyYxxwBKRpZzlns6bX/nysfIPd\n" +
                          "lJ28Q01VfuFI4LZn6XoCgP3a5KMbvkFCcoQ0BWaJZUJXj8Taunn52+4yd02CojXJ\n" +
                          "6jnM8asmeZLME7k4BZrYGzmEZWqEWlArltpo315/lEdgPvmmIsSMn3N3wi8IJnlj\n" +
                          "1LRzDy3HF2m8Kk/3HPYorUKItEFdZ2yBHJd8SvrsoahoyqLN3rPkQ6TXFg5GD6XF\n" +
                          "76GUMSbr92sjKtWI4fta5CZi2iRnQEhiuAW9mvmsr9/g9fHAvNbs8QjLfYxdG8KW\n" +
                          "oLQIuTllXuQRiWJ6X33ea4seNDpD53I7rhZNhDxkns7YdEy9IsJjHJganVBY+/3f\n" +
                          "15bn34p3g3mQlsnLA2WMX2ZyLrVWaEt82iIZKAFzHjO38fANno6IXh0HP1xy6uQd\n" +
                          "37SZV2h0nUlJYw483RIUcJghkEBkKnIJInb6wGKXSpEZE2ObDJV0cH9vJflygh+G\n" +
                          "P6IvpzJ9dGNO8yNuyxvxcG7C+yDjgWjXkHqBYDS8lY9rM0yk\n" + "-----END CERTIFICATE-----\n";

            String ca = "-----BEGIN CERTIFICATE-----\n" +
                        "MIIFPzCCAyegAwIBAgIJAODkvo4frTJPMA0GCSqGSIb3DQEBBQUAMDYxCzAJBgNV\n" +
                        "BAYTAkRFMQswCQYDVQQIDAJCVzENMAsGA1UECgwEU1lTUzELMAkGA1UEAwwCQ0Ew\n" +
                        "HhcNMTQwNDEwMDczODI2WhcNMjQwNDA5MDczODI2WjA2MQswCQYDVQQGEwJERTEL\n" +
                        "MAkGA1UECAwCQlcxDTALBgNVBAoMBFNZU1MxCzAJBgNVBAMMAkNBMIICIjANBgkq\n" +
                        "hkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAxYEqG0gr2sxp1KJtj8VKmv802cdfXvIv\n" +
                        "3M10eB4weWtbRim/g8NcZUzPac5R672JxdQTpGERVl4y+6fxjjc9NsxuXBBoMOam\n" +
                        "ihINUsZzhxQatQ0Vos9WHX9rVdVQXLnVDrfn//thx8pLCzN777q8xDYwuyNeiUzG\n" +
                        "KVtlQV7ltbMTCL0jLRWiKe/pX27qDx0j+VipgohVZc6UHTFgbsWIn/x9tcCYaGPU\n" +
                        "IORUDYKLxURgUxikGipGxEaLa4zyOoniH9LqAVKZ0mXGV2OKYQqppb60zPm/N5Ke\n" +
                        "9R2fjBYdY8QAkkRDe6ZfEy9/+8dJTheVIxfQ2IAh4CKmgRCutlhD99n9xtUzXfJU\n" +
                        "X8E7+t2SKgdkC3uQvLWdABO5cLqw+JC8kn1alz9mQDuhjq/63iJ9kFa3mv6/GCPd\n" +
                        "oyQ5Ey+vqKwyWP/2Y42HF5Kj8eCiLV71Jf7Njifnnn6N7DgjHeJJPfLCBxmxFuiO\n" +
                        "aB4P9fLvoUpLkCIE3bYVCXiHKc6NrZ7Bx/5QVA8471KDH9YngFF7DpbnqwYVdRys\n" +
                        "kqMEupb2kUf0W0ObjLHAAUFMcPUdY081QFrVi9m78B2wAbW3ynguzrFBK4lmj6L3\n" +
                        "5rQipe5lEVFUbDnMYLONz55kLROGyw8H4EhgRuGCPwpiJtWYq3Tyc7smEy8Y9JcS\n" +
                        "iuskbUOgTgECAwEAAaNQME4wHQYDVR0OBBYEFFnOFj3NJmki19n08sfWC76qj7PG\n" +
                        "MB8GA1UdIwQYMBaAFFnOFj3NJmki19n08sfWC76qj7PGMAwGA1UdEwQFMAMBAf8w\n" +
                        "DQYJKoZIhvcNAQEFBQADggIBAC+mnC3rSJnDkKhMuL88DG0tErvHEUwL+qrgoeNN\n" +
                        "1rOdXxv2hsVFfRMG9ieVvyMd9aHVvSgbWf43o0NHCX347eFWRq7n6A9QFNvcx4lD\n" +
                        "DFgt8eptIgRAChbRg4QV0+GdsKeBSlOL/Y03zXwxvrCEpKBrluBYz8NZ4LTDtO6Q\n" +
                        "g//+q0lnd49Gk1+PENzKKLVk2OzyQFh70o7pkm16KlpmQLnhvtkQJQPsOBVizk7v\n" +
                        "hKtdUnn/fJytniJ5F4dZykH5GV4owILTRpjiuqO0BEOAznjvFDiMnzKif/jxX6XR\n" +
                        "PMyqlWTk9VS23i1ghcU223+oeDiSNj8lbla8lHFcI8ztsvY469206pfCrK51FanL\n" +
                        "dB/G7zd9P3zQRinwfaG9/9eL4nDsvPMVqotFiyrHhJJOAeZNQtRDB+e8c+334coS\n" +
                        "Y6GQpdmCPQeL1grxH5g5VypkITPgq+aPmgHv6jk5cHdFhHy25tOwpdrk1ppN3ln4\n" +
                        "GG2QjTbVnOEH01+ySZpB4eMaqF2wME0LQYuZo4OYz5Dfu565ft3E81TWqsaOxXyQ\n" +
                        "kLHLnzzlATLh7F30aUV254MJTLwf4TL2/p4DJklo9t47iS1ckYAwtFYwgbDIziGZ\n" +
                        "hJYa6ulLyko8z7MPf8OSOipYKOW/gXfV1XxMYh+k5qwaKLK4BsoXuwiB/kMVJtTJ\n" + "ndIN\n" +
                        "-----END CERTIFICATE-----\n";


            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream his = new ByteArrayInputStream(Encoding.ASCII.GetBytes(host));
            X509Certificate hostCert = (X509Certificate) certFactory.generateCertificate(his);
            InputStream cais = new ByteArrayInputStream(Encoding.ASCII.GetBytes(ca));
            X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(cais);


            const string hostName = "www.google.ch";
            List certs = new ArrayList();
            certs.add(hostCert);
            certs.add(caCert);

            try
            {
                //no exception registered yet
                new SystemCertificateStore().isTrusted(hostName, certs);
                Assert.Fail();
            }
            catch (EntryPointNotFoundException exception)
            {
                Console.WriteLine("TEST");
            }
            //register exception
            PreferencesFactory.get()
                .setProperty(hostName + ".certificate.accept",
                    SystemCertificateStore.ConvertCertificate(hostCert).SubjectName.Name);
            Assert.IsTrue(new SystemCertificateStore().isTrusted(hostName, certs));
        }
    }
}