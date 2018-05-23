// 
// Copyright (c) 2010-2018 Yves Langisch. All rights reserved.
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
using System.Diagnostics;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using ch.cyberduck.core;
using ch.cyberduck.core.diagnostics;

namespace Ch.Cyberduck.Core.Diagnostics
{
    public class TcpReachability : Reachability
    {
        public bool isReachable(Host h)
        {
            try
            {
                TcpClient c = new TcpClient(h.getHostname(), h.getPort());
                c.Close();
                return true;
            }
            catch (Exception)
            {
                return false;
            }
        }

        public void diagnose(Host h)
        {
            Process.Start("Rundll32.exe", "ndfapi,NdfRunDllDiagnoseIncident");
        }

        public void monitor(Host h, Reachability.Callback callback)
        {
            void Changed(object sender, NetworkAvailabilityEventArgs args)
            {
                callback.change();
                NetworkChange.NetworkAvailabilityChanged -= Changed;
            }

            NetworkChange.NetworkAvailabilityChanged += Changed;
        }
    }
}
