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
using System.IO;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using java.io;
using org.apache.logging.log4j;
using static Ch.Cyberduck.Core.Local.PlatformLocalSupport;
using CoreLocal = ch.cyberduck.core.Local;
using File = System.IO.File;
using Path = System.IO.Path;
using StringWriter = System.IO.StringWriter;

namespace Ch.Cyberduck.Core.Local
{
    public class SystemLocal : CoreLocal
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(SystemLocal).FullName);

        private static readonly char[] INVALID_CHARS = Path.GetInvalidFileNameChars();
        private static readonly char[] PATH_SEPARATORS = new[]
            { Path.AltDirectorySeparatorChar, Path.DirectorySeparatorChar };

        public SystemLocal(string parent, string name)
            : this(
#if NETCOREAPP
                   Path.Join(parent, name)
#else
                   Join(parent, name)
#endif
            )
        {
        }

        public SystemLocal(CoreLocal parent, string name)
            : this(parent.getAbsolute(), name)
        {
        }

        public SystemLocal(string path)
            : base(Canonicalize(path))
        {
        }

        public SystemLocal(SystemLocal copy)
            : base(copy.getAbsolute())
        {
        }

        public CoreLocal Resolve()
        {
            if (null == bookmark)
            {
                return this;
            }

            try
            {
                return (CoreLocal)new NTFSFilesystemBookmarkResolver(this).resolve(bookmark);
            }
            catch (LocalAccessDeniedException e)
            {
                Log.warn($"Failure resolving bookmark for {this}", e);
                return this;
            }
        }

        public override AttributedList list(Filter filter)
        {
            return base.list(Resolve().getAbsolute(), filter);
        }

        public override OutputStream getOutputStream(bool append)
        {
            return base.getOutputStream(Resolve().getAbsolute(), append);
        }

        public override InputStream getInputStream()
        {
            return base.getInputStream(Resolve().getAbsolute());
        }

        public override bool exists()
        {
            var resolved = Resolve();
            string path = resolved.PlatformPath();
#if NETCOREAPP
            return Path.Exists(path);
#else
            if (File.Exists(path))
            {
                return true;
            }

            if (Directory.Exists(path))
            {
                return true;
            }

            return false;
#endif
        }

        public override LocalAttributes attributes()
        {
            return new SystemLocalAttributes(this);
        }

        public override String getAbbreviatedPath()
        {
            return getAbsolute();
        }

        public override char getDelimiter()
        {
            return '\\';
        }

        public override CoreLocal getVolume() => LocalFactory.get(Path.GetPathRoot(getAbsolute()));

        public override bool isRoot() => getAbsolute().Equals(Path.GetPathRoot(getAbsolute()));

        public override bool isSymbolicLink()
        {
            return false;
        }

        private static string Canonicalize(string name)
        {
            if (string.IsNullOrWhiteSpace(name))
            {
                return "";
            }

            int start = 0;
            if (name[0] is '/')
            {
                // LocalFactory.get(Path.getAbsolute()) always retains '/' at the beginning.
                // Need a Path-Local translation, which removes this.
                // Adjust offset.
                start = 1;
            }

            var pathRoot =
#if NETCOREAPP
                PathRoot(name.AsSpan(start));
#else
                PathRoot(name.Substring(start));
#endif

            bool deviceSyntax = pathRoot.Length > 3
                && pathRoot[2] is '?' or '.'
                && IsDirectorySeparator(pathRoot[0])
                && IsDirectorySeparator(pathRoot[1])
                && IsDirectorySeparator(pathRoot[3]);
            bool deviceUnc = deviceSyntax && pathRoot.Length > 7
                && pathRoot[4] is 'U'
                && pathRoot[5] is 'N'
                && pathRoot[6] is 'C'
                && IsDirectorySeparator(pathRoot[7]);

            using StringWriter writer = new();
            var buffer = writer.GetStringBuilder();

            if (deviceUnc)
            {
                // Is \\?\UNC\X or \\.\UNC\X, write \\X
                buffer.EnsureCapacity(pathRoot.Length - 5);
                writer.Write(Path.DirectorySeparatorChar);
                // Include separator after UNC.
                WriteNormalized(pathRoot.Slice(7), writer);
            }
            else if (deviceSyntax && pathRoot[2] is '?')
            {
                // Only if we've got a real long-path (\\?\)
                // do we remove it.
                WriteNormalized(pathRoot.Slice(4), writer);
            }
            else
            {
                // Keep \\.\-prefixes (for e.g. Pipes),
                // and don't bother working out how `\\SHARE`-paths work.
                WriteNormalized(pathRoot, writer);
            }

            start += pathRoot.Length;

            // TODO: Replace following when we have ch.cyberduck.core.Path to ch.cyberduck.core.Local translation
            //       e.g. on Windows when converting a Path to Local the leading slash has to be stripped, this
            //       translation would also be responsible for cleaning up bad file and path names (blocked chars).
            var path = name.AsSpan(start);
            var skipped = pathRoot.Length > 0 && IsDirectorySeparator(pathRoot[pathRoot.Length - 1]) ? 1 : 0;
            for (int lastSegment = 0, index = 0; index != -1; lastSegment += index + 1)
            {
                var segment = path.Slice(lastSegment);
                if ((index = segment.IndexOfAny(PATH_SEPARATORS)) != -1)
                {
                    segment = segment.Slice(0, index);
                }

                if (skipped++ == 0)
                {
                    writer.Write(Path.DirectorySeparatorChar);
                }

                if (!segment.IsEmpty)
                {
                    // pass through segment sanitized from path invalid characters
                    foreach (ref readonly var c in segment)
                    {
                        writer.Write(Array.IndexOf(INVALID_CHARS, c) switch
                        {
                            -1 => c,
                            _ => '_'
                        });
                    }

                    skipped = 0;
                }
            }

            return writer.ToString();
        }

#if NETFRAMEWORK
        private static string Join(string root, string path)
        {
            if (string.IsNullOrEmpty(root))
            {
                return path;
            }

            if (string.IsNullOrEmpty(path))
            {
                return root;
            }

            // Path.Join doesn't exist in .NET Framework, need to replicate
            bool hasDirectorySeparator = IsDirectorySeparator(root[root.Length - 1]) || IsDirectorySeparator(path[0]);
            return hasDirectorySeparator
                ? string.Concat(root, path)
                : string.Concat(root, Path.DirectorySeparatorChar, path);
        }
#endif
    }
}
