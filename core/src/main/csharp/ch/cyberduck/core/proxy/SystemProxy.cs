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

using ch.cyberduck.core.proxy;
using org.apache.logging.log4j;
using System;
using Logger = org.apache.logging.log4j.Logger;

namespace Ch.Cyberduck.Core.Proxy
{
    public partial class SystemProxy : AbstractProxyFinder
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(SystemProxy).FullName);

        public override ch.cyberduck.core.proxy.Proxy find(string host)
        {
            if (Log.isDebugEnabled())
            {
                Log.debug($"Finding proxy for {host}");
            }
            Uri target;
            try
            {
                target = new Uri(host);
            }
            catch (UriFormatException e)
            {
                Log.warn($"Failure finding proxy for {host}", e);
                return ch.cyberduck.core.proxy.Proxy.DIRECT;
            }

            var systemProxy = DefaultSystemProxy;
            if (systemProxy.IsBypassed(target) || systemProxy.GetProxy(target) is not { } uri)
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug($"Direct connection for {host}");
                }
                return ch.cyberduck.core.proxy.Proxy.DIRECT;
            }

            var proxy = new ch.cyberduck.core.proxy.Proxy(ch.cyberduck.core.proxy.Proxy.Type.valueOf(uri.Scheme.ToUpper()),
                uri.Host, uri.Port, uri.UserInfo);
            if (Log.isDebugEnabled())
            {
                Log.debug($"Proxy {proxy} found for {host}");
            }

            return proxy;
        }
    }
}
