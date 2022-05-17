using ch.cyberduck.core.exception;
using ch.cyberduck.core.local;
using NUnit.Framework;
using java.nio.file;

namespace Ch.Cyberduck.Core.Local
{
    using Local = ch.cyberduck.core.Local;
    using Path = System.IO.Path;
    using Touch = ch.cyberduck.core.local.features.Touch;
    using Trash = ch.cyberduck.core.local.features.Trash;

    [TestFixture]
    public class RecycleTrashFeatureTest
    {
        private Local temp;
        private Touch touch;
        private Trash trash;

        [OneTimeSetUp]
        public void Setup()
        {
            temp = new Local(Path.GetTempPath());
            touch = new DefaultLocalTouchFeature();
            trash = new RecycleLocalTrashFeature();
        }

        [Test]
        public void TestTrash()
        {
            Local trashee = new Local(temp, Path.GetRandomFileName());
            trashee.mkdir();

            trash.trash(trashee);
        }

        [Test]
        public void testTrashNonEmpty()
        {
            Local trashee = new Local(temp, Path.GetRandomFileName());
            trashee.mkdir();
            Local sub = new Local(trashee, Path.GetRandomFileName());
            sub.mkdir();
            Local file = new Local(trashee, Path.GetRandomFileName());
            touch.touch(file);

            trash.trash(trashee);
        }

        [Test]
        public void testTrashOpenFile()
        {
            Local trashee = new Local(temp, Path.GetRandomFileName());
            trashee.mkdir();
            Local sub = new Local(trashee, Path.GetRandomFileName());
            sub.mkdir();
            Local file = new Local(trashee, Path.GetRandomFileName());
            touch.touch(file);

            using (file.getOutputStream(false))
            {
                Assert.Throws<LocalAccessDeniedException>(() => trash.trash(trashee));
            }
        }

        [Ignore("Unknown."), Test]
        public void testTrashOpenDirectoryEnumeration()
        {
            Local trashee = new Local(temp, Path.GetRandomFileName());
            trashee.mkdir();
            Local sub = new Local(trashee, Path.GetRandomFileName());
            sub.mkdir();
            Local file = new Local(trashee, Path.GetRandomFileName());
            touch.touch(file);

            using (Files.newDirectoryStream(Paths.get(file.getAbsolute())))
            {
                Assert.Throws<LocalAccessDeniedException>(() => trash.trash(trashee));
            }
        }
    }
}
