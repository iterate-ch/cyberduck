using System.ComponentModel;
using Ch.Cyberduck.Core.Local;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core;

public static class LocalExtensions
{
    /// <inheritdoc cref="PlatformLocalSupport.ToNativePath(CoreLocal)"/>
    public static string NativePath(this CoreLocal local) => PlatformLocalSupport.ToNativePath(local);

    /// <inheritdoc cref="PlatformLocalSupport.ToPlatformPath(CoreLocal)"/>
    public static string PlatformPath(this CoreLocal local) => PlatformLocalSupport.ToPlatformPath(local);
}
