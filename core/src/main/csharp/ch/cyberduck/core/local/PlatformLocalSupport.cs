using System;
using System.ComponentModel;
using System.IO;
using System.Runtime.CompilerServices;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local;

// This class must never appear in any stack traces.
// IKVM doesn't support `ReadOnlySpan<>` in method signatures.

internal static class PlatformLocalSupport
{
    private const string UncPathPrefix = @"\\?\";
    
    /// <summary>
    /// Returns a path that is usable for .NET IO.
    /// </summary>
    /// <returns>A path, optionally prefixed with \\?\ when needed.</returns>
    [Browsable(false), EditorBrowsable(EditorBrowsableState.Never)]
    public static string ToPlatformPath(CoreLocal local)
    {
#if NETCOREAPP
        // .NET Core automatically handles long paths.
        return local.getAbsolute();
#else
        // .NET Framework doesn't automatically prefix long paths.
        // Assume same semantics as Win32 paths.
        return ToNativePath(local);
#endif
    }

    /// <summary>
    /// Returns a path usable for Win32 IO, e.g. CreateFile.
    /// </summary>
    /// <returns>A path, optionally prefixed with \\?\ when needed.</returns>
    [Browsable(false), EditorBrowsable(EditorBrowsableState.Never)]
    public static string ToNativePath(CoreLocal local)
    {
        // TODO There is RtlAreLongPathsEnabled, but this is undocumented.
        // Manifest | LongPathsEnable | RtlAreLongPathsEnabled
        // True     | False           | False
        // False    | False           | False
        // False    | True            | False
        // True     | True            | True
        // Just prefix every long path.

        var path = local.getAbsolute();
        bool unc = path.Length > 2
            && IsDirectorySeparator(path[0])
            && IsDirectorySeparator(path[1]);
        bool extended = unc && path.Length > 3
            && path[2] is '?' or '.';

        if (!extended && path.Length > 248 /*MaxShortDirectoryName*/)
        {
            if (unc)
            {
                // When storing to a UNC drive, make \\?\UNC\X from \\X
                return path.Insert(2, @"?\UNC\");
            }
            else
            {
                // Create \\?\X from X
                return UncPathPrefix + path;
            }
        }

        return path;
    }

    internal static bool IsDirectorySeparator(char sep) =>
            sep == Path.DirectorySeparatorChar || sep == Path.AltDirectorySeparatorChar;

    [MethodImpl(MethodImplOptions.AggressiveInlining)]
#if NETCOREAPP
    internal static ReadOnlySpan<char> PathRoot(scoped in ReadOnlySpan<char> path)
    {
        return Path.GetPathRoot(path);
    }
#else
    internal static ReadOnlySpan<char> PathRoot(string path)
    {
        return Path.GetPathRoot(path).AsSpan();
    }
#endif

    internal static void WriteNormalized(in ReadOnlySpan<char> path, StringWriter writer)
    {
        writer.GetStringBuilder().EnsureCapacity(path.Length);
        foreach (ref readonly var c in path)
        {
            if (c == Path.AltDirectorySeparatorChar)
            {
                writer.Write(Path.DirectorySeparatorChar);
            }
            else
            {
                writer.Write(c);
            }
        }
    }
}
