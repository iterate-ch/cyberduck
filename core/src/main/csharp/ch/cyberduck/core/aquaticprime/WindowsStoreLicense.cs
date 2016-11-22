using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ch.cyberduck.core.aquaticprime;
using Windows.Services.Store;
using Windows.System;

namespace Ch.Cyberduck.Core.AquaticPrime
{
    public class WindowsStoreLicense : License
    {
        public string getName()
        {
            return (string)StoreContext.GetDefault().User.GetPropertyAsync(KnownUserProperties.DisplayName).AsTask().Result;
        }

        public string getValue(string str)
        {
            StoreContext storeContext = StoreContext.GetDefault();
            StoreAppLicense license = storeContext.GetAppLicenseAsync().AsTask().Result;
            if (license.IsActive)
            {
                if (license.IsTrial)
                {
                    return "Trial";
                }
                else
                {
                    return "Full";
                }
            }
            else
            {
                return "Invalid";
            }
        }

        public bool isReceipt()
        {
            return true;
        }

        public bool verify()
        {
            StoreContext storeContext = StoreContext.GetDefault();
            StoreAppLicense license = storeContext.GetAppLicenseAsync().AsTask().Result;
            return license.IsActive;
        }
    }
}
