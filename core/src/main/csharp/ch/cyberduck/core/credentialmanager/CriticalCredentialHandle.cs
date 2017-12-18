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
using Microsoft.Win32.SafeHandles;

namespace Ch.Cyberduck.Core.CredentialManager
{
    sealed class CriticalCredentialHandle : CriticalHandleZeroOrMinusOneIsInvalid
    {
        // Set the handle.
        internal CriticalCredentialHandle(IntPtr preexistingHandle)
        {
            SetHandle(preexistingHandle);
        }

        internal Credential GetCredential()
        {
            if (!IsInvalid)
            {
                // Get the Credential from the mem location
                NativeCode.NativeCredential ncred = (NativeCode.NativeCredential) Marshal.PtrToStructure(handle,
                    typeof(NativeCode.NativeCredential));

                // Create a managed Credential type and fill it with data from the native counterpart.
                Credential cred = new Credential(ncred);

                return cred;
            }
            else
            {
                throw new InvalidOperationException("Invalid CriticalHandle!");
            }
        }

        // Perform any specific actions to release the handle in the ReleaseHandle method.
        // Often, you need to use Pinvoke to make a call into the Win32 API to release the 
        // handle. In this case, however, we can use the Marshal class to release the unmanaged memory.

        override protected bool ReleaseHandle()
        {
            // If the handle was set, free it. Return success.
            if (!IsInvalid)
            {
                // NOTE: We should also ZERO out the memory allocated to the handle, before free'ing it
                // so there are no traces of the sensitive data left in memory.
                NativeCode.CredFree(handle);
                // Mark the handle as invalid for future users.
                SetHandleAsInvalid();
                return true;
            }
            // Return false. 
            return false;
        }
    }
}
