package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import org.apache.log4j.Logger;

/**
* @version $Id$
 */
public class Rendezvous extends Observable implements javax.jmdns.ServiceListener {
    private static Logger log = Logger.getLogger(Rendezvous.class);
	
//    private static Rendezvous instance;
    private static final String[] serviceTypes = new String[]{"_sftp._tcp.local.", "_ftp._tcp.local.", "_ssh._tcp.local."};
	
    private Map services;
	private javax.jmdns.JmDNS jmDNS;
	
    public Rendezvous() {
		log.debug("Rendezvous");
		this.services = new HashMap();
    }
	
    public void init() {
		try {
			for(int i = 0; i < serviceTypes.length; i++) {
				this.jmDNS = new javax.jmdns.JmDNS();
				log.info("Adding Rendezvous service listener for "+serviceTypes[i]);
				this.jmDNS.addServiceListener(serviceTypes[i], this);
			}
		}
		catch(IOException e) {
			log.error(e.getMessage());
		}
    }
	
	public void quit() {
		log.info("Removing Rendezvous service listener");
		this.jmDNS.removeServiceListener(this);
	}
	
//    public static Rendezvous instance() {
//		log.debug("instance");
//		if(null == instance) {
//			instance = new Rendezvous();
//		}
//		return instance;
//    }
	
    public void callObservers(Message arg) {
		log.debug("callObservers:"+arg);
		this.setChanged();
		this.notifyObservers(arg);
    }
	
    public Object getService(String key) {
		log.debug("getService:"+key);
		return services.get(key);
	}
	
    /**
		* This method is called when jmDNS discovers a service
     * for the first time. Only its name and type are known. We can
     * now request the service information.
     * @param type something like _ftp._tcp.local.
     */
    public void addService(javax.jmdns.JmDNS jmDNS, String type, String name) {
		log.debug("addService:"+name+","+type);
		this.jmDNS.requestServiceInfo(type, name);
    }
	
    /**
		* This method is called when the ServiceInfo record is resolved.
     * The ServiceInfo.getURL() constructs an http url given the addres,
     * port, and path properties found in the ServiceInfo record.
     */
    public void resolveService(javax.jmdns.JmDNS jmDNS, String type, String name, javax.jmdns.ServiceInfo info) {
		if (info != null) {
			log.debug("resolveService:"+name+","+type+","+info);
			Host h = new Host(info.getName(), info.getServer(), info.getPort(), new Login(Preferences.instance().getProperty("connection.login.name"))); //todo fix .local.
			this.services.put(h.getURL(), h);
			log.debug(info.toString());
			this.callObservers(new Message(Message.RENDEZVOUS, h));
		}
		else {
			log.error("Failed to resolve "+name+" with type "+type);
		}
    }
	
    /**
		* This method is called when a service is no longer available.
     */
    public void removeService(javax.jmdns.JmDNS jmDNS, String type, String name) {
		log.debug("removeService:"+name);
		this.services.remove(name+".local.");
		//	this.callObservers(new Message(Message.RENDEZVOUS, this.services.get(name+".local.")));
    }
}
