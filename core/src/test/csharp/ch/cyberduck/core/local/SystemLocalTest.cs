using ch.cyberduck.core;
using java.nio.file;
using java.util;
using NUnit.Framework;
using CoreLocal = ch.cyberduck.core.Local;
using CorePath = ch.cyberduck.core.Path;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    [TestFixture]
    public class SystemLocalTest
    {
        const string PIPE_NAME = @"\\.\pipe\openssh-ssh-agent";
        const string WSL_PATH = @"\\wsl$\test\";

        [Test]
        public void FilesGetUsableSpace()
        {
            var store = Files.getFileStore(Paths.get(@"c:\"));
            store.getUsableSpace();
        }

        [Test]
        public void PathsToFileGetUsableSpace()
        {
            Paths.get(@"c:\").toFile().getUsableSpace();
        }

        [Test]
        public void TestBadDriveLetter1()
        {
            const string PATH = @"#:\";
            Assert.That(new SystemLocal(PATH).getAbsolute(), Is.Empty);
        }

        [Test]
        public void TestBadDriveLetter2()
        {
            const string PATH = @"\#:\";
            Assert.That(new SystemLocal(PATH).getAbsolute(), Is.Empty);
        }

        [Test]
        public void TestBadDriveLetter3()
        {
            const string PATH = @"\\.\#:\";
            Assert.That(new SystemLocal(PATH).getAbsolute(), Is.Empty);
        }

        [Test]
        public void TestConvertToDirectorySeparator()
        {
            var path = new SystemLocal(PIPE_NAME.Replace('\\', '/'));
            Assert.That(path.getAbsolute(), Is.EqualTo(PIPE_NAME));
        }

        [Test]
        public void TestDirectoryAltSeparators()
        {
            var path = new SystemLocal(@"C:" + Path.AltDirectorySeparatorChar);
            Assert.That(path.getAbsolute(), Is.EqualTo($"C:{Path.DirectorySeparatorChar}"));
        }

        [Test]
        public void TestDirectorySeparators()
        {
            var path = new SystemLocal(@"C:" + Path.DirectorySeparatorChar);
            Assert.That(path.getAbsolute(), Is.EqualTo($"C:{Path.DirectorySeparatorChar}"));
        }

        [Test]
        public void TestEmptyPath()
        {
            Assert.That(new SystemLocal("").getAbsolute(), Is.Empty);
        }

        /// <remarks>
        /// <see href="https://learn.microsoft.com/en-us/dotnet/standard/io/file-path-formats#example-ways-to-refer-to-the-same-file" />
        /// </remarks>
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
                Assert.That(local.getAbsolute(), Is.EqualTo(item));
            }
        }

        [Test]
        public void TestInvalidCharacters()
        {
            const string TEST = @"C:\:?<>:";
            const string EXPECT = @"C:\_____";
            CoreLocal local = new SystemLocal(TEST);
            Assert.That(local.getAbsolute(), Is.EqualTo(EXPECT));
        }

        [Test]
        public void TestLocalCompound()
        {
            CoreLocal root = new SystemLocal(@"C:\");
            CoreLocal compound = new SystemLocal(root, @"C:\");
            Assert.That(compound.getAbsolute(), Is.EqualTo("C:\\C$"));
        }

        [Test]
        public void TestMountainDuckPath()
        {
            CoreLocal local = new SystemLocal("/C:/Users/Public");
            Assert.That(local.getAbsolute(), Is.EqualTo("C:\\Users\\Public"));
        }

        [Test]
        public void TestPathAbsoluteWin32()
        {
            var test = @"C:\Directory\File.ext";
            var path = new SystemLocal(test);
            Assert.That(path.getAbsolute(), Is.EqualTo(test));
        }

        [Test]
        public void TestPathFromNonWindows()
        {
            const string TEST = @"\Volumes\System\Test";
            SystemLocal path = new(TEST);
            Assert.That(path.getAbsolute(), Is.EqualTo(TEST));
        }

        [Test]
        public void TestPathsPipe()
        {
            var path = Paths.get(PIPE_NAME);
            Assert.That(path.toString(), Is.EqualTo(PIPE_NAME));
        }

        [Test]
        public void TestPathsWslPath()
        {
            var path = Paths.get(WSL_PATH);
            Assert.That(path.toString(), Is.EqualTo(WSL_PATH));
        }

        [Test]
        public void TestPathToLocal()
        {
            CorePath path = new CorePath("C:\\Users\\Public", EnumSet.of(AbstractPath.Type.directory));
            var local = new SystemLocal(path.getAbsolute());
            Assert.That(local.getAbsolute(), Is.EqualTo("C:\\Users\\Public"));
        }

        [Test]
        public void TestPipeName()
        {
            CoreLocal local = new SystemLocal(PIPE_NAME);
            Assert.That(local.getAbsolute(), Is.EqualTo(PIPE_NAME));
        }

        [Test]
        public void TestTildePath()
        {
            CoreLocal local = new SystemLocal("~/.ssh/known_hosts");
            Assert.That(local.getAbsolute(), Is.Not.Empty);
        }

        [Test]
        public void TestUnicode()
        {
            var test = @"\\?\C:\ÄÖÜßßäöü";
            var path = new SystemLocal(test);
            Assert.That(path.getAbsolute(), Is.EqualTo(test));
        }

        [Test]
        public void TestWslPath()
        {
            CoreLocal local = new SystemLocal(WSL_PATH);
            Assert.That(local.getAbsolute(), Is.EqualTo(WSL_PATH));
        }
    }
}
