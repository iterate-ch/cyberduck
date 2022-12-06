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

using System;
using System.IO;
using System.Text;
using ch.cyberduck.core;
using java.nio.file;
using org.apache.commons.io;
using org.apache.commons.lang3;
using org.apache.logging.log4j;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    public class SystemLocal : ch.cyberduck.core.Local
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(SystemLocal).FullName);

        public SystemLocal(string parent, string name)
            : base(parent, name)
        {
        }

        public SystemLocal(ch.cyberduck.core.Local parent, string name)
            : base(parent, name)
        {
        }

        public SystemLocal(string path)
            : base(Sanitize(path))
        {
        }

        public override char getDelimiter()
        {
            return '\\';
        }

        public override bool isRoot()
        {
            return getAbsolute().Equals(Directory.GetDirectoryRoot(getAbsolute()));
        }

        public override String getAbbreviatedPath()
        {
            return getAbsolute();
        }

        public override bool exists()
        {
            string path = getAbsolute();
            if (File.Exists(path))
            {
                return true;
            }
            bool directory = Directory.Exists(path);
            if (directory)
            {
                return true;
            }
            return false;
        }

        public override bool isSymbolicLink()
        {
            return false;
        }

        private static string Sanitize(string name)
        {
            if (name is null)
            {
                return null;
            }
            using StringWriter writer = new();
            using StringReader reader = new(name);

            char c = default;
            if (reader.Read() is int p && p == -1)
            {
                return null;
            }
            c = (char)p;

            if (c == Path.DirectorySeparatorChar || c == Path.AltDirectorySeparatorChar)
            {
                if (reader.Peek() == -1)
                {
                    // this is "\" or "/", not supported.
                    return null;
                }
                writer.Write(Path.DirectorySeparatorChar);
                c = (char)reader.Read();
                if (c == Path.DirectorySeparatorChar || c == Path.AltDirectorySeparatorChar)
                {
                    using StringWriter hostBuffer = new();
                    // UNC-path continue.
                    while (reader.Peek() != -1)
                    {
                        c = (char)reader.Peek();
                        if (c == Path.DirectorySeparatorChar || c == Path.AltDirectorySeparatorChar)
                        {
                            break;
                        }
                        hostBuffer.Write((char)reader.Read());
                    }
                    if (hostBuffer.GetStringBuilder().Length == 0)
                    {
                        // what is this? "\\\"
                        return null;
                    }
                    writer.Write(Path.DirectorySeparatorChar);
                    // allow everything. This _may_ be bad, _but_ Local uses `Paths.get()` thus we don't need to do any more sanitization here.
                    writer.Write(hostBuffer.ToString());
                }
                else
                {
                    // this is something different than "\\" and "//", ignore as not supported
                    return null;
                }
            }
            else
            {
                /// <see cref="System.Char.IsLetter(char)" />
                var letter = (c | 0x20) - 'a';
                if (letter < 0 || letter > 25)
                {
                    // letter is not in range A to Z.
                    return null;
                }
                // change lowercase letter to uppercase
                writer.Write((char)(c & ~0x20));
                if (reader.Peek() != Path.VolumeSeparatorChar)
                {
                    // this is something like C\, CX, C/, 
                    return null;
                }
                writer.Write((char)reader.Read());
                // at X:, following is only a path, thus pass on to shared implementation
            }
            SanitizePath(reader, writer);

            return writer.ToString();

            static void ApplyRoot(StringReader reader, StringWriter writer)
            {
                if (reader.Peek() is int p && (p == Path.DirectorySeparatorChar || p == Path.AltDirectorySeparatorChar))
                {
                    reader.Read();
                }
                writer.Write(Path.DirectorySeparatorChar);
            }

            static void SanitizePath(StringReader reader, StringWriter writer)
            {
                var invalid = Path.GetInvalidFileNameChars();
                ApplyRoot(reader, writer);
                StringBuilder segmentBuilder = new();
                while (reader.Peek() != -1)
                {
                    char c = (char)reader.Read();
                    if (c == Path.AltDirectorySeparatorChar || c == Path.DirectorySeparatorChar)
                    {
                        if (segmentBuilder.Length > 0)
                        {
                            writer.Write(segmentBuilder.ToString().Trim());
                            writer.Write(Path.DirectorySeparatorChar);
                            segmentBuilder.Clear();
                        }
                    }
                    else if (Array.IndexOf(invalid, c) != -1)
                    {
                        segmentBuilder.Append('_');
                    }
                    else
                    {
                        segmentBuilder.Append(c);
                    }
                }
                if (segmentBuilder.Length != 0)
                {
                    writer.Write(segmentBuilder.ToString().Trim());
                }
            }
        }
    }
}
