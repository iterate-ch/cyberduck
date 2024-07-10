using ch.cyberduck.core.exception;
using ch.cyberduck.core.local;
using ch.cyberduck.core.local.features;
using java.nio.file;
using NUnit.Framework;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    using Local = ch.cyberduck.core.Local;

    [TestFixture]
    public class TrashFeatureTest
    {
        private Local temp;
        private Touch touch;
        private Trash trash;

        [OneTimeSetUp]
        public void Setup()
        {
            temp = new Local(Path.GetTempPath());
            touch = new DefaultLocalTouchFeature();
            trash = new NativeLocalTrashFeature();
        }

        [Test]
        public void TestTrash()
        {
            Local trashee = new(temp, Path.GetRandomFileName());
            trashee.mkdir();

            trash.trash(trashee);
        }

        [Test]
        public void testTrashNonEmpty()
        {
            Local trashee = new(temp, Path.GetRandomFileName());
            trashee.mkdir();
            Local sub = new(trashee, Path.GetRandomFileName());
            sub.mkdir();
            Local file = new(trashee, Path.GetRandomFileName());
            touch.touch(file);

            trash.trash(trashee);
        }

        [Test]
        public void testTrashOpenFile()
        {
            Local trashee = new(temp, Path.GetRandomFileName());
            trashee.mkdir();
            Local sub = new(trashee, Path.GetRandomFileName());
            sub.mkdir();
            Local file = new(trashee, Path.GetRandomFileName());
            touch.touch(file);

            using (file.getOutputStream(false))
            {
                Assert.Throws<LocalAccessDeniedException>(() => trash.trash(trashee));
            }
        }

        [Ignore("Unknown."), Test]
        public void testTrashOpenDirectoryEnumeration()
        {
            Local trashee = new(temp, Path.GetRandomFileName());
            trashee.mkdir();
            Local sub = new(trashee, Path.GetRandomFileName());
            sub.mkdir();
            Local file = new(trashee, Path.GetRandomFileName());
            touch.touch(file);

            using (Files.newDirectoryStream(Paths.get(file.getAbsolute())))
            {
                Assert.Throws<LocalAccessDeniedException>(() => trash.trash(trashee));
            }
        }
    }
}
