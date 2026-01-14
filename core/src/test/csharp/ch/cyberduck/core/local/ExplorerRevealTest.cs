using System.IO;
using ch.cyberduck.core.local;
using NUnit.Framework;

namespace Ch.Cyberduck.Core.Local;

[TestFixture(Explicit = true)]
public class ExplorerRevealTest
{
    [Test]
    public void TestReveal()
    {
        SystemLocal temp = new(Path.GetTempPath());
        SystemLocal file = new(temp, Path.GetRandomFileName());
        new DefaultLocalTouchFeature().touch(file);
        Assert.That(new ExplorerRevealService().reveal(file, true), Is.True);
    }
}
