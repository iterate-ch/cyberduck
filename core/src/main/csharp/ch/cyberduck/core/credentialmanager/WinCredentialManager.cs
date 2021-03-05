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

using Ch.Cyberduck.Core.Microsoft.Windows.Sdk;
using org.apache.log4j;
using System;
using System.Buffers;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Text;

namespace Ch.Cyberduck.Core.CredentialManager
{
    using static PInvoke;

    public static class WinCredentialManager
    {
        private static readonly Logger log = Logger.getLogger(typeof(WinCredentialManager).AssemblyQualifiedName);

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

        /// <summary>
        /// Extract the stored credential from Windows Credential store
        /// </summary>
        /// <param name="target">Name of the application/Url where the credential is used for</param>
        /// <returns>empty credentials if target not found, else stored credentials</returns>
        public unsafe static WindowsCredentialManagerCredential GetCredentials(string target)
        {
            using var handle = new CredHandle();
            if (!CredRead(target, CRED_TYPE.CRED_TYPE_GENERIC, 0, out handle.Put()))
            {
                log.error($"Cannot get credential for \"{target}\"", Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error()));
                return default;
            }

            ref CREDENTIALW cred = ref handle.Value;
            var type = (CRED_TYPE)cred.Type;
            var flags = (CRED_FLAGS)cred.Flags;
            var persist = (CRED_PERSIST)cred.Persist;
            var username = cred.UserName.ToString();
            var password = Encoding.Default.GetString(cred.CredentialBlob, (int)cred.CredentialBlobSize);
            var attributes = new Dictionary<string, string>();

            if (cred.AttributeCount > 0)
            {
                uint count = cred.AttributeCount;
                for (var i = 0; i < count; i++)
                {
                    var attribute = cred.Attributes[i];
                    var keyword = attribute.Keyword.ToString();
                    var blob = Encoding.Default.GetString(
                        attribute.Value, unchecked((int)attribute.ValueSize));
                    attributes.Add(keyword, blob);
                }
                var keyGroups = attributes.Keys.Select(x => (key: x, separator: x.IndexOf(':'))).Where(x => x.separator != -1).ToLookup(
                    x => x.key.Substring(0, x.separator),
                    x => x.key.Substring(x.separator + 1));
                foreach (var item in keyGroups)
                {
                    var key = item.Key;
                    var builder = new StringBuilder();
                    foreach (var page in item.OrderBy(x => int.Parse(x)).ToDictionary(x => attributes[key + ":" + x]))
                    {
                        attributes.Remove(page.Key);
                        builder.Append(page.Value);
                    }
                    attributes[key] = builder.ToString();
                    // auto concatenate paged-objects to full string
                }
            }
            return new WindowsCredentialManagerCredential(username, password, type, flags, persist, attributes);
        }

        public static bool RemoveCredentials(string target)
        {
            if (!CredDelete(target, (uint)CRED_TYPE.CRED_TYPE_GENERIC, 0))
            {
                log.error($"Could not remove credentials \"{target}\"", Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error()));
                return false;
            }
            return true;
        }

        public static bool SaveCredentials(string target, NetworkCredential credential) => SaveCredentials(target, new WindowsCredentialManagerCredential(
            credential.UserName,
            credential.Password,
            CRED_TYPE.CRED_TYPE_GENERIC, 0, CRED_PERSIST.CRED_PERSIST_ENTERPRISE, new Dictionary<string, string>()));

        public unsafe static bool SaveCredentials(string target, WindowsCredentialManagerCredential credential)
        {
            var cred = new CREDENTIALW
            {
                TargetName = target,
                UserName = credential.UserName,
                Type = (uint)credential.Type,
                Flags = (uint)credential.Flags,
                Persist = (uint)credential.Persist,
            };

            if (!string.IsNullOrWhiteSpace(credential.Password))
            {
                var passwordBytes = Encoding.Default.GetBytes(credential.Password);
                cred.CredentialBlobSize = (uint)passwordBytes.Length;
                cred.CredentialBlob = (byte*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(passwordBytes.AsSpan()));
            }

            var pages = credential.Attributes.Values.Aggregate(0, (a, v) =>
            {
                if (string.IsNullOrWhiteSpace(v))
                {
                    return a;
                }

                var length = v.Length;
                var full = length / 256;
                if (length - (256 * full) > 0)
                {
                    full += 1;
                }
                return a + full;
            });
            using var attributes = MemoryPool<CREDENTIAL_ATTRIBUTEW>.Shared.Rent(pages);
            var index = 0;
            foreach (var item in credential.Attributes)
            {
                if (string.IsNullOrWhiteSpace(item.Value))
                {
                    continue;
                }
                if (item.Value.Length > 256)
                {
                    var innerIndex = 0;
                    for (int i = 0; i < item.Value.Length; i += 256)
                    {
                        var key = item.Key + ":" + innerIndex;
                        var bytes = Encoding.Default.GetBytes(item.Value.Substring(i, Math.Min(256, item.Value.Length - i)));
                        attributes.Memory.Span[index + innerIndex] = new CREDENTIAL_ATTRIBUTEW()
                        {
                            Keyword = key,
                            ValueSize = (uint)bytes.Length,
                            Value = (byte*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(bytes.AsSpan())),
                        };

                        innerIndex += 1;
                    }
                    index += innerIndex;
                }
                else
                {
                    var bytes = Encoding.Default.GetBytes(item.Value);
                    attributes.Memory.Span[index] = new CREDENTIAL_ATTRIBUTEW()
                    {
                        Keyword = item.Key,
                        ValueSize = (uint)bytes.Length,
                        Value = (byte*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(bytes.AsSpan())),
                    };
                    index += 1;
                }
            }
            cred.AttributeCount = (uint)pages;
            cred.Attributes = (CREDENTIAL_ATTRIBUTEW*)Unsafe.AsPointer(ref MemoryMarshal.GetReference(attributes.Memory.Span));

            if (!CredWrite(&cred, 0))
            {
                log.error($"Failed saving credentials for \"{target}\"", Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error()));
                return false;
            }
            return true;
        }
    }

    public record WindowsCredentialManagerCredential
    {
        public string UserName { get; }
        public string Password { get; }
        public CRED_TYPE Type { get; }
        public CRED_FLAGS Flags { get; }
        public CRED_PERSIST Persist { get; }
        public IDictionary<string, string> Attributes { get; }

        public WindowsCredentialManagerCredential(string userName, string password, CRED_TYPE type, CRED_FLAGS flags, CRED_PERSIST persist, IDictionary<string, string> attributes)
        {
            UserName = userName;
            Password = password;
            Type = type;
            Flags = flags;
            Persist = persist;
            Attributes = attributes;
        }
    }
}
