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

import java.io.IOException;
import java.io.InputStream;

import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelInputStream;
import com.sshtools.j2ssh.connection.SshMsgChannelExtendedData;
import com.sshtools.j2ssh.transport.ServiceOperationException;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

import com.sshtools.j2ssh.subsystem.SubsystemClient;

/**
 *  This class implements a connection protocol channel providing the session
 *  specification of the SSH protocol.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: SessionChannelClient.java,v 1.9 2002/12/18 19:27:29 martianx
 *      Exp $
 */
public class SessionChannelClient
         extends Channel {
    private static Logger log = Logger.getLogger(SessionChannelClient.class);
    private Integer exitCode = null;
    private ChannelInputStream stderr = null;


    /**
     *  Constructs the session channel
     */
    public SessionChannelClient() {
        super();
    }


    /**
     *  Gets the channel open request data, this returns null
     *
     *@return    returns null as no request data is required
     */
    public byte[] getChannelOpenData() {
        return null;
    }


    /**
     *  Gets the channel type.
     *
     *@return    "session"
     */
    public String getChannelType() {
        return "session";
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
     *  Sets the environment variable for the command or shell to be started
     *
     *@param  name                         The environment variable name
     *@param  value                        The environment variable value
     *@return                              <tt>true</tt> if the variable was set
     *      otherwise <tt>false</tt>
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public boolean setEnvironmentVariable(String name, String value)
             throws TransportProtocolException,
            ServiceOperationException {
        log.debug("Requesting environment variable to be set [" + name + "="
                + value + "]");

        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            baw.writeString(name);
            baw.writeString(value);

            return connection.sendChannelRequest(this, "env", true,
                    baw.toByteArray());
        } catch (IOException ioe) {
            throw new ServiceOperationException("Could not set environment variable;"
                    + " error writing request data");
        }
    }


    /**
     *  Gets the exit code of the command exectued. This may be null if nothing
     *  has been returned from the remote side.
     *
     *@return    the completed command/shell/subsystem exit code
     */
    public Integer getExitCode() {
        return exitCode;
    }


    /**
     *  Changes the terminal dimensions to the newly provided settings
     *
     *@param  term                         The pseudo terminal instance
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public void changeTerminalDimensions(PseudoTerminal term)
             throws TransportProtocolException,
            ServiceOperationException {
        log.debug("Changing terminal dimensions");

        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            baw.writeInt(term.getColumns());
            baw.writeInt(term.getRows());
            baw.writeInt(term.getWidth());
            baw.writeInt(term.getHeight());

            connection.sendChannelRequest(this, "window-change", false,
                    baw.toByteArray());
        } catch (IOException ioe) {
            throw new ServiceOperationException("Failed to change terminal dimensions; error writing request data");
        }
    }


    /**
     *  Requests that the remote server start the execution of the given
     *  command.
     *
     *@param  command                      The command to execute
     *@return                              <tt>true</tt> if the command was
     *      executed otherwise <tt>false</tt>
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public boolean executeCommand(String command)
             throws TransportProtocolException,
            ServiceOperationException {
        log.info("Requesting command execution");
        log.debug("Command is " + command);

        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            baw.writeString(command);

            return connection.sendChannelRequest(this, "exec", true,
                    baw.toByteArray());
        } catch (IOException ioe) {
            throw new ServiceOperationException("Failed to request command execution; error writing request data");
        }
    }


    /**
     *  Requests that the remote server allocate a psuedo terminal for the
     *  session.
     *
     *@param  term                         The terminal answerback mode
     *@return                              <tt>true</tt> if the terminal was
     *      allocated otherwise <tt>false</tt>
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public boolean requestPseudoTerminal(PseudoTerminal term)
             throws TransportProtocolException,
            ServiceOperationException {
        log.info("Requesting pseudo terminal");
        log.debug("Terminal Type is " + term.getTerm());

        // This requests a pseudo terminal
        try {
            ByteArrayWriter baw = new ByteArrayWriter();
            baw.writeString(term.getTerm());
            baw.writeInt(term.getColumns());
            baw.writeInt(term.getRows());
            baw.writeInt(term.getWidth());
            baw.writeInt(term.getHeight());
            baw.writeString(term.getEncodedTerminalModes());

            return connection.sendChannelRequest(this, "pty-req", true,
                    baw.toByteArray());
        } catch (IOException ioe) {
            throw new ServiceOperationException("Failed to write channel request data to ByteArrayWriter");
        }
    }


    /**
     *  Requests that the remote server start the users shell
     *
     *@return                              <tt>true</tt> if the shell was
     *      started otherwise <tt>false</tt>
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public boolean startShell()
             throws IOException {
        log.debug("Requesting users shell");

        // Send the request for a shell, we want a reply
        return connection.sendChannelRequest(this, "shell", true, null);
    }


    /**
     *  Requests that the remote server executes the predefined subsystem (for
     *  example SFTP).
     *
     *@param  subsystem                    The predefined subsystem to start
     *@return                              <tt>true</tt> if the subsystem was
     *      started otherwise <tt>false</tt>
     *@throws  TransportProtocolException  if a transport protocol error occurs
     *@throws  ServiceOperationException   if a critical service operation fails
     */
    public boolean startSubsystem(String subsystem)
             throws TransportProtocolException,
            ServiceOperationException {
        log.info("Starting " + subsystem + " subsystem");

        try {
            ByteArrayWriter baw = new ByteArrayWriter();

            baw.writeString(subsystem);

            return connection.sendChannelRequest(this, "subsystem", true,
                    baw.toByteArray());
        } catch (IOException ioe) {
            throw new ServiceOperationException("Failed to start the "
                    + subsystem
                    + " subsystem; could not write request data");
        }
    }

    public boolean startSubsystem(SubsystemClient subsystem) throws IOException {

      boolean result = startSubsystem(subsystem.getName());
      if(result) {
        subsystem.setInputStream(getInputStream());
        subsystem.setOutputStream(getOutputStream());
        subsystem.start();
      }

      return result;

    }


    /**
     *  Handles the closing of the channel
     *
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelClose()
             throws ServiceOperationException {
        Integer exitCode = getExitCode();

        if (exitCode != null) {
            log.debug("Exit code " + exitCode.toString());
        }
    }


    /**
     *  Handles the channel EOF
     *
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelEOF()
             throws ServiceOperationException { }


    /**
     *  Handles the opening of the channel and calls initSession
     *
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelOpen()
             throws ServiceOperationException {

        stderr = new ChannelInputStream(incoming,
                new Integer(SshMsgChannelExtendedData.SSH_EXTENDED_DATA_STDERR));
    }


    /**
     *  Gets the sessions stderr input stream
     *
     *@return                             the stderr input stream for the
     *      channel
     *@throws  ServiceOperationException  if the session has not been started
     */
    public InputStream getStderrInputStream() throws ServiceOperationException {
        if (stderr == null) {
            throw new ServiceOperationException("The session must be started first!");
        }

        return stderr;
    }


    /**
     *  TODO: Exit signal requests need to be implemented
     *
     *@param  requestType                 the request type
     *@param  wantReply                   <tt>true</tt> if the remote computer
     *      wants a reply otherwise <tt>false</tt>
     *@param  requestData                 the request data
     *@throws  ServiceOperationException  if a critical service operation fails
     */
    protected void onChannelRequest(String requestType, boolean wantReply,
            byte requestData[])
             throws ServiceOperationException {
        log.debug("Channel Request received: " + requestType);

        if (requestType.equals("exit-status")) {
            exitCode = new Integer(ByteArrayReader.readInt(requestData, 0));
        }

        if (requestType.equals("exit-signal")) {
        }
    }
}
