﻿//
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
using Ch.Cyberduck.Core.Collections;
using java.util;
using Microsoft.Win32;
using org.apache.commons.io;
using org.apache.log4j;
using File = java.io.File;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class LocalImpl : Local
    {
        protected const int ErrorAccessDenied = 5;
        protected const int ErrorFileNotFound = 2;
        private static readonly Logger Log = Logger.getLogger(typeof (LocalImpl).FullName);

        private static readonly LRUCache<string, string> defaultApplicationCache = new LRUCache<string, string>(100);

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

        /// <summary>
        /// Return with Explorer registered application by file's extension (for editing)
        /// </summary>
        /// <param name="filename"></param>
        /// <param name="command"></param>
        /// <returns></returns>
        /// <see cref="http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html"/>
        /// <see cref="http://msdn.microsoft.com/en-us/library/cc144154%28VS.85%29.aspx"/>
        private string GetExplorerRegisteredApplication(string filename)
        {
            string command = null;
            string strExt = Utils.GetSafeExtension(filename);

            try
            {
                strExt = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\" + strExt;
                using (RegistryKey oApplication = Registry.CurrentUser.OpenSubKey(strExt))
                {
                    if (null != oApplication)
                    {
                        //for Windows XP and earlier
                        string strExe = (string) oApplication.GetValue("Application");

                        if (string.IsNullOrEmpty(strExe))
                        {
                            //for Vista and later there might be a UserChoice entry
                            using (RegistryKey userChoice = oApplication.OpenSubKey("UserChoice"))
                            {
                                if (null != userChoice)
                                {
                                    string progId = (string) userChoice.GetValue("Progid");
                                    if (!string.IsNullOrEmpty(progId))
                                    {
                                        using (RegistryKey p = Registry.ClassesRoot.OpenSubKey(progId))
                                        {
                                            command = GetEditCommand(p);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!string.IsNullOrEmpty(command))
                {
                    command = Utils.ExtractExeFromCommand(command);
                }
                return command;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Extract edit command, fallback is the open command
        /// </summary>
        /// <param name="root">expected substructure shell/edit/command or shell/open/command</param>
        /// <returns>null if not found</returns>
        private string GetEditCommand(RegistryKey root)
        {
            if (null != root)
            {
                using (var editSk = root.OpenSubKey("shell\\edit\\command"))
                {
                    if (null != editSk)
                    {
                        return (string) editSk.GetValue("");
                    }

                    using (var openSk = root.OpenSubKey("shell\\open\\command"))
                    {
                        if (null != openSk)
                        {
                            return (string) openSk.GetValue("");
                        }
                    }
                }
            }
            return null;
        }

        public override string getDefaultApplication()
        {
            //see http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html
            string strExt = Utils.GetSafeExtension(getName());
            string command;
            Log.debug(string.Format("GetRegisteredDefaultApplication for filname {0}", getName()));

            if (defaultApplicationCache.TryGetValue(strExt, out command))
            {
                Log.debug(string.Format("Return cached default application {0} for extension {1}", command, strExt));
                return command;
            }

            command = GetExplorerRegisteredApplication(getName());
            if (null != command)
            {
                defaultApplicationCache.Add(strExt, command);
                return command;
            }

            try
            {
                string strProgID;
                using (var extSubKey = Registry.ClassesRoot.OpenSubKey(strExt))
                {
                    if (null != extSubKey)
                    {
                        strProgID = (string) extSubKey.GetValue(null);

                        if (null != strProgID)
                        {
                            // Get associated application and its edit command
                            using (var oProgId = Registry.ClassesRoot.OpenSubKey(strProgID))
                            {
                                if (null != oProgId)
                                {
                                    string strExe = GetEditCommand(oProgId.OpenSubKey(strProgID));

                                    if (!string.IsNullOrEmpty(strExe))
                                    {
                                        command = Utils.ExtractExeFromCommand(strExe);
                                    }
                                }
                            }
                        }
                    }
                    defaultApplicationCache.Add(strExt, command);
                    return command;
                }
            }
            catch (Exception)
            {
                defaultApplicationCache.Add(strExt, command);
                return command;
            }
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
            return Utils.ConvertToJavaList(Utils.OpenWithListForExtension(this.getExtension()));
        }

        public override bool exists()
        {
            if (System.IO.File.Exists(getAbsolute()))
            {
                return true;
            }
            return Directory.Exists(getAbsolute());
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
            IntPtr hSuccess = Shell32.SHGetFileInfo(extension, 0, ref shinfo, (uint)Marshal.SizeOf(shinfo),
                                             Shell32.SHGFI_TYPENAME | Shell32.SHGFI_USEFILEATTRIBUTES);
            if (hSuccess != IntPtr.Zero)
            {
                return Convert.ToString(shinfo.szTypeName.Trim());
            }
            return null;
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

        private Attributes info = null;

        public override Attributes attributes()
        {
            if (null == info)
            {
                info = new FileInfoAttributes(this);
            }
            return info;
        }

        private class FileInfoAttributes : LocalAttributes
        {
            private Local file;

            public FileInfoAttributes(Local l) : base(l)
            {
                file = l;
            }
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

        /// <summary>
        /// Delete to trash is not supported yet.
        /// </summary>
        /// <see cref="http://social.msdn.microsoft.com/forums/en-US/netfxbcl/thread/f2411a7f-34b6-4f30-a25f-9d456fe1c47b/"/>
        /// <see cref="http://stackoverflow.com/questions/222463/is-it-possible-with-java-to-delete-to-the-recycle-bin"/>
        public override void trash()
        {
            delete();
        }

        public override bool reveal()
        {
            if (exists())
            {
                //select first file downloaded. We could just open the containing folder alternatively.
                return Utils.StartProcess("explorer.exe", "/select, " + getAbsolute());
            }
            return false;
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

        protected override void setPath(string parent, string name)
        {
            string p = MakeValidPath(parent);
            string n = MakeValidFilename(name);
            base.setPath(p, n);
        }

        protected override void setPath(string filename)
        {
            string parent = Path.Combine(FilenameUtils.getPrefix(filename),
                                         MakeValidPath(FilenameUtils.getPath(filename)));
            string name = MakeValidFilename(FilenameUtils.getName(filename));
            base.setPath(parent + name);
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