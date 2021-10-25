using NUnit.Framework;

namespace Ch.Cyberduck.Core.Local
{
    using Local = ch.cyberduck.core.Local;

    [TestFixture]
    public class LocalTest
    {
        [Test]
        public void TestWSL()
        {
            var temp = new SystemLocal(@"\\wsl$\home\user\.ssh\id_rsa");
        }
    }
}
