using System.Runtime.InteropServices;
using System.Text;
using ch.cyberduck.core;
using java.nio.file;
using java.util;
using NUnit.Framework;
using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;
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
        public void TestBadDriveLetter([Values(@"#:\", @"\\.\#:\")] string path)
        {
            // Bad paths are copied verbatim.
            Assert.That(new SystemLocal(path).getAbsolute(), Is.EqualTo(path));
        }

        [Test, Sequential]
        public void TestPathCanonicalize(
            [Values([
                /* 00 */ "C:\\C:",
                /* 01 */ @"\\?\C:",
                /* 02 */ @"\\?\C:\",
            ])] string path,
            [Values([
                /* 00 */ "C:\\C_",
                /* 01 */ "C:\\",
                /* 02 */ "C:\\",
            ])] string expected)
        {
            Assert.That(new SystemLocal(path).getAbsolute(), Is.EqualTo(expected));
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

        /// <remarks>
        /// <see href="https://learn.microsoft.com/en-us/dotnet/standard/io/file-path-formats#example-ways-to-refer-to-the-same-file" />
        /// </remarks>
        [Test]
        public unsafe void TestPathCchCanonicalize([Values(
            @"c:\temp\test-file.txt",
            @"\\127.0.0.1\c$\temp\test-file.txt",
            @"\\LOCALHOST\c$\temp\test-file.txt",
            @"\\?\UNC\LOCALHOST\c$\temp\test-file.txt",
            @"\\?\c:\temp\test-file.txt",
            @"\\127.0.0.1\c$\temp\test-file.txt",
            @"\\?\C:\Temp",
            //@"\\?\C:\Temp\", // Paths.get() removes trailing separator
            @"\\?\C:\ Leading Trailing Whitespace \", // Paths.get() keeps trailing separator
            @"\Test"
        )] string path)
        {
            SystemLocal local = new(path);
            var absolute = local.getAbsolute();
            string canonical;
            fixed (char* canonicalLocal = new char[CorePInvoke.PATHCCH_MAX_CCH])
            {
                fixed (char* pathLocal = path)
                {
                    if (PInvoke.PathCchCanonicalizeEx(canonicalLocal, CorePInvoke.PATHCCH_MAX_CCH, pathLocal, PATHCCH_OPTIONS.PATHCCH_ALLOW_LONG_PATHS | PATHCCH_OPTIONS.PATHCCH_FORCE_ENABLE_LONG_NAME_PROCESS) is
                        {
                            Failed: true,
                            Value: { } canonicalizeError
                        })
                    {
                        throw Marshal.GetExceptionForHR(canonicalizeError);
                    }
                }

                canonical = ((PCWSTR)canonicalLocal).ToString();
            }

            Assert.That(absolute, Is.EqualTo(canonical));
        }

        [Test]
        public void EnsurePrefixPlatform()
        {
            StringBuilder builder = new((int)CorePInvoke.PATHCCH_MAX_CCH);
            builder.Append("C:");
            while (builder.Length < 260)
            {
                builder.Append('\\');
                builder.Append(Path.GetTempFileName());
            }
            SystemLocal local = new(builder.ToString());
            Assert.That(local.getAbsolute(), Is.Not.SubPathOf(@"\\?\"));
            Assert.That(local.NativePath(), Is.SubPathOf(@"\\?\"));
#if NETFRAMEWORK
            Assert.That(local.PlatformPath(), Is.SubPathOf(@"\\?\"));
#endif
        }
    }
}
