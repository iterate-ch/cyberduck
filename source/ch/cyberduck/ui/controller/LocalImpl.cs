//
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 
using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using java.util;
using org.apache.commons.io;
using org.apache.log4j;
using File = java.io.File;
using Locale = ch.cyberduck.core.i18n.Locale;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class LocalImpl : Local
    {
        protected const int ErrorAccessDenied = 5;
        protected const int ErrorFileNotFound = 2;
        private static readonly Logger Log = Logger.getLogger(typeof (LocalImpl).Name);

        public LocalImpl(string parent, string name) : base(parent, name)
        {
            ;
        }

        public LocalImpl(Local parent, string name) : base(parent, name)
        {
            ;
        }

        public LocalImpl(string path) : base(path)
        {
            ;
        }

        public LocalImpl(File path) : base(path)
        {
            ;
        }

        public override string getDefaultApplication()
        {
            //Todo. Move here.
            return Utils.GetRegisteredDefaultApplication(getAbsolute());
        }

        public override char getPathDelimiter()
        {
            return '\\';
        }

        public override bool isRoot()
        {
            return getAbsolute().Equals(Directory.GetDirectoryRoot(getAbsolute()));
        }

        public override List getDefaultApplications()
        {
            //Todo. Return #getDefaultApplication() as List.
            return null;
        }

        public override bool exists()
        {
            if(this.attributes().isDirectory())
            {
                return System.IO.Directory.Exists(getAbsolute());
            }
            return System.IO.File.Exists(getAbsolute());
        }

        public override void writeUnixPermission(Permission p, bool b)
        {
            ;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns>True if application was found to open the file with</returns>
        public override bool open()
        {
            if (Log.isDebugEnabled())
            {
                Log.debug("open():" + getAbsolute());
            }
            return Utils.StartProcess(getAbsolute());
        }

        public override void bounce()
        {
            ;
        }

        public static string kind(string extension)
        {
            Shell32.SHFILEINFO shinfo = new Shell32.SHFILEINFO();
            IntPtr i = Shell32.SHGetFileInfo(extension, 0, ref shinfo, (uint) Marshal.SizeOf(shinfo),
                                             Shell32.SHGFI_TYPENAME | Shell32.SHGFI_USEFILEATTRIBUTES);
            return Convert.ToString(shinfo.szTypeName.Trim());
        }

        public override string kind()
        {
            // Native file type mapping
            String kind = LocalImpl.kind(getExtension());
            if (string.IsNullOrEmpty(kind))
            {
                return base.kind();
            }
            return kind;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="progress">An integer from -1 and 9. If -1 is passed, the icon should be removed.</param>
        public override void setIcon(int progress)
        {
            ;
        }

        public override void delete()
        {
            delete(false);
        }

        public override void trash()
        {
            throw new InvalidOperationException();
            //http://social.msdn.microsoft.com/forums/en-US/netfxbcl/thread/f2411a7f-34b6-4f30-a25f-9d456fe1c47b/
            //http://stackoverflow.com/questions/222463/is-it-possible-with-java-to-delete-to-the-recycle-bin
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="originUrl">The URL of the resource originally hosting the quarantined item, from the user's point of
        ///                view. For web downloads, this property is the URL of the web page on which the user initiated
        ///                the download. For attachments, this property is the URL of the resource to which the quarantined
        ///                item was attached (e.g. the email message, calendar event, etc.). The origin URL may be a file URL
        ///                for local resources, or a custom URL to which the quarantining application will respond when asked
        ///                to open it. The quarantining application should respond by displaying the resource to the user.
        ///                Note: The origin URL should not be set to the data URL, or the quarantining application may start
        ///                downloading the file again if the user choses to view the origin URL while resolving a quarantine
        ///                warning.</param>
        /// <param name="dataUrl">The URL from which the data for the quarantined item data was
        ///                  actaully streamed or downloaded, if available</param>
        public override void setQuarantine(string originUrl, string dataUrl)
        {
            ;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="dataUrl">Href where the file was downloaded from</param>
        public override void setWhereFrom(string dataUrl)
        {
            ;
        }

        public override void setPath(string parent, string name)
        {
            string p = MakeValidPath(parent);
            string n = MakeValidFilename(name);
            base.setPath(p, n);
        }

        public override void setPath(string filename)
        {
            string parent = Path.Combine(FilenameUtils.getPrefix(filename),
                                         MakeValidPath(FilenameUtils.getPath(filename)));
            string name = MakeValidFilename(FilenameUtils.getName(filename));
            base.setPath(parent, name);
        }

        private string MakeValidPath(string path)
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
                        cleanpart = cleanpart.Replace(c.ToString(), "_");
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

        private string MakeValidFilename(string name)
        {
            if (Utils.IsNotBlank(name))
            {
                foreach (char c in Path.GetInvalidFileNameChars())
                {
                    name = name.Replace(c.ToString(), "_");
                }
            }
            return name;
        }

        public static void Register()
        {
            LocalFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : LocalFactory
        {
            protected override Local create(Local parent, string name)
            {
                return new LocalImpl(parent, name);
            }

            protected override Local create(string parent, string name)
            {
                return new LocalImpl(parent, name);
            }

            protected override Local create(string path)
            {
                return new LocalImpl(path);
            }

            protected override Local create(File path)
            {
                return new LocalImpl(path);
            }

            protected override object create()
            {
                return new LocalImpl(Environment.GetEnvironmentVariable("HOME"));
            }
        }
    }
}