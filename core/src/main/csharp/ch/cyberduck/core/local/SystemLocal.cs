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

using ch.cyberduck.core;
using org.apache.logging.log4j;
using System;
using System.IO;
using System.Text;
using CoreLocal = ch.cyberduck.core.Local;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    public class SystemLocal : CoreLocal
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(SystemLocal).FullName);

        public SystemLocal(string parent, string name)
            : base(parent, name)
        {
        }

        public SystemLocal(CoreLocal parent, string name)
            : base(parent, name)
        {
        }

        public SystemLocal(string path)
            : base(Sanitize(path))
        {
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

        private static string Sanitize(string name)
        {
            if (string.IsNullOrWhiteSpace(name))
            {
                return null;
            }
            using StringWriter writer = new();
            using StringReader reader = new(name);

            if (reader.Peek() is int p && (p == Path.DirectorySeparatorChar || p == Path.AltDirectorySeparatorChar))
            {
                if (!ReadUnc(reader, writer))
                {
                    return null;
                }
            }
            else if (!ReadDriveLetter(reader, writer, false))
            {
                return null;
            }

            SanitizePath(reader, writer);

            return writer.ToString();

            static bool ReadUnc(StringReader reader, StringWriter writer)
            {
                // test for "\" or "/"
                var p = reader.Read();
                if (p == -1 || !(p == Path.AltDirectorySeparatorChar || p == Path.DirectorySeparatorChar))
                {
                    return false;
                }
                // test for "//" or "\\"
                p = reader.Read();
                if (p == -1 || !(p == Path.AltDirectorySeparatorChar || p == Path.DirectorySeparatorChar))
                {
                    return false;
                }
                writer.Write(Path.DirectorySeparatorChar);

                using StringWriter hostBuffer = new();
                bool trail = false;
                // UNC-path continue.
                while (reader.Read() is int c && c != -1)
                {
                    if (c == Path.DirectorySeparatorChar || c == Path.AltDirectorySeparatorChar)
                    {
                        trail = true;
                        break;
                    }
                    hostBuffer.Write((char)c);
                }
                if (hostBuffer.GetStringBuilder().Length == 0)
                {
                    // what is this? "\\\"
                    return false;
                }
                writer.Write(Path.DirectorySeparatorChar);
                var host = hostBuffer.ToString();
                writer.Write(host);
                if (trail)
                {
                    writer.Write(Path.DirectorySeparatorChar);
                }
                if (host == "." || host == "?")
                {
                    ReadDriveLetter(reader, writer, true);
                }
                return true;
            }

            static bool ReadDriveLetter(StringReader reader, StringWriter writer, bool readToSeparator)
            {
                using StringWriter buffer = new();

                while (reader.Read() is int p && p > 0)
                {
                    if (p == Path.DirectorySeparatorChar || p == Path.AltDirectorySeparatorChar)
                    {
                        break;
                    }
                    buffer.Write((char)p);
                }
                using (StringReader component = new(buffer.ToString()))
                using (StringWriter driveLetter = new())
                {
                    if (HandleDriveLetter(component, driveLetter))
                    {
                        writer.Write(driveLetter.ToString());
                        return true;
                    }
                }
                if (!readToSeparator)
                {
                    return false;
                }
                writer.Write(buffer.ToString());
                return true;

                static bool HandleDriveLetter(StringReader reader, StringWriter writer)
                {
                    var c = reader.Read();
                    /// <see cref="System.Char.IsLetter(char)" />
                    var letter = (c | 0x20) - 'a';
                    if (letter < 0 || letter > 25)
                    {
                        // letter is not in range A to Z.
                        return false;
                    }
                    // change lowercase letter to uppercase
                    writer.Write((char)c);
                    if (reader.Peek() != Path.VolumeSeparatorChar)
                    {
                        // this is something like C\, CX, C/,
                        return false;
                    }
                    writer.Write((char)reader.Read());
                    return true;
                }
            }

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
