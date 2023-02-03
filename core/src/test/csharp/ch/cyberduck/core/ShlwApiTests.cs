using NUnit.Framework;
using static Windows.Win32.CorePInvoke;

namespace Cyberduck.Core.Test
{
    [TestFixture]
    public class ShlwApiTests
    {
        [Test]
        public static void TestLoadIndirect()
        {
            string original = "C:\\Windows\\System32\\imageres.dll";
            string load = SHLoadIndirectString(original);
            Assert.AreEqual(original, load);
        }
    }
}
