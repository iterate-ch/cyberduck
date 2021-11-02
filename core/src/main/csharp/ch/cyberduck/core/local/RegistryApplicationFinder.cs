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

using ch.cyberduck.core.cache;
using ch.cyberduck.core.local;
using java.util;
using Microsoft.Win32;
using org.apache.commons.io;
using org.apache.logging.log4j;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using Windows.Win32;
using Windows.Win32.UI.Shell;
using static Windows.Win32.CoreConstants;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.Shell.ASSOCSTR;

namespace Ch.Cyberduck.Core.Local
{
    public class RegistryApplicationFinder : ApplicationFinder
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(RegistryApplicationFinder).Name);

        private static readonly LRUCache applicationNameCache = LRUCache.build(100);
        private static readonly LRUCache defaultApplicationCache = LRUCache.build(100);
        private static readonly LRUCache defaultApplicationListCache = LRUCache.build(100);

        //vormals GetApplicationNameForExe
        public Application getDescription(string application)
        {
            if (Utils.IsBlank(application))
            {
                return Application.notfound;
            }
            if (!applicationNameCache.contains(application))
            {
                string path = WindowsApplicationLauncher.GetExecutableCommand(application);
                if (File.Exists(path))
                {
                    FileVersionInfo info = FileVersionInfo.GetVersionInfo(path);
                    if (Utils.IsBlank(info.FileDescription))
                    {
                        // Does not contain version information
                        applicationNameCache.put(application, new Application(
                                application.ToLower(),
                                FilenameUtils.getName(application)));
                    }
                    else
                    {
                        applicationNameCache.put(application, new Application(
                                application.ToLower(),
                                info.FileDescription));
                    }
                }
                else
                {
                    applicationNameCache.put(application, new Application(
                            application.ToLower(),
                            FilenameUtils.getName(application)));
                }
            }
            return applicationNameCache.get(application) as Application;
        }

        public Application find(String filename)
        {
            string extension = Utils.GetSafeExtension(filename);
            if (Utils.IsBlank(extension))
            {
                return Application.notfound;
            }
            Application app = defaultApplicationCache.get(extension) as Application;
            Log.debug(string.Format("GetRegisteredDefaultApplication for filename {0}", filename));

            if (app != null)
            {
                Log.debug(string.Format("Return cached default application {0} for extension {1}", app, extension));
                return app;
            }
            // Step 1 / Check if there is a registered edit command with File Explorer
            String exe = GetExplorerRegisteredApplication(extension, "edit");
            if (null != exe)
            {
                defaultApplicationCache.put(extension, getDescription(exe));
            }
            // Step 2 / Check registry 
            if (null == exe)
            {
                try
                {
                    string strProgID;
                    using (var extSubKey = Registry.ClassesRoot.OpenSubKey(extension))
                    {
                        if (null != extSubKey)
                        {
                            strProgID = (string)extSubKey.GetValue(null);

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
                            defaultApplicationCache.put(extension, getDescription(exe));
                        }
                    }
                }
                catch (Exception)
                {
                    Log.error(string.Format("Exception while finding application for {0}", filename));
                }
            }
            // Step 3 / Check if there is a registered open command with File Explorer
            if (null == exe)
            {
                exe = GetExplorerRegisteredApplication(extension, "open");
                if (null != exe)
                {
                    defaultApplicationCache.put(extension, getDescription(exe));
                }
            }
            app = defaultApplicationCache.get(extension) as Application;
            return app ?? Application.notfound;
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
            if (!defaultApplicationListCache.contains(extension))
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
                    delegate (Application app1, Application app2)
                    {
                        return app1.getIdentifier().CompareTo(app2.getIdentifier());
                    });
                defaultApplicationListCache.put(extension, map);
            }
            return Utils.ConvertToJavaList(defaultApplicationListCache.get(extension) as IList<Application>);
        }

        public bool isInstalled(Application application)
        {
            return Utils.IsNotBlank(application.getIdentifier()) && File.Exists(application.getIdentifier());
        }

        /// <summary>
        /// Return with Explorer registered application by file's extension (for editing)
        /// </summary>
        /// <param name="extension"></param>
        /// <param name="verb"></param>
        /// <returns></returns>
        /// <see cref="http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html"/>
        /// <see cref="http://msdn.microsoft.com/en-us/library/cc144154%28VS.85%29.aspx"/>
        private unsafe string GetExplorerRegisteredApplication(string extension, string verb)
        {
            try
            {
                AssocCreate<IQueryAssociations>(CLSID_QueryAssociations, out var qa);
                qa.Init(ASSOCF_INIT_DEFAULTTOSTAR, extension, default, default);

                uint size = 0;
                qa.GetString(ASSOCF_NOTRUNCATE, ASSOCSTR_COMMAND, verb, default, ref size);

                // GetString assumes null-terminated string. C#-Strings are null-terminated.
                // Excludes last char.
                var cmd = new string(char.MinValue, (int)size - 1);
                qa.GetString(ASSOCF_NOTRUNCATE, ASSOCSTR_COMMAND, verb, cmd, ref size);

                if (Utils.IsBlank(cmd))
                {
                    return null;
                }

                if (cmd.Contains("\""))
                {
                    return cmd.Substring(1, cmd.IndexOf("\"", 1) - 1);
                }
                return cmd.Substring(0, cmd.IndexOf(" "));
            }
            catch (Exception e)
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
                        return (string)editSk.GetValue(String.Empty);
                    }

                    using (var openSk = root.OpenSubKey("shell\\open\\command"))
                    {
                        if (null != openSk)
                        {
                            return (string)openSk.GetValue(String.Empty);
                        }
                    }
                }
            }
            return null;
        }
    }
}
