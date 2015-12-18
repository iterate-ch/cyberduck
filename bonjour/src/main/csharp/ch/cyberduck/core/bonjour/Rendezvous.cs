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
using System.Collections.Generic;
using System.Text;
using Bonjour;
using ch.cyberduck.core;
using ch.cyberduck.core.bonjour;

namespace Ch.Cyberduck.Core.Bonjour
{
    public class Rendezvous : AbstractRendezvous
    {
        private readonly Dictionary<string, DNSSDService> _browsers = new Dictionary<string, DNSSDService>();

        private DNSSDEventManager _eventManager;
        private DNSSDService _service;

        public override void init()
        {
            base.init();
            _eventManager = new DNSSDEventManager();
            _eventManager.ServiceFound += ServiceFound;
            _eventManager.ServiceLost += ServiceLost;
            _eventManager.ServiceResolved += ServiceResolved;
            _eventManager.OperationFailed += OperationFailed;
            _service = new DNSSDService();
            for (int i = 0; i < getServiceTypes().Length; i++)
            {
                _browsers.Add(getServiceTypes()[i], _service.Browse(0, 0, getServiceTypes()[i], null, _eventManager));
            }
        }

        public override void quit()
        {
            if (null == _eventManager)
            {
                return;
            }
            _eventManager.ServiceFound -= ServiceFound;
            _eventManager.ServiceLost -= ServiceLost;
            _eventManager.ServiceResolved -= ServiceResolved;
            _eventManager.OperationFailed -= OperationFailed;
            for (int i = 0; i < getServiceTypes().Length; i++)
            {
                DNSSDService browser;
                if (_browsers.TryGetValue(getServiceTypes()[i], out browser))
                {
                    if (null != browser)
                    {
                        browser.Stop();
                    }
                }
            }
            if (_service != null)
            {
                _service.Stop();
            }
            base.quit();
        }

        //
        // ServiceFound
        //
        // This call is invoked by the DNSService core.  We create
        // a BrowseData object and invoked the appropriate method
        // in the GUI thread so we can update the UI
        //
        public void ServiceFound(DNSSDService service, DNSSDFlags flags, uint ifIndex, String serviceName,
                                 String regType, String domain)
        {
            service.Resolve(flags, ifIndex, serviceName, regType, domain, _eventManager);
        }

        public void ServiceLost(DNSSDService service, DNSSDFlags flags, uint ifIndex, String serviceName, String regType,
                                String domain)
        {
            string fullname = serviceName + "." + regType + domain;
            base.remove(fullname);
        }

        public void ServiceResolved(DNSSDService service, DNSSDFlags flags, uint ifIndex, String fullName,
                                    String hostName, ushort port, TXTRecord txtRecord)
        {
            String user = null;
            String password = null;
            String path = null;
            if (txtRecord.ContainsKey("u"))
            {
                user = ByteArrayToString((byte[]) txtRecord.GetValueForKey("u"));
            }
            if (txtRecord.ContainsKey("p"))
            {
                password = ByteArrayToString((byte[]) txtRecord.GetValueForKey("p"));
            }
            if (txtRecord.ContainsKey("path"))
            {
                path = ByteArrayToString((byte[]) txtRecord.GetValueForKey("path"));
            }
            base.add(fullName, hostName, port, user, password, path);
            service.Stop();
        }

        private string ByteArrayToString(byte[] input)
        {
            UTF8Encoding enc = new UTF8Encoding();
            return enc.GetString(input);
        }

        public void OperationFailed(DNSSDService service, DNSSDError error)
        {
            service.Stop();
        }
    }
}