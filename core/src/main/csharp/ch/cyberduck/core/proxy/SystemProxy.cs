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
using System.Net;
using ch.cyberduck.core;
using ch.cyberduck.core.proxy;
using ch.cyberduck.core.preferences;

using InetSocketAddress = java.net.InetSocketAddress;

namespace Ch.Cyberduck.Core.Proxy
{
    public class SystemProxy : AbstractProxyFinder
    {
        private readonly IWebProxy _system = WebRequest.GetSystemWebProxy();

        public override ch.cyberduck.core.proxy.Proxy find(Host host)
        {
            if (!PreferencesFactory.get().getBoolean("connection.proxy.enable"))
            {
                return ch.cyberduck.core.proxy.Proxy.DIRECT;
            }
            Uri target;
            try
            {
                target = new Uri(new ProxyHostUrlProvider().get(host));
            }
            catch (UriFormatException)
            {
                return ch.cyberduck.core.proxy.Proxy.DIRECT;
            }
            if (_system.IsBypassed(target))
            {
                return ch.cyberduck.core.proxy.Proxy.DIRECT;
            }
            Uri proxy = _system.GetProxy(target);
            return new ch.cyberduck.core.proxy.Proxy(ch.cyberduck.core.proxy.Proxy.Type.valueOf(proxy.Scheme.ToUpper()), proxy.Host, proxy.Port);
        }
    }
}