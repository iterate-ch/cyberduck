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
using NUnit.Framework;

namespace Ch.Cyberduck.Ui.Controller
{
    [TestFixture]
    public class LocalImplTest
    {
        [SetUp]
        public void Init()
        {
            UserPreferences.Register();
            LocaleImpl.Register();
            LocalImpl.Register();
        }

        [Test]
        public void AbsolutePaths()
        {
            LocalImpl l = new LocalImpl(@"G:\");
            Assert.AreEqual(@"G:\", l.getAbsolute());
            Assert.AreEqual(string.Empty, l.getName());

            l = new LocalImpl(@"C:\path\relative");
            Assert.AreEqual(@"relative", l.getName());
            Assert.AreEqual(@"C:\path\relative", l.getAbsolute());

            l = new LocalImpl(@"C:\path", "cyberduck.log");
            Assert.AreEqual(@"cyberduck.log", l.getName());
            Assert.AreEqual(@"C:\path\cyberduck.log", l.getAbsolute());

            l = new LocalImpl(@"C:\path", "Sessions");
            Assert.AreEqual(@"Sessions", l.getName());
            Assert.AreEqual(@"C:\path\Sessions", l.getAbsolute());
        }
    }
}