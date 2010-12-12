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
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;
using ch.cyberduck.core;
using java.nio.charset;
using java.util;
using Microsoft.Win32;
using org.apache.log4j;

namespace Ch.Cyberduck.Core
{
    public class Utils
    {
        public delegate object ApplyPerItemForwardDelegate<T>(T item);

        public delegate T ApplyPerItemReverseDelegate<T>(object item);

        public static readonly bool IsVistaOrLater = OperatingSystemVersion.Current >= OSVersionInfo.Vista;
        public static readonly bool IsWin7OrLater = OperatingSystemVersion.Current >= OSVersionInfo.Win7;

        private static readonly Logger Log = Logger.getLogger(typeof (Utils).FullName);

        public static bool StartProcess(string filename, string args)
        {
            try
            {
                Process.Start(filename, args);
                return true;
            }
            catch (Win32Exception e)
            {
                Log.error(String.Format("StartProcess: {0},{1}", e.Message, e.NativeErrorCode));
                return false;
            }
        }

        public static bool StartProcess(string filename)
        {
            return StartProcess(filename, null);
        }

        public static bool IsBlank(string value)
        {
            if (String.IsNullOrEmpty(value))
            {
                return true;
            }

            return String.IsNullOrEmpty(value.Trim());
        }

        public static bool IsNotBlank(string value)
        {
            return !IsBlank(value);
        }

        public static string ReplaceNewlines(string blockOfText, string replaceWith)
        {
            return blockOfText.Replace("\r\n", replaceWith).Replace("\n", replaceWith).Replace("\r", replaceWith);
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
                String ext = LocalFactory.createLocal(filename).getExtension();
                return "." + ext;
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
        /// <param name="list"></param>
        /// <returns>A List<typeparamref name="T"/></returns>
        public static ICollection<T> ConvertFromJavaList<T>(List list)
        {
            return ConvertFromJavaList<T>(list, null);
        }

        /// <summary>
        /// Convert a java list to a generic collection
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="list"></param>
        /// <param name="applyPerItem">Apply this delegate to all list items before adding</param>
        /// <returns>A List<typeparamref name="T"/></returns>
        public static IList<T> ConvertFromJavaList<T>(List list, ApplyPerItemReverseDelegate<T> applyPerItem)
        {
            List<T> result = new List<T>(list.size());
            for (int i = 0; i < list.size(); i++)
            {
                if (null != applyPerItem)
                {
                    result.Add(applyPerItem(list.get(i)));
                    continue;
                }
                result.Add((T) list.get(i));
            }
            return result;
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

                if (null != cmd && LocalFactory.createLocal(cmd).exists())
                {
                    return cmd;
                }
            }
            return null;
        }

        /// <summary>
        /// Extract open command
        /// </summary>
        /// <param name="root">expected substructure is shell/open/command</param>
        /// <returns>null if not found</returns>
        private static string GetOpenCommand2(RegistryKey root)
        {
            using (var editSk = root.OpenSubKey("shell\\open\\command"))
            {
                if (null != editSk)
                {
                    return (string) editSk.GetValue("");
                }
            }
            return null;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns>The available character sets available on this platform</returns>
        public static string[] AvailableCharsets()
        {
            List<string> charsets = new List<string>();
            object[] collection = Charset.availableCharsets().values().toArray();
            foreach (Charset charset in collection)
            {
                string name = charset.displayName();
                if (!(name.StartsWith("IBM") || name.StartsWith("x-") || name.StartsWith("X-")))
                {
                    charsets.Add(name);
                }
            }
            return charsets.ToArray();
        }


        private static IList<String> OpenWithListForExtension(String ext, RegistryKey rootKey)
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
                String appName = GetApplicationNameForExe(exe);
                if (null != appName)
                {
                    map.Add(new KeyValuePair<string, string>(appName, exe));
                }
                else
                {
                    map.Add(new KeyValuePair<string, string>(LocalFactory.createLocal(exe).getName(), exe));
                }
            }
            map.Sort(
                delegate(KeyValuePair<string, string> pair1, KeyValuePair<string, string> pair2) { return pair1.Key.CompareTo(pair2.Key); });

            return map;
        }

        public static String GetApplicationNameForExe(string exe)        
        {   //Vista/Win7
            using (
                RegistryKey muiCache =
                    Registry.ClassesRoot.OpenSubKey(
                        "Local Settings\\Software\\Microsoft\\Windows\\Shell\\MuiCache"))
            {
                if (null != muiCache)
                {
                    foreach (string valueName in muiCache.GetValueNames())
                    {
                        if (valueName.Equals(exe, StringComparison.CurrentCultureIgnoreCase))
                        {
                            return (string) muiCache.GetValue(valueName);
                        }
                    }
                }
            }
            //WindowsXP
            using (
                RegistryKey muiCache =
                    Registry.CurrentUser.OpenSubKey(
                        "Software\\Microsoft\\Windows\\ShellNoRoam\\MUICache"))
            {
                if (null != muiCache)
                {
                    foreach (string valueName in muiCache.GetValueNames())
                    {
                        if (valueName.Equals(exe, StringComparison.CurrentCultureIgnoreCase))
                        {
                            return (string)muiCache.GetValue(valueName);
                        }
                    }
                }
            }
            return null;
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


        /// <summary>
        /// Extract open command
        /// </summary>
        /// <param name="root">expected substructure is shell/open/command</param>
        /// <returns>null if not found</returns>
        private static string GetExeFromOpenCommand(RegistryKey root)
        {
            if (null != root)
            {
                using (var editSk = root.OpenSubKey("shell\\open\\command"))
                {
                    if (null != editSk)
                    {
                        String cmd = (String) editSk.GetValue("");
                        //todo replcae with extract exe from command
                        if (!String.IsNullOrEmpty(cmd))
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
                    }
                }
            }
            return null;
        }
    }
}