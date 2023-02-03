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
using CoreLocal = ch.cyberduck.core.Local;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    public class SystemLocal : CoreLocal
    {
        private static readonly char[] INVALID_CHARS = Path.GetInvalidFileNameChars();
        private static readonly Logger Log = LogManager.getLogger(typeof(SystemLocal).FullName);
        private static readonly char[] PATH_SEPARATORS = new[] { Path.AltDirectorySeparatorChar, Path.DirectorySeparatorChar };

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
                return "";
            }
            using StringWriter writer = new();

            var namespan = name.AsSpan();
            int? leadingSeparators = 0;
            bool hasUnc = false, nextDriveLetter = true;
            for (int lastSegment = 0, index = 0; index != -1; lastSegment = index + 1)
            {
                index = name.IndexOfAny(PATH_SEPARATORS, lastSegment);
                ReadOnlySpan<char> segment = (index switch
                {
                    -1 => namespan.Slice(lastSegment),
                    _ => namespan.Slice(lastSegment, index - lastSegment)
                }).Trim();

                if (segment.IsEmpty && leadingSeparators is int lead)
                {
                    // handles up to first two leading separators, that is "\" and "\\"
                    leadingSeparators = ++lead;
                    if (lead == 2)
                    {
                        // in the case this is "\\" continue with assuming UNC
                        // thus a drive letter _must not_ follow
                        hasUnc = true;
                        nextDriveLetter = false;
                        leadingSeparators = null;
                        writer.Write(Path.DirectorySeparatorChar);
                        writer.Write(Path.DirectorySeparatorChar);
                    }
                    // hereafter (after "\\" has been read) every empty segment is skipped "\\\" -> "\"
                    // or after anything except "\\" has been read, every empty segment is skipped, "\\" -> "\"
                }
                else if (!segment.IsEmpty)
                {
                    var firstChanceDriveLetter = nextDriveLetter;
                    nextDriveLetter = false;
                    if (hasUnc)
                    {
                        // ignore UNC, whatever is in as first value segment is passed-through as is
                        // there is no need to validate hostnames here, would bail out somewhere else
                        // handles all cases of "\\*\"
                        // including, but not limited to: wsl$, wsl.localhost, \\?\ (MAX_PATH bypass), any network share
                        Append(segment, writer);
                        nextDriveLetter = segment.Length == 1 && (segment[0] == '?' || segment[0] == '.');
                    }
                    else if (firstChanceDriveLetter && segment.Length == 2 && segment[1] == Path.VolumeSeparatorChar)
                    {
                        // _only_ if there is a two-letter segment, that is ending in ':' (VolumeSeparatorChar)
                        // is this thing here run.
                        // If there is _anything_ wrong (that is not "[A-Z]:") return empty value

                        /// <see cref="System.Char.IsLetter(char)" />
                        var letter = (segment[0] | 0x20) - 'a';
                        if (letter < 0 || letter > 25)
                        {
                            // letter is not in range A to Z.
                            return "";
                        }
                        // check above is simplified only, this passes raw input through
                        // check is 'a' but segment is 'A:', then 'A:' is written to output
                        Append(segment, writer);
                        // additionally, this strips away all leading separator characters before the drive letter
                        // "/C:" becomes "C:".
                    }
                    else
                    {
                        if (leadingSeparators > 0)
                        {
                            // workaround.
                            // there may be input that is leading with one separator, but contains no
                            writer.Write(Path.DirectorySeparatorChar);
                        }

                        // pass through segment sanitized from path invalid characters
                        foreach (ref readonly var c in segment)
                        {
                            writer.Write(Array.IndexOf(INVALID_CHARS, c) switch
                            {
                                -1 => c,
                                _ => '_'
                            });
                        }
                    }
                    hasUnc = false;
                    leadingSeparators = null;

                    if (index != -1)
                    {
                        // allow for input of "C:\Abc" and "C:\Abc\", preserve trailing separators, where
                        // (1) return "C:\Abc"
                        // (2) return "C:\Abc\"
                        writer.Write(Path.DirectorySeparatorChar);
                    }
                }
            }
            return writer.ToString();

            static void Append(in ReadOnlySpan<char> range, StringWriter writer)
            {
                // skip any allocation of strings or arrays.
                foreach (ref readonly var c in range)
                {
                    writer.Write(c);
                }
            }
        }
    }
}
