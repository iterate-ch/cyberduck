package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Rendezvous extends Observable implements ServiceListener {
    private static Logger log = Logger.getLogger(Rendezvous.class);

    private static final String[] serviceTypes = new String[]{
        "_sftp._tcp.local.",
        "_ssh._tcp.local.",
        "_ftp._tcp.local."
    };

    private Map services;
    private JmDNS jmDNS;

    public Rendezvous() {
        log.debug("Rendezvous");
        this.services = new HashMap();
    }

    public void init() {
        log.debug("init");
        new Thread() {
            public void run() {
                try {
                    Rendezvous.this.jmDNS = new JmDNS(java.net.InetAddress.getLocalHost());
                    for (int i = 0; i < serviceTypes.length; i++) {
                        log.info("Adding Rendezvous service listener for " + serviceTypes[i]);
                        Rendezvous.this.jmDNS.addServiceListener(serviceTypes[i], Rendezvous.this);
                    }
                }
                catch (IOException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void quit() {
        log.info("Removing Rendezvous service listener");
        if (this.jmDNS != null) {
            this.jmDNS.removeServiceListener(this);
        }
    }

    public void callObservers(Message arg) {
        if (log.isDebugEnabled()) {
            log.debug("callObservers:" + arg);
            log.debug(this.countObservers() + " observer(s) known.");
        }
        this.setChanged();
        this.notifyObservers(arg);
    }

    public Host getService(String key) {
        log.debug("getService:" + key);
        return (Host)services.get(key);
    }

    /**
     * This method is called when jmDNS discovers a service
     * for the first time. Only its name and type are known. We can
     * now request the service information.
     *
     * @param type something like _ftp._tcp.local.
     */
    public void addService(JmDNS j, String type, String name) {
        log.debug("addService:" + name + "," + type);
        j.requestServiceInfo(type, name);
    }

    /**
     * This method is called when the ServiceInfo record is resolved.
     * The ServiceInfo.getURL() constructs an http url given the addres,
     * port, and path properties found in the ServiceInfo record.
     */
    public void resolveService(JmDNS jmDNS, String type, String name, ServiceInfo info) {
		log.debug("resolveService:" + name + "," + type + "," + info);
        if (info != null) {
            log.info("Rendezvous Service Name:" + info.getName());
            log.info("Rendezvous Server Name:" + info.getServer());

            //Host(String hostname, int port, Login login, String nickname)
            Host h = new Host(info.getServer(),
                    info.getPort(),
                    new Login(info.getServer(), Preferences.instance().getProperty("connection.login.name"), null));
			if(h.getProtocol().equals(Session.FTP)) {
				h.setLogin(new Login(info.getServer(), null, null)); //use anonymous login for FTP
			}
			
            String identifier = info.getServer() + " (" + Host.getDefaultProtocol(info.getPort()).toUpperCase() + ")";
            //@todo dont't rely on standard port numbers
			
            this.services.put(identifier, h);
            this.callObservers(new Message(Message.RENDEZVOUS_ADD, identifier));
        }
        else {
            log.error("Failed to resolve " + name + " with type " + type);
        }
    }

    /**
     * This method is called when a service is no longer available.
     */
    public void removeService(JmDNS j, String type, String name) {
        log.debug("removeService:" + name);
        ServiceInfo info = j.getServiceInfo(type, name);
        if (info != null) {
            String identifier = info.getServer() + " (" + Host.getDefaultProtocol(info.getPort()).toUpperCase() + ")";
            this.services.remove(identifier);
            this.callObservers(new Message(Message.RENDEZVOUS_REMOVE, identifier));
        }
    }
}
