// Copyright (c) 2010-2025 iterate GmbH. All rights reserved.
// https://cyberduck.io/
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

using System;
using System.Globalization;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.local;
using Microsoft.Win32.SafeHandles;
using org.apache.logging.log4j;
using Windows.Win32;
using Windows.Win32.Storage.FileSystem;
using Windows.Win32.UI.Shell;
using CoreLocal = ch.cyberduck.core.Local;
using NetPath = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    public class NTFSFilesystemBookmarkResolver(CoreLocal local) : FilesystemBookmarkResolver
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(NTFSFilesystemBookmarkResolver).FullName);

        public string create(CoreLocal file) => FilesystemBookmarkResolver.__DefaultMethods.create(this, file);

        public string create(CoreLocal file, bool prompt)
        {
            Span<char> finalNameBuffer = new char[CorePInvoke.PATHCCH_MAX_CCH];
            if (CorePInvoke.PathCchCanonicalizeEx(
                ref finalNameBuffer,
                NetPath.GetFullPath(file.getAbsolute()),
                PATHCCH_OPTIONS.PATHCCH_ALLOW_LONG_PATHS | PATHCCH_OPTIONS.PATHCCH_FORCE_ENABLE_LONG_NAME_PROCESS) is
                {
                    Failed: true,
                    Value: { } error
                })
            {
                goto error;
            }

            FILE_ID_INFO info;
            using (var handle = CorePInvoke.CreateFile(
                lpFileName: finalNameBuffer,
                dwDesiredAccess: 0,
                dwShareMode: (FILE_SHARE_MODE)7,
                lpSecurityAttributes: null,
                dwCreationDisposition: FILE_CREATION_DISPOSITION.OPEN_EXISTING,
                dwFlagsAndAttributes: FILE_FLAGS_AND_ATTRIBUTES.FILE_FLAG_BACKUP_SEMANTICS,
                hTemplateFile: null))
            {
                if (handle.IsInvalid)
                {
                    goto error;
                }

                if (!CorePInvoke.GetFileInformationByHandleEx(handle, FILE_INFO_BY_HANDLE_CLASS.FileIdInfo, out info))
                {
                    goto error;
                }
            }

            return Unsafe.As<FILE_ID_128, long>(ref info.FileId).ToString("X16");

        error:
            return null;
        }

        public object resolve(string bookmark)
        {
            if (!ToFileId(bookmark, out var fileId))
            {
                throw new LocalAccessDeniedException(bookmark);
            }

            SafeFileHandle fileHandle = null;
            try
            {
                SafeFileHandle rootHandle = null;
                try
                {
                    if (!TryFindRoot(local, out rootHandle))
                    {
                        throw new LocalAccessDeniedException($"Cannot find root for \"{local}\"");
                    }

                    FILE_ID_DESCRIPTOR fileDescriptor = new()
                    {
                        dwSize = (uint)Marshal.SizeOf<FILE_ID_DESCRIPTOR>(),
                        Type = FILE_ID_TYPE.FileIdType,
                        Anonymous =
                        {
                            FileId = fileId
                        }
                    };

                    fileHandle = CorePInvoke.OpenFileById(
                        hVolumeHint: rootHandle,
                        lpFileId: fileDescriptor,
                        dwDesiredAccess: 0,
                        dwShareMode: (FILE_SHARE_MODE)7,
                        lpSecurityAttributes: null,
                        dwFlagsAndAttributes: FILE_FLAGS_AND_ATTRIBUTES.FILE_FLAG_BACKUP_SEMANTICS);
                    if (fileHandle.IsInvalid)
                    {
                        var errorCode = Marshal.GetHRForLastWin32Error();
                        Log.warn(
                            $"Opening file {local.getAbsolute()} with id {bookmark} ({errorCode:X8})");
                        throw new LocalAccessDeniedException(bookmark);
                    }
                }
                finally
                {
                    rootHandle?.Dispose();
                }

                // Allocate enough space to store 32768-wchars.
                Span<char> finalNameBuffer = new char[32 * 1024 + 1];
                var length = CorePInvoke.GetFinalPathNameByHandle(
                    hFile: fileHandle,
                    lpszFilePath: finalNameBuffer,
                    dwFlags: GETFINALPATHNAMEBYHANDLE_FLAGS.VOLUME_NAME_DOS |
                             GETFINALPATHNAMEBYHANDLE_FLAGS.FILE_NAME_NORMALIZED);
                if (length == 0)
                {
                    var errorCode = Marshal.GetHRForLastWin32Error();
                    Log.warn(
                        $"Get final path name for {fileId} originally {local.getAbsolute()} ({errorCode:X8})");
                    throw new LocalAccessDeniedException(bookmark);
                }

                /*
                 * OpenJDK 8 and .NET 8 are implicitely long-path aware,
                 * thus we don't need to carry the long path-prefix,
                 * which for OpenJDK means long-path prefixed paths fail.
                 */
                if (CorePInvoke.PathCchStripPrefix(ref finalNameBuffer, length) is
                    {
                        Failed: true, /* PathCchStripPrefix is Success (S_OK (0), S_FALSE(1)) or Failed (HRESULT, <0) */
                        Value: { } stripPrefixError
                    })
                {
                    var errorCode = Marshal.GetHRForLastWin32Error();
                    Log.warn(
#if NETCOREAPP
                        $"Path Strip Prefix \"{finalNameBuffer}\" ({errorCode:X8})");
#else
                        $"Path Strip Prefix \"{finalNameBuffer.ToString()}\" ({errorCode:X8})");
#endif
                    throw new LocalAccessDeniedException(bookmark);
                }

                return LocalFactory.get(finalNameBuffer.ToString()).setBookmark(bookmark);
            }
            finally
            {
                fileHandle?.Dispose();
            }
        }

        public static bool ToFileId(string bookmark, out long fileId)
        {
            long fileIdResult = 0;
            try
            {
                return bookmark?.Length == 16 &&
                       long.TryParse(bookmark, NumberStyles.HexNumber, null, out fileIdResult);
            }
            finally
            {
                fileId = fileIdResult;
            }
        }

        private static bool TryFindRoot(CoreLocal local, out SafeFileHandle handle)
        {
            while (!local.isRoot())
            {
                local = local.getParent();
                SafeFileHandle result = null;
                try
                {
                    result = CorePInvoke.CreateFile(
                        lpFileName: local.getAbsolute(),
                        dwDesiredAccess: 0,
                        dwShareMode: (FILE_SHARE_MODE)7,
                        lpSecurityAttributes: null, dwCreationDisposition: FILE_CREATION_DISPOSITION.OPEN_EXISTING,
                        dwFlagsAndAttributes: FILE_FLAGS_AND_ATTRIBUTES.FILE_FLAG_BACKUP_SEMANTICS,
                        hTemplateFile: null);
                    if (result.IsInvalid)
                    {
                        continue;
                    }

                    handle = result;
                    result = null;
                    return true;
                }
                finally
                {
                    result?.Dispose();
                }
            }

            handle = null;
            return false;
        }
    }
}
