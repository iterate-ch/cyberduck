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

using NUnit.Framework;

namespace Ch.Cyberduck.Core.Ssl
{
    [TestFixture]
    internal class HostnameVerifierTest
    {
        [SetUp]
        public void Init()
        {
        }

        [Test]
        public void WilcardCertificate()
        {
            Assert.That(HostnameVerifier.Match("foo.secure.example.com", "*.secure.example.com"));
        }
    }
}
