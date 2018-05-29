using ch.cyberduck.core;
using ch.cyberduck.core.aquaticprime;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Services.Store;
using Windows.System;

namespace Ch.Cyberduck.Core.AquaticPrime
{
    public class WindowsStoreLicense : License
    {
        public string getName()
        {
            StoreContext storeContext = StoreContext.GetDefault();
            StoreAppLicense license = GetResult(storeContext.GetAppLicenseAsync());
            if (license == null)
            {
                return LocaleFactory.localizedString("Unknown");
            }
            if (license.IsActive)
            {
                if (license.IsTrial)
                {
                    return LocaleFactory.localizedString("Trial Version", "License");
                }
                else
                {
                    var user = storeContext.User;
                    if (user == null)
                    {
                        return (string)GetResult(user.GetPropertyAsync(KnownUserProperties.DisplayName));
                    }
                    else
                    {
                        return LocaleFactory.localizedString("Unknown");
                    }
                }
            }
            else
            {
                return LocaleFactory.localizedString("Unknown");
            }
        }

        public string getValue(string str)
        {
            return LocaleFactory.localizedString("Unknown");
        }

        public bool isReceipt()
        {
            return true;
        }

        public override string ToString()
        {
            return string.Format(LocaleFactory.localizedString("Registered to {0}", "License"), getName());
        }

        public bool verify(LicenseVerifierCallback callback)
        {
            StoreContext storeContext = StoreContext.GetDefault();
            StoreAppLicense license = GetResult(storeContext.GetAppLicenseAsync());
            return true || (license?.IsActive ?? true); // HACK Windows Store Submission Process
        }

        private static T GetResult<T>(IAsyncOperation<T> operation)
        {
            T result;
            try
            {
                result = operation.GetResults();
            }
            finally
            {
                operation.Close();
            }
            return result;
        }
    }
}
