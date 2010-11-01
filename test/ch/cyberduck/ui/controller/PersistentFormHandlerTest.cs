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
using System.Drawing;
using NUnit.Framework;

namespace Ch.Cyberduck.Ui.Controller
{
    [TestFixture]
    public class PersistentFormHandlerTest
    {
        [SetUp]
        public void Init()
        {
        }

        [Test]
        public void SetGetTest()
        {
            PersistentFormHandler handler = new PersistentFormHandler(GetType(), 0, Rectangle.Empty);
            handler.Set("key", 189);
            handler.Save();
            handler = new PersistentFormHandler(GetType(), 0, Rectangle.Empty);
            Assert.AreEqual(189, handler.Get<int>("key"));
        }

        [Test]
        public void SetGetTestDefaultValue()
        {
            PersistentFormHandler handler = new PersistentFormHandler(GetType(), 0, Rectangle.Empty);
            handler.Set("keyDefault", 111);
            handler.Save();
            handler = new PersistentFormHandler(GetType(), 0, Rectangle.Empty);
            Assert.AreEqual(500, handler.Get<int>("keyNotAvailable", 500));
        }
    }
}