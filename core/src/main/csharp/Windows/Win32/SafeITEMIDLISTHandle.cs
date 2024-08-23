using Windows.Win32.UI.Shell.Common;

namespace Windows.Win32;

public class SafeITEMIDLISTHandle(bool ownsHandle) : CoTaskMemFreeHandle(ownsHandle)
{
    public unsafe PITEMIDLIST Value => new((ITEMIDLIST*)handle);

    public SafeITEMIDLISTHandle(nint handle, bool ownsHandle) : this(ownsHandle)
    {
        SetHandle(handle);
    }
}

