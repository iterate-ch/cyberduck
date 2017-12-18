// 
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
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

using System;
using System.Net;
using System.Runtime.InteropServices;
using System.Text;

namespace Ch.Cyberduck.Core.CredentialManager
{
    internal class Credential
    {
        public UInt32 AttributeCount;
        public IntPtr Attributes;
        public string Comment;
        public string CredentialBlob;
        public UInt32 CredentialBlobSize;
        public UInt32 Flags;
        public DateTime LastWritten;
        public NativeCode.Persistance Persist;
        public string TargetAlias;
        public string TargetName;
        public NativeCode.CredentialType Type;
        public string UserName;

        public Credential()
        {
        }

        internal Credential(NativeCode.NativeCredential ncred)
        {
            CredentialBlobSize = ncred.CredentialBlobSize;
            CredentialBlob = Marshal.PtrToStringUni(ncred.CredentialBlob,
                (int) ncred.CredentialBlobSize / 2);
            UserName = Marshal.PtrToStringUni(ncred.UserName);
            TargetName = Marshal.PtrToStringUni(ncred.TargetName);
            TargetAlias = Marshal.PtrToStringUni(ncred.TargetAlias);
            Type = ncred.Type;
            Flags = ncred.Flags;
            Persist = (NativeCode.Persistance) ncred.Persist;
            try
            {
                LastWritten =
                    DateTime.FromFileTime((long) ((ulong) ncred.LastWritten.dwHighDateTime << 32 |
                                                  (ulong) ncred.LastWritten.dwLowDateTime));
            }
            catch (ArgumentOutOfRangeException e)
            {
            }
        }

        public Credential(NetworkCredential credential)
        {
            CredentialBlob = credential.Password;
            UserName = String.IsNullOrWhiteSpace(credential.Domain)
                ? credential.UserName
                : credential.Domain + "\\" + credential.UserName;
            CredentialBlobSize = (UInt32) Encoding.Unicode.GetBytes(credential.Password).Length;
            AttributeCount = 0;
            Attributes = IntPtr.Zero;
            Comment = null;
            TargetAlias = null;
            Type = NativeCode.CredentialType.Generic;
            Persist = NativeCode.Persistance.Session;
        }

        /// <summary>
        /// This method derives a NativeCredential instance from a given Credential instance.
        /// </summary>
        /// <param name="cred">The managed Credential counterpart containing data to be stored.</param>
        /// <returns>A NativeCredential instance that is derived from the given Credential
        /// instance.</returns>
        internal NativeCode.NativeCredential GetNativeCredential()
        {
            NativeCode.NativeCredential ncred = new NativeCode.NativeCredential();
            ncred.AttributeCount = 0;
            ncred.Attributes = IntPtr.Zero;
            ncred.Comment = IntPtr.Zero;
            ncred.TargetAlias = IntPtr.Zero;
            ncred.Type = Type;
            ncred.Persist = (UInt32) Persist;
            ncred.UserName = Marshal.StringToCoTaskMemUni(UserName);
            ncred.TargetName = Marshal.StringToCoTaskMemUni(TargetName);
            ncred.CredentialBlob = Marshal.StringToCoTaskMemUni(CredentialBlob);
            ncred.CredentialBlobSize = (UInt32) CredentialBlobSize;
            if (LastWritten != DateTime.MinValue)
            {
                var fileTime = LastWritten.ToFileTimeUtc();
                ncred.LastWritten.dwLowDateTime = (int) (fileTime & 0xFFFFFFFFL);
                ncred.LastWritten.dwHighDateTime = (int) ((fileTime >> 32) & 0xFFFFFFFFL);
            }
            return ncred;
        }
    }
}
