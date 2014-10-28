// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
using ch.cyberduck.core.aquaticprime;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Aquaticprime
{
    internal class LicenseImpl : AbstractLicense
    {
        private static readonly Logger Log = Logger.getLogger(typeof (LicenseImpl).FullName);
        private readonly ch.cyberduck.core.Local _file;

        public LicenseImpl(ch.cyberduck.core.Local file)
            : base(file)
        {
            _file = file;
        }

        public override bool verify()
        {
            bool valid = LicenseVerifier.Instance.VerifyLicenseData(_file.getAbsolute());
            if (valid)
            {
                Log.info("Valid donation key:" + _file.getAbsolute());
            }
            else
            {
                Log.warn("Not a valid donation key:" + _file.getAbsolute());
            }
            return valid;
        }

        public override string getValue(string property)
        {
            return LicenseVerifier.Instance.GetValue(_file.getAbsolute(), property);
        }
    }
}