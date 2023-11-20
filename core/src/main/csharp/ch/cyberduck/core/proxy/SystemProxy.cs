// 
// Copyright (c) 2010-2020 Yves Langisch. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io
// 

using System;
using System.Globalization;
using System.Net;
using System.Threading;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.proxy;

namespace Ch.Cyberduck.Core.Proxy
{
    public class SystemProxy : AbstractProxyFinder
    {
        private readonly IWebProxy _system = WebRequest.GetSystemWebProxy();

        public override ch.cyberduck.core.proxy.Proxy find(string host)
        {
            Uri target;
            try
            {
                target = new Uri(host);
            }
            catch (UriFormatException)
            {
                return ch.cyberduck.core.proxy.Proxy.DIRECT;
            }

            if (_system.IsBypassed(target))
            {
                return ch.cyberduck.core.proxy.Proxy.DIRECT;
            }

            if (_system.GetProxy(target) is Uri proxy)
            {
                return new ch.cyberduck.core.proxy.Proxy(ch.cyberduck.core.proxy.Proxy.Type.valueOf(proxy.Scheme.ToUpper()),
                    proxy.Host, proxy.Port, proxy.UserInfo);
            }

            return ch.cyberduck.core.proxy.Proxy.DIRECT;
        }
    }
}
