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
package com.sshtools.j2ssh.io;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.sshtools.j2ssh.util.InvalidStateException;

import javax.swing.event.EventListenerList;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *  Connects and InputStream to an OutputStream. This has the effect of writing
 *  all data recieved from the input stream to the OutputStream.
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: IOStreamConnector.java,v 1.2 2002/12/09 23:35:42 martianx Exp
 *      $
 */
public class IOStreamConnector {
    private static Logger log = Logger.getLogger(IOStreamConnector.class);
    private IOStreamConnectorState state = new IOStreamConnectorState();
    private InputStream in = null;
    private OutputStream out = null;
    private Thread thread;
    private long bytes;

    /**
     *  List of listeners to be informed when something happens on the forwarded
     *  connection, such as starting, stopping or data
     */
    protected EventListenerList listenerList = new EventListenerList();


    /**
     *  Creates a new IOStreamConnector object.
     */
    public IOStreamConnector() { }


    /**
     *  Creates a new IOStreamConnector object.
     *
     *@param  in   The InputStream to read
     *@param  out  The OutputStream to write to
     */
    public IOStreamConnector(InputStream in, OutputStream out) {
        connect(in, out);
    }


    /**
     *  Gets the state of the IOStreamConnector
     *
     *@return    the IOStreamConnectors State instance
     */
    public IOStreamConnectorState getState() {
        return state;
    }


    /**
     *  Closes the IOStreamConnector and in turn the IO Streams
     */
    public void close() throws IOException {

            log.info("Closing IOStreamConnector");
            state.setValue(IOStreamConnectorState.CLOSED);
            in.close();
            out.close();
            thread = null;
    }


    /**
     *  Connects the IO Streams
     *
     *@param  in   The InputStream to connect
     *@param  out  The OutputStream to connect
     */
    public void connect(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        log.info("Connecting InputStream to OutputStream");

        state.setValue(IOStreamConnectorState.CONNECTED);

        thread = new Thread(new IOStreamConnectorThread());
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Return the number of bytes piped
     *
     * @return bytes
     */
    public long getBytes() {
        return bytes;
    }

    /**
     * Add a listener to be informed when data passes through this connection
     *
     * @param l listener to add
     */
    public void addIOStreamConnectorListener(IOStreamConnectorListener l) {
        listenerList.add(IOStreamConnectorListener.class, l);
    }

    /**
     * Remove a listener being informed when data passes through this connector
     *
     * @param l listener to remove
     */
    public void removeIOStreamConnectorListener(IOStreamConnectorListener l) {
        listenerList.remove(IOStreamConnectorListener.class, l);
    }


    /**
     *  This class performs the reading and writing to the IOStreams
     *
     *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
     *@created    20 December 2002
     *@version    $Id: IOStreamConnector.java,v 1.2 2002/12/09 23:35:42 martianx
     *      Exp $
     */
    class IOStreamConnectorThread
             implements Runnable {
        private Logger log = Logger.getLogger(IOStreamConnectorThread.class);


        /**
         *  Reads data from the InputStream and delivers to the OutputStream
         */
        public void run() {
            byte buffer[] = new byte[4096];
            int read = 0;
            int count;
            log.info("Starting IOStreamConnectorThread thread");

            while (state.getValue() == IOStreamConnectorState.CONNECTED) {
                try {
                    // Block
                    read = in.read(buffer, 0, 1);

                    if (read > 0) {
                        count = read;

                        // Verify the buffer length and adjust if necersary
                        if ((in.available() > 0)
                                && ((buffer.length - 1) < in.available())) {
                            byte tmp[] = new byte[in.available() + 1];
                            System.arraycopy(buffer, 0, tmp, 0, 1);
                            buffer = tmp;
                        }

                        // Read the remaining available bytes of the message
                        if (in.available() > 0) {
                            read = in.read(buffer, 1, in.available());
                            count += read;
                        }

                        // Write the message to the output stream
                        out.write(buffer, 0, count);
                        bytes += count;

                        // Flush it
                        out.flush();

                        // Inform all of the listeners
                        IOStreamConnectorListener[] l =
                            (IOStreamConnectorListener[])listenerList.getListeners(
                            IOStreamConnectorListener.class);
                        for(int i = (l.length - 1); i >= 0; i--)
                            l[i].data(buffer, count);
                    } else {
                        state.setValue(IOStreamConnectorState.EOF);
                    }
                } catch (IOException ioe) {
                   state.setValue(IOStreamConnectorState.EOF);
                }
            }

            log.debug("Closing IOStreamConnector");
            state.setValue(IOStreamConnectorState.CLOSED);

            try {
                in.close();
                out.close();
            } catch (IOException ioe) {
            }

            log.info("IOStreamConnectorThread is exiting");
        }
    }
}
