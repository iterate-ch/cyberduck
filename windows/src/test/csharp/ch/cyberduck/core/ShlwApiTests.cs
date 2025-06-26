using NUnit.Framework;
using static Windows.Win32.CoreRefreshMethods;

namespace Cyberduck.Core.Test
{
    [TestFixture]
    public class ShlwApiTests
    {
        [Test]
        public static void TestLoadIndirect()
        {
            string original = "C:\\Windows\\System32\\imageres.dll";
            Assert.That(SHLoadIndirectString(original), Is.EqualTo(original));
        }
    }
}
