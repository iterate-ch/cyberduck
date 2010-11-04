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
using System.Windows.Forms;
using Microsoft.Win32;

namespace Ch.Cyberduck.Core
{
    class URLSchemeHandlerConfiguration
    {
        private static readonly URLSchemeHandlerConfiguration instance = new URLSchemeHandlerConfiguration();

        private URLSchemeHandlerConfiguration()
        {
        }

        public static URLSchemeHandlerConfiguration Instance
        {
            get { return instance; }
        }

        private void RegisterCyberduckUrlHandler(RegistryKey registry)
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
        public void RegisterFtpProtocol(RegistryKey registry)
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
        public bool IsDefaultApplicationForFtp()
        {
            RegistryKey ftpUserChoice = Registry.CurrentUser.OpenSubKey(
                @"Software\Microsoft\Windows\Shell\Associations\UrlAssociations\ftp\UserChoice");
            return (null != ftpUserChoice && "CyberduckURL".Equals(ftpUserChoice.GetValue("Progid")));
        }

        /// <summary>
        /// Check if Cyberduck is the default application for SFTP URLs
        /// </summary>
        /// <returns></returns>
        public bool IsDefaultApplicationForSftp()
        {
            RegistryKey sftpClass = Registry.CurrentUser.OpenSubKey(@"Software\Classes\sftp");
            if (null != sftpClass)
            {
                RegistryKey command = sftpClass.OpenSubKey(@"shell\open\command");
                if (null != command)
                {
                    var value = (string) command.GetValue("");
                    return (null != value && value.Contains("Cyberduck"));
                }
            }
            return false;
        }

        public void RegisterSftpProtocol(RegistryKey registry)
        {
            CreateCustomUrlHandler(registry, "sftp", "sftp protocol", Application.ExecutablePath,
                                   Application.ExecutablePath + ",0");
        }

        private void CreateCustomUrlHandler(RegistryKey registry, string association, string description,
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
    }
}