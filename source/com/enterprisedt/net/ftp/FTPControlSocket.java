/**
 *
 *  edtFTPj
 *
 *  Copyright (C) 2000-2003 Enterprise Distributed Technologies Ltd
 *
 *  www.enterprisedt.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to bruce@enterprisedt.com
 */

package com.enterprisedt.net.ftp;

import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * Supports client-side FTP operations
 *
 * @author Bruce Blackshaw
 * @version $Revision$
 */
public class FTPControlSocket {
    protected static Logger log = Logger.getLogger(FTPControlSocket.class);

    /**
     * Standard FTP end of line sequence
     */
    private static final String EOL = "\r\n";

    /**
     * The default and standard control port number for FTP
     */
    public static final int CONTROL_PORT = 21;

    /**
     * Use strict return codes if true
     */
    private boolean strictReturnCodes = true;

    /**
     * The underlying socket.
     */
    protected Socket controlSock = null;

    /**
     * The write that writes to the control socket
     */
    private Writer writer = null;

    /**
     * The reader that reads control data from the
     * control socket
     */
    private BufferedReader reader = null;

    private String encoding;

    protected String getEncoding() {
        return this.encoding;
    }

    private FTPMessageListener listener = null;

    private int timeout = 0;

    /**
     * @param encoding character encoding used for data
     * @param listener listens for messages
     */
    protected FTPControlSocket(final String encoding, final FTPMessageListener listener) {
        this.encoding = encoding;
        this.listener = listener;
    }

    /**
     * Performs TCP connection and sets up reader/writer. Allows different control
     * port to be used
     *
     * @param remoteAddr  Remote inet address
     * @param controlPort port for control stream
     * @throws IOException  Thrown if no connection response could be read from the server.
     * @throws FTPException Thrown if the incorrect connection response was sent by the server.
     */
    protected void connect(final InetAddress remoteAddr, int controlPort)
            throws IOException, FTPException {

        this.controlSock = new Socket(remoteAddr, controlPort);
        try {
            this.controlSock.setKeepAlive(true);
        }
        catch(SocketException e) {
            log.error(e.getMessage());
        }
        try {
            this.controlSock.setSoTimeout(timeout);
        }
        catch(SocketException e) {
            log.error(e.getMessage());
        }
        this.initStreams();
        this.validateConnection();
    }

    /**
     * Checks that the standard 220 reply is returned
     * following the initiated connection
     */
    private void validateConnection()
            throws IOException, FTPException {

        this.validateReply(this.readReply(), "220");
    }

    /**
     * Obtain the reader/writer streams for this
     * connection
     */
    protected void initStreams() throws IOException {

        // input stream
        InputStream is = controlSock.getInputStream();
        reader = new BufferedReader(new InputStreamReader(is, encoding));

        // output stream
        OutputStream os = controlSock.getOutputStream();
        writer = new OutputStreamWriter(os, encoding);
    }

    /**
     * Get the name of the remote host
     *
     * @return remote host name
     */
    public String getRemoteHostName() {
        InetAddress addr = controlSock.getInetAddress();
        return addr.getHostName();
    }

    /**
     * Set strict checking of FTP return codes. If strict
     * checking is on (the default) code must exactly match the expected
     * code. If strict checking is off, only the first digit must match.
     *
     * @param strict true for strict checking, false for loose checking
     */
    public void setStrictReturnCodes(boolean strict) {
        this.strictReturnCodes = strict;
    }

    /**
     * Set the TCP timeout on the underlying control socket.
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException.
     *
     * @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(int millis) {
        this.timeout = millis;
    }

    /**
     * Quit this FTP session and clean up.
     */
    public void logout() {
        try {
            if(writer != null)
                writer.close();
        }
        catch(IOException e) {
            ; //ignore
        }
        try {
            if(reader != null)
                reader.close();
        }
        catch(IOException e) {
            ; //ignore
        }
        try {
            if(controlSock != null)
                controlSock.close();
        }
        catch(IOException e) {
            ; //ignore
        }
    }

    /**
     * Request a data socket be created on the
     * server, connect to it and return our
     * connected socket.
     *
     * @param connectMode if true, create in active mode, else
     *                    in passive mode
     * @return connected data socket
     */
    protected FTPDataSocket createDataSocket(FTPConnectMode connectMode)
            throws IOException, FTPException {

        try {
            if(connectMode == FTPConnectMode.ACTIVE) {
                return this.createDataSocketActive();
            }
            if(connectMode == FTPConnectMode.PASV) {
                return this.createDataSocketPassive();
            }
        }
        catch(FTPException e) {
            if(Preferences.instance().getBoolean("ftp.connectmode.fallback")) {
                // Fallback to other connect mode
                if(connectMode == FTPConnectMode.ACTIVE) {
                    return this.createDataSocketPassive();
                }
                if(connectMode == FTPConnectMode.PASV) {
                    return this.createDataSocketActive();
                }
            }
            throw e;
        }
        return null;
    }

    /**
     * Request a data socket be created on the Client
     * client on any free port, do not connect it to yet.
     *
     * @return not connected data socket
     */
    protected FTPDataSocket createDataSocketActive()
            throws IOException, FTPException {

        // use any available port
        FTPDataSocket socket = new FTPActiveDataSocket(new ServerSocket(0));

        // get the local address to which the control socket is bound.
        InetAddress localhost = controlSock.getLocalAddress();

        // send the PORT command to the server
        this.setDataPort(localhost, (short) socket.getLocalPort());

        return socket;
    }

    /**
     * Helper method to convert a byte into an unsigned short value
     *
     * @param value value to convert
     * @return the byte value as an unsigned short
     */
    private static short toUnsignedShort(byte value) {
        return (value < 0)
                ? (short) (value + 256)
                : (short) value;
    }

    /**
     * Convert a short into a byte array
     *
     * @param value value to convert
     * @return a byte array
     */
    private static byte[] toByteArray(short value) {

        byte[] bytes = new byte[2];
        bytes[0] = (byte) (value >> 8);     // bits 1- 8
        bytes[1] = (byte) (value & 0x00FF); // bits 9-16
        return bytes;
    }


    /**
     * Sets the data port on the server, i.e. sends a PORT
     * command
     *
     * @param host   the local host the server will connect to
     * @param port the port number to connect to
     */
    protected void setDataPort(InetAddress host, short port)
            throws IOException, FTPException {

        if(host instanceof Inet6Address) {
            this.setDataPortIPv6(host, port);
        }
        else {
            this.setDataPortIPv4(host, port);
        }
    }

    /**
     * FTP Extensions for IPv6 and NATs
     *
     * @param host
     * @param port
     * @throws IOException
     */
    protected void setDataPortIPv6(InetAddress host, short port) throws IOException {
        FTPReply ftpreply = sendCommand("EPRT |2|" + host.getHostAddress() + "|" + port + "|");
        validateReply(ftpreply, "200");
    }

    /**
     *
     * @param host
     * @param port
     * @throws IOException
     */
    protected void setDataPortIPv4(InetAddress host, short port) throws IOException {
        byte[] hostBytes = host.getAddress();
        byte[] portBytes = toByteArray(port);

        // assemble the PORT command
        String cmd = new StringBuilder("PORT ")
                .append(toUnsignedShort(hostBytes[0])).append(",")
                .append(toUnsignedShort(hostBytes[1])).append(",")
                .append(toUnsignedShort(hostBytes[2])).append(",")
                .append(toUnsignedShort(hostBytes[3])).append(",")
                .append(toUnsignedShort(portBytes[0])).append(",")
                .append(toUnsignedShort(portBytes[1])).toString();

        // send command and check reply
        FTPReply reply = sendCommand(cmd);
        validateReply(reply, "200");
    }

    /**
     * Request a data socket be created on the
     * server, connect to it and return our
     * connected socket.
     *
     * @return connected data socket
     */
    protected FTPDataSocket createDataSocketPassive()
            throws IOException, FTPException {

        if(controlSock.getInetAddress() instanceof Inet6Address) {
            // FTP Extensions for IPv6 and NATs
            return this.createDataSocketEPSV();
        }
        return this.createDataSocketPASV();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    protected FTPDataSocket createDataSocketPASV() throws IOException {
        // PASSIVE command - tells the server to listen for
        // a connection attempt rather than initiating it
        FTPReply replyObj = sendCommand("PASV");
        validateReply(replyObj, "227");
        String reply = replyObj.getReplyText();

        // The reply to PASV is in the form:
        // 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2).
        // where h1..h4 are the IP address to connect and
        // p1,p2 the port number
        // Example:
        // 227 Entering Passive Mode (128,3,122,1,15,87).
        // NOTE: PASV command in IBM/Mainframe returns the string
        // 227 Entering Passive Mode 128,3,122,1,15,87	(missing
        // brackets)
        //
        // Improvement: The first digit found after the reply code
        // is considered start of IP. End of IP can be EOL or random
        // characters. Should take care of all PASV reponse lines,
        // right?

        int parts[] = this.parsePASVResponse(reply);

        // assemble the IP address
        // we try connecting, so we don't bother checking digits etc
        String ipAddress = parts[0] + "." + parts[1] + "." +
                parts[2] + "." + parts[3];

        // assemble the port number
        int port = (parts[4] << 8) + parts[5];

        try {
            if(InetAddress.getByName(ipAddress).isSiteLocalAddress()) {
                // Do not trust a local address; may be a misconfigured router
                return new FTPPassiveDataSocket(new Socket(controlSock.getInetAddress(), port));
            }
            return new FTPPassiveDataSocket(new Socket(ipAddress, port));
        }
        catch (ConnectException e) {
            // See #15353
            throw new FTPException(e.getMessage());
		}
    }

    protected int[] parsePASVResponse(String reply) throws FTPException {
        // extract the IP data string from between the brackets
        int startIP = reply.indexOf('(');
        int endIP = reply.indexOf(')');

        // if didn't find start bracket, figure out where it should have been
        if(startIP < 0) {
            startIP = 0;
            while(startIP < reply.length() && !Character.isDigit(reply.charAt(startIP)))
                startIP++;
            startIP--; // go back so this is where the '(' should be
        }

        // if didn't find end bracket, set to end of reply
        if(endIP < 0) {
            endIP = reply.length() - 1;
            while(endIP > 0 && !Character.isDigit(reply.charAt(endIP)))
                endIP--;
            endIP++; // go forward so this is where the ')' should be
            if(endIP >= reply.length())
                reply += ")";
        }

        String ipData = reply.substring(startIP + 1, endIP).trim();
        int parts[] = new int[6];

        int len = ipData.length();
        int partCount = 0;
        StringBuilder buf = new StringBuilder();

        // loop thru and examine each char
        for(int i = 0; i < len && partCount <= 6; i++) {

            char ch = ipData.charAt(i);
            if(Character.isDigit(ch))
                buf.append(ch);
            else if(ch != ',' && ch != ' ') {
                throw new FTPException("Malformed PASV reply: " + reply);
            }

            // get the part
            if(ch == ',' || i + 1 == len) { // at end or at separator
                try {
                    parts[partCount++] = Integer.parseInt(buf.toString());
                    buf.setLength(0);
                }
                catch(NumberFormatException ex) {
                    throw new FTPException("Malformed PASV reply: " + reply);
                }
            }
        }
        return parts;
    }

    /**
     * FTP Extensions for IPv6 and NATs
     *
     * @return
     * @throws IOException
     */
    protected FTPDataSocket createDataSocketEPSV() throws IOException {
        FTPReply replyObj = sendCommand("EPSV");
        validateReply(replyObj, "229");
        String reply = replyObj.getReplyText();

        int port = this.parseEPSVResponse(reply);

        return new FTPPassiveDataSocket(new Socket(controlSock.getInetAddress(), port));
    }

    /**
     *
     * @param reply
     * @return Port number to connect
     */
    protected int parseEPSVResponse(String reply) {
        int i = reply.indexOf("(|||");
        i += "(|||".length();
        int j = reply.indexOf("|)");
        return Integer.parseInt(reply.substring(i, j).trim());
    }


    /**
     * Send a command to the FTP server and
     * return the server's reply as a structured
     * reply object
     *
     * @param command command to send
     * @return reply to the supplied command
     */
    public FTPReply sendCommand(String command)
            throws IOException {

        this.writeCommand(command);

        // and read the result
        return this.readReply();
    }

    /**
     * Send a command to the FTP server. Don't
     * read the reply
     *
     * @param command command to send
     */
    private void writeCommand(String command)
            throws IOException {

        this.log(command, true);

        // send it
        writer.write(command + EOL);
        writer.flush();
    }

    /**
     * Read the FTP server's reply to a previously
     * issued command. RFC 959 states that a reply
     * consists of the 3 digit code followed by text.
     * The 3 digit code is followed by a hyphen if it
     * is a muliline response, and the last line starts
     * with the same 3 digit code.
     *
     * @return structured reply object
     */
    protected FTPReply readReply()
            throws IOException {

        String line = reader.readLine();
        while(line != null && line.length() == 0)
            line = reader.readLine();

        if(line == null)
            throw new FTPNullReplyException();

        this.log(line, false);

        if(line.length() < 3)
            throw new IOException("Short reply received");

        String replyCode = line.substring(0, 3);
        StringBuilder reply = new StringBuilder("");
        if(line.length() > 3)
            reply.append(line.substring(4));

        Vector dataLines = null;

        // check for multiline response and build up
        // the reply
        if(line.charAt(3) == '-') {
            dataLines = new Vector();
            boolean complete = false;
            while(!complete) {
                line = reader.readLine();
                if(line == null)
                    throw new FTPNullReplyException();

                if(line.length() == 0)
                    continue;

                log(line, false);

                if(line.length() > 3 &&
                        line.substring(0, 3).equals(replyCode) &&
                        line.charAt(3) == ' ') {
                    reply.append(line.substring(3));
                    complete = true;
                }
                else { // not the last line
                    reply.append(" ").append(line);
                    dataLines.addElement(line);
                }
            } // end while
        } // end if

        if(dataLines != null) {
            String[] data = new String[dataLines.size()];
            dataLines.copyInto(data);
            return new FTPReply(replyCode, reply.toString(), data);
        }
        else {
            return new FTPReply(replyCode, reply.toString());
        }
    }


    /**
     * Validate the response the host has supplied against the
     * expected reply. If we get an unexpected reply we throw an
     * exception, setting the message to that returned by the
     * FTP server
     *
     * @param reply             the entire reply string we received
     * @param expectedReplyCode the reply we expected to receive
     */
    protected FTPReply validateReply(String reply, String expectedReplyCode)
            throws FTPException {

        FTPReply replyObj = new FTPReply(reply);

        if(validateReplyCode(replyObj, expectedReplyCode))
            return replyObj;

        // if unexpected reply, throw an exception
        throw new FTPException(replyObj);
    }


    /**
     * Validate the response the host has supplied against the
     * expected reply. If we get an unexpected reply we throw an
     * exception, setting the message to that returned by the
     * FTP server
     *
     * @param reply              the entire reply string we received
     * @param expectedReplyCodes array of expected replies
     * @return an object encapsulating the server's reply
     */
    public FTPReply validateReply(String reply, String[] expectedReplyCodes)
            throws IOException, FTPException {

        FTPReply replyObj = new FTPReply(reply);
        return validateReply(replyObj, expectedReplyCodes);
    }


    /**
     * Validate the response the host has supplied against the
     * expected reply. If we get an unexpected reply we throw an
     * exception, setting the message to that returned by the
     * FTP server
     *
     * @param reply              reply object
     * @param expectedReplyCodes array of expected replies
     * @return reply object
     */
    public FTPReply validateReply(FTPReply reply, String[] expectedReplyCodes)
            throws FTPException {

        for(int i = 0; i < expectedReplyCodes.length; i++)
            if(validateReplyCode(reply, expectedReplyCodes[i]))
                return reply;

        // got this far, not recognised
        throw new FTPException(reply);
    }

    /**
     * Validate the response the host has supplied against the
     * expected reply. If we get an unexpected reply we throw an
     * exception, setting the message to that returned by the
     * FTP server
     *
     * @param reply             reply object
     * @param expectedReplyCode expected reply
     * @return reply object
     */
    public FTPReply validateReply(FTPReply reply, String expectedReplyCode)
            throws FTPException {
        if(validateReplyCode(reply, expectedReplyCode))
            return reply;

        // got this far, not recognised
        throw new FTPException(reply);
    }

    /**
     * Validate reply object
     *
     * @param reply             reference to reply object
     * @param expectedReplyCode expect reply code
     * @return true if valid, false if invalid
     */
    private boolean validateReplyCode(FTPReply reply, String expectedReplyCode) {
        String replyCode = reply.getReplyCode();
        if(strictReturnCodes) {
            return replyCode.equals(expectedReplyCode);
        }
        else { // non-strict - match first char
            return replyCode.charAt(0) == expectedReplyCode.charAt(0);
        }
    }


    /**
     * Log a message, checking for passwords
     *
     * @param msg message to log
     */
    protected void log(String msg, boolean command) {
        if(msg.startsWith("PASS"))
            msg = "PASS ********";
        if(listener != null) {
            if(command) {
                listener.logCommand(msg);
            }
            else {
                listener.logReply(msg);
            }
        }
    }

    public void interrupt()
            throws IOException {
        if(null == controlSock) {
            log.warn("No control socket to interrupt");
            return;
        }
        controlSock.close();
        log.warn("Forced to close socket " + controlSock.toString());
    }

    public boolean isConnected() {
        if(null == controlSock) {
            return false;
        }
        return !(null == reader || null == writer);
    }
}
