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
using java.util;
using NUnit.Framework;

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

        private class TestProtocol : AbstractProtocol
        {
            public override string getIdentifier() => "test";

            public override string getDescription() => "Test";

            public override Protocol.Type getType() => Protocol.Type.ftp;

            public override Scheme getScheme() => Scheme.ftp;

            public override string getPrefix() => "ch.cyberduck.core.Null";

            public override bool isEnabled() => true;
        }
    }
}
