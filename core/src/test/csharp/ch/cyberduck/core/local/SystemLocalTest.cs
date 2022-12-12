using java.nio.file;
using NUnit.Framework;
using CoreLocal = ch.cyberduck.core.Local;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    [TestFixture]
    public class SystemLocalTest
    {
        const string PIPE_NAME = @"\\.\pipe\openssh-ssh-agent";
        const string WSL_PATH = @"\\wsl$\test\";

        [Test]
        public void TestDirectoryAltSeparators()
        {
            var path = new SystemLocal(@"C:" + Path.AltDirectorySeparatorChar);
            Assert.AreEqual(@"C:" + Path.DirectorySeparatorChar, path.getAbsolute());
        }

        [Test]
        public void TestDirectorySeparators()
        {
            var path = new SystemLocal(@"C:" + Path.DirectorySeparatorChar);
            Assert.AreEqual(@"C:" + Path.DirectorySeparatorChar, path.getAbsolute());
        }

        [Test]
        public void TestConvertToDirectorySeparator()
        {
            var path = new SystemLocal(PIPE_NAME.Replace('\\', '/'));
            Assert.AreEqual(PIPE_NAME, path.getAbsolute());
        }

        [Test]
        public void TestUnicode()
        {
            var test = @"\\?\C:\ÄÖÜßßäöü";
            var path = new SystemLocal(test);
            Assert.AreEqual(test, path.getAbsolute());
        }

        [Test]
        public void TestFileFormats()
        {
            string[] filenames =
            {
                @"c:\temp\test-file.txt",
                @"\\127.0.0.1\c$\temp\test-file.txt",
                @"\\LOCALHOST\c$\temp\test-file.txt",
                @"\\.\c:\temp\test-file.txt",
                @"\\?\c:\temp\test-file.txt",
                @"\\.\UNC\LOCALHOST\c$\temp\test-file.txt",
                @"\\127.0.0.1\c$\temp\test-file.txt"
            };
            foreach (var item in filenames)
            {
                var local = new SystemLocal(item);
                Assert.AreEqual(item, local.getAbsolute());
            }
        }

        [Test]
        public void TestPathAbsoluteWin32()
        {
            var test = @"C:\Directory\File.ext";
            var path = new SystemLocal(test);
            Assert.AreEqual(test, path.getAbsolute());
        }

        [Test]
        public void TestPathsPipe()
        {
            var path = Paths.get(PIPE_NAME);
            Assert.NotNull(path);
            Assert.AreEqual(PIPE_NAME, path.ToString());
        }

        [Test]
        public void TestPathsWslPath()
        {
            var path = Paths.get(WSL_PATH);
            Assert.NotNull(path);
            Assert.AreEqual(WSL_PATH, path.ToString());
        }

        [Test]
        public void TestPipeName()
        {
            CoreLocal local = new SystemLocal(PIPE_NAME);
            Assert.AreEqual(PIPE_NAME, local.getAbsolute());
        }

        [Test]
        public void TestWslPath()
        {
            CoreLocal local = new SystemLocal(WSL_PATH);
            Assert.AreEqual(WSL_PATH, local.getAbsolute());
        }
    }
}
