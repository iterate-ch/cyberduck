//
// Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
// http://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
//

using ch.cyberduck.core;
using ch.cyberduck.core.aquaticprime;
using System;
using System.Runtime.CompilerServices;
using Windows.Foundation.Metadata;
using Windows.Services.Store;
using Windows.System;

namespace Ch.Cyberduck.Core.AquaticPrime
{
    public class WindowsStoreLicense : License
    {
        public string getName()
        {
            StoreContext storeContext = StoreContext.GetDefault();
            StoreAppLicense license = storeContext.GetAppLicenseAsync().AsTask().Result;

            if (license is { IsActive: true })
            {
                if (license.IsTrial)
                {
                    return LocaleFactory.localizedString("Trial Version", "License");
                }

                User user = null;

                if (ApiInformation.IsApiContractPresent("Windows.Foundation.UniversalApiContract", 12))
                {
                    try
                    {
                        user = GetUser();
                    }
                    catch { }
                }

                if (user is { })
                {
                    try
                    {
                        var displayNameValue = user.GetPropertyAsync(KnownUserProperties.DisplayName).AsTask().Result;
                        if (displayNameValue is string displayName && !string.IsNullOrWhiteSpace(displayName))
                        {
                            return displayName;
                        }
                    }
                    catch { }
                }

            }

            return LocaleFactory.localizedString("Unknown");

            [MethodImpl(MethodImplOptions.NoInlining)]
            static User GetUser() => User.GetDefault();
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
            StoreAppLicense license = storeContext.GetAppLicenseAsync().AsTask().Result;
            return license?.IsActive ?? false;
        }
    }
}
