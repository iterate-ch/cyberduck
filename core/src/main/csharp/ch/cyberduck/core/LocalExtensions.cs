using System.ComponentModel;
using Ch.Cyberduck.Core.Local;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core;

public static class LocalExtensions
{
    /// <inheritdoc cref="SystemLocal.ToNativePath(CoreLocal)"/>
    public static string NativePath(this CoreLocal local) => SystemLocal.ToNativePath(local);

    /// <inheritdoc cref="SystemLocal.ToPlatformPath(CoreLocal)"/>
    public static string PlatformPath(this CoreLocal local) => SystemLocal.ToPlatformPath(local);
}
