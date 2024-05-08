// 
// Copyright (c) 2010-2020 Yves Langisch. All rights reserved.
// https://cyberduck.io/
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

using System.Diagnostics;
using System.Net.NetworkInformation;
using ch.cyberduck.core;
using ch.cyberduck.core.diagnostics;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Core.Diagnostics
{
    public class TcpReachability : Reachability, Reachability.Diagnostics
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(TcpReachability).FullName);

        public void test(Host h)
        {
            //
        }

        public bool isReachable(Host bookmark)
            => Reachability.__DefaultMethods.isReachable(this, bookmark);

        public void diagnose(Host h)
        {
            Process.Start("Rundll32.exe", "ndfapi,NdfRunDllDiagnoseIncident");
        }

        Reachability.Monitor Reachability.monitor(Host h, Reachability.Callback callback)
        {
            return new NetworkChangeMonitor(h, callback);
        }
    }

    class NetworkChangeMonitor : Reachability.Monitor
    {
        private readonly Reachability.Callback _callback;

        public NetworkChangeMonitor(Host h, Reachability.Callback callback)
        {
            _callback = callback;
        }

        public Reachability.Monitor start()
        {
            NetworkChange.NetworkAvailabilityChanged += Changed;
            return this;
        }

        public Reachability.Monitor stop()
        {
            NetworkChange.NetworkAvailabilityChanged -= Changed;
            return this;
        }

        void Changed(object sender, NetworkAvailabilityEventArgs args)
        {
            _callback.change();
        }
    }
}
