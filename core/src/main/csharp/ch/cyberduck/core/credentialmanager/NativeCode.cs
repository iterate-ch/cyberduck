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
using System.Runtime.InteropServices;
using System.Text;
using FILETIME = System.Runtime.InteropServices.ComTypes.FILETIME;

namespace Ch.Cyberduck.Core.CredentialManager
{
    internal static class NativeCode
    {
        [DllImport("credui")]
        internal static extern CredentialUIReturnCodes CredUIPromptForCredentials(ref CredentialUIInfo creditUR,
            string targetName,
            IntPtr reserved1,
            int iError,
            StringBuilder userName,
            int maxUserName,
            StringBuilder password,
            int maxPassword,
            [MarshalAs(UnmanagedType.Bool)] ref bool pfSave,
            CredentialUIFlags flags);

        [DllImport("credui.dll", EntryPoint = "CredUIParseUserNameW", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern CredentialUIReturnCodes CredUIParseUserName(
            string userName,
            StringBuilder user,
            int userMaxChars,
            StringBuilder domain,
            int domainMaxChars);

        [DllImport("credui.dll", CharSet = CharSet.Auto)]
        internal static extern bool CredUnPackAuthenticationBuffer(int dwFlags,
            IntPtr pAuthBuffer,
            uint cbAuthBuffer,
            StringBuilder pszUserName,
            ref int pcchMaxUserName,
            StringBuilder pszDomainName,
            ref int pcchMaxDomainame,
            StringBuilder pszPassword,
            ref int pcchMaxPassword);

        [DllImport("credui.dll", EntryPoint = "CredUIPromptForWindowsCredentialsW", CharSet = CharSet.Unicode,
            SetLastError = true)]
        internal static extern int CredUIPromptForWindowsCredentials(ref CredentialUIInfo creditUR,
            int authError,
            ref uint authPackage,
            IntPtr InAuthBuffer,
            uint InAuthBufferSize,
            out IntPtr refOutAuthBuffer,
            out uint refOutAuthBufferSize,
            ref bool fSave,
            PromptForWindowsCredentialsFlags flags);

        [DllImport("credui")]
        internal static extern CredentialUIReturnCodes CredUICmdLinePromptForCredentials(
            string targetName,
            IntPtr reserved1,
            int iError,
            StringBuilder userName,
            int maxUserName,
            StringBuilder password,
            int maxPassword,
            [MarshalAs(UnmanagedType.Bool)] ref bool pfSave,
            CredentialUIFlags flags);

        [DllImport("Advapi32.dll", EntryPoint = "CredDeleteW", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern bool CredDelete(string target, CredentialType type, int reservedFlag);

        [DllImport("Advapi32.dll", EntryPoint = "CredReadW", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern bool CredRead(string target, CredentialType type, int reservedFlag,
            out IntPtr CredentialPtr);

        [DllImport("Advapi32.dll", EntryPoint = "CredWriteW", CharSet = CharSet.Unicode, SetLastError = true)]
        internal static extern bool CredWrite([In] ref NativeCredential userCredential, [In] UInt32 flags);

        [DllImport("Advapi32.dll", EntryPoint = "CredFree", SetLastError = true)]
        internal static extern bool CredFree([In] IntPtr cred);

        [DllImport("ole32.dll", EntryPoint = "CoTaskMemFree", SetLastError = true)]
        internal static extern void CoTaskMemFree(IntPtr buffer);

        [Flags]
        internal enum CredentialUIFlags
        {
            IncorrectPassword = 0x1,
            DoNotPersist = 0x2,
            RequestAdministrator = 0x4,
            ExcludeCertificates = 0x8,
            RequireCertificate = 0x10,
            ShowSaveCheckBox = 0x40,
            AlwaysShowUi = 0x80,
            RequireSmartcard = 0x100,
            PasswordOnlyOk = 0x200,
            ValidateUsername = 0x400,
            CompleteUsername = 0x800,
            Persist = 0x1000,
            ServerCredential = 0x4000,
            ExpectConfirmation = 0x20000,
            GenericCredentials = 0x40000,
            UsernameTargetCredentials = 0x80000,
            KeepUsername = 0x100000
        }

        internal enum CredentialUIReturnCodes : uint
        {
            Success = 0,
            Cancelled = 1223,
            NoSuchLogonSession = 1312,
            NotFound = 1168,
            InvalidAccountName = 1315,
            InsufficientBuffer = 122,
            InvalidParameter = 87,
            InvalidFlags = 1004
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
        internal struct CredentialUIInfo
        {
            public int cbSize;
            public IntPtr hwndParent;
            public string pszMessageText;
            public string pszCaptionText;
            public IntPtr hbmBanner;
        }


        [Flags]
        internal enum PromptForWindowsCredentialsFlags : uint
        {
            GenericCredentials = 0x1,
            ShowCheckbox = 0x2,
            AuthpackageOnly = 0x10,
            InCredOnly = 0x20,
            EnumerateAdmins = 0x100,
            EnumerateCurrentUser = 0x200,
            SecurePrompt = 0x1000,
            Pack32Wow = 0x10000000
        }


        internal enum CredentialType : uint
        {
            Generic = 1,
            DomainPassword = 2,
            DomainCertificate = 3
        }

        internal enum Persistance : uint
        {
            Session = 1,
            LocalMachine = 2,
            Entrprise = 3
        }

        [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
        internal struct NativeCredential
        {
            public UInt32 Flags;
            public CredentialType Type;
            public IntPtr TargetName;
            public IntPtr Comment;
            public FILETIME LastWritten;
            public UInt32 CredentialBlobSize;
            public IntPtr CredentialBlob;
            public UInt32 Persist;
            public UInt32 AttributeCount;
            public IntPtr Attributes;
            public IntPtr TargetAlias;
            public IntPtr UserName;
        }
    }
}
