// 
// Copyright (c) 2010-2012 Yves Langisch. All rights reserved.
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
using System.IO;
using System.Linq;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Collections;
using Microsoft.Win32;
using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.local;
using java.util;
using org.apache.log4j;

namespace Ch.Cyberduck.core.editor
{
    internal class RegistryApplicationFinder : ApplicationFinder
    {
        private static readonly Logger Log = Logger.getLogger(typeof (RegistryApplicationFinder).Name);

        private static readonly LRUCache<string, Application> applicationNameCache =
            new LRUCache<string, Application>(100);

        private static readonly LRUCache<string, Application> defaultApplicationCache =
            new LRUCache<string, Application>(100);

        private static readonly LRUCache<string, IList<Application>> defaultApplicationListCache =
            new LRUCache<string, IList<Application>>(100);

        //vormals GetApplicationNameForExe
        public Application find(string application)
        {
            if (applicationNameCache.ContainsKey(application))
            {
                if (Utils.IsVistaOrLater)
                {
                    using (
                        RegistryKey muiCache =
                            Registry.ClassesRoot.OpenSubKey(
                                "Local Settings\\Software\\Microsoft\\Windows\\Shell\\MuiCache"))
                    {
                        if (null != muiCache)
                        {
                            foreach (string valueName in muiCache.GetValueNames())
                            {
                                if (valueName.Equals(application, StringComparison.CurrentCultureIgnoreCase))
                                {
                                    applicationNameCache.Add(new KeyValuePair<string, Application>(application,
                                                                                                   new Application(
                                                                                                       application,
                                                                                                       (string)
                                                                                                       muiCache.GetValue
                                                                                                           (valueName))));
                                    break;
                                }
                            }
                        }
                    }
                }
                else
                {
                    using (
                        RegistryKey muiCache =
                            Registry.CurrentUser.OpenSubKey(
                                "Software\\Microsoft\\Windows\\ShellNoRoam\\MUICache"))
                    {
                        if (null != muiCache)
                        {
                            foreach (string valueName in muiCache.GetValueNames())
                            {
                                if (valueName.Equals(application, StringComparison.CurrentCultureIgnoreCase))
                                {
                                    applicationNameCache.Add(new KeyValuePair<string, Application>(application,
                                                                                                   new Application(
                                                                                                       application,
                                                                                                       valueName)));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return applicationNameCache[application];
        }

        public Application find(Local file)
        {
            //see http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html
            string strExt = Utils.GetSafeExtension(file.getName());
            Application app;
            Log.debug(string.Format("GetRegisteredDefaultApplication for filename {0}", file.getName()));

            if (defaultApplicationCache.TryGetValue(strExt, out app))
            {
                Log.debug(string.Format("Return cached default application {0} for extension {1}", app, strExt));
                return app;
            }

            String exe = GetExplorerRegisteredApplication(file.getName());
            if (null != exe)
            {
                defaultApplicationCache.Add(strExt, find(exe));
            }
            else
            {
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
                                            exe = Utils.ExtractExeFromCommand(strExe);
                                        }
                                    }
                                }
                            }
                        }
                        defaultApplicationCache.Add(strExt, find(exe));
                    }
                }
                catch (Exception)
                {
                    Log.error(string.Format("Exception while finding application for {0}", file.getName()));
                }
            }
            return defaultApplicationCache[strExt];
        }

        public List findAll(Local file)
        {
            IList<String> progs = new List<string>();
            List<Application> map = new List<Application>();
            String extension = file.getExtension();

            if (Utils.IsBlank(extension))
            {
                return Utils.ConvertToJavaList(map);
            }
            if (!extension.StartsWith(".")) extension = "." + extension;

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
                    if (null != application)
                    {
                        map.Add(application);
                    }
                    else
                    {
                        map.Add(new Application(LocalFactory.createLocal(exe).getName(), exe));
                    }
                }
                map.Sort(
                    delegate(Application app1, Application app2) { return app1.getIdentifier().CompareTo(app2.getIdentifier()); });
                defaultApplicationListCache.Add(extension, map);
            }
            return Utils.ConvertToJavaList(defaultApplicationListCache[extension]);
        }

        public bool isInstalled(Application application)
        {
            return Utils.IsNotBlank(application.getIdentifier()) && File.Exists(application.getIdentifier());
        }

        public bool isOpen(Application application)
        {
            return false;
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
    }
}