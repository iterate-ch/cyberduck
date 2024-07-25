using ch.cyberduck.core;
using java.nio.file;
using java.util;
using NUnit.Framework;
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
        public void EnsureNioDoesntFail([Values(PIPE_NAME, WSL_PATH, "X:\\C$")] string path)
        {
            _ = Paths.get(path).toFile();
        }

        [Test]
        public void ValidateInvalidNioPaths([Values(@"\\?\X:")] string path)
        {
            Assert.Throws<InvalidPathException>(() => Paths.get(path));
        }

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
        public void TestBadDriveLetter([Values(@"#:\", @"\#:\", @"\\.\#:\")] string path)
        {
            // Sanitized empty
            Assert.That(new SystemLocal(path).getAbsolute(), Is.Empty);
        }

        [Test, Sequential]
        public void TestPathSanitize(
            [Values(
                /* 00 */ "C:\\C:"
            )] string path,
            [Values(
                /* 00 */ "C:\\C_"
            )] string expected)
        {
            Assert.That(new SystemLocal(path).getAbsolute(), Is.EqualTo(expected));
        }

        [Test]
        public void TestConvertToDirectorySeparator()
        {
            SystemLocal path = new(PIPE_NAME.Replace('\\', '/'));
            Assert.That(path.getAbsolute(), Is.EqualTo(PIPE_NAME));
        }

        [Test]
        public void TestDirectorySeparators([ValueSource(nameof(TestDirectorySeparatorsValues))] char sep)
        {
            Assert.That(new SystemLocal($"C:{sep}").getAbsolute(), Is.EqualTo($"C:{Path.DirectorySeparatorChar}"));
        }

        static char[] TestDirectorySeparatorsValues() => [Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar];

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
                // Local passes through invalid paths, and logs an error
                Assert.That(new SystemLocal(item).getAbsolute(), Is.EqualTo(item));
            }
        }

        [Test]
        public void TestPathToLocal()
        {
            const string PUBLIC_PATH = @"C:\Users\Public";
            CorePath path = new(PUBLIC_PATH, EnumSet.of(AbstractPath.Type.directory));
            SystemLocal local = new(path.getAbsolute());
            Assert.That(local.getAbsolute(), Is.EqualTo(PUBLIC_PATH));
        }

        [Test]
        public void TestAbsoluteEquality([Values(
            PIPE_NAME,
            @"\\?\C:\ÄÖÜßßäöü",
            WSL_PATH,
            @"\Volumes\System\Test",
            @"C:\Directory\File.ext",
            @"C:\C$")] string path)
        {
            Assert.That(new SystemLocal(path).getAbsolute(), Is.EqualTo(path));
        }

        [Test]
        public void TestTildePath()
        {
            Assert.That(new SystemLocal("~/.ssh/known_hosts").getAbsolute(), Is.Not.Empty);
        }
    }
}
