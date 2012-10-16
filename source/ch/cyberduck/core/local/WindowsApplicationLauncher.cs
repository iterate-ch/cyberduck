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
using System.ComponentModel;
using System.Diagnostics;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.local;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Local
{
    internal sealed class WindowsApplicationLauncher : ApplicationLauncher
    {
        private static readonly Logger Log = Logger.getLogger(typeof (WindowsApplicationLauncher).Name);

        public void bounce(ch.cyberduck.core.local.Local local)
        {
            throw new NotImplementedException();
        }

        public bool open(ch.cyberduck.core.local.Local local)
        {
            try
            {
                Process.Start(local.getAbsolute());
                return true;
            }
            catch (Win32Exception e)
            {
                Log.error(String.Format("StartProcess: {0},{1}", e.Message, e.NativeErrorCode));
                return false;
            }
        }

        public bool open(ch.cyberduck.core.local.Local local, Application application)
        {
            return open(application, local.getAbsolute());
        }

        public bool open(Application application, string args)
        {
            try
            {
                Process.Start(application.getIdentifier(), args);
                return true;
            }
            catch (Win32Exception e)
            {
                Log.error(String.Format("StartProcess: {0},{1}", e.Message, e.NativeErrorCode));
                return false;
            }
        }

        public static void register()
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