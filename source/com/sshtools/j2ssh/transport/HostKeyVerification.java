/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;

import java.security.AccessControlException;
import java.security.AccessController;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Implement this class to define specific handling of server host
 * authorization events. The default hosts file is hosts.xml and should be
 * located in SSHTOOLS_HOME
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public abstract class HostKeyVerification extends DefaultHandler {
    private static String defaultHostFile;
    private static Logger log = Logger.getLogger(HostKeyVerification.class);

    private List deniedHosts = new ArrayList();
    private Map allowedHosts = new HashMap();
    private String hostFile;
    private boolean hostFileWriteable;
    private boolean expectEndElement = false;
    private String currentElement = null;

    static {
        log.info("Determining default host file");

        // Get the sshtools.home system property
        defaultHostFile = ConfigurationLoader.getConfigurationDirectory();

        if (defaultHostFile==null) {
            log.info("No configuration location, persistence of host keys will be disabled.");
        } else {
            // Set the default host file name to our hosts.xml
            defaultHostFile += "hosts.xml";

            log.info("Defaulting host file to " + defaultHostFile);
        }
    }



    /**
     * Constructs the object loading the default hosts file.
     *
     * @exception InvalidHostFileException if the host file does not exsist or
     *            is invalid
     */
    public HostKeyVerification()
                        throws InvalidHostFileException {
        this(defaultHostFile);
        hostFile = defaultHostFile;
    }

    /**
     * Constructs the object loading the host file specified.
     *
     * @param hostFileName the path to the host file
     *
     * @exception InvalidHostFileException if the host file does not exsist or
     *            is invalid
     */
    public HostKeyVerification(String hostFileName)
                        throws InvalidHostFileException {
        InputStream in = null;

        try {
            //  If no host file is supplied, or there is not enough permission to load
            //  the file, then just create an empty list.
            if (hostFileName!=null) {
                if (System.getSecurityManager()!=null) {
                    AccessController.checkPermission(new FilePermission(hostFileName,
                                                                        "read"));
                }

                //  Load the hosts file. Do not worry if fle doesnt exist, just disable
                //  save of
                File f = new File(hostFileName);

                if (f.exists()) {
                    in = new FileInputStream(f);
                    hostFile = hostFileName;

                    /**
                     * Load in the hosts file
                     */
                     SAXParserFactory saxFactory = SAXParserFactory.newInstance();
                     SAXParser saxParser = saxFactory.newSAXParser();

                     saxParser.parse(in, this);

                    hostFileWriteable = f.canWrite();
                } else {
                    hostFileWriteable = true;
                }

                if (!hostFileWriteable) {
                    log.warn("Host file is not writeable.");
                }
            }
        } catch (AccessControlException ace) {
            log.warn("Not enough permission to load a hosts file, so just creating an empty list");
        } catch (IOException ioe) {
            throw new InvalidHostFileException("Could not open or read "
                                               + hostFileName);
        } catch(SAXException sax) {
          throw new InvalidHostFileException("Failed XML parsing: " + sax.getMessage());
        } catch(ParserConfigurationException pce) {
          throw new InvalidHostFileException("Failed to initialize xml parser: " + pce.getMessage());
        } finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
    }


    public void startElement(String uri, String localName, String qname,
                              Attributes attrs) throws SAXException {

        if(currentElement==null) {
          if(qname.equals("HostAuthorizations")) {
             allowedHosts.clear();
             deniedHosts.clear();
             currentElement = qname;
          }
          else
            throw new SAXException("Unexpected document element!");
        }
        else {

          if(!currentElement.equals("HostAuthorizations"))
            throw new SAXException("Unexpected parent element found!");

          if(qname.equals("AllowHost")) {
            String hostname = attrs.getValue("HostName");
            String fingerprint = attrs.getValue("Fingerprint");

          if(hostname!=null && fingerprint !=null) {
            log.debug("AllowHost element for host '" + hostname + "' with fingerprint '" + fingerprint + "'");
            allowedHosts.put(hostname, fingerprint);
            currentElement = qname;
          } else
            throw new SAXException("Requried attribute(s) missing!");

        }else if(qname.equals("DenyHost")) {
          String hostname = attrs.getValue("HostName");

          if(hostname!=null) {
            log.debug("DenyHost element for host " + hostname);
            deniedHosts.add(hostname);
            currentElement = qname;
          } else
            throw new SAXException("Required attribute hostname missing");

        } else {
          log.warn("Unexpected " + qname + " element found in allowed hosts file");
        }

      }



    }


    public void endElement(String uri, String localName, String qname)
                                          throws SAXException {

        if(currentElement==null)
          throw new SAXException("Unexpected end element found!");

        if(currentElement.equals("HostAuthorizations")) {
           currentElement = null;
           return;
        }

        if(currentElement.equals("AllowHost")) {
          currentElement = "HostAuthorizations";
          return;
        }

        if(currentElement.equals("DenyHost")) {
          currentElement = "HostAuthorizations";
          return;
        }
    }
    /**
     * Return if the host file is writeable. If not, then don't provide the
     * 'Always' option.
     *
     * @return host file writable
     */
    public boolean isHostFileWriteable() {
        return hostFileWriteable;
    }

    /**
     * <p>
     * Abstract method called by the framework when a connection has been made
     * to a denied host.
     * </p>
     *
     * <p>
     * NOTE: This currently does not supply the fingerprint of the denied host
     * for a reason; if the host has been denied, most probably by an
     * administrator then we should not be providing the host key for the user
     * to subsequently allow the host.
     * </p>
     *
     * @param host The name of the denied host.
     */
    public abstract void onDeniedHost(String host)
                                           throws TransportProtocolException;

    /**
     * Abstract method called by the framework when a host key has been
     * supplied that does not match the host key recorded in the host file.
     *
     * @param host The name of the host.
     * @param allowedHostKey The host key currently allowed.
     * @param actualHostKey The host key provided.
     */
    public abstract void onHostKeyMismatch(String host, String allowedHostKey,
                                           String actualHostKey)
                                           throws TransportProtocolException;

    /**
     * Abstract method called by the framework when a host key has been
     * supplied that is not currently recorded in the hosts file.
     *
     * @param host The name of the host.
     * @param hostKeyFingerprint The fingerprint of the host key supplied.
     */
    public abstract void onUnknownHost(String host, String hostKeyFingerprint)
                                            throws TransportProtocolException;

    /**
     * Allows the host access if the host provides the host key specified.
     *
     * @param host The name of the host.
     * @param hostKeyFingerprint The fingerprint of the acceptable host key.
     * @param always Specifies whether to always allow the host access (true)
     *        or only allow access this time.
     *
     * @exception InvalidHostFileException if the host file does not exsist or
     *            is invalid
     */
    public void allowHost(String host, String hostKeyFingerprint, boolean always)
                   throws InvalidHostFileException {
        log.debug("Allowing " + host + " with fingerprint "
                  + hostKeyFingerprint);

        // Put the host into the allowed hosts list, overiding any previous
        // entry
        allowedHosts.put(host, hostKeyFingerprint);

        // If we always want to allow then save the host file with the
        // new details
        if (always)
            saveHostFile();

    }

    /**
     * Denys the host access.
     *
     * @param host The name of the host.
     * @param always Specifies whether to always deny the specified host (true)
     *        or just deny the host acceess this time (false)
     *
     * @exception InvalidHostFileException if the host file does not exsist or
     *            is invalid
     */
    public void denyHost(String host, boolean always)
                  throws InvalidHostFileException {
        log.debug(host + " is denied access");

        // Get the denied host from the list
        if(!deniedHosts.contains(host))
          deniedHosts.add(host);

        // Save it if need be
        if (always)
            saveHostFile();

    }

    /**
     * Called by the framework to verify a host key
     *
     * @param host The name of the host
     * @param fingerprint The fingerprint of the host key
     *
     * @return The result of the host key verification
     */
    public boolean verifyHost(String host, String fingerprint)
                                          throws TransportProtocolException {
        log.info("Verifying " + host + " host key");
        log.debug("Fingerprint: " + fingerprint);

        // See if the host is denied by looking at the denied hosts list


        if(deniedHosts.contains(host)) {
            onDeniedHost(host);
            return false;
        }

        // Try the allowed hosts by looking at the allowed hosts map

        if (allowedHosts.containsKey(host)) {
            // The host is allowed so check the fingerprint
            String currentFingerprint = (String)allowedHosts.get(host);
            if (currentFingerprint.compareToIgnoreCase(fingerprint)==0) {
                return true;
            }

            // The host key does not match the recorded so call the abstract
            // method so that the user can decide
            onHostKeyMismatch(host, currentFingerprint, fingerprint);

            // Recheck the after the users input
            return checkFingerprint(host, fingerprint);
        } else {
            // The host is unknown os ask the user
            onUnknownHost(host, fingerprint);

            // Recheck ans return the result
            return checkFingerprint(host, fingerprint);
        }
    }



    /**
     * Compares the fingerprint against the allowed hosts.
     *
     * @param host The name of the host.
     * @param fingerprint The fingerprint of the host key.
     *
     * @return The result of the check
     */
    private boolean checkFingerprint(String host, String fingerprint) {

        String currentFingerprint = (String)allowedHosts.get(host);

        if (currentFingerprint!=null) {
            if (currentFingerprint.compareToIgnoreCase(fingerprint)==0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Saves the current host file.
     *
     * @exception InvalidHostFileException Description of the Exception
     */
    private void saveHostFile()
                       throws InvalidHostFileException {
        if (!hostFileWriteable) {
            throw new InvalidHostFileException("Host file is not writeable.");
        }

        log.info("Saving " + defaultHostFile);

        try {
            File f = new File(hostFile);

            FileOutputStream out = new FileOutputStream(f);

            /**
             * TODO: Save the allowed hosts file
             */
            out.write(toString().getBytes());

            out.close();
        } catch (IOException e) {
            throw new InvalidHostFileException("Could not write to " + hostFile);
        }
    }

    public String toString() {
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<HostAuthorizations>\n";
      xml+="<!-- Host Authorizations file, used by the abstract class HostKeyVerification to verify the servers host key -->";
      xml+="   <!-- Allow the following hosts access if they provide the correct public key -->\n";
      Map.Entry entry;
      Iterator it = allowedHosts.entrySet().iterator();
      while(it.hasNext()) {
        entry = (Map.Entry)it.next();
        xml+="   "+"<AllowHost HostName=\"" + entry.getKey().toString() +
                                "\" Fingerprint=\"" + entry.getValue().toString()
                                + "\"/>\n";
      }
      xml+="   <!-- Deny the following hosts access -->\n";
      it = deniedHosts.iterator();
      while(it.hasNext()) {
        xml+="   <DenyHost HostName=\"" + it.next().toString() +
                                "\"/>\n";
      }
      xml+="</HostAuthorizations>";
      return xml;
    }
}
