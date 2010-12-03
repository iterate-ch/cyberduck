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
using System.Text.RegularExpressions;
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
            String ext = LocalFactory.createLocal(filename).getExtension();
            if (IsNotBlank(filename))
            {
                ext = "." + ext;
            }
            return ext;
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
                            string command = GetOpenCommand(Registry.ClassesRoot);
                            if (null != command)
                            {
                                return ExtractExeFromCommand(command);
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
            Regex regex = new Regex("(\\\".*\\\")\\s.*");

            Match match = regex.Match(command);
            if (match.Groups.Count > 1)
            {
                return match.Groups[1].Value.Replace("\"", "");
            }
            else
            {
                int i = command.IndexOf(" ");
                if (i > 0)
                {
                    return command.Substring(0, i);
                }
            }
            return null;
        }

        /// <summary>
        /// Extract open command
        /// </summary>
        /// <param name="root">expected substructure is shell/open/command</param>
        /// <returns>null if not found</returns>
        private static string GetOpenCommand(RegistryKey root)
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
    }
}