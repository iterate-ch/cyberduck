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
// yves@langisch.ch
// 
using System;
using System.Net.Sockets;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Ui.Controller
{
    public class TcpReachability : Reachability
    {
        public bool isReachable(Host h)
        {
            if (!ch.cyberduck.core.Preferences.instance().getBoolean("connection.hostname.check"))
            {
                return true;
            }

            try
            {
                //todo honour proxy settings
                TcpClient clnt = new TcpClient(h.getHostname(), h.getPort());
                clnt.Close();
                return true;
            }
            catch (Exception)
            {
                return false;
            }
        }

        public void diagnose(Host h)
        {
            ;
        }

        public static void Register()
        {
            ReachabilityFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : ReachabilityFactory
        {
            protected override object create()
            {
                return new TcpReachability();
            }
        }
    }
}