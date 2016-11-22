using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace Ch.Cyberduck.windows.src.main.csharp.ch.cyberduck.ui.core.uwp
{
    [ComImport]
    [Guid("3E68D4BD-7135-4D10-8018-9FB6D9F33FA1")]
    [InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
    public interface IInitializeWithWindow
    {
        void Initialize(IntPtr hwnd);
    }
}
