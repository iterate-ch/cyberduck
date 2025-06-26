using Ch.Cyberduck.Core.I18n;
using NUnit.Framework;

namespace Cyberduck.Core.Test.I18n
{
    [TestFixture]
    public class StringLoaderTest
    {
        [Test]
        public void LoadString()
        {
            using StringLoader stringLoader = new();
            Assert.That(stringLoader.GetString(30396u), Is.Not.WhiteSpace);
        }
    }
}
