using ch.cyberduck.core.local;
using NUnit.Framework;
using NUnit.Framework.Constraints;
using CoreLocal = ch.cyberduck.core.Local;
using NetPath = System.IO.Path;

namespace Ch.Cyberduck.Core.Local;

[TestFixture]
public class NTFSFilesystemBookmarkResolverTest
{
    [Test]
    public void EnsureRoundtrip()
    {
        SystemLocal temp = new(NetPath.GetTempPath());
        SystemLocal file = new(temp, NetPath.GetRandomFileName());
        new DefaultLocalTouchFeature().touch(file);
        NTFSFilesystemBookmarkResolver resolver = new(file);
        var bookmark = resolver.create(file);
        Assert.That(bookmark, new NotConstraint(new NullConstraint()));
        CoreLocal resolved = (CoreLocal)resolver.resolve(bookmark);
        Assert.That(resolved, new EqualConstraint(file));
    }
}
