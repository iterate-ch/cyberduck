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
    public class LocalTest
    {
        const string PIPE_NAME = @"\\.\pipe\openssh-ssh-agent";
        const string WSL_PATH = @"\\wsl$\test\";

        [Test]
        public void FilesGetUsableSpace()
        {
            Files.getFileStore(Paths.get(@"c:\")).getUsableSpace();
        }

        [Test]
        public void PathsToFileGetUsableSpace()
        {
            Paths.get(@"c:\").toFile().getUsableSpace();
        }

        [Test]
        public void TestBadDriveLetter([Values(@"#:\", @"\#:\", @"\\.\#:\", @"C:\C:")] string path)
        {
            // Local passes through invalid paths, and logs an error
            Assert.That(new CoreLocal(path).getAbsolute(), Is.EqualTo(path));
        }

        [Test]
        public void TestConvertToDirectorySeparator()
        {
            CoreLocal path = new(PIPE_NAME.Replace('\\', '/'));
            Assert.That(path.getAbsolute(), Is.EqualTo(PIPE_NAME));
        }

        [Test]
        public void TestDirectorySeparators([ValueSource(nameof(TestDirectorySeparatorsValues))] char sep)
        {
            Assert.That(new CoreLocal($"C:{sep}").getAbsolute(), Is.EqualTo($"C:{Path.DirectorySeparatorChar}"));
        }

        static char[] TestDirectorySeparatorsValues() => [Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar];

        [Test]
        public void TestEmptyPath()
        {
            Assert.That(new CoreLocal("").getAbsolute(), Is.Empty);
        }

        /// <remarks>
        /// <see href="https://learn.microsoft.com/en-us/dotnet/standard/io/file-path-formats#example-ways-to-refer-to-the-same-file" />
        /// </remarks>
        [Test]
        public void TestFileFormats()
        {
            string[] filenames =
            [
                @"c:\temp\test-file.txt",
                @"\\127.0.0.1\c$\temp\test-file.txt",
                @"\\LOCALHOST\c$\temp\test-file.txt",
                @"\\.\c:\temp\test-file.txt",
                @"\\?\c:\temp\test-file.txt",
                @"\\.\UNC\LOCALHOST\c$\temp\test-file.txt",
                @"\\127.0.0.1\c$\temp\test-file.txt"
            ];
            foreach (var item in filenames)
            {
                Assert.That(new CoreLocal(item).getAbsolute(), Is.EqualTo(item));
            }
        }

        [Test]
        public void TestPathsPipe()
        {
            var path = Paths.get(PIPE_NAME);
            Assert.That(path, Is.Not.Null);
            Assert.That(path.toString(), Is.EqualTo(PIPE_NAME));
        }

        [Test]
        public void TestPathsWslPath()
        {
            var path = Paths.get(WSL_PATH);
            Assert.That(path, Is.Not.Null);
            Assert.That(path.toString(), Is.EqualTo(WSL_PATH));
        }

        [Test]
        public void TestPathToLocal()
        {
            const string PUBLIC_PATH = @"C:\Users\Public";
            CorePath path = new(PUBLIC_PATH, EnumSet.of(AbstractPath.Type.directory));
            CoreLocal local = new(path.getAbsolute());
            // Local keep "/" prefix of Path.
            Assert.That(local.getAbsolute(), Is.EqualTo($"/{PUBLIC_PATH}"));
        }

        [Test]
        public void TestAbsoluteEquality([Values(
            PIPE_NAME, 
            @"\\?\C:\ÄÖÜßßäöü",
            WSL_PATH,
            @"\Volumes\System\Test",
            @"C:\Directory\File.ext",
            "/C:/Users/Public",
            @"C:\C$")] string path)
        {
            Assert.That(new CoreLocal(path).getAbsolute(), Is.EqualTo(path));
        }

        [Test]
        public void TestTildePath()
        {
            Assert.That(new CoreLocal("~/.ssh/known_hosts").getAbsolute(), Is.Not.Empty);
        }
    }
}
