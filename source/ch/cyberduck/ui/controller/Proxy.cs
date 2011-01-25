// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using System.Net;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Ui.Controller
{
    public class Proxy : AbstractProxy
    {
        private readonly IWebProxy _webProxy = WebRequest.DefaultWebProxy;

        private Uri GetProxy(string scheme, string hostname)
        {
            Uri uri = new Uri(scheme + hostname);
            Uri proxy = _webProxy.GetProxy(uri);
            return proxy;
        }

        public override bool usePassiveFTP()
        {
            return true;
        }

        public override bool isSOCKSProxyEnabled(Host host)
        {
            Uri proxy = GetProxy("socks://", host.getHostname());
            return !proxy.Equals(new Uri("socks://" + host.getHostname()));
        }

        public override string getSOCKSProxyHost(Host host)
        {
            Uri proxy = GetProxy("socks://", host.getHostname());
            return proxy.Host;
        }

        public override int getSOCKSProxyPort(Host host)
        {
            Uri proxy = GetProxy("socks://", host.getHostname());
            return proxy.Port;
        }

        public override bool isHTTPProxyEnabled(Host host)
        {
            Uri proxy = GetProxy("http://", host.getHostname());
            return !proxy.Equals(new Uri("http://" + host.getHostname()));
        }

        public override string getHTTPProxyHost(Host host)
        {
            Uri proxy = GetProxy("http://", host.getHostname());
            return proxy.Host;
        }

        public override int getHTTPProxyPort(Host host)
        {
            Uri proxy = GetProxy("http://", host.getHostname());
            return proxy.Port;
        }

        public override bool isHTTPSProxyEnabled(Host host)
        {
            Uri proxy = GetProxy("https://", host.getHostname());
            return !proxy.Equals(new Uri("https://" + host.getHostname()));
        }

        public override string getHTTPSProxyHost(Host host)
        {
            Uri proxy = GetProxy("https://", host.getHostname());
            return proxy.Host;
        }

        public override int getHTTPSProxyPort(Host host)
        {
            Uri proxy = GetProxy("https://", host.getHostname());
            return proxy.Port;
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