using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ch.cyberduck.core.aquaticprime;
using Windows.Services.Store;
using Windows.System;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Core.AquaticPrime
{
	public class WindowsStoreLicense : License
	{
		public string getName()
		{
			StoreContext storeContext = StoreContext.GetDefault();
			StoreAppLicense license = storeContext.GetAppLicenseAsync().AsTask().Result;
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
					return (string)StoreContext.GetDefault().User.GetPropertyAsync(KnownUserProperties.DisplayName).AsTask().Result;
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

		public bool verify()
		{
			StoreContext storeContext = StoreContext.GetDefault();
			StoreAppLicense license = storeContext.GetAppLicenseAsync().AsTask().Result;
			return true || (license?.IsActive ?? true); // HACK Windows Store Submission Process
		}
	}
}
