package ch.cyberduck.core.ftp;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * @version $Id$
 */
public class FTPPath extends Path {
    private static Logger log = Logger.getLogger(FTPPath.class);

    private static final String DOS_LINE_SEPARATOR = "\r\n";
    private static final String MAC_LINE_SEPARATOR = "\r";
    private static final String UNIX_LINE_SEPARATOR = "\n";

    static {
        PathFactory.addFactory(Protocol.FTP, new Factory());
    }

    private static class Factory extends PathFactory<FTPSession> {
        @Override
        protected Path create(FTPSession session, String path, int type) {
            return new FTPPath(session, path, type);
        }

        @Override
        protected Path create(FTPSession session, String parent, String name, int type) {
            return new FTPPath(session, parent, name, type);
        }

        @Override
        protected Path create(FTPSession session, String path, Local file) {
            return new FTPPath(session, path, file);
        }

        @Override
        protected <T> Path create(FTPSession session, T dict) {
            return new FTPPath(session, dict);
        }
    }

    private final FTPSession session;

    protected FTPPath(FTPSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected FTPPath(FTPSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected FTPPath(FTPSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected <T> FTPPath(FTPSession s, T dict) {
        super(dict);
        this.session = s;
    }

    @Override
    public FTPSession getSession() {
        return this.session;
    }

    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            session.setWorkdir(this);
            // Cached file parser determined from SYST response with the timezone set from the bookmark
            final FTPFileEntryParser parser = session.getFileParser();
            boolean success = this.parse(childs, parser, session.FTP.stat(
                    StringUtils.isNotEmpty(this.getSymlinkTarget()) ? this.getSymlinkTarget() : this.getAbsolute()));
            if(!success || childs.isEmpty()) {
                // STAT listing failed or empty
                // Set transfer type for traditional data socket file listings
                session.FTP.setTransferType(FTPTransferType.ASCII);
                final BufferedReader mlsd = session.FTP.mlsd(this.session.getEncoding());
                success = this.parse(childs, mlsd);
                // MLSD listing failed
                if(null != mlsd) {
                    // Close MLSD data socket
                    session.FTP.finishDir();
                }
                if(!success) {
                    final BufferedReader lsa = session.FTP.list(this.session.getEncoding(), true);
                    success = this.parse(childs, parser, lsa);
                    if(null != lsa) {
                        // Close LIST data socket
                        session.FTP.finishDir();
                    }
                    if(!success) {
                        // LIST -a listing failed
                        final BufferedReader ls = session.FTP.list(this.session.getEncoding(), false);
                        success = this.parse(childs, parser, ls);
                        if(null != ls) {
                            // Close LIST data socket
                            session.FTP.finishDir();
                        }
                        if(!success) {
                            // LIST listing failed
                            log.error("No compatible file listing method found");
                        }
                    }
                }
            }
            for(Path child : childs) {
                if(child.attributes.getType() == Path.SYMBOLIC_LINK_TYPE) {
                    try {
                        session.setWorkdir(child);
                        child.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                    }
                    catch(FTPException e) {
                        child.attributes.setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                    }
                }
            }
        }
        catch(IOException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
        }
        return childs;
    }

    /**
     * The "facts" for a file in a reply to a MLSx command consist of
     * information about that file.  The facts are a series of keyword=value
     * pairs each followed by semi-colon (";") characters.  An individual
     * fact may not contain a semi-colon in its name or value.  The complete
     * series of facts may not contain the space character.  See the
     * definition or "RCHAR" in section 2.1 for a list of the characters
     * that can occur in a fact value.  Not all are applicable to all facts.
     * <p/>
     * A sample of a typical series of facts would be: (spread over two
     * lines for presentation here only)
     * <p/>
     * size=4161;lang=en-US;modify=19970214165800;create=19961001124534;
     * type=file;x.myfact=foo,bar;
     * <p/>
     * This document defines a standard set of facts as follows:
     * <p/>
     * size       -- Size in octets
     * modify     -- Last modification time
     * create     -- Creation time
     * type       -- Entry type
     * unique     -- Unique id of file/directory
     * perm       -- File permissions, whether read, write, execute is
     * allowed for the login id.
     * lang       -- Language of the file name per IANA [11] registry.
     * media-type -- MIME media-type of file contents per IANA registry.
     * charset    -- Character set per IANA registry (if not UTF-8)
     *
     * @param response
     * @return
     */
    protected Map<String, Map<String, String>> parseFacts(String[] response) {
        Map<String, Map<String, String>> files = new HashMap<String, Map<String, String>>();
        for(String line : response) {
            files.putAll(this.parseFacts(line));
        }
        return files;
    }

    /**
     * @param line
     */
    protected Map<String, Map<String, String>> parseFacts(String line) {
        final Pattern p = Pattern.compile("\\s?(\\S+\\=\\S+;)*\\s(.*)");
        final Matcher result = p.matcher(line);
        Map<String, Map<String, String>> file = new HashMap<String, Map<String, String>>();
        if(result.matches()) {
            final String filename = result.group(2);
            final Map<String, String> facts = new HashMap<String, String>();
            for(String fact : result.group(1).split(";")) {
                if(fact.contains("=")) {
                    facts.put(fact.split("=")[0].toLowerCase(), fact.split("=")[1].toLowerCase());
                }
            }
            file.put(filename, facts);
        }
        else {
            log.warn("No match for " + line);
        }
        return file;
    }

    /**
     * Parse response of MLSD
     *
     * @param childs
     * @param reader
     * @return
     * @throws IOException
     */
    private boolean parse(final AttributedList<Path> childs, BufferedReader reader)
            throws IOException {

        if(null == reader) {
            // This is an empty directory
            return false;
        }
        boolean success = false;
        String line;
        while((line = reader.readLine()) != null) {
            final Map<String, Map<String, String>> file = this.parseFacts(line);
            if(file.isEmpty()) {
                continue;
            }
            success = true; // At least one entry successfully parsed
            for(String name : file.keySet()) {
                final Path parsed = PathFactory.createPath(session, this.getAbsolute(), name, Path.FILE_TYPE);
                parsed.setParent(this);
                //                * size       -- Size in octets
                //                * modify     -- Last modification time
                //                * create     -- Creation time
                //                * type       -- Entry type
                //                * unique     -- Unique id of file/directory
                //                * perm       -- File permissions, whether read, write, execute is
                //                * allowed for the login id.
                //                * lang       -- Language of the file name per IANA [11] registry.
                //                * media-type -- MIME media-type of file contents per IANA registry.
                //                * charset    -- Character set per IANA registry (if not UTF-8)
                for(Map<String, String> facts : file.values()) {
                    if(!facts.containsKey("type")) {
                        continue;
                    }
                    if("dir".equals(facts.get("type").toLowerCase())) {
                        parsed.attributes.setType(Path.DIRECTORY_TYPE);
                    }
                    else if("file".equals(facts.get("type").toLowerCase())) {
                        parsed.attributes.setType(Path.FILE_TYPE);
                    }
                    else {
                        log.warn("Unsupported type: " + line);
                        continue;
                    }
                    if(facts.containsKey("sizd")) {
                        parsed.attributes.setSize(Long.parseLong(facts.get("sizd")));
                    }
                    if(facts.containsKey("size")) {
                        parsed.attributes.setSize(Long.parseLong(facts.get("size")));
                    }
                    if(facts.containsKey("unix.uid")) {
                        parsed.attributes.setOwner(facts.get("unix.uid"));
                    }
                    if(facts.containsKey("unix.owner")) {
                        parsed.attributes.setOwner(facts.get("unix.owner"));
                    }
                    if(facts.containsKey("unix.gid")) {
                        parsed.attributes.setGroup(facts.get("unix.gid"));
                    }
                    if(facts.containsKey("unix.group")) {
                        parsed.attributes.setGroup(facts.get("unix.group"));
                    }
                    if(facts.containsKey("unix.mode")) {
                        parsed.attributes.setPermission(new Permission(Integer.parseInt(facts.get("unix.mode"))));
                    }
                    if(facts.containsKey("modify")) {
                        parsed.attributes.setModificationDate(session.FTP.parseTimestamp(facts.get("modify")));
                    }
                    if(facts.containsKey("create")) {
                        parsed.attributes.setCreationDate(session.FTP.parseTimestamp(facts.get("create")));
                    }
                    childs.add(parsed);
                }
            }
        }
        return success;
    }

    /**
     * Parse all lines from the reader.
     *
     * @param parser
     * @param reader
     * @return An empty list if no parsable lines are found
     * @throws IOException
     */
    private boolean parse(final AttributedList<Path> childs, FTPFileEntryParser parser, BufferedReader reader)
            throws IOException {
        if(null == reader) {
            // This is an empty directory
            return false;
        }
        boolean success = false;
        String line;
        while((line = parser.readNextEntry(reader)) != null) {
            final FTPFile f = parser.parseFTPEntry(line);
            if(null == f) {
                continue;
            }
            final String name = f.getName();
            if(!success) {
                // Workaround for #2410. STAT only returns ls of directory itself
                // Workaround for #2434. STAT of symbolic link directory only lists the directory itself.
                if(this.getAbsolute().equals(name)) {
                    continue;
                }
            }
            success = true; // At least one entry successfully parsed
            if(name.equals(".") || name.equals("..")) {
                continue;
            }
            // The filename should never contain a delimiter
            final Path parsed = PathFactory.createPath(session, this.getAbsolute(),
                    name.substring(name.lastIndexOf(DELIMITER) + 1), Path.FILE_TYPE);
            parsed.setParent(this);
            switch(f.getType()) {
                case FTPFile.SYMBOLIC_LINK_TYPE:
                    parsed.setSymlinkTarget(this.getAbsolute(), f.getLink());
                    parsed.attributes.setType(Path.SYMBOLIC_LINK_TYPE);
                    break;
                case FTPFile.DIRECTORY_TYPE:
                    parsed.attributes.setType(Path.DIRECTORY_TYPE);
                    break;
            }
            parsed.attributes.setSize(f.getSize());
            parsed.attributes.setOwner(f.getUser());
            parsed.attributes.setGroup(f.getGroup());
            if(session.isPermissionSupported(parser)) {
                parsed.attributes.setPermission(new Permission(
                        new boolean[][]{
                                {f.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION),
                                        f.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION),
                                        f.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION)
                                },
                                {f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION),
                                        f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION),
                                        f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION)
                                },
                                {f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION),
                                        f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION),
                                        f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION)
                                }
                        }
                ));
            }
            final Calendar timestamp = f.getTimestamp();
            if(timestamp != null) {
                parsed.attributes.setModificationDate(timestamp.getTimeInMillis());
            }
            childs.add(parsed);
        }
        return success;
    }

    @Override
    public void mkdir(boolean recursive) {
        log.debug("mkdir:" + this.getName());
        try {
            if(recursive) {
                if(!this.getParent().exists()) {
                    this.getParent().mkdir(recursive);
                }
            }
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));

            session.setWorkdir(this.getParent());
            session.FTP.mkdir(this.getName());
        }
        catch(IOException e) {
            this.error("Cannot create folder", e);
        }
    }

    @Override
    public void rename(AbstractPath renamed) {
        log.debug("rename:" + renamed);
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            session.setWorkdir(this.getParent());
            session.FTP.rename(this.getName(), renamed.getAbsolute());
            this.setPath(renamed.getAbsolute());
        }
        catch(IOException e) {
            if(attributes.isFile()) {
                this.error("Cannot rename file", e);
            }
            if(attributes.isDirectory()) {
                this.error("Cannot rename folder", e);
            }
        }
    }

    @Override
    public void readSize() {
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                    this.getName()));

            if(attributes.isFile()) {
                if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                    if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                        session.FTP.setTransferType(FTPTransferType.ASCII);
                    }
                    else {
                        session.FTP.setTransferType(FTPTransferType.BINARY);
                    }
                }
                else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                        FTPTransferType.BINARY.toString())) {
                    session.FTP.setTransferType(FTPTransferType.BINARY);
                }
                else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                        FTPTransferType.ASCII.toString())) {
                    session.FTP.setTransferType(FTPTransferType.ASCII);
                }
                else {
                    throw new FTPException("Transfer type not set");
                }
                attributes.setSize(session.FTP.size(this.getAbsolute()));
            }
            if(-1 == attributes.getSize()) {
                // Read the size from the directory listing
                final AttributedList<AbstractPath> l = this.getParent().childs();
                if(l.contains(this)) {
                    attributes.setSize(l.get(l.indexOf(this)).attributes.getSize());
                }
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void readTimestamp() {
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                    this.getName()));

            if(attributes.isFile()) {
                // The "pathname" specifies an object in the NVFS which may be the object of a RETR command.
                // Attempts to query the modification time of files that exist but are unable to be
                // retrieved may generate an error-response
                attributes.setModificationDate(session.FTP.mdtm(this.getAbsolute()));
            }
            if(-1 == attributes.getModificationDate()) {
                // Read the timestamp from the directory listing
                final AttributedList<AbstractPath> l = this.getParent().childs();
                if(l.contains(this)) {
                    attributes.setModificationDate(l.get(l.indexOf(this)).attributes.getModificationDate());
                }
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);

        }
    }

    @Override
    public void readPermission() {
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                    this.getName()));

            // Read the permission from the directory listing
            final AttributedList<AbstractPath> l = this.getParent().childs();
            if(l.contains(this)) {
                attributes.setPermission(l.get(l.indexOf(this)).attributes.getPermission());
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void delete() {
        log.debug("delete:" + this.toString());
        try {
            session.check();
            if(attributes.isFile() || attributes.isSymbolicLink()) {
                session.setWorkdir(this.getParent());
                session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                session.FTP.delete(this.getName());
            }
            else if(attributes.isDirectory()) {
                session.setWorkdir(this);
                for(AbstractPath file : this.childs()) {
                    if(!session.isConnected()) {
                        break;
                    }
                    if(file.attributes.isFile() || file.attributes.isSymbolicLink()) {
                        session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                                file.getName()));

                        session.FTP.delete(file.getName());
                    }
                    else if(file.attributes.isDirectory()) {
                        file.delete();
                    }
                }
                session.setWorkdir(this.getParent());
                session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                session.FTP.rmdir(this.getName());
            }
        }
        catch(IOException e) {
            if(attributes.isFile()) {
                this.error("Cannot delete file", e);
            }
            if(attributes.isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    @Override
    public void writeOwner(String owner, boolean recursive) {
        String command = "chown";
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Changing owner of {0} to {1}", "Status"),
                    this.getName(), owner));

            session.setWorkdir(this.getParent());
            if(attributes.isFile() && !attributes.isSymbolicLink()) {
                session.FTP.site(command + " " + owner + " " + this.getName());
            }
            else if(attributes.isDirectory()) {
                session.FTP.site(command + " " + owner + " " + this.getName());
                if(recursive) {
                    for(AbstractPath child : this.childs()) {
                        if(!session.isConnected()) {
                            break;
                        }
                        ((Path) child).writeOwner(owner, recursive);
                    }
                }
            }
        }
        catch(IOException e) {
            this.error("Cannot change owner", e);
        }
    }

    @Override
    public void writeGroup(String group, boolean recursive) {
        String command = "chgrp";
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Changing group of {0} to {1}", "Status"),
                    this.getName(), group));

            session.setWorkdir(this.getParent());
            if(attributes.isFile() && !attributes.isSymbolicLink()) {
                session.FTP.site(command + " " + group + " " + this.getName());
            }
            else if(attributes.isDirectory()) {
                session.FTP.site(command + " " + group + " " + this.getName());
                if(recursive) {
                    for(AbstractPath child : this.childs()) {
                        if(!session.isConnected()) {
                            break;
                        }
                        ((Path) child).writeGroup(group, recursive);
                    }
                }
            }
        }
        catch(IOException e) {
            this.error("Cannot change group", e);
        }
    }

    @Override
    public void writePermissions(Permission perm, boolean recursive) {
        log.debug("changePermissions:" + perm);
        final String command = "CHMOD";
        try {
            session.check();
            session.message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                    this.getName(), perm.getOctalString()));

            session.setWorkdir(this.getParent());
            if(attributes.isFile() && !attributes.isSymbolicLink()) {
                if(recursive) {
                    // Do not write executable bit for files if not already set when recursively updating directory.
                    // See #1787
                    Permission modified = new Permission(perm);
                    if(!attributes.getPermission().getOwnerPermissions()[Permission.EXECUTE]) {
                        modified.getOwnerPermissions()[Permission.EXECUTE] = false;
                    }
                    if(!attributes.getPermission().getGroupPermissions()[Permission.EXECUTE]) {
                        modified.getGroupPermissions()[Permission.EXECUTE] = false;
                    }
                    if(!attributes.getPermission().getOtherPermissions()[Permission.EXECUTE]) {
                        modified.getOtherPermissions()[Permission.EXECUTE] = false;
                    }
                    session.FTP.site(command + " " + modified.getOctalString() + " " + this.getName());
                }
                else {
                    session.FTP.site(command + " " + perm.getOctalString() + " " + this.getName());
                }
                attributes.setPermission(perm);
            }
            else if(attributes.isDirectory()) {
                session.FTP.site(command + " " + perm.getOctalString() + " " + this.getName());
                if(recursive) {
                    for(AbstractPath child : this.childs()) {
                        if(!session.isConnected()) {
                            break;
                        }
                        child.writePermissions(perm, recursive);
                    }
                }
                attributes.setPermission(perm);
            }
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    @Override
    public void download(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        log.debug("download:" + this.toString());
        try {
            if(check) {
                session.check();
            }
            if(attributes.isFile()) {
                session.setWorkdir(this.getParent());
                if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                    if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                        this.downloadASCII(throttle, listener);
                    }
                    else {
                        this.downloadBinary(throttle, listener);
                    }
                }
                else if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.BINARY.toString())) {
                    this.downloadBinary(throttle, listener);
                }
                else if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.ASCII.toString())) {
                    this.downloadASCII(throttle, listener);
                }
                else {
                    throw new FTPException("Transfer mode not set");
                }
            }
            else if(attributes.isDirectory()) {
                this.getLocal().mkdir(true);
            }
        }
        catch(IOException e) {
            this.error("Download failed", e);
        }
    }

    private void downloadBinary(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            if(this.getStatus().isResume()) {
                if(!session.FTP.isFeatureSupported("REST STREAM")) {
                    this.getStatus().setResume(false);
                }
            }
            in = session.FTP.get(this.getName(), this.getStatus().isResume() ? this.getLocal().attributes.getSize() : 0);
            out = new Local.OutputStream(this.getLocal(), this.getStatus().isResume());
            this.download(in, out, throttle, listener);
            if(this.getStatus().isComplete()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.validateTransfer();
            }
            if(this.getStatus().isCanceled()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.abor();
            }
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    private void downloadASCII(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            String lineSeparator = System.getProperty("line.separator"); //default value
            if(Preferences.instance().getProperty("ftp.line.separator").equals("unix")) {
                lineSeparator = UNIX_LINE_SEPARATOR;
            }
            else if(Preferences.instance().getProperty("ftp.line.separator").equals("mac")) {
                lineSeparator = MAC_LINE_SEPARATOR;
            }
            else if(Preferences.instance().getProperty("ftp.line.separator").equals("win")) {
                lineSeparator = DOS_LINE_SEPARATOR;
            }
            session.FTP.setTransferType(FTPTransferType.ASCII);
            in = new FromNetASCIIInputStream(session.FTP.get(this.getName(), 0),
                    lineSeparator);
            out = new FromNetASCIIOutputStream(new Local.OutputStream(this.getLocal(), false),
                    lineSeparator);
            this.download(in, out, throttle, listener);
            if(this.getStatus().isComplete()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.validateTransfer();
            }
            if(this.getStatus().isCanceled()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.abor();
            }
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener, final Permission p, final boolean check) {
        log.debug("upload:" + this.toString());
        try {
            if(check) {
                session.check();
            }
            if(attributes.isFile()) {
                session.setWorkdir(this.getParent());
                if(Preferences.instance().getProperty("ftp.transfermode").equals(FTPTransferType.AUTO.toString())) {
                    if(this.getTextFiletypePattern().matcher(this.getName()).matches()) {
                        this.uploadASCII(throttle, listener);
                    }
                    else {
                        this.uploadBinary(throttle, listener);
                    }
                }
                else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                        FTPTransferType.BINARY.toString())) {
                    this.uploadBinary(throttle, listener);
                }
                else if(Preferences.instance().getProperty("ftp.transfermode").equals(
                        FTPTransferType.ASCII.toString())) {
                    this.uploadASCII(throttle, listener);
                }
                else {
                    throw new FTPException("Transfer mode not set");
                }
            }
            if(attributes.isDirectory()) {
                this.mkdir();
            }
            if(null != p) {
                try {
                    log.info("Updating permissions:" + p.getOctalString());
                    session.FTP.chmod(p.getOctalString(),
                            this.getName());
                }
                catch(FTPException ignore) {
                    //CHMOD not supported; ignore
                    log.warn(ignore.getMessage());
                }
            }
            if(Preferences.instance().getBoolean("queue.upload.preserveDate")) {
                log.info("Updating timestamp");
                session.FTP.mfmt(this.getLocal().attributes.getModificationDate(),
                        this.getLocal().attributes.getCreationDate(), this.getName());
            }
        }
        catch(IOException e) {
            this.error("Upload failed", e);
        }
    }

    private void uploadBinary(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.BINARY);
            in = new Local.InputStream(this.getLocal());
            out = session.FTP.put(this.getName(), this.getStatus().isResume());
            if(null == out) {
                throw new IOException("Unable opening data stream");
            }
            this.upload(out, in, throttle, listener);
            if(this.getStatus().isComplete()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.validateTransfer();
            }
            if(getStatus().isCanceled()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.abor();
            }
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    private void uploadASCII(final BandwidthThrottle throttle, final StreamListener listener) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            session.FTP.setTransferType(FTPTransferType.ASCII);
            in = new ToNetASCIIInputStream(new Local.InputStream(this.getLocal()));
            out = new ToNetASCIIOutputStream(session.FTP.put(this.getName(),
                    this.getStatus().isResume()));
            this.upload(out, in, throttle, listener);
            if(this.getStatus().isComplete()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.validateTransfer();
            }
            if(getStatus().isCanceled()) {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
                session.FTP.abor();
            }
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }
}