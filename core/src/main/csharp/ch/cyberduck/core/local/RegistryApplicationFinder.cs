// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using Ch.Cyberduck.Core.Collections;
using Ch.Cyberduck.Core.Local;
using Microsoft.Win32;
using ch.cyberduck.core.local;
using java.util;
using org.apache.commons.io;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Local
{
    public class RegistryApplicationFinder : ApplicationFinder
    {
        private static readonly Guid CLSID_QueryAssociations = new Guid("a07034fd-6caa-4954-ac3f-97a27216f98a");
        private static readonly Logger Log = Logger.getLogger(typeof (RegistryApplicationFinder).Name);

        private static readonly LRUCache<string, Application> applicationNameCache =
            new LRUCache<string, Application>(100);

        private static readonly LRUCache<string, Application> defaultApplicationCache =
            new LRUCache<string, Application>(100);

        private static readonly LRUCache<string, IList<Application>> defaultApplicationListCache =
            new LRUCache<string, IList<Application>>(100);

        private static Guid IID_IQueryAssociations = new Guid("c46ca590-3c3f-11d2-bee6-0000f805ca57");


        //vormals GetApplicationNameForExe
        public Application getDescription(string application)
        {
            if (!applicationNameCache.ContainsKey(application))
            {
                string path = WindowsApplicationLauncher.GetExecutableCommand(application);
                if (File.Exists(path))
                {
                    FileVersionInfo info = FileVersionInfo.GetVersionInfo(path);
                    applicationNameCache.Add(new KeyValuePair<string, Application>(application,
                                                                                   new Application(
                                                                                       application.ToLower(),
                                                                                       info.FileDescription)));
                }
                else
                {
                    applicationNameCache.Add(new KeyValuePair<string, Application>(application,
                                                                                   new Application(
                                                                                       application.ToLower(),
                                                                                       FilenameUtils.getName(application))));
                }
            }
            Application result;
            applicationNameCache.TryGetValue(application, out result);
            return result;
        }

        public Application find(String filename)
        {
            string extension = Utils.GetSafeExtension(filename);
            if (Utils.IsBlank(extension))
            {
                return Application.notfound;
            }
            Application app;
            Log.debug(string.Format("GetRegisteredDefaultApplication for filename {0}", filename));
            if (defaultApplicationCache.TryGetValue(extension, out app))
            {
                Log.debug(string.Format("Return cached default application {0} for extension {1}", app, extension));
                return app;
            }
            String exe = GetExplorerRegisteredApplication(extension);
            if (null != exe)
            {
                defaultApplicationCache.Add(extension, getDescription(exe));
            }
            else
            {
                try
                {
                    string strProgID;
                    using (var extSubKey = Registry.ClassesRoot.OpenSubKey(extension))
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
                                            exe = Utils.ExtractExeFromCommand(strExe);
                                        }
                                    }
                                }
                            }
                        }
                        if (null != exe)
                        {
                            defaultApplicationCache.Add(extension, getDescription(exe));
                        }
                    }
                }
                catch (Exception)
                {
                    Log.error(string.Format("Exception while finding application for {0}", filename));
                }
            }
            defaultApplicationCache.TryGetValue(extension, out app);
            if(null == app) {
                return Application.notfound;
            }
            return app;
        }

        public List findAll(String filename)
        {
            IList<String> progs = new List<string>();
            List<Application> map = new List<Application>();
            string extension = Utils.GetSafeExtension(filename);
            if (Utils.IsBlank(extension))
            {
                return Utils.ConvertToJavaList(map);
            }
            if (!defaultApplicationListCache.ContainsKey(extension))
            {
                using (RegistryKey clsExt = Registry.ClassesRoot.OpenSubKey(extension))
                {
                    IList<string> rootList = Utils.OpenWithListForExtension(extension, clsExt);
                    foreach (string s in rootList)
                    {
                        progs.Add(s);
                    }
                }
                using (
                    RegistryKey clsExt =
                        Registry.CurrentUser.OpenSubKey(
                            "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\" + extension))
                {
                    IList<string> explorerList = Utils.OpenWithListForExtension(extension, clsExt);
                    foreach (string s in explorerList)
                    {
                        progs.Add(s);
                    }
                }

                foreach (string exe in progs.Distinct())
                {
                    Application application = find(exe);
                    if (isInstalled(application))
                    {
                        map.Add(application);
                    }
                    else
                    {
                        map.Add(getDescription(exe));
                    }
                }
                map.Sort(
                    delegate(Application app1, Application app2)
                        { return app1.getIdentifier().CompareTo(app2.getIdentifier()); });
                defaultApplicationListCache.Add(extension, map);
            }
            return Utils.ConvertToJavaList(defaultApplicationListCache[extension]);
        }

        public bool isInstalled(Application application)
        {
            return Utils.IsNotBlank(application.getIdentifier()) && File.Exists(application.getIdentifier());
        }

        [DllImport("shlwapi.dll")]
        private static extern int AssocCreate(Guid clsid, ref Guid riid,
                                              [MarshalAs(UnmanagedType.Interface)] out object ppv);

        /// <summary>
        /// Return with Explorer registered application by file's extension (for editing)
        /// </summary>
        /// <param name="filename"></param>
        /// <param name="command"></param>
        /// <returns></returns>
        /// <see cref="http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html"/>
        /// <see cref="http://msdn.microsoft.com/en-us/library/cc144154%28VS.85%29.aspx"/>
        private string GetExplorerRegisteredApplication(string extension)
        {
            try
            {
                object obj;
                AssocCreate(CLSID_QueryAssociations, ref IID_IQueryAssociations, out obj);
                IQueryAssociations qa = (IQueryAssociations) obj;
                qa.Init(ASSOCF.INIT_DEFAULTTOSTAR, extension, UIntPtr.Zero, IntPtr.Zero);

                int size = 0;
                qa.GetString(ASSOCF.NOTRUNCATE, ASSOCSTR.COMMAND, "edit", null, ref size);

                StringBuilder sb = new StringBuilder(size);
                qa.GetString(ASSOCF.NOTRUNCATE, ASSOCSTR.COMMAND, "edit", sb, ref size);

                string cmd = sb.ToString();
                if (Utils.IsBlank(cmd))
                {
                    return null;
                }

                if (cmd.Contains("\""))
                {
                    return cmd.Substring(1, cmd.LastIndexOf("\""));
                }
                return cmd.Substring(0, cmd.IndexOf(" "));
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
                        return (string) editSk.GetValue(String.Empty);
                    }

                    using (var openSk = root.OpenSubKey("shell\\open\\command"))
                    {
                        if (null != openSk)
                        {
                            return (string) openSk.GetValue(String.Empty);
                        }
                    }
                }
            }
            return null;
        }

        private enum ASSOCDATA
        {
            MSIDESCRIPTOR = 1,
            NOACTIVATEHANDLER,
            QUERYCLASSSTORE,
            HASPERUSERASSOC,
            EDITFLAGS,
            VALUE
        }

        [Flags]
        private enum ASSOCF
        {
            INIT_NOREMAPCLSID = 0x00000001,
            INIT_BYEXENAME = 0x00000002,
            OPEN_BYEXENAME = 0x00000002,
            INIT_DEFAULTTOSTAR = 0x00000004,
            INIT_DEFAULTTOFOLDER = 0x00000008,
            NOUSERSETTINGS = 0x00000010,
            NOTRUNCATE = 0x00000020,
            VERIFY = 0x00000040,
            REMAPRUNDLL = 0x00000080,
            NOFIXUPS = 0x00000100,
            IGNOREBASECLASS = 0x00000200,
            INIT_IGNOREUNKNOWN = 0x00000400
        }

        private enum ASSOCKEY
        {
            SHELLEXECCLASS = 1,
            APP,
            CLASS,
            BASECLASS
        }

        private enum ASSOCSTR
        {
            COMMAND = 1,
            EXECUTABLE,
            FRIENDLYDOCNAME,
            FRIENDLYAPPNAME,
            NOOPEN,
            SHELLNEWVALUE,
            DDECOMMAND,
            DDEIFEXEC,
            DDEAPPLICATION,
            DDETOPIC,
            INFOTIP,
            QUICKTIP,
            TILEINFO,
            CONTENTTYPE,
            DEFAULTICON,
            SHELLEXTENSION
        }

        [Guid("c46ca590-3c3f-11d2-bee6-0000f805ca57"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        private interface IQueryAssociations
        {
            void Init([In] ASSOCF flags, [In, MarshalAs(UnmanagedType.LPWStr)] string pszAssoc, [In] UIntPtr hkProgid,
                      [In] IntPtr hwnd);

            void GetString([In] ASSOCF flags, [In] ASSOCSTR str, [In, MarshalAs(UnmanagedType.LPWStr)] string pwszExtra,
                           [Out, MarshalAs(UnmanagedType.LPWStr)] StringBuilder pwszOut, [In, Out] ref int pcchOut);

            void GetKey([In] ASSOCF flags, [In] ASSOCKEY str, [In, MarshalAs(UnmanagedType.LPWStr)] string pwszExtra,
                        [Out] out UIntPtr phkeyOut);

            void GetData([In] ASSOCF flags, [In] ASSOCDATA data, [In, MarshalAs(UnmanagedType.LPWStr)] string pwszExtra,
                         [Out, MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 4)] out byte[] pvOut,
                         [In, Out] ref int pcbOut);

            void GetEnum(); // not used actually
        }
    }
}