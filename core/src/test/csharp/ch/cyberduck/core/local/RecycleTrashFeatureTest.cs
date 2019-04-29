using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.local;
using ch.cyberduck.core.local.features;
using ch.cyberduck.core.preferences;
using java.io;
using java.nio.file;
using java.util;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Ch.Cyberduck.Core.Local
{
    using Local = ch.cyberduck.core.Local;

    [TestFixture]
    public class RecycleTrashFeatureTest
    {
        private Trash trash;
        private SupportDirectoryFinder finder;

        [TestFixtureSetUp]
        public void Setup()
        {
            trash = new RecycleLocalTrashFeature();
            finder = new TemporarySupportDirectoryFinder();
        }

        [Test]
        public void TestTrash()
        {
            Local temp = finder.find();
            Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
            directory.mkdir();

            trash.trash(directory);
        }

        [Test]
        public void testTrashNonEmpty()
        {
            Local temp = finder.find();
            Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
            directory.mkdir();
            Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
            sub.mkdir();
            Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
            Touch touch = LocalTouchFactory.get();
            touch.touch(file);

            trash.trash(directory);
        }

        [Test, ExpectedException(typeof(LocalAccessDeniedException))]
        public void testTrashOpenFile()
        {
            Local temp = finder.find();
            Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
            directory.mkdir();
            Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
            sub.mkdir();
            Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
            Touch touch = LocalTouchFactory.get();
            touch.touch(file);

            using (OutputStream stream = file.getOutputStream(false))
            {
                trash.trash(directory);
            }
        }

        [Test, ExpectedException(typeof(LocalAccessDeniedException))]
        public void testTrashOpenDirectoryEnumeration()
        {
            Local temp = finder.find();
            Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
            directory.mkdir();
            Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
            sub.mkdir();
            Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
            Touch touch = LocalTouchFactory.get();
            touch.touch(file);

            using (DirectoryStream stream = Files.newDirectoryStream(Paths.get(sub.getAbsolute())))
            {
                trash.trash(directory);
            }
        }
    }
}
