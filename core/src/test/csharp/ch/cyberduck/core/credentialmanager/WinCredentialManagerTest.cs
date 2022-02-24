using Ch.Cyberduck.Core.CredentialManager;
using java.util;
using NUnit.Framework;
using System.Net;

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
            var saveResult = WinCredentialManager.SaveCredentials(target, credentials);
            Assert.True(saveResult);
            var query = WinCredentialManager.GetCredentials(target);
            Assert.AreEqual(credentials.UserName, query.UserName);
            Assert.AreEqual(credentials.Password, query.Password);
            var removeResult = WinCredentialManager.RemoveCredentials(target);
            Assert.True(removeResult);
        }

        [Test]
        public void TestSaveServiceUser([ValueSource(nameof(UserNames))] string user)
        {
            var target = $"protocol://service.{UUID.randomUUID().toString()}.tld";
            var credentials = new NetworkCredential(user, UUID.randomUUID().toString());
            var saveResult = WinCredentialManager.SaveCredentials(target, credentials);
            Assert.True(saveResult);
            var query = WinCredentialManager.GetCredentials(target);
            Assert.AreEqual(credentials.UserName, query.UserName);
            Assert.AreEqual(credentials.Password, query.Password);
            var removeResult = WinCredentialManager.RemoveCredentials(target);
            Assert.True(removeResult);
        }

        [Test]
        public void TestSaveSimpleEmbeddedUser([ValueSource(nameof(UserNames))] string user)
        {
            var target = $"{user}@{UUID.randomUUID().toString()}";
            var credentials = new NetworkCredential(user, UUID.randomUUID().toString());
            var saveResult = WinCredentialManager.SaveCredentials(target, credentials);
            Assert.True(saveResult);
            var query = WinCredentialManager.GetCredentials(target);
            Assert.AreEqual(credentials.UserName, query.UserName);
            Assert.AreEqual(credentials.Password, query.Password);
            var removeResult = WinCredentialManager.RemoveCredentials(target);
            Assert.True(removeResult);
        }

        [Test]
        public void TestSaveSimpleUser([ValueSource(nameof(UserNames))] string user)
        {
            var target = UUID.randomUUID().toString();
            var credentials = new NetworkCredential(user, UUID.randomUUID().toString());
            var saveResult = WinCredentialManager.SaveCredentials(target, credentials);
            Assert.True(saveResult);
            var query = WinCredentialManager.GetCredentials(target);
            Assert.AreEqual(credentials.UserName, query.UserName);
            Assert.AreEqual(credentials.Password, query.Password);
            var removeResult = WinCredentialManager.RemoveCredentials(target);
            Assert.True(removeResult);
        }
    }
}
