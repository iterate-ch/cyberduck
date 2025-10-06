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
using System.Runtime.InteropServices;
using Windows.Win32;
using Windows.Win32.Storage.FileSystem;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.local;
using Microsoft.Win32.SafeHandles;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Core.Local
{
    public class NTFSFilesystemBookmarkResolver(ch.cyberduck.core.Local root) : FilesystemBookmarkResolver
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(NTFSFilesystemBookmarkResolver).FullName);

        public string create(ch.cyberduck.core.Local file) => FilesystemBookmarkResolver.__DefaultMethods.create(this, file);

        public string create(ch.cyberduck.core.Local file, bool prompt) => null;

        public object resolve(string bookmark)
        {
            if (!ToFileId(bookmark, out var fileId))
            {
                throw new LocalAccessDeniedException(bookmark);
            }

            var rootPath = root.getAbsolute();
            SafeFileHandle fileHandle = null;
            try
            {
                using (var rootHandle = CorePInvoke.CreateFile(
                           lpFileName: rootPath,
                           dwDesiredAccess: 0,
                           dwShareMode: (FILE_SHARE_MODE)7,
                           lpSecurityAttributes: null,
                           dwCreationDisposition: FILE_CREATION_DISPOSITION.OPEN_EXISTING,
                           dwFlagsAndAttributes: FILE_FLAGS_AND_ATTRIBUTES.FILE_FLAG_BACKUP_SEMANTICS,
                           hTemplateFile: null))
                {
                    if (rootHandle.IsInvalid)
                    {
                        if (Log.isDebugEnabled())
                        {
                            var errorCode = Marshal.GetHRForLastWin32Error();
                            Log.debug(
                                $"Opening root {rootPath} error ({errorCode:X8})");
                        }

                        throw new LocalAccessDeniedException(bookmark);
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
                            $"Opening file id {bookmark} on {rootPath} ({errorCode:X8})");
                        throw new LocalAccessDeniedException(bookmark);
                    }
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
                        $"Get final path name for {fileId} in {rootPath} ({errorCode:X8})");
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
                        $"Path Strip Prefix \"{finalNameBuffer}\" ({errorCode:X8})");
                    throw new LocalAccessDeniedException(bookmark);
                }

                var finalName = LocalFactory.get(finalNameBuffer.ToString());
                if (!finalName.equals(root) && !finalName.isChild(root))
                {
                    Log.warn($"Mismatched root: \"{finalNameBuffer}\", expected \"{rootPath}\"");
                    throw new LocalAccessDeniedException(bookmark);
                }

                return finalName.setBookmark(bookmark);
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
    }
}
