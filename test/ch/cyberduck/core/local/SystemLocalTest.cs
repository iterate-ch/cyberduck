// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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

using Ch.Cyberduck.Core.I18n;
using Ch.Cyberduck.Ui.Controller;
using NUnit.Framework;

namespace Ch.Cyberduck.Core.Local
{
    [TestFixture]
    public class SystemLocalTest
    {
        [SetUp]
        public void Init()
        {
            UserPreferences.Register();
            DictionaryLocale.Register();
            SystemLocal.Register();
        }

        [Test]
        public void AbsolutePaths()
        {
            SystemLocal l = new SystemLocal(@"G:\");
            Assert.AreEqual(@"G:\", l.getAbsolute());
            Assert.AreEqual(string.Empty, l.getName());

            l = new SystemLocal(@"C:\path\relative");
            Assert.AreEqual(@"relative", l.getName());
            Assert.AreEqual(@"C:\path\relative", l.getAbsolute());

            l = new SystemLocal(@"C:\path", "cyberduck.log");
            Assert.AreEqual(@"cyberduck.log", l.getName());
            Assert.AreEqual(@"C:\path\cyberduck.log", l.getAbsolute());

            l = new SystemLocal(@"C:\path", "Sessions");
            Assert.AreEqual(@"Sessions", l.getName());
            Assert.AreEqual(@"C:\path\Sessions", l.getAbsolute());
        }
    }
}