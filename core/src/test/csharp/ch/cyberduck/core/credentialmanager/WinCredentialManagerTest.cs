using Ch.Cyberduck.Core.CredentialManager;
using java.util;
using NUnit.Framework;
using System.Net;
using System;
using System.Buffers;
using System.Security.Cryptography;
using Windows.Win32.Security.Credentials;
using System.IO;
using NUnit.Framework.Legacy;

namespace Ch.Cyberduck.Core.credentialmanager
{
    [TestFixture]
    public class WinCredentialManagerTest
    {
        public static string[] UserNames
        {
            get
            {
                return new string[]
                {
                    "user",
                    "user@short.tld",
                    "user@service.domain.tld",
                    "cdid1.credential.cd1..dywpmxvbnncbzmlappwofuzndjoqqonyenswravjbvuyckeihqcoirpumtxj",
                    "svimjbjpojbxgvhgqivytxodldswkvzkoqeqgwcjciwueewpsrvsnygloehmveuvakwvqtsddmioyvawgl",
                    "separated.user",
                    "dash-user",
                    "EsocwbpPZdIwo",
                    "ZSwkeCVQXDOtuCBu",
                    "ZSwkeCVQXDOtuCBupwUoozsI",
                    "llaZivuuLhpguktsjGfGKedsdxwntvBH"
                };
            }
        }

        [Test]
        public void TestGetInvalid()
        {
            var credentials = WinCredentialManager.GetCredentials(UUID.randomUUID().toString());
            Assert.That(credentials.UserName, Is.Null.Or.Empty);
            Assert.That(credentials.Password, Is.Null.Or.Empty);
        }

        [Test]
        public void TestSaveServiceEmbeddedUser([ValueSource(nameof(UserNames))] string user)
        {
            var target = $"protocol://{user}@service.{UUID.randomUUID().toString()}.tld";
            var credentials = new NetworkCredential(user, UUID.randomUUID().toString());
            Assert.That(WinCredentialManager.SaveCredentials(target, credentials));
            var query = WinCredentialManager.GetCredentials(target);
            Assert.That(query.UserName, Is.EqualTo(credentials.UserName));
            Assert.That(credentials.Password, Is.EqualTo(credentials.Password));
            Assert.That(WinCredentialManager.RemoveCredentials(target));
        }

        [Test]
        public void TestSaveServiceUser([ValueSource(nameof(UserNames))] string user)
        {
            var target = $"protocol://service.{UUID.randomUUID().toString()}.tld";
            var credentials = new NetworkCredential(user, UUID.randomUUID().toString());
            Assert.That(WinCredentialManager.SaveCredentials(target, credentials));
            var query = WinCredentialManager.GetCredentials(target);
            Assert.That(query.UserName, Is.EqualTo(credentials.UserName));
            Assert.That(credentials.Password, Is.EqualTo(credentials.Password));
            Assert.That(WinCredentialManager.RemoveCredentials(target));
        }

        [Test]
        public void TestSaveSimpleEmbeddedUser([ValueSource(nameof(UserNames))] string user)
        {
            var target = $"{user}@{UUID.randomUUID().toString()}";
            var credentials = new NetworkCredential(user, UUID.randomUUID().toString());
            Assert.That(WinCredentialManager.SaveCredentials(target, credentials));
            var query = WinCredentialManager.GetCredentials(target);
            Assert.That(query.UserName, Is.EqualTo(credentials.UserName));
            Assert.That(credentials.Password, Is.EqualTo(credentials.Password));
            Assert.That(WinCredentialManager.RemoveCredentials(target));
        }

        [Test]
        public void TestSaveSimpleUser([ValueSource(nameof(UserNames))] string user)
        {
            var target = UUID.randomUUID().toString();
            var credentials = new NetworkCredential(user, UUID.randomUUID().toString());
            Assert.That(WinCredentialManager.SaveCredentials(target, credentials));
            var query = WinCredentialManager.GetCredentials(target);
            Assert.That(query.UserName, Is.EqualTo(credentials.UserName));
            Assert.That(credentials.Password, Is.EqualTo(credentials.Password));
            Assert.That(WinCredentialManager.RemoveCredentials(target));
        }

        [Test]
        public void TestSaveHugeBlob()
        {
            var target = UUID.randomUUID().toString();
            string blob;
            using (MemoryStream memory = new MemoryStream())
            {
                byte[] buffer = ArrayPool<byte>.Shared.Rent(16);
                try
                {
                    System.Random random = new();
                    using (ToBase64Transform transform = new ToBase64Transform())
                    using (CryptoStream cryptoStream = new CryptoStream(memory, transform, CryptoStreamMode.Write, true))
                    {
                        const int targetLength = 512;

                        int remaining = targetLength;
                        while (remaining > 0)
                        {
                            random.NextBytes(buffer);
                            int write = Math.Min(remaining, buffer.Length);
                            cryptoStream.Write(buffer, 0, write);
                            remaining -= write;
                        }
                    }
                    memory.Seek(0, SeekOrigin.Begin);
                    using (StreamReader reader = new StreamReader(memory))
                    {
                        blob = reader.ReadToEnd();
                    }
                }
                finally
                {
                    ArrayPool<byte>.Shared.Return(buffer);
                }
            }

            WindowsCredentialManagerCredential cred = new WindowsCredentialManagerCredential(string.Empty, string.Empty, CRED_TYPE.CRED_TYPE_GENERIC, default, CRED_PERSIST.CRED_PERSIST_ENTERPRISE)
            {
                Attributes =
                {
                    ["Blob"] = blob
                }
            };
            var credTarget = "test:large-blob" + target;
            Assert.That(WinCredentialManager.SaveCredentials(credTarget, cred), Is.True);
            Assert.That(WinCredentialManager.GetCredentials(credTarget).Attributes["Blob"], Is.EqualTo(blob));
            WinCredentialManager.RemoveCredentials(credTarget);
        }
    }
}
