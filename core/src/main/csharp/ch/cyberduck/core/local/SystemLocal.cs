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
using System.IO;
using System.Text;
using ch.cyberduck.core;
using org.apache.commons.io;
using org.apache.log4j;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    public class SystemLocal : ch.cyberduck.core.Local
    {
        private static readonly Logger Log = Logger.getLogger(typeof(SystemLocal).FullName);

        public SystemLocal(string parent, string name)
            : base(parent, MakeValidFilename(name))
        {
        }

        public SystemLocal(ch.cyberduck.core.Local parent, string name)
            : base(parent, MakeValidFilename(name))
        {
        }

        public SystemLocal(string path)
            : base(
                Path.Combine(FilenameUtils.getPrefix(path), MakeValidPath(FilenameUtils.getPath(path))) +
                MakeValidFilename(FilenameUtils.getName(path)))
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
            Log.warn(path + " is a non-existing file");
            return false;
        }

        public override bool isSymbolicLink()
        {
            return false;
        }

        private static string MakeValidPath(string path)
        {
            if (Utils.IsNotBlank(path))
            {
                path = FilenameUtils.separatorsToSystem(path);
                string prefix = FilenameUtils.getPrefix(path);
                if (!path.EndsWith(Path.DirectorySeparatorChar.ToString()))
                {
                    path = path + Path.DirectorySeparatorChar;
                }
                path = FilenameUtils.getPath(path);

                StringBuilder sb = new StringBuilder();
                if (Utils.IsNotBlank(prefix))
                {
                    sb.Append(prefix);
                }
                path = FilenameUtils.separatorsToSystem(path);
                string[] parts = path.Split(Path.DirectorySeparatorChar);
                foreach (string part in parts)
                {
                    string cleanpart = part;
                    foreach (char c in Path.GetInvalidFileNameChars())
                    {
                        cleanpart = cleanpart.Replace(c.ToString(), URIEncoder.encode(c.ToString())).Trim();
                    }
                    sb.Append(cleanpart);
                    if (!parts[parts.Length - 1].Equals(part))
                    {
                        sb.Append(Path.DirectorySeparatorChar);
                    }
                }
                return sb.ToString();
            }
            return path;
        }

        private static string MakeValidFilename(string name)
        {
            if (Utils.IsNotBlank(name))
            {
                foreach (char c in Path.GetInvalidFileNameChars())
                {
                    name = name.Replace(c.ToString(), "_");
                }
            }
            return name.Trim();
        }
    }
}