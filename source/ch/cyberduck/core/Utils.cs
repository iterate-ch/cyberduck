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
using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using Ch.Cyberduck.Core.Collections;
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

        private static readonly Logger Log = Logger.getLogger(typeof (Utils).Name);

        private static readonly LRUCache<string, string> defaultApplicationCache = new LRUCache<string, string>(100);
        public static Encoding encoding = Encoding.UTF8;

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

        //see http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html
        public static string GetRegisteredDefaultApplication(string filename)
        {
            string strExt = Path.GetExtension(filename);
            string command;
            Log.debug(string.Format("GetRegisteredDefaultApplication for filname {0}", filename));

            if (defaultApplicationCache.TryGetValue(strExt, out command))
            {
                Log.debug(string.Format("Return cached default application {0} for extension {1}", command, strExt));
                return command;
            }

            command = GetExplorerRegisteredApplication(filename);
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
                                        command = ExtractExeFromCommand(strExe);
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

        /// <summary>
        /// Return with Explorer registered application by file's extension (for editing)
        /// </summary>
        /// <param name="filename"></param>
        /// <param name="command"></param>
        /// <returns></returns>
        /// <see cref="http://windevblog.blogspot.com/2008/09/get-default-application-in-windows-xp.html"/>
        /// <see cref="http://msdn.microsoft.com/en-us/library/cc144154%28VS.85%29.aspx"/>
        private static string GetExplorerRegisteredApplication(string filename)
        {
            string command = null;
            string strExt = Path.GetExtension(filename);

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
                    command = ExtractExeFromCommand(command);
                }
                return command;
            }
            catch (Exception)
            {
                return null;
            }
        }

        private static string ExtractExeFromCommand(string command)
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
        /// Extract edit command, fallback is the open command
        /// </summary>
        /// <param name="root">expected substructure shell/edit/command or shell/open/command</param>
        /// <returns>null if not found</returns>
        private static string GetEditCommand(RegistryKey root)
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
        //todo evtl. in anderen Controller verschieben (MainController)
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

        public static void RegisterCyberduckUrlHandler(RegistryKey registry)
        {
            CreateCustomUrlHandler(registry, "CyberduckURL", "Cyberduck URL", Application.ExecutablePath,
                                   Application.ExecutablePath + ",0");
        }

        /// <summary>
        /// Register Cyberduck as default application for the FTP protocol. To make it work with the Windows Search box we need
        /// some more tweaking (e.g. remove the ShellFolder from the ftp entry in the registry).
        /// TODO !! braucht es admin Rechte dazu etc. -> sauber testen
        /// </summary>
        /// <param name="registry"></param>
        public static void RegisterFtpProtocol(RegistryKey registry)
        {
            RegisterCyberduckUrlHandler(registry);
            RegistryKey r =
                registry.CreateSubKey(@"Software\Microsoft\Windows\Shell\Associations\UrlAssociations\ftp\UserChoice");
            r.SetValue("Progid", "CyberduckURL");
            r.Close();
        }

        /// <summary>
        /// Check if Cyberduck is the default application for FTP URLs
        /// </summary>
        /// <returns></returns>
        public static bool IsDefaultApplicationForFtp()
        {
            RegistryKey ftpUserChoice = Registry.CurrentUser.OpenSubKey(
                @"Software\Microsoft\Windows\Shell\Associations\UrlAssociations\ftp\UserChoice");
            return (null != ftpUserChoice && "CyberduckURL".Equals(ftpUserChoice.GetValue("Progid")));
        }

        /// <summary>
        /// Check if Cyberduck is the default application for SFTP URLs
        /// </summary>
        /// <returns></returns>
        public static bool IsDefaultApplicationForSftp()
        {
            RegistryKey sftpClass = Registry.CurrentUser.OpenSubKey(@"Software\Classes\sftp");
            if (null != sftpClass)
            {
                RegistryKey command = sftpClass.OpenSubKey(@"shell\open\command");
                if (null != command)
                {
                    string value = (string) command.GetValue("");
                    return (null != value && value.Contains("Cyberduck"));
                }
            }
            return false;
        }

        public static void RegisterSftpProtocol(RegistryKey registry)
        {
            CreateCustomUrlHandler(registry, "sftp", "sftp protocol", Application.ExecutablePath,
                                   Application.ExecutablePath + ",0");
        }

        private static void CreateCustomUrlHandler(RegistryKey registry, string association, string description,
                                                   string applicationPath, string icon)
        {
            RegistryKey r32 = null;
            RegistryKey r64 = null;
            try
            {
                r32 = registry.CreateSubKey(@"SOFTWARE\Classes\" + association);
                r32.SetValue("", description);
                r32.SetValue("URL Protocol", "");

                RegistryKey defaultIcon = r32.CreateSubKey("DefaultIcon");
                defaultIcon.SetValue("", applicationPath);

                RegistryKey command = r32.CreateSubKey(@"shell\open\command");
                command.SetValue("", "\"" + applicationPath + "\" \"%1\"");

                // If 64-bit OS, also register in the 32-bit registry area. 
                if (registry.OpenSubKey(@"SOFTWARE\Wow6432Node\Classes") != null)
                {
                    r64 = registry.CreateSubKey(@"SOFTWARE\Wow6432Node\Classes" + association);
                    r64.SetValue("", description);
                    r64.SetValue("URL Protocol", "");

                    defaultIcon = r64.CreateSubKey("DefaultIcon");
                    defaultIcon.SetValue("", icon);

                    command = r64.CreateSubKey(@"shell\open\command");
                    command.SetValue("", "\"" + applicationPath + "\" \"%1\"");
                }
            }
            catch (UnauthorizedAccessException)
            {
                //todo localize
                MessageBox.Show(
                    "You do not have permission to make changes to the registry!\n\nMake sure that you have administrative rights on this computer.",
                    "Cyberduck", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                if (null != r32) r32.Close();
                if (null != r64) r64.Close();
            }
        }

        /// <summary>
        /// Post the data as a multipart form
        /// postParameters with a value of type byte[] will be passed in the form as a file, and value of type string will be
        /// passed as a name/value pair.
        /// </summary>
        public static HttpWebResponse MultipartFormDataPost(string postUrl, string userAgent,
                                                            Dictionary<string, object> postParameters)
        {
            string formDataBoundary = "-----------------------------0xKhTmLbOuNdArY";
            string contentType = "multipart/form-data; boundary=" + formDataBoundary;

            byte[] formData = GetMultipartFormData(postParameters, formDataBoundary);

            return PostForm(postUrl, userAgent, contentType, formData);
        }

        /// <summary>
        /// Post a form
        /// </summary>
        private static HttpWebResponse PostForm(string postUrl, string userAgent, string contentType, byte[] formData)
        {
            HttpWebRequest request = WebRequest.Create(postUrl) as HttpWebRequest;

            if (request == null)
            {
                throw new NullReferenceException("request is not a http request");
            }

            // Add these, as we're doing a POST
            request.Method = "POST";
            request.ContentType = contentType;
            request.UserAgent = userAgent;
            request.CookieContainer = new CookieContainer();

            // We need to count how many bytes we're sending. 
            request.ContentLength = formData.Length;

            using (Stream requestStream = request.GetRequestStream())
            {
                // Push it out there
                requestStream.Write(formData, 0, formData.Length);
                requestStream.Close();
            }

            return request.GetResponse() as HttpWebResponse;
        }

        /// <summary>
        /// Turn the key and value pairs into a multipart form.
        /// See http://www.ietf.org/rfc/rfc2388.txt for issues about file uploads
        /// </summary>
        private static byte[] GetMultipartFormData(Dictionary<string, object> postParameters, string boundary)
        {
            Stream formDataStream = new MemoryStream();

            foreach (var param in postParameters)
            {
                if (param.Value is byte[])
                {
                    byte[] fileData = param.Value as byte[];

                    // Add just the first part of this param, since we will write the file data directly to the Stream
                    string header =
                        string.Format(
                            "--{0}\r\nContent-Disposition: form-data; name=\"{1}\"; filename=\"{2}\";\r\nContent-Type: application/octet-stream\r\n\r\n",
                            boundary, param.Key, param.Key);
                    formDataStream.Write(encoding.GetBytes(header), 0, header.Length);

                    // Write the file data directly to the Stream, rather than serializing it to a string.  This 
                    formDataStream.Write(fileData, 0, fileData.Length);
                }
                else
                {
                    string postData =
                        string.Format("--{0}\r\nContent-Disposition: form-data; name=\"{1}\"\r\n\r\n{2}\r\n", boundary,
                                      param.Key, param.Value);
                    formDataStream.Write(encoding.GetBytes(postData), 0, postData.Length);
                }
            }

            // Add the end of the request
            string footer = "\r\n--" + boundary + "--\r\n";
            formDataStream.Write(encoding.GetBytes(footer), 0, footer.Length);

            // Dump the Stream into a byte[]
            formDataStream.Position = 0;
            byte[] formData = new byte[formDataStream.Length];
            formDataStream.Read(formData, 0, formData.Length);
            formDataStream.Close();

            return formData;
        }

        public static bool HasEastAsianFontSupport()
        {
            if (Utils.IsVistaOrLater)
            {
                return true;
            }
            return Convert.ToBoolean(NativeMethods.IsValidLocale(CultureInfo.CreateSpecificCulture("zh").LCID, NativeConstants.LCID_INSTALLED));
        }
    }
}