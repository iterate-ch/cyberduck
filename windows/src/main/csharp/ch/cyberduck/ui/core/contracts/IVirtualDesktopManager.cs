using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Core.Contracts
{
    public interface IVirtualDesktopManager
    {
        void BringToCurrentDesktop(Form form);
    }
}
