/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.configuration;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FilenameFilter;

import java.net.MalformedURLException;
import java.net.URL;

import java.security.AccessControlException;
import java.security.AccessController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.PropertyPermission;

import com.sshtools.j2ssh.util.DynamicClassLoader;

import com.sshtools.j2ssh.configuration.SshAPIConfiguration;
import com.sshtools.j2ssh.configuration.PlatformConfiguration;
import com.sshtools.j2ssh.configuration.ServerConfiguration;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


/**
 *  Performs configuration loading for the sshtools project.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: ConfigurationLoader.java,v 1.17 2002/12/09 23:35:42 martianx
 *      Exp $
 */
public class ConfigurationLoader {
    private static Logger log = Logger.getLogger(ConfigurationLoader.class);
    private static SshAPIConfiguration config;
    private static ServerConfiguration server;
    private static PlatformConfiguration platform;
    private static String configDirectory;
    private static String configResource;
    private static String hostsResource;
    private static String serverResource;
    private static String platformResource;
    private static String homeDir;
    private static DynamicClassLoader ext;
    private static ClassLoader clsLoader = null;

    static {

        // Get the sshtools.home system property. If system properties can not be
        // read then we are running in a sandbox
        try {
            //  We should only need to check a single property
            if (System.getSecurityManager() != null) {
                AccessController.checkPermission(new PropertyPermission("sshtools.home",
                        "read"));
            }

            homeDir = System.getProperty("sshtools.home");
            configResource = System.getProperty("sshtools.config");
            serverResource = System.getProperty("sshtools.server");
            platformResource = System.getProperty("sshtools.platform");

            if (homeDir == null) {
                log.info("sshtools.home not set; defaulting to java.home/lib");

                // Not sshtools.home so lets try the java.home instead
                homeDir = System.getProperty("java.home");

                if (!homeDir.endsWith(File.separator)) {
                    homeDir += File.separator;
                }

                configDirectory = homeDir + "lib";
            } else {
                if (!homeDir.endsWith(File.separator)) {
                    homeDir += File.separator;
                }

                configDirectory = homeDir + "conf";
            }

            if (!configDirectory.endsWith(File.separator)) {
                configDirectory += File.separator;
            }

            if (configResource == null) {
                configResource = configDirectory + "sshtools.xml";
            } else {
                // Determine if the config file is in config dir or absolute
                File f = new File(configResource);

                if (!f.exists()) {
                    f = new File(configDirectory + configResource);

                    if (f.exists()) {
                        configResource = configDirectory + configResource;
                    }
                    else {
                      log.warn("Failed to locate api configuration file " + configResource);
                      configResource = configDirectory + "sshtools.xml";
                    }

                }
            }

            //  Look at the SSHTOOLS_HOME/lib/ext directory for additional
            //  Jar files to add to the classpath
            File dir = new File(homeDir + "lib" + File.separator + "ext");

            // Filter for .jar files
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            };

            // Get the list
            File[] children = dir.listFiles(filter);
            List classpath = new Vector();

            if (children == null) {
                // Either dir does not exist or is not a directory
                log.info("Extension directory not available");
            } else {
                for (int i=0; i<children.length; i++) {
                    // Get filename of file or directory
                    classpath.add(children[i].getAbsolutePath());
                    log.debug("Adding " + children[i] + " to dynamic classpath");
                }
            }

            // We need to setup the dynamic class loading with the extension jars
            ext = new DynamicClassLoader(classpath);

            // Now load the configuration
            loadAPIConfiguration();

            if (serverResource == null) {
                serverResource = configDirectory + "server.xml";
            } else {
                // Determine if the config file is in config dir or absolute
                File f = new File(serverResource);

                if (!f.exists()) {
                    f = new File(configDirectory + serverResource);

                    if (f.exists()) {
                        serverResource = configDirectory + serverResource;
                    }
                    else {
                      log.warn("Failed to locate server configuration file " + serverResource);
                      serverResource = configDirectory + "server.xml";
                    }

                }
            }

            loadServerConfiguration();

            if (platformResource == null) {
                platformResource = configDirectory + "platform.xml";
            } else {
                // Determine if the config file is in config dir or absolute
                File f = new File(platformResource);

                if (!f.exists()) {
                    f = new File(configDirectory + platformResource);

                    if (f.exists()) {
                        platformResource = configDirectory + platformResource;
                    }
                    else {
                      log.warn("Failed to locate platform configuration file " + platformResource);
                      platformResource = configDirectory + "platform.xml";
                    }

                }
            }

            loadPlatformConfiguration();
        } catch (AccessControlException ace) {
            log.info("No access to system properties. Must use "
                    + "setAPIConfigurationResource() and / or "
                    + "setServerConfigurationResource() before "
                    + "using getAPIConfiguration() or getServerConfiguration()");
        }
    }


    /**
     *  Constructor for the ConfigurationLoader object
     */
    protected ConfigurationLoader() { }


    /**
     *  Gets the API Configuration instance.
     *
     *@return
     */
    public static SshAPIConfiguration getAPIConfiguration() {
        return config;
    }


    /**
     *  Set the location of the API configuration resource. This must be done
     *  before any calls are made to <code>getAPIConfiguration()</code>. If this
     *  is not done, then the configuration will be loaded using the sshtools.
     *  systems properties.<br>
     *  <br>
     *  The resource may either be a fully qualified URL, or a filename (
     *  relative or fully qualified) <strong>Note, this <u>must</u> be set if
     *  running inside an applet as it is like there will not be enough
     *  permission to access the system properties</strong>
     *
     *@param  configResource  api configuration resource
     */
    public static void setAPIConfigurationResource(String configResource) {
        ConfigurationLoader.configResource = configResource;
        loadAPIConfiguration();
    }


    /**
     *  Returns the sshtools home directory. If the system property is not found
     *  then the loader defaults to java.home/lib. This method will always
     *  return the path with a trailing File.separator
     *
     *@return
     */
    public static String getConfigurationDirectory() {
        return configDirectory;
    }


    /**
     *  Attempts to load a class through the dynamic class loader. This searchs
     *  both the standard classpath and additional jars specified in the API
     *  configuration file by using the ExtensionJar xml element
     *
     *@param  name                     the fully qualified class name
     *@return                          a Class instance for the class
     *@throws  ClassNotFoundException  if the class could not be found
     */
    public static Class getExtensionClass(String name)
             throws ClassNotFoundException {
        return ext.loadClass(name);
    }


    /**
     *  Gets the sshtools home directory
     *
     *@return    the location of the sshtools installation
     */
    public static String getHomeDirectory() {
        return homeDir;
    }


    /**
     *  Gets the platform configuration
     *
     *@return    the platform configuration
     */
    public static PlatformConfiguration getPlatformConfiguration() {
        return platform;
    }

    /**
     * Sets the context class loader
     * @param clsLoader
     */
    public static void setContextClassLoader(ClassLoader clsLoader){
      ConfigurationLoader.clsLoader = clsLoader;
    }

    /**
     *
     */
    public static ClassLoader getContextClassLoader() {
      return ConfigurationLoader.clsLoader;
    }

    public static boolean isContextClassLoader() {
      return (clsLoader!=null);
    }


    /**
     *  Gets the server configuration instance
     *
     *@return
     */
    public static ServerConfiguration getServerConfiguration() {
        return server;
    }


    /**
     *  Set the location of the server configuration resource. This must be done
     *  before any calls are made to <code>getServerConfiguration()</code>. If
     *  this is not done, then the configuration will be loaded using the
     *  sshtools. systems properties.<br>
     *  <br>
     *  The resource may either be a fully qualified URL, or a filename (
     *  relative or fully qualified) <strong>Note, this <u>must</u> be set if
     *  running inside an applet as it is like there will not be enough
     *  permission to access the system properties</strong>
     *
     *@param  serverResource  server configuration resource
     */
    public static void setServerConfigurationResource(String serverResource) {
        ConfigurationLoader.serverResource = serverResource;
        loadServerConfiguration();
    }

    /**
     * Set the location of the platform configuration resource. This must be done
     *  before any calls are made to <code>getPlatformConfiguration()</code>. If
     *  this is not done, then the configuration will be loaded using the
     *  sshtools. systems properties.<br>
     *  <br>
     *  The resource may either be a fully qualified URL, or a filename (
     *  relative or fully qualified) <strong>Note, this <u>must</u> be set if
     *  running inside an applet as it is like there will not be enough
     *  permission to access the system properties</strong>
     *
     * @param platformResource path to the platform configuration resource
     */
    public static void setPlatformConfigurationResource(String platformResource) {
      ConfigurationLoader.platformResource = platformResource;
      loadPlatformConfiguration();
    }

    /**
     *  Attempts to load a file; first from the configuration directory and
     *  second as an absolute file path.
     *
     *@param  filename  The filename to open
     *@return           The open input stream
     */
    public static InputStream loadFile(String filename)
                                        throws FileNotFoundException {
        FileInputStream in;

        log.info("Attempting to load " + filename);
        try {
            in = new FileInputStream(configDirectory + filename);
            return in;
        } catch (FileNotFoundException fnfe) {
            log.info("Failed to load file from configuration directory, trying as absolute path");
        }

        in = new FileInputStream(filename);

        return in;

    }


    /**
     *  Return a resource name as a fully qualified <code>URL</code>. If a
     *  <code>URL</code> can be made from the resource, then it is used as is,
     *  otherwise, it is considered to be a filename and is turned into a file
     *  resource
     *
     *@param  resource  resource
     *@return           fully qualified URL
     */
    private static URL getResourceURL(String resource) {
        try {
            return new URL(resource);
        } catch (MalformedURLException murle) {
            try {
                return new File(resource).toURL();
            } catch (MalformedURLException murle2) {
                return null;
            }
        }
    }


    /**
     *  Load the API configuration
     */
    private static void loadAPIConfiguration() {
        // Load the configuration resource
        InputStream in = null;

        try {
            URL resource = getResourceURL(configResource);
            log.info("Loading api configuration from "
                    + resource.toExternalForm());
            in = resource.openStream();

            config = new SshAPIConfiguration(in);

        } catch (Exception e) {
            log.warn("Api configuration not available",e);
            config = null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
    }


    /**
     *  Loads the platofrm configuration
     */
    private static void loadPlatformConfiguration() {
        // Load the configuration resource
        InputStream in = null;

        try {
            URL resource = getResourceURL(platformResource);
            log.info("Loading platform configuration from "
                    + resource.toExternalForm());
            in = resource.openStream();

             platform = new PlatformConfiguration(in);

        } catch (Exception e) {
            log.warn("Platform configuration not available",e);
            platform = null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
    }


    /**
     *  Load the server configuration
     */
    private static void loadServerConfiguration() {
        // Load the server resource
        InputStream in = null;

        try {
            URL resource = getResourceURL(serverResource);
            log.info("Loading server configuration from "
                    + resource.toExternalForm());
            in = resource.openStream();

            server = new ServerConfiguration(in);

        } catch (Exception e) {
            log.warn("Server configuration failed to load",e);
            server = null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
    }
}
