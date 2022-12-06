using NUnit.Framework;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local
{
    [TestFixture]
    public class SystemLocalTest
    {
        const string PIPE_NAME = @"\\.\pipe\openssh-ssh-agent";

        [Test]
        public void TestPipeName()
        {
            CoreLocal local = new SystemLocal(PIPE_NAME);
            Assert.AreEqual($"Local{{path='{PIPE_NAME}'}}", local.ToString());
        }
    }
}
