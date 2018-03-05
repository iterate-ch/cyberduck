// 
// Copyright (c) 2010-2018 Yves Langisch. All rights reserved.
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

using org.apache.log4j;
using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Net;
using System.Runtime.InteropServices;
using System.Text;

namespace Ch.Cyberduck.Core.CredentialManager
{
    //ref: http://blogs.msdn.com/b/peerchan/archive/2005/11/01/487834.aspx

    public static class WinCredentialManager
    {
        private static readonly Logger log = Logger.getLogger(typeof(WinCredentialManager).AssemblyQualifiedName);

        /// <summary>
        /// Saves teh given Network Credential into Windows Credential store
        /// </summary>
        /// <param name="Target">Name of the application/Url where the credential is used for</param>
        /// <param name="credential">Credential to store</param>
        /// <returns>True:Success, False:Failure</returns>
        public static bool SaveCredentials(string Target, NetworkCredential credential)
        {
            // Go ahead with what we have are stuff it into the CredMan structures.
            Credential cred = new Credential(credential);
            cred.TargetName = Target;
            cred.Persist = NativeCode.Persistance.Entrprise;
            NativeCode.NativeCredential ncred = cred.GetNativeCredential();
            // Write the info into the CredMan storage.
            try
            {
                return NativeCode.CredWrite(ref ncred, 0);
            }
            catch (Exception e)
            {
                log.error($"Failed saving credentials for {Target}", e);
                return false;
            }
        }

        /// <summary>
        /// Extract the stored credential from WIndows Credential store
        /// </summary>
        /// <param name="Target">Name of the application/Url where the credential is used for</param>
        /// <returns>empty credentials if target not found, else stored credentials</returns>
        public static NetworkCredential GetCredentials(string Target)
        {
            IntPtr nCredPtr;
            var username = String.Empty;
            var passwd = String.Empty;
            try
            {
                // Make the API call using the P/Invoke signature
                bool ret = NativeCode.CredRead(Target, NativeCode.CredentialType.Generic, 0, out nCredPtr);
                // If the API was successful then...
                if (ret)
                {
                    using (CriticalCredentialHandle critCred = new CriticalCredentialHandle(nCredPtr))
                    {
                        Credential cred = critCred.GetCredential();
                        passwd = cred.CredentialBlob;
                        username = cred.UserName;
                    }
                }
            }
            catch (Exception e)
            {
                log.error($"Could not get credentials for {Target}", e);
            }
            return new NetworkCredential(username, passwd, string.Empty);
        }

        /// <summary>
        /// Remove stored credentials from windows credential store
        /// </summary>
        /// <param name="Target">Name of the application/Url where the credential is used for</param>
        /// <returns>True: Success, False: Failure</returns>
        public static bool RemoveCredentials(string Target)
        {
            try
            {
                // Make the API call using the P/Invoke signature
                return NativeCode.CredDelete(Target, NativeCode.CredentialType.Generic, 0);
            }
            catch (Exception e)
            {
                log.error($"Could not remove credentials {Target}", e);
                return false;
            }
        }

        /// <summary>
        /// Generates a string that can be used for "Auth" headers in web requests, "username:password" encoded in Base64
        /// </summary>
        /// <param name="cred"></param>
        /// <returns></returns>
        public static string GetBasicAuthString(this NetworkCredential cred)
        {
            byte[] credentialBuffer = new UTF8Encoding().GetBytes(cred.UserName + ":" + cred.Password);
            return Convert.ToBase64String(credentialBuffer);
        }
    }
}
