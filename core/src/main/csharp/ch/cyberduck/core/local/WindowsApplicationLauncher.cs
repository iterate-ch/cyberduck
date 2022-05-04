// 
// Copyright (c) 2010-2022 Yves Langisch. All rights reserved.
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

using System.Diagnostics;
using ch.cyberduck.core.local;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Core.Local
{
    public sealed class WindowsApplicationLauncher : ApplicationLauncher
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(WindowsApplicationLauncher).Name);

        public void bounce(ch.cyberduck.core.Local local)
        {
            //
        }

        public bool open(ch.cyberduck.core.Local local)
        {
            Process process = new Process();
            process.StartInfo.FileName = "\"" + local.getAbsolute() + "\"";
            process.StartInfo.UseShellExecute = true;
            return Utils.StartProcess(process);
        }

        public bool open(ch.cyberduck.core.Local local, Application application)
        {
            if (application is IInvokeApplication invoke)
            {
                invoke.Launch(local);
                return true;
            }
            else
            {
                Process process = new Process();
                if (null == application)
                {
                    process.StartInfo.FileName = "\"" + local.getAbsolute() + "\"";
                }
                else
                {
                    process.StartInfo.FileName = GetExecutableCommand(application.getIdentifier());
                    process.StartInfo.UseShellExecute = true;
                    process.StartInfo.Arguments = "\"" + local.getAbsolute() + "\"";
                }

                return Utils.StartProcess(process);
            }
        }

        public bool open(Application application, string args)
        {
            Process process = new Process();
            process.StartInfo.FileName = GetExecutableCommand(application.getIdentifier());
            process.StartInfo.UseShellExecute = true;
            if (Utils.IsNotBlank(args))
            {
                process.StartInfo.Arguments = args;
            }

            return Utils.StartProcess(process);
        }

        public static string GetExecutableCommand(string command)
        {
            if (Utils.IsNotBlank(command) && command.Contains(".exe"))
            {
                return command.Substring(0, command.LastIndexOf(".exe") + 4);
            }

            return command;
        }

        internal interface IInvokeApplication
        {
            void Launch(ch.cyberduck.core.Local local);
        }
    }
}
