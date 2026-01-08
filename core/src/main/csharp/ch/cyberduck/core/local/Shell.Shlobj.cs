using System;
using System.Runtime.InteropServices;
using Windows.Win32;
using Windows.Win32.System.SystemServices;
using CoreLocal = ch.cyberduck.core.Local;

namespace Ch.Cyberduck.Core.Local;

public static partial class Shell
{
    public static SafeITEMIDLISTHandle GetParent(ref SafeITEMIDLISTHandle handle)
    {
        SafeITEMIDLISTHandle child;
        if ((child = CorePInvoke.ILFindLastID(handle)).IsInvalid)
        {
            return null;
        }

        if ((child = CorePInvoke.ILClone(child)).IsInvalid)
        {
            return null;
        }

        var parent = handle;
        handle = child;
        if (!CorePInvoke.ILRemoveLastID(parent))
        {
            parent.Dispose();
        }

        return parent;
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
