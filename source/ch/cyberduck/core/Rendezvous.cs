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
// yves@cyberduck.ch
// 
using System;
using System.Collections.Generic;
using Bonjour;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Core
{
    internal class Rendezvous : AbstractRendezvous
    {
        private readonly Dictionary<string, DNSSDService> browsers
            = new Dictionary<string, DNSSDService>();

        private DNSSDEventManager eventManager;
        private DNSSDService service;

        public override void init()
        {
            eventManager = new DNSSDEventManager();
            eventManager.ServiceFound += ServiceFound;
            eventManager.ServiceLost += ServiceLost;
            eventManager.ServiceResolved += ServiceResolved;
            eventManager.OperationFailed += OperationFailed;
            service = new DNSSDService();
            for (int i = 0; i < getServiceTypes().Length; i++)
            {
                browsers.Add(getServiceTypes()[i], service.Browse(0, 0, getServiceTypes()[i], null, eventManager));
            }
        }

        public override void quit()
        {
            if (null == eventManager)
            {
                return;
            }

            eventManager.ServiceFound -= ServiceFound;
            eventManager.ServiceLost -= ServiceLost;
            eventManager.ServiceResolved -= ServiceResolved;
            eventManager.OperationFailed -= OperationFailed;
            for (int i = 0; i < getServiceTypes().Length; i++)
            {
                DNSSDService browser;
                if (browsers.TryGetValue(getServiceTypes()[i], out browser))
                {
                    if (null != browser)
                    {
                        browser.Stop();
                    }
                }
            }
            if (service != null)
            {
                service.Stop();
            }
        }

        //
        // ServiceFound
        //
        // This call is invoked by the DNSService core.  We create
        // a BrowseData object and invoked the appropriate method
        // in the GUI thread so we can update the UI
        //
        public void ServiceFound(DNSSDService service,
                                 DNSSDFlags flags,
                                 uint ifIndex,
                                 String serviceName,
                                 String regType,
                                 String domain)
        {
            service.Resolve(flags, ifIndex, serviceName, regType, domain, eventManager);
        }

        public void ServiceLost(DNSSDService service,
                                DNSSDFlags flags,
                                uint ifIndex,
                                String serviceName,
                                String regType,
                                String domain)
        {
            string fullname = serviceName + "." + regType + domain;
            base.remove(fullname);
        }

        public void ServiceResolved(DNSSDService service,
                                    DNSSDFlags flags,
                                    uint ifIndex,
                                    String fullName,
                                    String hostName,
                                    ushort port,
                                    TXTRecord txtRecord)
        {
            String user = null;
            String password = null;
            String path = null;
            if (txtRecord.ContainsKey("u"))
            {
                user = txtRecord.GetValueForKey("u").ToString();
            }
            if (txtRecord.ContainsKey("p"))
            {
                password = txtRecord.GetValueForKey("p").ToString();
            }
            if (txtRecord.ContainsKey("path"))
            {
                path = txtRecord.GetValueForKey("path").ToString();
            }
            base.add(fullName, hostName, port, user, password, path);
            service.Stop();
        }

        public void OperationFailed(DNSSDService service, DNSSDError error)
        {
            service.Stop();
        }

        public static void Register()
        {
            RendezvousFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : RendezvousFactory
        {
            protected override object create()
            {
                return new Rendezvous();
            }
        }
    }
}