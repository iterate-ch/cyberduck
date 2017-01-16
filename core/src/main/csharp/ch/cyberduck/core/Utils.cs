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
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Text;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using java.util;
using Microsoft.Win32;
using org.apache.commons.io;
using org.apache.log4j;
using Collection = java.util.Collection;

namespace Ch.Cyberduck.Core
{
    public class Utils
    {
        public delegate object ApplyPerItemForwardDelegate<T>(T item);

        public delegate T ApplyPerItemReverseDelegate<T>(object item);

        [DllImport("kernel32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
        static extern int GetCurrentPackageFullName(ref int packageFullNameLength, ref StringBuilder packageFullName);

        private static readonly List<String> ExtendedCharsets = new List<string>
        {
            "Big5",
            "Big5-HKSCS",
            "EUC-JP",
            "EUC-KR",
            "GB18030",
            "GB2312",
            "GBK",
            "ISO-2022-CN",
            "ISO-2022-JP",
            "ISO-2022-JP-2",
            "ISO-2022-KR",
            "ISO-8859-3",
            "ISO-8859-6",
            "ISO-8859-8",
            "JIS_X0201",
            "JIS_X0212-1990",
            "Shift_JIS",
            "TIS-620",
            "windows-1255",
            "windows-1256",
            "windows-1258",
            "windows-31j"
        };

        public static readonly bool IsVistaOrLater = OperatingSystemVersion.Current >= OSVersionInfo.Vista;
        public static readonly bool IsWin7OrLater = OperatingSystemVersion.Current >= OSVersionInfo.Win7;

        // Original by Matteo Pagani (https://github.com/qmatteoq/DesktopBridgeHelpers) licensed under MIT
        // modified by Jöran Malek for iterate GmbH
        public static bool IsRunningAsUWP
        {
            get
            {
                if (Environment.OSVersion.Version.Major + Environment.OSVersion.Version.Minor/10.0 <= 6.1)
                    return false;
                StringBuilder sb = new StringBuilder(1024);
                int length = 0;
                int result = GetCurrentPackageFullName(ref length, ref sb);
                return result != 15700;
            }
        }

        private static readonly Logger Log = Logger.getLogger(typeof (Utils).FullName);

        public static bool IsBlank(string value)
        {
            if (String.IsNullOrEmpty(value))
            {
                return true;
            }

            return String.IsNullOrEmpty(value.Trim());
        }

        public static Assembly Me()
        {
            return Assembly.GetExecutingAssembly();
        }

        public static bool IsNotBlank(string value)
        {
            return !IsBlank(value);
        }

        /// <summary>
        /// Get file extension. Ignores OS specific special characters. Includes the dot if available.
        /// </summary>
        /// <param name="filename"></param>
        /// <returns></returns>
        public static string GetSafeExtension(string filename)
        {
            if (IsNotBlank(filename))
            {
                //see http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html
                string extension = FilenameUtils.getExtension(filename);
                if (IsBlank(extension))
                {
                    return String.Empty;
                }
                return "." + extension;
            }
            return String.Empty;
        }

        public static string SafeString(string s)
        {
            return s ?? string.Empty;
        }

        /// <summary>
        /// Check if a given object is parseable to an int32
        /// </summary>
        /// <param name="expression"></param>
        /// <returns></returns>
        public static bool IsInt(object expression)
        {
            int retNum;

            bool isNum = int.TryParse(Convert.ToString(expression), NumberStyles.Any, NumberFormatInfo.InvariantInfo,
                out retNum);
            return isNum;
        }

        /// <summary>
        /// Convert a generic IEnumerable to a java list
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="list">IEnumerable to convert</param>
        /// <param name="applyPerItem">Apply this delegate to all list items before adding</param>
        /// <returns>A java list</returns>
        public static List ConvertToJavaList<T>(IEnumerable<T> list, ApplyPerItemForwardDelegate<T> applyPerItem)
        {
            ArrayList javaList = new ArrayList();
            foreach (T item in list)
            {
                if (null != applyPerItem)
                {
                    javaList.add(applyPerItem(item));
                    continue;
                }
                javaList.Add(item);
            }
            return javaList;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="K"></typeparam>
        /// <typeparam name="V"></typeparam>
        /// <param name="dictionary"></param>
        /// <returns></returns>
        public static Map ConvertToJavaMap<K, V>(IDictionary<K, V> dictionary)
        {
            Map javaMap = new HashMap();
            foreach (KeyValuePair<K, V> pair in dictionary)
            {
                javaMap.put(pair.Key, pair.Value);
            }
            return javaMap;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <typeparam name="K"></typeparam>
        /// <typeparam name="V"></typeparam>
        /// <param name="javaMap"></param>
        /// <returns></returns>
        public static IDictionary<K, V> ConvertFromJavaMap<K, V>(Map javaMap)
        {
            IDictionary<K, V> result = new Dictionary<K, V>();
            Iterator iterator = javaMap.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry entry = (Map.Entry) iterator.next();
                result.Add((K) entry.getKey(), (V) entry.getValue());
            }
            return result;
        }

        /// <summary>
        /// Convert a generic IEnumerable to a java list
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="list">IEnumerable to convert</param>
        /// <returns>A java list</returns>
        public static List ConvertToJavaList<T>(IEnumerable<T> list)
        {
            return ConvertToJavaList(list, null);
        }

        /// <summary>
        /// Convert a java list to a generic collection
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="collection"></param>
        /// <returns>A List<typeparamref name="T"/></returns>
        public static ICollection<T> ConvertFromJavaList<T>(Collection collection)
        {
            return ConvertFromJavaList<T>(collection, null);
        }

        /// <summary>
        /// Convert a java list to a generic collection
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="collection"></param>
        /// <param name="applyPerItem">Apply this delegate to all list items before adding</param>
        /// <returns>A List<typeparamref name="T"/></returns>
        public static IList<T> ConvertFromJavaList<T>(Collection collection, ApplyPerItemReverseDelegate<T> applyPerItem)
        {
            List<T> result = new List<T>(collection.size());
            for (Iterator iterator = collection.iterator(); iterator.hasNext();)
            {
                Object next = iterator.next();
                if (null != applyPerItem)
                {
                    result.Add(applyPerItem(next));
                    continue;
                }
                result.Add((T) next);
            }
            return result;
        }

        public static IList<KeyValuePair<string, string>> OpenWithListForExtension(String ext)
        {
            IList<String> progs = new List<string>();
            List<KeyValuePair<string, string>> map = new List<KeyValuePair<string, string>>();

            if (IsBlank(ext)) return map;

            if (!ext.StartsWith(".")) ext = "." + ext;
            using (RegistryKey clsExt = Registry.ClassesRoot.OpenSubKey(ext))
            {
                IList<string> rootList = OpenWithListForExtension(ext, clsExt);
                foreach (string s in rootList)
                {
                    progs.Add(s);
                }
            }
            using (
                RegistryKey clsExt =
                    Registry.CurrentUser.OpenSubKey(
                        "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\" + ext))
            {
                IList<string> explorerList = OpenWithListForExtension(ext, clsExt);
                foreach (string s in explorerList)
                {
                    progs.Add(s);
                }
            }

            foreach (string exe in progs.Distinct())
            {
                ApplicationFinder finder = ApplicationFinderFactory.get();
                Application application = finder.getDescription(exe);
                if (finder.isInstalled(application))
                {
                    map.Add(new KeyValuePair<string, string>(application.getName(), exe));
                }
                else
                {
                    map.Add(new KeyValuePair<string, string>(LocalFactory.get(exe).getName(), exe));
                }
            }
            map.Sort(
                delegate(KeyValuePair<string, string> pair1, KeyValuePair<string, string> pair2)
                {
                    return pair1.Key.CompareTo(pair2.Key);
                });

            return map;
        }

        public static IList<String> OpenWithListForExtension(String ext, RegistryKey rootKey)
        {
            IList<String> result = new List<string>();

            if (null != rootKey)
            {
                //PerceivedType
                String perceivedType = (String) rootKey.GetValue("PerceivedType");
                if (null != perceivedType)
                {
                    using (
                        RegistryKey openWithKey =
                            Registry.ClassesRoot.OpenSubKey("SystemFileAssociations\\" + perceivedType +
                                                            "\\OpenWithList"))
                    {
                        IList<String> appCmds = GetApplicationCmdsFromOpenWithList(openWithKey);
                        foreach (string appCmd in appCmds)
                        {
                            result.Add(appCmd);
                        }
                    }
                }

                //OpenWithProgIds
                using (RegistryKey key = rootKey.OpenSubKey("OpenWithProgIds"))
                {
                    IList<String> appCmds = GetApplicationCmdsFromOpenWithProgIds(key);
                    foreach (string appCmd in appCmds)
                    {
                        result.Add(appCmd);
                    }
                }

                //OpenWithList
                using (RegistryKey openWithKey = rootKey.OpenSubKey("OpenWithList"))
                {
                    IList<String> appCmds = GetApplicationCmdsFromOpenWithList(openWithKey);
                    foreach (string appCmd in appCmds)
                    {
                        result.Add(appCmd);
                    }
                }
            }
            return result;
        }

        private static IList<String> GetApplicationCmdsFromOpenWithList(RegistryKey openWithKey)
        {
            IList<String> appCmds = new List<string>();
            if (openWithKey != null)
            {
                //all subkeys
                string[] exes = openWithKey.GetSubKeyNames();
                IList<String> cands = exes.ToList();
                //all values);
                string[] values = openWithKey.GetValueNames();
                foreach (string value in values)
                {
                    object o = openWithKey.GetValue(value);
                    if (o is String)
                    {
                        cands.Add(o as String);
                    }
                }


                foreach (string s in exes)
                {
                    cands.Add(s);
                }

                foreach (string progid in cands)
                {
                    using (RegistryKey key = Registry.ClassesRoot.OpenSubKey("Applications\\" + progid))
                    {
                        String cmd = GetExeFromOpenCommand(key);
                        if (!String.IsNullOrEmpty(cmd))
                        {
                            appCmds.Add(cmd);
                        }
                    }
                }
            }
            return appCmds;
        }

        private static IList<String> GetApplicationCmdsFromOpenWithProgIds(RegistryKey key)
        {
            IList<String> appCmds = new List<string>();
            if (key != null)
            {
                string[] progids = key.GetValueNames();
                foreach (string progid in progids)
                {
                    if (!string.IsNullOrEmpty(progid))
                    {
                        using (RegistryKey clsProgid = Registry.ClassesRoot.OpenSubKey(progid))
                        {
                            String cmd = GetExeFromOpenCommand(clsProgid);
                            if (!String.IsNullOrEmpty(cmd))
                            {
                                appCmds.Add(cmd);
                            }
                        }
                    }
                }
            }
            return appCmds;
        }

        public static bool StartProcess(Process process)
        {
            try
            {
                process.Start();
                return true;
            }
            catch (InvalidOperationException e)
            {
                Log.error(e);
            }
            catch (Win32Exception e)
            {
                Log.error(String.Format("Error while StartProcess: {0},{1}", e.Message, e.NativeErrorCode));
            }
            return false;
        }

        public static string ExtractApplicationPath(string cmd)
        {
            if (!String.IsNullOrEmpty(cmd) && !cmd.Contains("rundll32.exe"))
            {
                String command = null;
                if (cmd.StartsWith("\""))
                {
                    int i = cmd.IndexOf("\"", 1);
                    if (i > 2)
                        command = cmd.Substring(1, i - 1);
                }
                else
                {
                    int i = cmd.IndexOf(" ");
                    if (i > 0)
                        command = cmd.Substring(0, i);
                }

                if (File.Exists(command))
                {
                    return command;
                }
            }
            return null;
        }

        /// <summary>
        /// Extract open command
        /// </summary>
        /// <param name="root">expected substructure is shell/open/command</param>
        /// <returns>null if not found</returns>
        public static string GetExeFromOpenCommand(RegistryKey root)
        {
            if (null != root)
            {
                using (var editSk = root.OpenSubKey("shell\\open\\command"))
                {
                    if (null != editSk)
                    {
                        String cmd = (String) editSk.GetValue(String.Empty);
                        return ExtractApplicationPath(cmd);
                    }
                }
            }
            return null;
        }

        /// <summary>
        /// method for retrieving the users default web browser
        /// </summary>
        /// <returns></returns>
        public static string GetSystemDefaultBrowser()
        {
            try
            {
                //for Vista and later we first check the UserChoice
                using (
                    var uc =
                        Registry.CurrentUser.OpenSubKey(
                            @"HKEY_CURRENT_USER\Software\Microsoft\Windows\Shell\Associations\UrlAssociations\http\UserChoice")
                    )
                {
                    if (null != uc)
                    {
                        string progid = (string) uc.GetValue("Progid");
                        if (null != progid)
                        {
                            string exe = GetExeFromOpenCommand(Registry.ClassesRoot);
                            if (null != exe)
                            {
                                return exe;
                            }
                        }
                    }
                }

                //set the registry key we want to open
                using (var regKey = Registry.ClassesRoot.OpenSubKey("HTTP\\shell\\open\\command", false))
                {
                    return ExtractExeFromCommand((string) regKey.GetValue(null));
                }
            }
            catch (Exception)
            {
                return null;
            }
        }

        public static string ExtractExeFromCommand(string command)
        {
            if (!String.IsNullOrEmpty(command))
            {
                String cmd = null;
                if (command.StartsWith("\""))
                {
                    int i = command.IndexOf("\"", 1);
                    if (i > 2)
                        cmd = command.Substring(1, i - 1);
                }
                else
                {
                    int i = command.IndexOf(" ");
                    if (i > 0)
                        cmd = command.Substring(0, i);
                }

                if (null != cmd && LocalFactory.get(cmd).exists())
                {
                    return cmd;
                }
            }
            return null;
        }
    }
}