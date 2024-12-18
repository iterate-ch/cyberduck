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

using ch.cyberduck.core.local;
using Ch.Cyberduck.Core.Local;
using java.util;
using org.apache.logging.log4j;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.Reflection;
using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.Shell.ASSOCIATIONLEVEL;
using static Windows.Win32.UI.Shell.ASSOCIATIONTYPE;
using static Windows.Win32.UI.Shell.ASSOCSTR;
using Collection = java.util.Collection;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Core
{
    public class Utils
    {
        public delegate object ApplyPerItemForwardDelegate<T>(T item);

        public delegate T ApplyPerItemReverseDelegate<T>(object item);

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

        public static bool IsWin10 => OSVersion.IsVersion(OSVersion.Windows10.Release);
        public static bool IsWin10FallCreatorsUpdate => OSVersion.IsVersion(OSVersion.Windows10.V1709);
        public static bool IsWin101809 => OSVersion.IsVersion(OSVersion.Windows10.V1809);

        // Original by Matteo Pagani (https://github.com/qmatteoq/DesktopBridgeHelpers) licensed under MIT
        // modified by Jöran Malek for iterate GmbH
        public static bool IsRunningAsUWP
        {
            get
            {
                var major = Environment.OSVersion.Version.Major;
                var minor = Environment.OSVersion.Version.Minor;
                var minorReduced = minor / 10.0;
                var combined = major + minorReduced;
                var win7OrOlder = combined <= 6.1;
                if (win7OrOlder)
                    return false;
                try
                {
                    uint length = 0;
                    return GetCurrentPackageFullName(ref length, default) != WIN32_ERROR.APPMODEL_ERROR_NO_PACKAGE;
                }
                catch (EntryPointNotFoundException entryPointNotFoundException) // Fix for MD-3274
                {
                    Log.error($"Environment Version Check returned wrong version. {major} {minor} ({minorReduced}) combined is {combined} result is {win7OrOlder}", entryPointNotFoundException);
                    return false;
                }
            }
        }

        private static readonly Logger Log = LogManager.getLogger(typeof(Utils).FullName);

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
                string extension = Path.getExtension(filename);
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
                Map.Entry entry = (Map.Entry)iterator.next();
                result.Add((K)entry.getKey(), (V)entry.getValue());
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
        public static IList<T> ConvertFromJavaList<T>(Collection collection)
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
                result.Add((T)next);
            }
            return result;
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

        /// <summary>
        /// method for retrieving the users default web browser
        /// </summary>
        /// <returns></returns>
        public unsafe static Application GetSystemDefaultBrowser()
        {
            if (SHCreateAssociationRegistration(out IApplicationAssociationRegistration reg).Failed)
            {
                return Application.notfound;
            }

            PWSTR defaultQuery = default;
            try
            {
                reg.QueryCurrentDefault("http", AT_URLPROTOCOL, AL_EFFECTIVE, out defaultQuery);

                var qa = (IQueryAssociations)Activator.CreateInstance(Type.GetTypeFromCLSID(CLSID_QueryAssociations));
                qa.Init(0, defaultQuery, default, default);
                if (!qa.GetString(ASSOCSTR_FRIENDLYAPPNAME, "open", out var friendlyAppName))
                {
                    return Application.notfound;
                }
                if (!qa.GetString(ASSOCSTR_DEFAULTICON, "open", out var defaultIcon))
                {
                    return Application.notfound;
                }

                return new ShellApplicationFinder.ProgIdApplication(defaultQuery.ToString(), friendlyAppName.ToString(), defaultIcon.ToString());
            }
            catch
            {
                return Application.notfound;
            }
            finally
            {
                CoTaskMemFree(defaultQuery.Value);
            }
        }

        public static class OSVersion
        {
            public static readonly System.Version Windows7 = new System.Version(6, 1, 7600);
            public static readonly System.Version Windows7SP1 = new System.Version(6, 1, 7601);
            public static readonly System.Version Windows8 = new System.Version(6, 2);
            public static readonly System.Version Windows81 = new System.Version(6, 3);

            public static class Windows10
            {
                public static readonly System.Version Release = new System.Version(10, 0);
                public static readonly System.Version V1511 = new System.Version(10, 0, 10586);
                public static readonly System.Version V1607 = new System.Version(10, 0, 14393);
                public static readonly System.Version V1703 = new System.Version(10, 0, 15063);
                public static readonly System.Version V1709 = new System.Version(10, 0, 16299);
                public static readonly System.Version V1803 = new System.Version(10, 0, 17134);
                public static readonly System.Version V1809 = new System.Version(10, 0, 17763);
                public static readonly System.Version V1903 = new System.Version(10, 0, 18362);
            }

            public static bool IsVersion(System.Version version) => TestOSVersion(version, Environment.OSVersion.Version);

            public static bool TestOSVersion(System.Version version, System.Version osVersion)
            {
                if (osVersion.Major < version.Major)
                {
                    return false;
                }

                if (osVersion.Minor < version.Minor)
                {
                    return false;
                }

                if (version.Build != 0)
                {
                    return osVersion.Build >= version.Build;
                }

                return true;
            }
        }
    }
}
