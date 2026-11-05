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

using System;
using System.Diagnostics;
using System.Net.NetworkInformation;
using System.Runtime.InteropServices;
using System.Threading;
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
        private const ulong WINHTTP_PROXY_NOTIFY_CHANGE = 0x1;
        // Proxy settings are written in bursts, wait for them to settle
        private static readonly TimeSpan ProxyChangeDelay = TimeSpan.FromMilliseconds(500);

        private static readonly Logger Log = LogManager.getLogger(typeof(NetworkChangeMonitor).FullName);

        [UnmanagedFunctionPointer(CallingConvention.Winapi)]
        private delegate void ProxyChangeCallback(ulong flags, IntPtr context);

        [DllImport("winhttp.dll")]
        private static extern uint WinHttpRegisterProxyChangeNotification(ulong flags, ProxyChangeCallback callback, IntPtr context, out IntPtr registration);

        [DllImport("winhttp.dll")]
        private static extern uint WinHttpUnregisterProxyChangeNotification(IntPtr registration);

        private readonly Reachability.Callback _callback;
        // Keep a reference to prevent the delegate from being collected while registered
        private readonly ProxyChangeCallback _proxyChanged;
        private readonly Timer _proxyChangeTimer;
        private IntPtr _proxyRegistration;

        public NetworkChangeMonitor(Host h, Reachability.Callback callback)
        {
            _callback = callback;
            _proxyChanged = (flags, context) => _proxyChangeTimer.Change(ProxyChangeDelay, Timeout.InfiniteTimeSpan);
            _proxyChangeTimer = new Timer(_ => _callback.change(), null, Timeout.Infinite, Timeout.Infinite);
        }

        public Reachability.Monitor start()
        {
            NetworkChange.NetworkAvailabilityChanged += Changed;
            NetworkChange.NetworkAddressChanged += Changed;
            if (_proxyRegistration == IntPtr.Zero)
            {
                var result = WinHttpRegisterProxyChangeNotification(WINHTTP_PROXY_NOTIFY_CHANGE, _proxyChanged, IntPtr.Zero, out var registration);
                if (result == 0)
                {
                    _proxyRegistration = registration;
                }
                else
                {
                    Log.warn($"Failure {result} registering for proxy configuration changes");
                }
            }
            return this;
        }

        public Reachability.Monitor stop()
        {
            NetworkChange.NetworkAvailabilityChanged -= Changed;
            NetworkChange.NetworkAddressChanged -= Changed;
            if (_proxyRegistration != IntPtr.Zero)
            {
                WinHttpUnregisterProxyChangeNotification(_proxyRegistration);
                _proxyRegistration = IntPtr.Zero;
            }
            _proxyChangeTimer.Change(Timeout.Infinite, Timeout.Infinite);
            return this;
        }

        void Changed(object sender, NetworkAvailabilityEventArgs args)
        {
            _callback.change();
        }

        void Changed(object sender, EventArgs args)
        {
            _callback.change();
        }
    }
}
