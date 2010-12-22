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
using System.Text.RegularExpressions;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using Microsoft.Win32;

namespace Ch.Cyberduck.Ui.Controller
{
    public class Proxy : AbstractProxy
    {
        private static bool IsProxyEnabled()
        {
            RegistryKey registry =
                Registry.CurrentUser.OpenSubKey("Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", false);
            if (registry != null)
            {
                return Convert.ToBoolean(registry.GetValue("ProxyEnable", 0));
            }
            return false;
        }

        private static string GetProxy(string protocol, out int port)
        {
            string host = null;
            port = 80;
            RegistryKey registry =
                Registry.CurrentUser.OpenSubKey("Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", false);
            if (registry != null)
            {
                string server = (string) registry.GetValue("ProxyServer");
                if (Utils.IsNotBlank(server))
                {
                    string[] l = server.Split(';');
                    foreach (string proxy in l)
                    {
                        Regex h = new Regex(String.Format("{0}=([\\w\\.]*)(:([\\d]+))?", protocol));
                        if (h.IsMatch(proxy))
                        {
                            Match match = h.Match(proxy);
                            host = match.Groups[1].Value;
                            if (match.Groups.Count == 4 && Utils.IsNotBlank(match.Groups[3].Value))
                            {
                                port = Convert.ToInt32(match.Groups[3].Value);
                            }
                            break;
                        }
                    }
                    if (l.Length == 1 && !"socks".Equals(protocol))
                    {
                        //Use the same proxy server for all protocols is checked
                        //server has the format 'host:port' only
                        string[] allServer = server.Split(':');
                        host = allServer[0];
                        if (allServer.Length == 2 && null != allServer[1])
                        {
                            port = Convert.ToInt32(allServer[1]);
                        }
                    }
                }
            }
            return host;
        }

        public static Regex WildcardToRegex(string pattern)
        {
            return new Regex("^" +
                             Regex.Escape(pattern).
                                 Replace("\\*", ".*").
                                 Replace("\\?", ".") +
                             "$");
        }

        public override bool isSOCKSProxyEnabled(Host host)
        {
            if(this.isHostExcluded(host.getHostname())) {
                return false;
            }
            if (IsProxyEnabled())
            {
                int port;
                string proxy = GetProxy("socks", out port);
                return (null != proxy);
            }
            return false;
        }

        public override string getSOCKSProxyHost()
        {
            int port;
            return GetProxy("socks", out port);
        }

        public override int getSOCKSProxyPort()
        {
            int port;
            GetProxy("socks", out port);
            return port;
        }

        public override bool usePassiveFTP()
        {
            return true;
        }

        public override bool isHTTPProxyEnabled(Host host)
        {
            if(this.isHostExcluded(host.getHostname())) {
                return false;
            }
            if (IsProxyEnabled())
            {
                int port;
                string proxy = GetProxy("http", out port);
                return (null != proxy);
            }
            return false;
        }

        public override string getHTTPProxyHost()
        {
            int port;
            return GetProxy("http", out port);
        }

        public override int getHTTPProxyPort()
        {
            int port;
            GetProxy("http", out port);
            return port;
        }

        public override bool isHTTPSProxyEnabled(Host host)
        {
            if(this.isHostExcluded(host.getHostname())) {
                return false;
            }
            if (IsProxyEnabled())
            {
                int port;
                string proxy = GetProxy("https", out port);
                return (null != proxy);
            }
            return false;
        }

        public override string getHTTPSProxyHost()
        {
            int port;
            return GetProxy("https", out port);
        }

        public override int getHTTPSProxyPort()
        {
            int port;
            GetProxy("https", out port);
            return port;
        }

        private bool isHostExcluded(string hostname)
        {
            RegistryKey registry =
                Registry.CurrentUser.OpenSubKey("Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings", false);
            if (registry != null)
            {
                string excludes = (string) registry.GetValue("ProxyOverride");
                if (Utils.IsNotBlank(excludes))
                {
                    string[] l = excludes.Split(';');
                    foreach (string exclude in l)
                    {
                        if (WildcardToRegex(exclude).IsMatch(hostname))
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public static void Register()
        {
            ProxyFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : ProxyFactory
        {
            protected override object create()
            {
                return new Proxy();
            }
        }
    }
}