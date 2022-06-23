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

using org.apache.logging.log4j;
using System;
using System.Buffers;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Text;
using Windows.Win32.Foundation;
using Windows.Win32.Security.Credentials;
using static System.Runtime.CompilerServices.Unsafe;
using static System.Runtime.InteropServices.MemoryMarshal;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.Security.Credentials.CRED_PERSIST;
using static Windows.Win32.Security.Credentials.CRED_TYPE;

namespace Ch.Cyberduck.Core.CredentialManager
{
    public static class WinCredentialManager
    {
        private static readonly Logger log = LogManager.getLogger(typeof(WinCredentialManager).AssemblyQualifiedName);

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
        public static unsafe WindowsCredentialManagerCredential GetCredentials(string target)
        {
            using CredHandle handle = new();
            if (!CredRead(target, CRED_TYPE_GENERIC, 0, out handle.Handle))
            {
                if (log.isDebugEnabled())
                {
                    var message = Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error());
                    log.debug($"Cannot get credential for \"{target}\": {message.Message}");
                }
                return default;
            }

            ref CREDENTIALW cred = ref handle.Value;
            var type = cred.Type;
            var flags = cred.Flags;
            var persist = cred.Persist;
            var username = cred.UserName.ToString();
            string password = default;
            if (cred.CredentialBlobSize > 0)
            {
                password = new string((sbyte*)cred.CredentialBlob, 0, (int)cred.CredentialBlobSize, Encoding.Unicode);
            }
            var attributes = new Dictionary<string, string>();

            var result = new WindowsCredentialManagerCredential(username, password, type, flags, persist);
            if (cred.AttributeCount > 0)
            {
                uint count = cred.AttributeCount;

                Dictionary<string, List<(int, nint, int)>> groups = new();
                for (int i = 0; i < count; i++)
                {
                    ref var attribute = ref cred.Attributes[i];
                    var keyword = attribute.Keyword.ToString();
                    var separator = keyword.IndexOf(':');
                    int index = 0;
                    if (separator != -1)
                    {
                        index = int.Parse(keyword.Substring(separator + 1));
                        keyword = keyword.Substring(0, separator);
                    }
                    if (!groups.TryGetValue(keyword, out var list))
                    {
                        groups[keyword] = list = new();
                    }
                    list.Add((index, (nint)attribute.Value, (int)attribute.ValueSize));
                }

                foreach (var group in groups)
                {
                    using var memory = MemoryPool<byte>.Shared.Rent(group.Value.Count * 256);
                    Span<byte> buffer = default;

                    // auto concatenate paged-objects to full string
                    foreach (var item in group.Value.OrderBy(x => x.Item1))
                    {
                        var source = buffer.Length;
                        buffer = memory.Memory.Span.Slice(0, buffer.Length + item.Item3);
                        Span<byte> blob = new((void*)item.Item2, item.Item3);
                        blob.CopyTo(buffer.Slice(source));
                    }
                    result.Attributes[group.Key] = new((sbyte*)AsPointer(ref GetReference(buffer)), 0, buffer.Length, Encoding.Unicode);
                }
            }
            return result;
        }

        public static bool RemoveCredentials(string target)
        {
            if (!CredDelete(target, (uint)CRED_TYPE_GENERIC, 0))
            {
                var message = Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error());
                log.error($"Could not remove credentials \"{target}\": {message.Message}");
                return false;
            }
            return true;
        }

        public static bool SaveCredentials(string target, NetworkCredential credential) => SaveCredentials(target, new WindowsCredentialManagerCredential(
            credential.UserName,
            credential.Password,
            CRED_TYPE_GENERIC, 0, CRED_PERSIST_ENTERPRISE));

        public static unsafe bool SaveCredentials(string target, WindowsCredentialManagerCredential credential)
        {
            var cred = new CREDENTIALW
            {
                TargetName = target,
                UserName = credential.UserName,
                Type = credential.Type,
                Flags = credential.Flags,
                Persist = credential.Persist,
            };

            if (!string.IsNullOrWhiteSpace(credential.Password))
            {
                PCWSTR wstr = credential.Password;
                cred.CredentialBlobSize = (uint)(credential.Password.Length * 2);
                cred.CredentialBlob = (byte*)wstr.Value;
            }

            using var attributes = MemoryPool<CREDENTIAL_ATTRIBUTEW>.Shared.Rent(64); // cannot be larger than this.
            var index = 0;
            foreach (var item in credential.Attributes)
            {
                if (string.IsNullOrWhiteSpace(item.Value))
                {
                    continue;
                }

                PCWSTR wstr = item.Value;
                var byteLen = item.Value.Length * 2;
                string formatString = byteLen switch
                {
                    > 256 => "{0}:{1}",
                    _ => "{0}"
                };

                for (int i = 0, innerIndex = 0; i < byteLen; i += 256, innerIndex++)
                {
                    var length = Math.Min(256, byteLen - i);
                    var key = string.Format(formatString, item.Key, innerIndex);
                    attributes.Memory.Span[index] = new CREDENTIAL_ATTRIBUTEW()
                    {
                        Keyword = key,
                        ValueSize = (uint)length,
                        Value = ((byte*)wstr.Value) + i
                    };
                    index += 1;
                }
            }
            cred.AttributeCount = (uint)index;
            cred.Attributes = (CREDENTIAL_ATTRIBUTEW*)AsPointer(ref GetReference(attributes.Memory.Span));

            if (!CredWrite(&cred, 0))
            {
                var message = Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error());
                log.error($"Failed saving credentials for \"{target}\": {message.Message}");
                return false;
            }
            return true;
        }
    }

    public record struct WindowsCredentialManagerCredential(
        string UserName,
        string Password,
        CRED_TYPE Type,
        CRED_FLAGS Flags,
        CRED_PERSIST Persist)
    {
        public Dictionary<string, string> Attributes { get; } = new();
    }
}
