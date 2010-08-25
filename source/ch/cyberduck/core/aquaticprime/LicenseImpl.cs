// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 
using System;
using Ch.Cyberduck.Core.Aquaticprime;
using org.apache.log4j;

namespace ch.cyberduck.core.aquaticprime
{
    internal class LicenseImpl : AbstractLicense
    {
        private static readonly Logger Log = Logger.getLogger(typeof (LicenseImpl).Name);

        public LicenseImpl(Local file) : base(file)
        {
            ;
        }

        public override bool verify()
        {
            bool valid = LicenseVerifier.Instance.VerifyLicenseData(getFile().getAbsolute());
            if (valid)
            {
                Log.info("Valid donation key:" + getFile().getAbsolute());
            }
            else
            {
                Log.warn("Not a valid donation key:" + getFile().getAbsolute());
            }
            return valid;
        }

        public override string getValue(string property)
        {
            return LicenseVerifier.Instance.GetValue(getFile().getAbsolute(), property);
        }

        public static void Register()
        {
            LicenseFactory.addFactory(core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : LicenseFactory
        {
            protected override License open(Local l)
            {
                return new LicenseImpl(l);
            }

            protected override object create()
            {
                throw new NotSupportedException();
            }
        }
    }
}