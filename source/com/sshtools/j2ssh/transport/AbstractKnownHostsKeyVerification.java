/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.transport;

import java.io.*;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.j2ssh.transport.publickey.SshKeyPairFactory;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;
import com.sshtools.j2ssh.util.Base64;


/**
 * <p/>
 * An abstract <code>HostKeyVerification</code> class providing validation
 * against the known_hosts format.
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 * @since 0.2.0
 */
public abstract class AbstractKnownHostsKeyVerification
    implements HostKeyVerification {
	private static String defaultHostFile;
	private static Log log = LogFactory.getLog(HostKeyVerification.class);

	//private List deniedHosts = new ArrayList();
	private Map allowedHosts = new HashMap();
	private String knownhosts;
	private boolean hostFileWriteable;

	//private boolean expectEndElement = false;
	//private String currentElement = null;

	/**
	 * <p/>
	 * Constructs a host key verification instance reading the specified
	 * known_hosts file.
	 * </p>
	 *
	 * @param knownhosts the path of the known_hosts file
	 * @throws InvalidHostFileException if the known_hosts file is invalid
	 * @since 0.2.0
	 */
	public AbstractKnownHostsKeyVerification(String knownhosts)
	    throws InvalidHostFileException {
		InputStream in = null;
		try {
			//  If no host file is supplied, or there is not enough permission to load
			//  the file, then just create an empty list.
			if(knownhosts != null) {
				if(System.getSecurityManager() != null) {
					AccessController.checkPermission(new FilePermission(knownhosts, "read"));
				}
				//  Load the hosts file. Do not worry if fle doesnt exist, just disable
				//  save of
				File f = new File(knownhosts);
				if(f.exists()) {
					in = new FileInputStream(f);
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					String line;
					while((line = reader.readLine()) != null) {
						String host = null;
						String algorithm = null;
						String key = null;
						StringTokenizer tokens = new StringTokenizer(line, " ");
						if(tokens.hasMoreTokens())
							host = (String)tokens.nextElement();
						if(tokens.hasMoreTokens())
							algorithm = (String)tokens.nextElement();
						if(tokens.hasMoreTokens()) {
							key = (String)tokens.nextElement();
							SshPublicKey pk = SshKeyPairFactory.decodePublicKey(Base64.decode(key));
							/*if (host.indexOf(",") > -1) {
								host = host.substring(0, host.indexOf(","));
							}*/
							this.putAllowedKey(host, pk);
						}
					}
					reader.close();
					this.hostFileWriteable = f.canWrite();
				}
				else {
					// Try to create the file and its parents if necersary
					f.getParentFile().mkdirs();
					if(f.createNewFile()) {
						FileOutputStream out = new FileOutputStream(f);
						out.write(toString().getBytes());
						out.close();
						this.hostFileWriteable = true;
					}
					else {
						this.hostFileWriteable = false;
					}
				}
				if(!hostFileWriteable) {
					log.warn("Host file is not writeable.");
				}
				this.knownhosts = knownhosts;
			}
		}
		catch(AccessControlException ace) {
			hostFileWriteable = false;
			log.warn("Not enough permission to load a hosts file, so just creating an empty list");
		}
		catch(IOException ioe) {
			hostFileWriteable = false;
			log.info("Could not open or read "+knownhosts+": "+
			    ioe.getMessage());
		}
		finally {
			if(in != null) {
				try {
					in.close();
				}
				catch(IOException ioe) {
				}
			}
		}
	}

	/**
	 * <p/>
	 * Determines whether the host file is writable.
	 * </p>
	 *
	 * @return true if the host file is writable, otherwise false
	 * @since 0.2.0
	 */
	public boolean isHostFileWriteable() {
		return this.hostFileWriteable;
	}

	/**
	 * <p/>
	 * Called by the <code>verifyHost</code> method when the host key supplied
	 * by the host does not match the current key recording in the known hosts
	 * file.
	 * </p>
	 *
	 * @param host           the name of the host
	 * @param allowedHostKey the current key recorded in the known_hosts file.
	 * @param actualHostKey  the actual key supplied by the user
	 * @throws TransportProtocolException if an error occurs
	 * @since 0.2.0
	 */
	public abstract void onHostKeyMismatch(String host,
	                                       SshPublicKey allowedHostKey, SshPublicKey actualHostKey)
	    throws TransportProtocolException;

	/**
	 * <p/>
	 * Called by the <code>verifyHost</code> method when the host key supplied
	 * is not recorded in the known_hosts file.
	 * </p>
	 * <p/>
	 * <p></p>
	 *
	 * @param host the name of the host
	 * @param key  the public key supplied by the host
	 * @throws TransportProtocolException if an error occurs
	 * @since 0.2.0
	 */
	public abstract void onUnknownHost(String host, SshPublicKey key)
	    throws TransportProtocolException;

	/**
	 * <p/>
	 * Allows a host key, optionally recording the key to the known_hosts file.
	 * </p>
	 *
	 * @param host   the name of the host
	 * @param pk     the public key to allow
	 * @param always true if the key should be written to the known_hosts file
	 * @throws InvalidHostFileException if the host file cannot be written
	 * @since 0.2.0
	 */
	public void allowHost(String host, SshPublicKey pk, boolean always)
	    throws InvalidHostFileException {
		if(log.isDebugEnabled()) {
			log.debug("Allowing "+host+" with fingerprint "+
			    pk.getFingerprint());
		}

		// Put the host into the allowed hosts list, overiding any previous
		// entry
		this.putAllowedKey(host, pk);

		//allowedHosts.put(host, pk);
		// If we always want to allow then save the host file with the
		// new details
		if(always) {
			saveHostFile();
		}
	}

	/**
	 * <p/>
	 * Returns a Map of the allowed hosts.
	 * </p>
	 * <p/>
	 * <p/>
	 * The keys of the returned Map are comma separated strings of
	 * "hostname,ipaddress". The value objects are Maps containing a string
	 * key of the public key alogorithm name and the public key as the value.
	 * </p>
	 *
	 * @return the allowed hosts
	 * @since 0.2.0
	 */
	public Map allowedHosts() {
		return allowedHosts;
	}

	/**
	 * <p/>
	 * Removes an allowed host.
	 * </p>
	 *
	 * @param host the host to remove
	 * @since 0.2.0
	 */
	public void removeAllowedHost(String host) {
		Iterator it = allowedHosts.keySet().iterator();
		while(it.hasNext()) {
			StringTokenizer tokens = new StringTokenizer((String)it.next(), ",");
			while(tokens.hasMoreElements()) {
				String name = (String)tokens.nextElement();
				if(name.equals(host)) {
					allowedHosts.remove(name);
				}
			}
		}
	}

	/**
	 * <p/>
	 * Verifies a host key against the list of known_hosts.
	 * </p>
	 * <p/>
	 * <p/>
	 * If the host unknown or the key does not match the currently allowed host
	 * key the abstract <code>onUnknownHost</code> or
	 * <code>onHostKeyMismatch</code> methods are called so that the caller
	 * may identify and allow the host.
	 * </p>
	 *
	 * @param host the name of the host
	 * @param pk   the host key supplied
	 * @return true if the host is accepted, otherwise false
	 * @throws TransportProtocolException if an error occurs
	 * @since 0.2.0
	 */
	public boolean verifyHost(String host, SshPublicKey pk)
	    throws TransportProtocolException {
		String fingerprint = pk.getFingerprint();
		log.info("Verifying "+host+" host key");

		if(log.isDebugEnabled()) {
			log.debug("Fingerprint: "+fingerprint);
		}

		Iterator it = allowedHosts.keySet().iterator();

		while(it.hasNext()) {
			// Could be a comma delimited string of names/ip addresses
			String names = (String)it.next();

			if(names.equals(host)) {
				return validateHost(names, pk);
			}

			StringTokenizer tokens = new StringTokenizer(names, ",");

			while(tokens.hasMoreElements()) {
				// Try the allowed hosts by looking at the allowed hosts map
				String name = (String)tokens.nextElement();

				if(name.equalsIgnoreCase(host)) {
					return validateHost(names, pk);
				}
			}
		}

		// The host is unknown os ask the user
		onUnknownHost(host, pk);

		// Recheck and return the result
		return checkKey(host, pk);
	}

	private boolean validateHost(String names, SshPublicKey pk)
	    throws TransportProtocolException {
		// The host is allowed so check the fingerprint
		SshPublicKey pub = getAllowedKey(names, pk.getAlgorithmName()); //shPublicKey) allowedHosts.get(host + "#" + pk.getAlgorithmName());

		if((pub != null) && pk.equals(pub)) {
			return true;
		}
		else {
			// The host key does not match the recorded so call the abstract
			// method so that the user can decide
			if(pub == null) {
				onUnknownHost(names, pk);
			}
			else {
				onHostKeyMismatch(names, pub, pk);
			}

			// Recheck the after the users input
			return checkKey(names, pk);
		}
	}

	private boolean checkKey(String host, SshPublicKey key) {
		SshPublicKey pk = getAllowedKey(host, key.getAlgorithmName()); //shPublicKey) allowedHosts.get(host + "#" + key.getAlgorithmName());

		if(pk != null) {
			if(pk.equals(key)) {
				return true;
			}
		}

		return false;
	}

	private SshPublicKey getAllowedKey(String names, String algorithm) {
		if(allowedHosts.containsKey(names)) {
			Map map = (Map)allowedHosts.get(names);

			return (SshPublicKey)map.get(algorithm);
		}

		return null;
	}

	private void putAllowedKey(String host, SshPublicKey key) {
		if(!allowedHosts.containsKey(host)) {
			allowedHosts.put(host, new HashMap());
		}
		Map map = (Map)allowedHosts.get(host);
		map.put(key.getAlgorithmName(), key);
	}

	/**
	 * <p/>
	 * Save's the host key file to be saved.
	 * </p>
	 *
	 * @throws InvalidHostFileException if the host file is invalid
	 * @since 0.2.0
	 */
	public void saveHostFile() throws InvalidHostFileException {
		if(!hostFileWriteable) {
			throw new InvalidHostFileException("Host file is not writeable.");
		}

		log.info("Saving "+defaultHostFile);

		try {
			File f = new File(knownhosts);
			FileOutputStream out = new FileOutputStream(f);
			out.write(toString().getBytes());
			out.close();
		}
		catch(IOException e) {
			throw new InvalidHostFileException("Could not write to "+
			    knownhosts);
		}
	}

	/**
	 * <p/>
	 * Outputs the allowed hosts in the known_hosts file format.
	 * </p>
	 * <p/>
	 * <p/>
	 * The format consists of any number of lines each representing one key for
	 * a single host.
	 * </p>
	 * <code> titan,192.168.1.12 ssh-dss AAAAB3NzaC1kc3MAAACBAP1/U4Ed.....
	 * titan,192.168.1.12 ssh-rsa AAAAB3NzaC1kc3MAAACBAP1/U4Ed.....
	 * einstein,192.168.1.40 ssh-dss AAAAB3NzaC1kc3MAAACBAP1/U4Ed..... </code>
	 *
	 * @return
	 * @since 0.2.0
	 */
	public String toString() {
		String knownhosts = "";
		Map.Entry entry;
		Map.Entry entry2;
		Iterator it = allowedHosts.entrySet().iterator();

		while(it.hasNext()) {
			entry = (Map.Entry)it.next();

			Iterator it2 = ((Map)entry.getValue()).entrySet().iterator();

			while(it2.hasNext()) {
				entry2 = (Map.Entry)it2.next();

				SshPublicKey pk = (SshPublicKey)entry2.getValue();
				knownhosts += (entry.getKey().toString()+" "+
				    pk.getAlgorithmName()+" "+
				    Base64.encodeBytes(pk.getEncoded(), true)+"\n");
			}
		}

		return knownhosts;
	}
}
