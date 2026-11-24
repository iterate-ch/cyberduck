// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.Collections.Concurrent;
using System.Collections.Generic;
using ch.cyberduck.core.bonjour;
using org.apache.logging.log4j;
using Windows.Devices.Enumeration;

namespace Ch.Cyberduck.Core.Bonjour
{
    /// <summary>
    /// Service discovery using the DNS-SD client of Windows 10.
    /// </summary>
    public class Rendezvous : AbstractRendezvous
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(Rendezvous).FullName);

        private const string DnssdProtocolId = "{4526e8c1-8aac-4153-9b16-55e86ada0e54}";
        private const string Domain = "local.";

        private static readonly string[] Properties =
        {
            "System.Devices.Dnssd.HostName",
            "System.Devices.Dnssd.PortNumber",
            "System.Devices.Dnssd.TextAttributes",
            "System.Devices.Dnssd.InstanceName",
            "System.Devices.Dnssd.ServiceName"
        };

        private readonly List<Browser> _browsers = new List<Browser>();

        public override void init()
        {
            base.init();
            foreach (string type in getServiceTypes())
            {
                try
                {
                    Browser browser = new Browser(this, type.TrimEnd('.'));
                    browser.Start();
                    _browsers.Add(browser);
                }
                catch (Exception e)
                {
                    Log.error(string.Format("Failure browsing for service type {0}: {1}", type, e.Message), e);
                }
            }
        }

        public override void quit()
        {
            foreach (Browser browser in _browsers)
            {
                browser.Stop();
            }
            _browsers.Clear();
            base.quit();
        }

        private static string GetAttribute(IEnumerable<string> attributes, string key)
        {
            if (null == attributes)
            {
                return null;
            }
            foreach (string attribute in attributes)
            {
                int separator = attribute.IndexOf('=');
                if (separator > 0 && string.Equals(attribute.Substring(0, separator), key, StringComparison.OrdinalIgnoreCase))
                {
                    return attribute.Substring(separator + 1);
                }
            }
            return null;
        }

        /// <summary>
        /// Watch for instances of a single service type.
        /// </summary>
        private sealed class Browser
        {
            private readonly Rendezvous _rendezvous;
            private readonly string _type;
            private readonly DeviceWatcher _watcher;

            /// <summary>
            /// Device information by identifier of the service instance.
            /// </summary>
            private readonly ConcurrentDictionary<string, DeviceInformation> _devices = new ConcurrentDictionary<string, DeviceInformation>();

            /// <summary>
            /// Full service name by identifier of the service instance.
            /// </summary>
            private readonly ConcurrentDictionary<string, string> _names = new ConcurrentDictionary<string, string>();

            public Browser(Rendezvous rendezvous, string type)
            {
                _rendezvous = rendezvous;
                _type = type;
                string filter = string.Format(
                    "System.Devices.AepService.ProtocolId:={0} AND System.Devices.Dnssd.Domain:=\"{1}\" AND System.Devices.Dnssd.ServiceName:=\"{2}\"",
                    DnssdProtocolId, Domain.TrimEnd('.'), type);
                _watcher = DeviceInformation.CreateWatcher(filter, Properties, DeviceInformationKind.AssociationEndpointService);
                _watcher.Added += OnAdded;
                _watcher.Updated += OnUpdated;
                _watcher.Removed += OnRemoved;
            }

            public void Start()
            {
                Log.info(string.Format("Adding service listener for {0}", _type));
                _watcher.Start();
            }

            public void Stop()
            {
                Log.info(string.Format("Removing service listener for {0}", _type));
                _watcher.Added -= OnAdded;
                _watcher.Updated -= OnUpdated;
                _watcher.Removed -= OnRemoved;
                try
                {
                    if (_watcher.Status == DeviceWatcherStatus.Started ||
                        _watcher.Status == DeviceWatcherStatus.EnumerationCompleted)
                    {
                        _watcher.Stop();
                    }
                }
                catch (Exception e)
                {
                    Log.warn(string.Format("Failure stopping service listener for {0}: {1}", _type, e.Message), e);
                }
            }

            private void OnAdded(DeviceWatcher sender, DeviceInformation device)
            {
                _devices[device.Id] = device;
                Resolve(device);
            }

            private void OnUpdated(DeviceWatcher sender, DeviceInformationUpdate update)
            {
                DeviceInformation device;
                if (_devices.TryGetValue(update.Id, out device))
                {
                    device.Update(update);
                    Resolve(device);
                }
            }

            private void OnRemoved(DeviceWatcher sender, DeviceInformationUpdate update)
            {
                DeviceInformation device;
                _devices.TryRemove(update.Id, out device);
                string name;
                if (_names.TryRemove(update.Id, out name))
                {
                    _rendezvous.remove(name);
                }
            }

            private void Resolve(DeviceInformation device)
            {
                try
                {
                    string host = Property<string>(device, "System.Devices.Dnssd.HostName");
                    object port = Property<object>(device, "System.Devices.Dnssd.PortNumber");
                    if (string.IsNullOrEmpty(host) || null == port)
                    {
                        Log.debug(string.Format("Service {0} not yet resolved", device.Name));
                        return;
                    }
                    string instance = Property<string>(device, "System.Devices.Dnssd.InstanceName") ?? device.Name;
                    // Must contain the service type to identify the protocol
                    string name = string.Format("{0}.{1}.{2}", instance, _type, Domain);
                    string[] attributes = Property<string[]>(device, "System.Devices.Dnssd.TextAttributes");
                    _names[device.Id] = name;
                    _rendezvous.add(name, host.TrimEnd('.'), Convert.ToInt32(port),
                        GetAttribute(attributes, "u"), GetAttribute(attributes, "p"), GetAttribute(attributes, "path"));
                }
                catch (Exception e)
                {
                    Log.error(string.Format("Failure resolving service {0}: {1}", device.Name, e.Message), e);
                }
            }

            private static T Property<T>(DeviceInformation device, string key) where T : class
            {
                object value;
                return device.Properties.TryGetValue(key, out value) ? value as T : null;
            }
        }
    }
}
