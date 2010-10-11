// 
// Copyright (c) 2010 David Kocher. All rights reserved.
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
// dkocher@cyberduck.ch
// 
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Bonjour;

namespace ch.cyberduck.core
{
    class Rendezvous : ch.cyberduck.core.AbstractRendezvous
    {
        private Bonjour.DNSSDEventManager eventManager;

        private Bonjour.DNSSDService service;

        Dictionary<string, DNSSDService> browsers
            = new Dictionary<string, DNSSDService>();

        public override void init()
        {
            eventManager = new DNSSDEventManager();
            eventManager.ServiceFound += new _IDNSSDEvents_ServiceFoundEventHandler(this.ServiceFound);
            eventManager.ServiceLost += new _IDNSSDEvents_ServiceLostEventHandler(this.ServiceLost);
            eventManager.ServiceResolved += new _IDNSSDEvents_ServiceResolvedEventHandler(this.ServiceResolved);
            eventManager.OperationFailed += new _IDNSSDEvents_OperationFailedEventHandler(this.OperationFailed);
            service = new DNSSDService();
            for (int i = 0; i < this.getServiceTypes().Length; i++)
            {
                browsers.Add(this.getServiceTypes()[i], service.Browse(0, 0, this.getServiceTypes()[i], null, eventManager));
            }
        }

        public override void quit()
        {
            eventManager.ServiceFound -= new _IDNSSDEvents_ServiceFoundEventHandler(this.ServiceFound);
            eventManager.ServiceLost -= new _IDNSSDEvents_ServiceLostEventHandler(this.ServiceLost);
            eventManager.ServiceResolved -= new _IDNSSDEvents_ServiceResolvedEventHandler(this.ServiceResolved);
            eventManager.OperationFailed -= new _IDNSSDEvents_OperationFailedEventHandler(this.OperationFailed);
            for (int i = 0; i < this.getServiceTypes().Length; i++)
            {
                DNSSDService browser;
                if (browsers.TryGetValue(this.getServiceTypes()[i], out browser))
                {
                    browser.Stop();
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
        public void ServiceFound(
                        DNSSDService service,
                        DNSSDFlags flags,
                        uint ifIndex,
                        String serviceName,
                        String regType,
                        String domain
                        )
        {
            service.Resolve(flags, ifIndex, serviceName, regType, domain, eventManager);
        }

        public void ServiceLost(
                        DNSSDService service,
                        DNSSDFlags flags,
                        uint ifIndex,
                        String serviceName,
                        String regType,
                        String domain
                        )
        {
            string fullname = null;
            base.remove(fullname);
        }

        public void ServiceResolved(
                        DNSSDService service,
                        DNSSDFlags flags,
                        uint ifIndex,
                        String fullName,
                        String hostName,
                        ushort port,
                        TXTRecord txtRecord
                        )
        {
            String user = null;
            String password = null;
            String path = null;
            if (txtRecord.ContainsKey("u"))
            {
                //user = txtRecord.GetValueForKey("u").ToString();
            }
            if (txtRecord.ContainsKey("p"))
            {
                //password = txtRecord.GetValueForKey("p").ToString();
            }
            if (txtRecord.ContainsKey("path"))
            {
                //path = txtRecord.GetValueForKey("path").ToString();
            }
            base.add(fullName, hostName, port, user, password, path);
            service.Stop();
        }

        public void OperationFailed(
                DNSSDService service,
                DNSSDError error
                )
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