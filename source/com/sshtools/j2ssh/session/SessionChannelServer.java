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
package com.sshtools.j2ssh.session;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.PlatformConfiguration;
import com.sshtools.j2ssh.configuration.ServerConfiguration;
import com.sshtools.j2ssh.configuration.AllowedSubsystem;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ConnectionProtocol;
import com.sshtools.j2ssh.connection.ChannelOutputStream;
import com.sshtools.j2ssh.platform.NativeProcessProvider;
import com.sshtools.j2ssh.subsystem.SubsystemServer;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.IOStreamConnector;

/**
 *  Implements the server side of the session channel.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SessionChannelServer.java,v 1.17 2002/12/18 19:27:29 martianx
 *      Exp $
 */
public class SessionChannelServer
         extends Channel {
    private static Logger log = Logger.getLogger(SessionChannelServer.class);
    private static Map allowedSubsystems = new HashMap();

    /**
     *  the channel type
     */
    public final static String SESSION_CHANNEL_TYPE = "session";
    private InputStream channelIn;
    private Map environment = new HashMap();
    private NativeProcessProvider processInstance;
    private OutputStream channelOut;
    private ChannelOutputStream stderr;
    private SubsystemServer subsystemInstance;
    private Thread thread;


    /**
     *  Constructs the session channel
     */
    public SessionChannelServer() {
        super();

        // Load the allowed subsystems from the server configuration
        allowedSubsystems.putAll(
              ConfigurationLoader.getServerConfiguration().getSubsystems());

    }


    /**
     *  Gets the channel open request data
     *
     *@return    this method returns null
     */
    public byte[] getChannelOpenData() {
        return null;
    }


    /**
     *  Gets the minimum number of window space bytes
     *
     *@return
     */
    protected int getMinimumWindowSpace() {
        return 1024;
    }


    /**
     *  Get the maximum number of bytes that should be available for window
     *  space
     *
     *@return    the maximum number of bytes for window space
     */
    protected int getMaximumWindowSpace() {
        return 32648;
    }


    /**
     *  Gets the maximum number of bytes the remote side can send at once
     *
     *@return    the maximum packet size
     */
    protected int getMaximumPacketSize() {
        return 32648;
    }


    /**
     *  Returns the channel type
     *
     *@return    "session"
     */
    public String getChannelType() {
        return SESSION_CHANNEL_TYPE;
    }


    /**
     *  Handles the change terminal dimensions request
     *
     *@param  cols    the new number of columns
     *@param  rows    the new number of rows
     *@param  width   the new width in pixels
     *@param  height  the new height in pixels
     */
    protected void onChangeTerminalDimensions(int cols, int rows, int width,
            int height) { }


    /**
     *  Handles the channel close.<br>
     *  <br>
     *  This method attempts to close down any process or subsystem that the
     *  channel has started.
     *
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelClose()
             throws ServiceOperationException {
        try {
            if (processInstance != null) {
                processInstance.kill();
            }

            if (subsystemInstance != null) {
                subsystemInstance.stop();
            }

            channelIn.close();
            channelOut.close();
        } catch (IOException ioe) {
        }
    }


    /**
     *  Handles the channel EOF
     *
     *@throws  ServiceOperationException
     */
    protected void onChannelEOF()
             throws ServiceOperationException { }


    /**
     *  Handles incoming extended session data
     *
     *@param  data                        the extended data
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelExtData(byte data[])
             throws ServiceOperationException {

        // Do something with the data

    }


    /**
     *  Handles the channel open. This implementaiton does nothing
     *
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelOpen()
             throws ServiceOperationException { }


    /**
     *  Handles session channel requests.
     *
     *@param  requestType                 the request name
     *@param  wantReply                   <tt>true</tt> if the client wants a
     *      reply otherwise <tt>false</tt>
     *@param  requestData                 the request specific data
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelRequest(String requestType, boolean wantReply,
            byte requestData[])
             throws IOException {

            log.debug("Channel Request received: " + requestType);

            boolean success = false;

            if (requestType.equals("shell")) {
                success = onStartShell();
            }

            if (requestType.equals("env")) {
                ByteArrayReader bar = new ByteArrayReader(requestData);
                String name = bar.readString();
                String value = bar.readString();
                onSetEnvironmentVariable(name, value);
                success = true;
            }

            if (requestType.equals("exec")) {
                ByteArrayReader bar = new ByteArrayReader(requestData);
                String command = bar.readString();
                success = onExecuteCommand(command);
            }

            if (requestType.equals("subsystem")) {
                ByteArrayReader bar = new ByteArrayReader(requestData);
                String subsystem = bar.readString();
                success = onStartSubsystem(subsystem);
            }

            if (requestType.equals("pty-req")) {
                ByteArrayReader bar = new ByteArrayReader(requestData);
                String term = bar.readString();
                int cols = (int) bar.readInt();
                int rows = (int) bar.readInt();
                int width = (int) bar.readInt();
                int height = (int) bar.readInt();
                String modes = bar.readString();

                success =
                        onRequestPseudoTerminal(term, cols, rows, width, height,
                        modes);
            }

            if (requestType.equals("window-change")) {
                ByteArrayReader bar = new ByteArrayReader(requestData);
                int cols = (int) bar.readInt();
                int rows = (int) bar.readInt();
                int width = (int) bar.readInt();
                int height = (int) bar.readInt();

                onChangeTerminalDimensions(cols, rows, width, height);
            }

            if (wantReply) {
                if (success) {
                    connection.sendChannelRequestSuccess(this);
                } else {
                    connection.sendChannelRequestFailure(this);
                }
            }
    }


    /**
     *  Executes a command using the configured <code>NativeProcessProvider</code>
     *  .
     *
     *@param  command  the command to execute.
     *@return          <tt>true</tt> if the command has been executed otherwise
     *      <tt>false</tt>
     */
    protected boolean onExecuteCommand(String command) {
        PlatformConfiguration platform =
                ConfigurationLoader.getPlatformConfiguration();

        if (platform == null) {
            log.error("Cannot execute command; platform configuration not available");

            return false;
        }

        // Create an instance of the RedirectedProcessProvider implementation
        processInstance = NativeProcessProvider.newInstance();

        if (processInstance == null) {
            return false;
        }

        boolean result =
                processInstance.start(command, environment, nativeSettings);

        if (result) {
            // Setup the channel data
            channelIn = processInstance.getInputStream();
            channelOut = processInstance.getOutputStream();

            IOStreamConnector input = new IOStreamConnector(channelIn, getOutputStream());
            IOStreamConnector output = new IOStreamConnector(getInputStream(), channelOut);
        }

        return result;
    }


    /**
     *  Handles the request for a pseudo terminal.. currently does nothing
     *
     *@param  term    the terminal answerback mode
     *@param  cols    the number of columns
     *@param  rows    the number of rows
     *@param  width   the width in pixels
     *@param  height  the height in pixels
     *@param  modes   encoded terminal modes
     *@return         <tt>true</tt> if the terminal has been allocated otherwise
     *      <tt>false</tt>
     */
    protected boolean onRequestPseudoTerminal(String term, int cols, int rows,
            int width, int height,
            String modes) {
        return true;
    }


    /**
     *  Adds an environment variable to the list to be supplied on the execution
     *  of a command or shell
     *
     *@param  name   the environment variable name
     *@param  value  the environment variable value
     */
    protected void onSetEnvironmentVariable(String name, String value) {
        environment.put(name, value);
    }


    /**
     *  Starts the users shell by executing the command specified in the
     *  TerminalProvider element in the server configuration file
     *
     *@return                             <tt>true</tt> if the shell has been
     *      started otherwise <tt>false</tt>
     *@throws  ServiceOperationException  if the service operation critically
     *      fails
     */
    protected boolean onStartShell()
             throws ServiceOperationException {
        ServerConfiguration server =
                ConfigurationLoader.getServerConfiguration();

        if (server == null) {
            throw new ServiceOperationException("Server configuration not available!");
        }

        String shell = server.getTerminalProvider();

        return onExecuteCommand(shell);
    }


    /**
     *  Starts a given subsystem
     *
     *@param  subsystem  the subsystem name
     *@return            <tt>true</tt> if the subsystem has been started
     *      otherwise <tt>false</tt>
     */
    protected boolean onStartSubsystem(String subsystem) {
        boolean result = false;

        try {
            if (!allowedSubsystems.containsKey(subsystem)) {
                log.error(subsystem + " Subsystem is not available");

                return false;
            }

            AllowedSubsystem obj = (AllowedSubsystem) allowedSubsystems.get(subsystem);

            if (obj.getType().equals("class")) {
                // Create the class implementation and start the subsystem
                Class cls = Class.forName(obj.getProvider());
                subsystemInstance = (SubsystemServer) cls.newInstance();
                subsystemInstance.start();
                channelIn = subsystemInstance.getInputStream();
                channelOut = subsystemInstance.getOutputStream();
            } else {
                PlatformConfiguration platform =
                        ConfigurationLoader.getPlatformConfiguration();

                if (platform == null) {
                    log.error("Cannot execute subsystem; platform configuration not available");

                    return false;
                }

                processInstance = NativeProcessProvider.newInstance();

                if (processInstance == null) {
                    return false;
                }

                // Determine the subsystem provider
                String provider = obj.getProvider();

                // Look in the bin directory?
                File f =
                        new File(ConfigurationLoader.getHomeDirectory() + provider);

                if (f.exists()) {
                    provider = f.getPath();
                } else {
                    f = new File(provider);

                    if (!f.exists()) {
                        log.debug("Failed to locate subsystem provider '"
                                + provider + "'");
                    }

                    return false;
                }

                // Try absolute
                processInstance.start(provider, environment, nativeSettings);
                channelIn = processInstance.getInputStream();
                channelOut = processInstance.getOutputStream();
            }

            IOStreamConnector input = new IOStreamConnector(channelIn, getOutputStream());
            IOStreamConnector output = new IOStreamConnector(getInputStream(), channelOut);

            return true;
        } catch (ClassNotFoundException cnfe) {
        } catch (IllegalAccessException iae) {
        } catch (InstantiationException ie) {
        }

        return false;
    }
}
