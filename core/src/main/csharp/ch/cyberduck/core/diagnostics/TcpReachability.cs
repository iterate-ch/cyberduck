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
using System.Net;
using System.Net.Cache;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using ch.cyberduck.core;
using ch.cyberduck.core.diagnostics;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Core.Diagnostics
{
    public class TcpReachability : Reachability, Reachability.Diagnostics
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(TcpReachability).FullName);

        static TcpReachability()
        {
            ServicePointManager.SecurityProtocol =
                SecurityProtocolType.Tls | SecurityProtocolType.Tls11 | SecurityProtocolType.Tls12;
        }

        public bool isReachable(Host h)
        {
            try
            {
                switch ((Scheme.__Enum)h.getProtocol().getScheme().ordinal())
                {
                    case Scheme.__Enum.file:
                        return true;

                    case Scheme.__Enum.http:
                    case Scheme.__Enum.https:
                        try
                        {
                            WebRequest.DefaultWebProxy.Credentials = CredentialCache.DefaultNetworkCredentials;
                            WebRequest.DefaultCachePolicy = new RequestCachePolicy(RequestCacheLevel.NoCacheNoStore);
                            var url = new HostUrlProvider().withUsername(false).withPath(true).get(h);
                            if (Log.isDebugEnabled())
                            {
                                Log.debug($"Reachability test with url {url}");
                            }

                            HttpWebRequest request = WebRequest.CreateHttp(url);
                            request.UserAgent = new PreferencesUseragentProvider().get();
                            request.Timeout = 10000;
                            using (request.GetResponse())
                            {
                                return true;
                            }
                        }
                        catch (WebException e)
                        {
                            switch (e.Status)
                            {
                                // TLS version not supported on .NET Framework/Windows-Kernel
                                case WebExceptionStatus.SecureChannelFailure:
                                // HTTP returned error
                                case WebExceptionStatus.ProtocolError:
                                //Certificate not trusted
                                case WebExceptionStatus.TrustFailure:
                                // not an exception?
                                case WebExceptionStatus.Success:
                                    return true;

                                default:
                                    if (Log.isDebugEnabled())
                                    {
                                        Log.debug($"WebException thrown with status {e.Status} for {h}");
                                    }

                                    return false;
                            }
                        }

                    default:
                        if (Log.isDebugEnabled())
                        {
                            Log.debug($"Try TCP connection to {h.getHostname()}:{h.getPort()}");
                        }

                        using (new TcpClient(h.getHostname(), h.getPort()))
                        {
                            return true;
                        }
                }
            }
            catch (Exception e)
            {
                Log.warn($"Reachability check for {h} failed {e.Message}");
                return false;
            }
        }

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
