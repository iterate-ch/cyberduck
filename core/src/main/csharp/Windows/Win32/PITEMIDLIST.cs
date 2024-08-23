using Windows.Win32.UI.Shell.Common;

namespace Windows.Win32;

public readonly unsafe struct PITEMIDLIST(ITEMIDLIST* value)
{
    public readonly ITEMIDLIST* Value = value;

    public ref readonly ITEMIDLIST ValueRef => ref *Value;

    public static implicit operator PITEMIDLIST(ITEMIDLIST* value) => new(value);
}
