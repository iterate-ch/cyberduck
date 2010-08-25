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
using ch.cyberduck.core;

namespace Ch.Cyberduck.Ui.Controller
{
    public class Proxy : AbstractProxy
    {
        public override bool isSOCKSProxyEnabled()
        {
            return false;
        }

        public override string getSOCKSProxyHost()
        {
            return null;
        }

        public override bool usePassiveFTP()
        {
            return true;
        }

        public override bool isHTTPProxyEnabled()
        {
            return false;
        }

        public override string getHTTPProxyHost()
        {
            throw new NotImplementedException();
        }

        public override int getHTTPProxyPort()
        {
            throw new NotImplementedException();
        }

        public override bool isHTTPSProxyEnabled()
        {
            return false;
        }

        public override string getHTTPSProxyHost()
        {
            throw new NotImplementedException();
        }

        public override int getHTTPSProxyPort()
        {
            throw new NotImplementedException();
        }

        public override bool isHostExcluded(string hostname)
        {
            return false;
        }

        public override int getSOCKSProxyPort()
        {
            throw new NotImplementedException();
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