using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Core.VirtualDesktop
{
    public class DisabledVirtualDesktopManager : Contracts.IVirtualDesktopManager
    {
        public void BringToCurrentDesktop(Form form)
        {
            // Do nothing.
        }
    }
}
