using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ch.cyberduck.core;
using ch.cyberduck.core.aquaticprime;
using java.util;

namespace Ch.Cyberduck.Core.AquaticPrime
{
    public class WindowsStoreLicenseFactory : LicenseFactory
    {
        public WindowsStoreLicense License { get; } = new WindowsStoreLicense();

        public override List open()
        {
            return java.util.Collections.singletonList(License);
        }

        protected override License open(ch.cyberduck.core.Local l)
        {
            return EMPTY_LICENSE;
        }
    }
}
