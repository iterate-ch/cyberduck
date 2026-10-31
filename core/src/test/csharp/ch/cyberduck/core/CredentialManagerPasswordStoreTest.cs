//
// Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
// https://cyberduck.io/
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

using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.CredentialManager;
using java.util;
using NUnit.Framework;
using System;
using System.Net;

namespace Ch.Cyberduck.Core
{
    [TestFixture]
    public class CredentialManagerPasswordStoreTest
    {
        private CredentialManagerPasswordStore store;
        private Host bookmark;

        [SetUp]
        public void SetUp()
        {
            store = new CredentialManagerPasswordStore();
            bookmark = new Host(new TestProtocol(), $"{UUID.randomUUID().toString()}.example.net");
            bookmark.getCredentials().setUsername("user");
        }

        [TearDown]
        public void TearDown()
        {
            store.deleteLoginPassword(bookmark);
        }

        [Test]
        public void TestFindMissing()
        {
            Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
        }

        [Test]
        public void TestAddLoginPassword()
        {
            store.addLoginPassword(bookmark, "secret");
            Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("secret"));
        }

        [Test]
        public void TestAddLoginPasswordReplacesExisting()
        {
            store.addLoginPassword(bookmark, "secret");
            store.addLoginPassword(bookmark, "changed");
            Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("changed"));
        }

        [Test]
        public void TestAddLoginPasswordPerUser()
        {
            store.addLoginPassword(bookmark, "secret");
            bookmark.getCredentials().setUsername("other");
            Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
            store.addLoginPassword(bookmark, "other-secret");
            Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("other-secret"));
            store.deleteLoginPassword(bookmark);
            bookmark.getCredentials().setUsername("user");
            Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("secret"));
        }

        [Test]
        public void TestDeleteLoginPassword()
        {
            store.addLoginPassword(bookmark, "secret");
            store.deleteLoginPassword(bookmark);
            Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
        }

        [Test]
        public void TestDeleteLoginPasswordMissing()
        {
            Assert.DoesNotThrow(() => store.deleteLoginPassword(bookmark));
            Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
        }

        [Test]
        public void TestDeleteLoginPasswordRemovesDeprecatedEntry()
        {
            // Entry in deprecated format only found as fallback
            store.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(), bookmark.getHostname(), "user", "legacy");
            Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("legacy"));
            store.deleteLoginPassword(bookmark);
            Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
        }

        [Test]
        public void TestLoginPasswordPreferredOverDeprecatedEntry()
        {
            store.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(), bookmark.getHostname(), "user", "legacy");
            store.addLoginPassword(bookmark, "secret");
            Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("secret"));
        }

        [Test]
        public void TestSaveThenDeleteLoginPassword()
        {
            bookmark.getCredentials().setPassword("saved");
            store.save(bookmark);
            Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("saved"));
            store.deleteLoginPassword(bookmark);
            Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
        }

        [TestCase("", false)]
        [TestCase("jane", false)]
        [TestCase("", true)]
        [TestCase("jane", true)]
        public void TestBrickDoesNotFindUnboundLegacyApiKey(string username, bool legacyProfile)
        {
            var identifier = $"brick-test-{UUID.randomUUID()}";
            bookmark = new Host(legacyProfile ? new LegacyBrickProtocol(identifier) : new TestBrickProtocol(identifier),
                bookmark.getHostname(), new Credentials(username));
            var other = new Host(bookmark.getProtocol(), $"other-{UUID.randomUUID()}.example.net", new Credentials(username));
            var legacyTarget = LegacyTarget(bookmark);
            try
            {
                Assert.That(WinCredentialManager.SaveCredentials(legacyTarget, new NetworkCredential(username, "legacy-api-key")), Is.True);
                Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
                Assert.That(store.findLoginPassword(other), Is.Null.Or.Empty);

                store.addLoginPassword(bookmark, "current-api-key");
                Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("current-api-key"));
                Assert.That(store.findLoginPassword(other), Is.Null.Or.Empty);
                Assert.That(WinCredentialManager.GetCredentials(legacyTarget).Password, Is.Null.Or.Empty);
            }
            finally
            {
                WinCredentialManager.RemoveCredentials(legacyTarget);
                store.deleteLoginPassword(other);
            }
        }

        [TestCase("", false)]
        [TestCase("jane", false)]
        [TestCase("", true)]
        [TestCase("jane", true)]
        public void TestBrickSavedApiKeysAreScopedToHostname(string username, bool legacyProfile)
        {
            var identifier = $"brick-test-{UUID.randomUUID()}";
            bookmark = new Host(legacyProfile ? new LegacyBrickProtocol(identifier) : new TestBrickProtocol(identifier),
                bookmark.getHostname(), new Credentials(username, "current-api-key"));
            var other = new Host(bookmark.getProtocol(), $"other-{UUID.randomUUID()}.example.net", new Credentials(username));
            try
            {
                store.save(bookmark);
                Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("current-api-key"));
                Assert.That(store.findLoginPassword(other), Is.Null.Or.Empty);
                store.addLoginPassword(other, "other-api-key");
                Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("current-api-key"));
                Assert.That(store.findLoginPassword(other), Is.EqualTo("other-api-key"));
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(bookmark)).Password, Is.Null.Or.Empty);
            }
            finally
            {
                store.deleteLoginPassword(other);
                WinCredentialManager.RemoveCredentials(LegacyTarget(bookmark));
            }
        }

        [TestCase(false)]
        [TestCase(true)]
        public void TestBrickSavedApiKeysAreScopedToPort(bool legacyProfile)
        {
            var identifier = $"brick-test-{UUID.randomUUID()}";
            bookmark = new Host(legacyProfile ? new LegacyBrickProtocol(identifier) : new TestBrickProtocol(identifier),
                bookmark.getHostname(), new Credentials("jane", "current-api-key"));
            var other = new Host(bookmark.getProtocol(), bookmark.getHostname(), 8443, new Credentials("jane"));
            try
            {
                store.save(bookmark);
                Assert.That(store.findLoginPassword(other), Is.Null.Or.Empty);
                store.addLoginPassword(other, "other-api-key");
                Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("current-api-key"));
                Assert.That(store.findLoginPassword(other), Is.EqualTo("other-api-key"));
                store.delete(other);
                Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("current-api-key"));
                Assert.That(store.findLoginPassword(other), Is.Null.Or.Empty);
            }
            finally
            {
                store.deleteLoginPassword(other);
                WinCredentialManager.RemoveCredentials(LegacyTarget(bookmark));
            }
        }

        [TestCase("", false, false)]
        [TestCase("jane", false, false)]
        [TestCase("", true, false)]
        [TestCase("jane", true, false)]
        [TestCase("", false, true)]
        [TestCase("jane", false, true)]
        [TestCase("", true, true)]
        [TestCase("jane", true, true)]
        public void TestDeleteBrickCredentialsCleansLegacyEntriesAndPreservesOtherSitesAndUsers(string username, bool deleteCredentials, bool legacyProfile)
        {
            var identifier = $"brick-test-{UUID.randomUUID()}";
            bookmark = new Host(legacyProfile ? new LegacyBrickProtocol(identifier) : new TestBrickProtocol(identifier),
                bookmark.getHostname(), new Credentials(username));
            var other = new Host(bookmark.getProtocol(), $"other-{UUID.randomUUID()}.example.net", new Credentials(username));
            try
            {
                store.addLoginPassword(bookmark, "current-api-key");
                store.addLoginPassword(other, "other-api-key");
                AddLegacyEntries(bookmark);
                if (deleteCredentials)
                {
                    store.delete(bookmark);
                }
                else
                {
                    store.deleteLoginPassword(bookmark);
                }
                Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
                Assert.That(store.findLoginPassword(other), Is.EqualTo("other-api-key"));
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(bookmark)).Password, Is.Null.Or.Empty);
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(bookmark, "")).Password, Is.Null.Or.Empty);
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(bookmark, "bob")).Password, Is.EqualTo("legacy-api-key"));
            }
            finally
            {
                RemoveLegacyEntries(bookmark);
                store.deleteLoginPassword(other);
            }
        }

        [TestCase(false)]
        [TestCase(true)]
        public void TestBrickSaveCleansNamedAndNoUsernameLegacyEntries(bool addLoginPassword)
        {
            bookmark = new Host(new TestBrickProtocol($"brick-test-{UUID.randomUUID()}"),
                bookmark.getHostname(), new Credentials("jane", "new-api-key"));
            var other = new Host(bookmark.getProtocol(), $"other-{UUID.randomUUID()}.example.net", new Credentials("jane"));
            try
            {
                store.addLoginPassword(other, "other-api-key");
                AddLegacyEntries(bookmark);
                if (addLoginPassword)
                {
                    store.addLoginPassword(bookmark, "new-api-key");
                }
                else
                {
                    store.save(bookmark);
                }
                Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("new-api-key"));
                Assert.That(store.findLoginPassword(other), Is.EqualTo("other-api-key"));
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(bookmark)).Password, Is.Null.Or.Empty);
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(bookmark, "")).Password, Is.Null.Or.Empty);
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(bookmark, "bob")).Password, Is.EqualTo("legacy-api-key"));
            }
            finally
            {
                RemoveLegacyEntries(bookmark);
                store.deleteLoginPassword(other);
            }
        }

        [TestCase(null, false)]
        [TestCase(null, true)]
        [TestCase("", false)]
        [TestCase("", true)]
        [TestCase(" \t", false)]
        [TestCase(" \t", true)]
        public void TestBrickEmptySaveLeavesLegacyEntries(string password, bool addLoginPassword)
        {
            bookmark = new Host(new TestBrickProtocol($"brick-test-{UUID.randomUUID()}"),
                bookmark.getHostname(), new Credentials("jane", password));
            try
            {
                AddLegacyEntries(bookmark);
                if (addLoginPassword)
                {
                    store.addLoginPassword(bookmark, password);
                }
                else
                {
                    store.save(bookmark);
                }
                AssertLegacyEntriesUnchanged(bookmark);
            }
            finally
            {
                RemoveLegacyEntries(bookmark);
            }
        }

        [TestCase(false)]
        [TestCase(true)]
        public void TestBrickFailedSaveLeavesLegacyEntries(bool addLoginPassword)
        {
            // Exceed the Windows credential blob limit to exercise a real native write failure.
            var password = new string('x', 6000);
            bookmark = new Host(new TestBrickProtocol($"brick-test-{UUID.randomUUID()}"),
                bookmark.getHostname(), new Credentials("jane", password));
            try
            {
                AddLegacyEntries(bookmark);
                if (addLoginPassword)
                {
                    store.addLoginPassword(bookmark, password);
                }
                else
                {
                    Assert.Throws<LocalAccessDeniedException>(() => store.save(bookmark));
                }
                Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
                AssertLegacyEntriesUnchanged(bookmark);
            }
            finally
            {
                RemoveLegacyEntries(bookmark);
            }
        }

        [Test]
        public void TestBrickSavingDisabledLeavesLegacyEntries()
        {
            bookmark = new Host(new TestBrickProtocol($"brick-test-{UUID.randomUUID()}"),
                bookmark.getHostname(), new Credentials("jane", "new-api-key").setSaved(false));
            try
            {
                AddLegacyEntries(bookmark);
                new KeychainLoginService(store).save(bookmark);
                Assert.That(store.findLoginPassword(bookmark), Is.Null.Or.Empty);
                AssertLegacyEntriesUnchanged(bookmark);
            }
            finally
            {
                RemoveLegacyEntries(bookmark);
            }
        }

        [TestCase(false)]
        [TestCase(true)]
        public void TestOtherProtocolsDoNotCleanLegacyEntries(bool addLoginPassword)
        {
            bookmark = new Host(new TestProtocol($"other-test-{UUID.randomUUID()}"),
                bookmark.getHostname(), new Credentials("jane", "new-password"));
            try
            {
                AddLegacyEntries(bookmark);
                if (addLoginPassword)
                {
                    store.addLoginPassword(bookmark, "new-password");
                }
                else
                {
                    store.save(bookmark);
                }
                Assert.That(store.findLoginPassword(bookmark), Is.EqualTo("new-password"));
                AssertLegacyEntriesUnchanged(bookmark);
            }
            finally
            {
                RemoveLegacyEntries(bookmark);
            }
        }

        [Test]
        public void TestOtherProtocolsRetainUnscopedCredentials()
        {
            bookmark = new Host(new LegacyTestProtocol($"other-test-{UUID.randomUUID()}"),
                bookmark.getHostname(), new Credentials("jane", "saved-api-key"));
            store.save(bookmark);
            var other = new Host(bookmark.getProtocol(), $"other-{UUID.randomUUID()}.example.net", new Credentials("jane"));
            Assert.That(store.findLoginPassword(other), Is.EqualTo("saved-api-key"));
        }

        private static void AddLegacyEntries(Host host)
        {
            foreach (var username in new[] { host.getCredentials().getUsername(), "", "bob" })
            {
                Assert.That(WinCredentialManager.SaveCredentials(LegacyTarget(host, username),
                    new NetworkCredential(username, "legacy-api-key")), Is.True);
            }
        }

        private static void AssertLegacyEntriesUnchanged(Host host)
        {
            foreach (var username in new[] { host.getCredentials().getUsername(), "", "bob" })
            {
                Assert.That(WinCredentialManager.GetCredentials(LegacyTarget(host, username)).Password, Is.EqualTo("legacy-api-key"));
            }
        }

        private static void RemoveLegacyEntries(Host host)
        {
            foreach (var username in new[] { host.getCredentials().getUsername(), "", "bob" })
            {
                WinCredentialManager.RemoveCredentials(LegacyTarget(host, username));
            }
        }

        private static string LegacyTarget(Host host, string username = null)
        {
            var target = new UriBuilder(PreferencesFactory.get().getProperty("application.container.name"), string.Empty)
            {
                Path = host.getProtocol().getIdentifier()
            };
            username ??= host.getCredentials().getUsername();
            if (!string.IsNullOrWhiteSpace(username))
            {
                target.Query = "user=" + username;
            }
            return target.Uri.AbsoluteUri;
        }

        private class TestProtocol : AbstractProtocol
        {
            private readonly string identifier;

            public TestProtocol(string identifier = "test")
            {
                this.identifier = identifier;
            }

            public override string getIdentifier() => identifier;

            public override string getDescription() => "Test";

            public override Protocol.Type getType() => Protocol.Type.ftp;

            public override Scheme getScheme() => Scheme.ftp;

            public override string getPrefix() => "ch.cyberduck.core.Null";

            public override bool isEnabled() => true;
        }

        private class TestBrickProtocol : TestProtocol
        {
            public TestBrickProtocol(string identifier) : base(identifier)
            {
            }

            public override Protocol.Type getType() => Protocol.Type.brick;

            public override Scheme getScheme() => Scheme.https;

            public override bool isPortConfigurable() => false;
        }

        private class LegacyTestProtocol : TestProtocol
        {
            public LegacyTestProtocol(string identifier) : base(identifier)
            {
            }

            public override bool isHostnameConfigurable() => false;

            public override bool isTokenConfigurable() => true;
        }

        private class LegacyBrickProtocol : TestBrickProtocol
        {
            public LegacyBrickProtocol(string identifier) : base(identifier)
            {
            }

            public override bool isHostnameConfigurable() => false;

            public override bool isTokenConfigurable() => true;
        }
    }
}
