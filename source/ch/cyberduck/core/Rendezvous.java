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
import java.util.Map;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;

import ch.cyberduck.core.Host;

/**
* @version $Id$
 */
public class Rendezvous extends Observable implements com.strangeberry.rendezvous.ServiceListener {
    private static Logger log = Logger.getLogger(Rendezvous.class);

    private static Rendezvous instance;
    private static String[] serviceTypes = new String[]{"_ftp._tcp.local.", "_ssh._tcp.local."};

    private Map services;

    private Rendezvous() {
	log.debug("Rendezvous");
	this.services = new HashMap();
//	System.getProperties().put("rendezvous.debug", "1");
    }

    public void init() {
	try {
	    for(int i = 0; i < serviceTypes.length; i++) {
		com.strangeberry.rendezvous.Rendezvous rendezvous = new com.strangeberry.rendezvous.Rendezvous();
		log.info("Adding Rendezvous service listener for "+serviceTypes[i]);
		rendezvous.addServiceListener(serviceTypes[i], this);
	    }
	}
	catch(IOException e) {
	    log.error(e.getMessage());
	}
    }

    public static Rendezvous instance() {
	log.debug("instance");
	if(null == instance) {
	    instance = new Rendezvous();
	}
	return instance;
    }

    public void callObservers(Message arg) {
	log.debug("callObservers:"+arg);
	this.setChanged();
	this.notifyObservers(arg);
    }

    public Object getService(String key) {
	log.debug("getService:"+key);
	return services.get(key);
    }

    public Map getServices() {
	return this.services;
    }

    /**
	* This method is called when rendezvous discovers a service
     * for the first time. Only its name and type are known. We can
     * now request the service information.
     * @param type something like _ftp._tcp.local.
     */
    public void addService(com.strangeberry.rendezvous.Rendezvous rendezvous, String type, String name) {
	log.debug("addService:"+name+","+type);
	rendezvous.requestServiceInfo(type, name);
    }

    /**
	* This method is called when the ServiceInfo record is resolved.
     * The ServiceInfo.getURL() constructs an http url given the addres,
     * port, and path properties found in the ServiceInfo record.
     */
    public void resolveService(com.strangeberry.rendezvous.Rendezvous rendezvous, String type, String name, com.strangeberry.rendezvous.ServiceInfo info) {
	if (info != null) {
	    log.debug("resolveService:"+name+","+type+","+info);
	    Host h = new Host(info.getName(), info.getPort(), new Login());
	    this.services.put(h.getURL(), h);
	    log.debug(info.toString());
//	    log.debug(rendezvous.getInterface());
	    //todo this.callObservers(new Message(Message.RENDEZVOUS, host));
	    this.callObservers(new Message(Message.RENDEZVOUS, "Rendezvous service resolved: "+name));
	}
	else {
	    log.error("Failed to resolve "+name+" with type "+type);
	}
    }

    /**
	* This method is called when a service is no longer available.
     */
    public void removeService(com.strangeberry.rendezvous.Rendezvous rendezvous, String type, String name) {
	log.debug("removeService:"+name);
	//@todothis.services.remove(services.indexOf(name));
	this.callObservers(new Message(Message.RENDEZVOUS, "Rendezvous service removed: "+name));
    }
}
