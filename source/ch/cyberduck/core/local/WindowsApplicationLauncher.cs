// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Local
{
    internal sealed class WindowsApplicationLauncher : ApplicationLauncher
    {
        private static readonly Logger Log = Logger.getLogger(typeof (WindowsApplicationLauncher).Name);

        public void bounce(ch.cyberduck.core.local.Local local)
        {
            ;
        }

        public bool open(ch.cyberduck.core.local.Local local)
        {
            return Utils.StartProcess(local.getAbsolute());
        }

        public bool open(ch.cyberduck.core.local.Local local, Application application)
        {
            return open(application, local.getAbsolute());
        }

        public bool open(Application application, string args)
        {
            return Utils.StartProcess(application.getIdentifier(), args);
        }

        public static void Register()
        {
            ApplicationLauncherFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : ApplicationLauncherFactory
        {
            protected override object create()
            {
                return new WindowsApplicationLauncher();
            }
        }
    }
}