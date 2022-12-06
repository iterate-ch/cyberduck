using java.nio.file;
using NUnit.Framework;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local
{
    [TestFixture]
    public class SystemLocalTest
    {
        const string PIPE_NAME = @"\\.\pipe\openssh-ssh-agent";
        const string WSL_PATH = @"\\wsl$\test\";

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
