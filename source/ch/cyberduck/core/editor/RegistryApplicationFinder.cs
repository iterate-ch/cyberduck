// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
        public Application getDescription(string application)
        {
            if (application == null)
            {
                return null;
            }
            if (!applicationNameCache.ContainsKey(application))
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
                                if (valueName.Equals(application + ".FriendlyAppName",
                                                     StringComparison.CurrentCultureIgnoreCase))
                                {
                                    applicationNameCache.Add(new KeyValuePair<string, Application>(application,
                                                                                                   new Application(
                                                                                                       application
                                                                                                           .ToLower(),
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
                                                                                                       application
                                                                                                           .ToLower(),
                                                                                                       valueName)));
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!applicationNameCache.ContainsKey(application))
                {
                    applicationNameCache.Add(new KeyValuePair<string, Application>(application,
                                                                                   new Application(
                                                                                       application.ToLower(),
                                                                                       LocalFactory
                                                                                           .createLocal(
                                                                                               application)
                                                                                           .getName())));
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
                return null;
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
                        defaultApplicationCache.Add(extension, getDescription(exe));
                    }
                }
                catch (Exception)
                {
                    Log.error(string.Format("Exception while finding application for {0}", filename));
                }
            }
            defaultApplicationCache.TryGetValue(extension, out app);
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
                    if (null != application)
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

        public static void Register()
        {
            ApplicationFinderFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

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
            string command = null;
            try
            {
                extension = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\" + extension;
                using (RegistryKey oApplication = Registry.CurrentUser.OpenSubKey(extension))
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

        private class Factory : ApplicationFinderFactory
        {
            protected override object create()
            {
                return new RegistryApplicationFinder();
            }
        }
    }
}