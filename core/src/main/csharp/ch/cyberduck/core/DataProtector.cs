// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.Security.Cryptography;
using System.Text;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Core
{
    public class DataProtector
    {
        private static readonly Logger Log = LogManager.getLogger(typeof (DataProtector).FullName);

        public static string Encrypt(string data)
        {
            try
            {
                byte[] plain = Encoding.UTF8.GetBytes(data);
                byte[] encrypted = ProtectedData.Protect(plain, null, DataProtectionScope.CurrentUser);
                return Convert.ToBase64String(encrypted);
            }
            catch (CryptographicException e)
            {
                Log.error("Error while encrypting data.", e);
                return null;
            }
        }

        public static string Decrypt(string base64)
        {
            try
            {
                byte[] decrypted = ProtectedData.Unprotect(Convert.FromBase64String(base64), null,
                    DataProtectionScope.CurrentUser);
                return Encoding.UTF8.GetString(decrypted);
            }
            catch (CryptographicException e)
            {
                Log.error("Error while decrypting data.", e);
                return null;
            }
        }
    }
}
