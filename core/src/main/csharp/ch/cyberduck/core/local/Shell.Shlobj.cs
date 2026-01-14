using System;
using System.Runtime.InteropServices;
using Windows.Win32;
using Windows.Win32.System.SystemServices;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local;

public static partial class Shell
{
    /// <summary>
    /// Tries to extract the last item id in a PIDLIST_ABSOLUTE into <paramref name="child" />, and copies the remainder into <paramref name="parent" />.
    /// </summary>
    /// <param name="child">A IDLIST_ABSOLUTE, which receives the last ID on return.</param>
    /// <param name="parent">Receives the original <paramref name="child"/> IDLIST, with the last item zeroed.</param>
    /// <returns>True on success, and false on any failure.</returns>
    /// <remarks>Callers are responsible to cleanup resources after returning.</remarks>
    public static bool GetParent(ref SafeITEMIDLISTHandle child, out SafeITEMIDLISTHandle parent)
    {
        parent = child;
        if ((child = CorePInvoke.ILFindLastID(parent)).IsInvalid)
        {
            return false;
        }

        if ((child = CorePInvoke.ILClone(child)).IsInvalid)
        {
            return false;
        }

        return CorePInvoke.ILRemoveLastID(parent);
    }

    public static Exception ItemIdListFromDisplayName(string displayName, out SafeITEMIDLISTHandle handle)
    {
        return Marshal.GetExceptionForHR(CorePInvoke.SHParseDisplayName(displayName, null, SFGAO_FLAGS.SFGAO_FILESYSTEM, out handle, out _).Value);
    }

    public static Exception ItemIdListFromLocal(CoreLocal local, out SafeITEMIDLISTHandle handle)
    {
        return ItemIdListFromDisplayName(local.getAbsolute(), out handle);
    }
}
