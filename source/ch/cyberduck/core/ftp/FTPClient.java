package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPCommand;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Id:$
 */
public class FTPClient extends FTPSClient {
    private static Logger log = Logger.getLogger(FTPSession.class);

    public FTPClient() throws NoSuchAlgorithmException {
        super("TLS", false);
    }

    /**
     * Cached
     */
    private List<String> features = Collections.emptyList();

    /**
     * Query feature set.
     */
    private boolean featSupported = Preferences.instance().getBoolean("ftp.command.feat");

    /**
     * Get the server supplied features
     *
     * @return string containing server features, or null if no features or not
     *         supported
     */
    public List<String> listFeatures() throws IOException {
        if(features.isEmpty()) {
            if(featSupported) {
                if(FTPReply.isPositiveCompletion(this.feat())) {
                    features = Arrays.asList(this.getReplyStrings());
                }
                else {
                    featSupported = false;
                }
            }
        }
        return features;
    }

    /**
     * @param command
     * @return
     * @throws IOException
     */
    public boolean isFeatureSupported(final int command) throws IOException {
        return this.isFeatureSupported(this.getCommand(command));
    }

    /**
     * @param feature
     * @return
     * @throws IOException
     */
    public boolean isFeatureSupported(final String feature) throws IOException {
        for(String item : this.listFeatures()) {
            if(item.trim().startsWith(feature)) {
                return true;
            }
        }
        log.warn("No " + feature + " support");
        return false;
    }

    /**
     * Additional commands supported
     */
    private static final Map<Integer, String> commands
            = new HashMap<Integer, String>();

    public static final int MLST = 50;
    public static final int MLSD = 51;
    public static final int SIZE = 52;
    public static final int PRET = 53;

    static {
        commands.put(MLST, "MLST");
        commands.put(MLSD, "MLSD");
        commands.put(SIZE, "SIZE");
        commands.put(PRET, "PRET");
    }

    /**
     * Take MLSD into account
     *
     * @param command
     * @param args
     * @return
     * @throws IOException
     */
    @Override
    public int sendCommand(int command, String args) throws IOException {
        return super.sendCommand(this.getCommand(command), args);
    }

    /**
     * Take MLSD into account
     *
     * @param command
     * @return
     */
    protected String getCommand(int command) {
        String value = commands.get(command);
        if(null == value) {
            return FTPCommand.getCommand(command);
        }
        return value;
    }

    @Override
    protected Socket _openDataConnection_(int command, String arg) throws IOException {
        Socket socket = super._openDataConnection_(command, arg);
        if(null == socket) {
            throw new FTPException(this.getReplyString());
        }
        return socket;
    }

    /**
     * SSL versions enabled.
     */
    private List<String> versions = Collections.emptyList();

    @Override
    public void setEnabledProtocols(String[] protocols) {
        versions = Arrays.asList(protocols);
        super.setEnabledProtocols(protocols);
    }

    @Override
    protected void execAUTH() throws IOException {
        if(versions.isEmpty()) {
            log.debug("No trust manager configured");
            return;
        }
        super.execAUTH();
    }

    @Override
    public void execPROT(String prot) throws SSLException, IOException {
        try {
            super.execPROT(prot);
        }
        catch(SSLException e) {
            if("P".equals(prot)) {
                // Compatibility mode if server does only accept clear data connections.
                log.warn("No data channel security: " + e.getMessage());
                this.setSocketFactory(null);
                this.setServerSocketFactory(null);
            }
        }
    }

    @Override
    protected void sslNegotiation() throws java.io.IOException {
        if(versions.isEmpty()) {
            log.debug("No trust manager configured");
            return;
        }
        super.sslNegotiation();
    }

    /**
     * No argument to command
     *
     * @param command
     * @return
     * @throws IOException
     */
    public List<String> list(int command) throws IOException {
        return this.list(command, null);
    }

    /**
     * Read from the data socket
     *
     * @param command
     * @param pathname
     * @return
     * @throws IOException
     */
    public List<String> list(int command, String pathname) throws IOException {
        this.pret(this.getCommand(command), pathname);

        Socket socket = _openDataConnection_(command, pathname);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), getControlEncoding()));
        ArrayList<String> results = new ArrayList<String>();
        String line;
        while((line = reader.readLine()) != null) {
            _commandSupport_.fireReplyReceived(-1, line);
            results.add(line);
        }

        reader.close();
        socket.close();

        if(completePendingCommand()) {
            return results;
        }
        return Collections.emptyList();
    }

    @Override
    public boolean retrieveFile(String remote, OutputStream local) throws IOException {
        this.pret(this.getCommand(FTPCommand.RETR), remote);
        return super.retrieveFile(remote, local);
    }

    @Override
    public InputStream retrieveFileStream(String remote) throws IOException {
        this.pret(this.getCommand(FTPCommand.RETR), remote);
        return super.retrieveFileStream(remote);
    }

    @Override
    public boolean storeFile(String remote, InputStream local) throws IOException {
        this.pret(this.getCommand(FTPCommand.STOR), remote);
        return super.storeFile(remote, local);
    }

    @Override
    public OutputStream storeFileStream(String remote) throws IOException {
        this.pret(this.getCommand(FTPCommand.STOR), remote);
        return super.storeFileStream(remote);
    }

    @Override
    public boolean appendFile(String remote, InputStream local) throws IOException {
        this.pret(this.getCommand(FTPCommand.APPE), remote);
        return super.appendFile(remote, local);
    }

    @Override
    public OutputStream appendFileStream(String remote) throws IOException {
        this.pret(this.getCommand(FTPCommand.APPE), remote);
        return super.appendFileStream(remote);
    }

    /**
     * http://drftpd.org/index.php/PRET_Specifications
     *
     * @param file
     * @throws IOException
     */
    protected void pret(String command, String file) throws IOException {
        if(this.isFeatureSupported(PRET)) {
            // PRET support
            if(!FTPReply.isPositiveCompletion(this.sendCommand(PRET, command + " " + file))) {
                log.warn("PRET command failed:" + this.getReplyString());
            }
        }
    }

    /**
     * @param pathname
     * @return -1 if unknown
     */
    public long getSize(String pathname) throws IOException {
        if(this.isFeatureSupported(SIZE)) {
            if(!this.setFileType(FTPClient.BINARY_FILE_TYPE)) {
                throw new FTPException(this.getReplyString());
            }
            if(FTPReply.isPositiveCompletion(this.sendCommand(SIZE, pathname))) {
                String status = StringUtils.chomp(this.getReplyString().substring(3).trim());
                // Trim off any trailing characters after a space, e.g. webstar
                // responds to SIZE with 213 55564 bytes
                Matcher matcher = Pattern.compile("\\d+").matcher(status);
                if(matcher.matches()) {
                    try {
                        return Long.parseLong(matcher.group());
                    }
                    catch(NumberFormatException ex) {
                        log.warn("Failed to parse reply:" + status);
                    }
                }
            }
        }
        return -1;
    }
}
