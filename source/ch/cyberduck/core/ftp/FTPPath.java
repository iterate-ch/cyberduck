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
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.ui.DateFormatterFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Id$
 */
public class FTPPath extends Path {
    private static Logger log = Logger.getLogger(FTPPath.class);

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
        protected Path create(FTPSession session, String parent, Local file) {
            return new FTPPath(session, parent, file);
        }

        @Override
        protected <T> Path create(FTPSession session, T dict) {
            return new FTPPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
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
        return session;
    }

    /**
     *
     */
    private abstract static class DataConnectionAction {
        /**
         * Implementation
         *
         * @return
         * @throws IOException
         */
        public abstract boolean run() throws IOException;
    }

    /**
     *
     * @param action
     * @return
     * @throws IOException
     */
    private boolean data(DataConnectionAction action) throws IOException {
        try {
            return action.run();
        }
        catch(SocketTimeoutException failure) {
            log.warn("Timeout opening data socket:" + failure.getMessage());
            // Fallback handling
            if(Preferences.instance().getBoolean("ftp.connectmode.fallback")) {
                this.getSession().interrupt();
                this.getSession().check();
                try {
                    return this.fallback(action);
                }
                catch(IOException e) {
                    this.getSession().interrupt();
                    log.warn("Connect mode fallback failed:" + e.getMessage());
                    // Throw original error message
                    throw failure;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param action
     * @return
     * @throws IOException
     */
    private boolean fallback(DataConnectionAction action) throws IOException {
        // Fallback to other connect mode
        if(getSession().getClient().getDataConnectionMode() == FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to active data connection");
            this.getSession().getClient().enterLocalActiveMode();
        }
        else if(this.getSession().getClient().getDataConnectionMode() == FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to passive data connection");
            this.getSession().getClient().enterLocalPassiveMode();
        }
        return action.run();
    }

    @Override
    public AttributedList<Path> list(final AttributedList<Path> children) {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                        this.getName()));

                // Cached file parser determined from SYST response with the timezone set from the bookmark
                final FTPFileEntryParser parser = this.getSession().getFileParser();
                boolean success = false;
                try {
                    if(this.getSession().isStatListSupportedEnabled()) {
                        int response = this.getSession().getClient().stat(this.getAbsolute());
                        if(FTPReply.isPositiveCompletion(response)) {
                            String[] reply = this.getSession().getClient().getReplyStrings();
                            final List<String> result = new ArrayList<String>(reply.length);
                            for(final String line : reply) {
                                //Some servers include the status code for every line.
                                if(line.startsWith(String.valueOf(response))) {
                                    try {
                                        result.add(line.substring(line.indexOf(response) + line.length() + 1).trim());
                                    }
                                    catch(IndexOutOfBoundsException e) {
                                        log.error("Failed parsing line '" + line + "':" + e.getMessage());
                                    }
                                }
                                else {
                                    result.add(StringUtils.stripStart(line, null));
                                }
                            }
                            success = this.parseListResponse(children, parser, result);
                        }
                        else {
                            this.getSession().setStatListSupportedEnabled(false);
                        }
                    }
                }
                catch(IOException e) {
                    log.warn("Command STAT failed with I/O error:" + e.getMessage());
                    this.getSession().interrupt();
                    this.getSession().check();
                }
                if(!success || children.isEmpty()) {
                    success = this.data(new DataConnectionAction() {
                        @Override
                        public boolean run() throws IOException {
                            if(!getSession().getClient().changeWorkingDirectory(getAbsolute())) {
                                throw new FTPException(getSession().getClient().getReplyString());
                            }
                            if(!getSession().getClient().setFileType(FTPClient.ASCII_FILE_TYPE)) {
                                // Set transfer type for traditional data socket file listings. The data transfer is over the
                                // data connection in type ASCII or type EBCDIC.
                                throw new FTPException(getSession().getClient().getReplyString());
                            }
                            boolean success = false;
                            // STAT listing failed or empty
                            if(getSession().isMlsdListSupportedEnabled()
                                    // Note that there is no distinct FEAT output for MLSD.
                                    // The presence of the MLST feature indicates that both MLST and MLSD are supported.
                                    && getSession().getClient().isFeatureSupported(FTPClient.MLST)) {
                                success = parseMlsdResponse(children, getSession().getClient().list(FTPClient.MLSD));
                                if(!success) {
                                    getSession().setMlsdListSupportedEnabled(false);
                                }
                            }
                            if(!success) {
                                // MLSD listing failed or not enabled
                                if(getSession().isExtendedListEnabled()) {
                                    success = parseListResponse(children, parser, getSession().getClient().list(FTPCommand.LIST, "-a"));
                                }
                                if(!success) {
                                    // LIST -a listing failed or not enabled
                                    getSession().setExtendedListEnabled(false);
                                    success = parseListResponse(children, parser, getSession().getClient().list(FTPCommand.LIST));
                                }
                            }
                            return success;
                        }
                    });
                }
                for(Path child : children) {
                    if(child.attributes().isSymbolicLink()) {
                        if(this.getSession().getClient().changeWorkingDirectory(child.getAbsolute())) {
                            child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                        }
                        else {
                            // Try if CWD to symbolic link target succeeds
                            if(this.getSession().getClient().changeWorkingDirectory(child.getSymlinkTarget().getAbsolute())) {
                                // Workdir change succeeded
                                child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.DIRECTORY_TYPE);
                            }
                            else {
                                child.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                            }
                        }
                    }
                }
                if(success) {
                    this.getSession().setWorkdir(this);
                }
                else {
                    // LIST listing failed
                    log.error("No compatible file listing method found");
                }
            }
            catch(IOException e) {
                log.warn("Listing directory failed:" + e.getMessage());
                children.attributes().setReadable(false);
                if(this.cache().isEmpty()) {
                    this.error(e.getMessage(), e);
                }
            }
        }
        return children;
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
                String key = StringUtils.substringBefore(fact, "=");
                if(StringUtils.isBlank(key)) {
                    continue;
                }
                String value = StringUtils.substringAfter(fact, "=");
                if(StringUtils.isBlank(value)) {
                    continue;
                }
                facts.put(key.toLowerCase(), value);
            }
            file.put(filename, facts);
            return file;
        }
        log.warn("No match for " + line);
        return null;
    }

    /**
     * Parse response of MLSD
     *
     * @param children
     * @param replies
     * @return
     * @throws IOException
     */
    protected boolean parseMlsdResponse(final AttributedList<Path> children, List<String> replies)
            throws IOException {

        if(null == replies) {
            // This is an empty directory
            return false;
        }
        boolean success = false; // At least one entry successfully parsed
        for(String line : replies) {
            final Map<String, Map<String, String>> file = this.parseFacts(line);
            if(null == file) {
                log.error("Error parsing line:" + line);
                continue;
            }
            for(String name : file.keySet()) {
                final Path parsed = PathFactory.createPath(this.getSession(), this.getAbsolute(),
                        StringUtils.removeStart(name, this.getAbsolute() + Path.DELIMITER), Path.FILE_TYPE);
                parsed.setParent(this);
                // size       -- Size in octets
                // modify     -- Last modification time
                // create     -- Creation time
                // type       -- Entry type
                // unique     -- Unique id of file/directory
                // perm       -- File permissions, whether read, write, execute is allowed for the login id.
                // lang       -- Language of the file name per IANA [11] registry.
                // media-type -- MIME media-type of file contents per IANA registry.
                // charset    -- Character set per IANA registry (if not UTF-8)
                for(Map<String, String> facts : file.values()) {
                    if(!facts.containsKey("type")) {
                        log.error("No type fact:" + line);
                        continue;
                    }
                    if("dir".equals(facts.get("type").toLowerCase())) {
                        parsed.attributes().setType(Path.DIRECTORY_TYPE);
                    }
                    else if("file".equals(facts.get("type").toLowerCase())) {
                        parsed.attributes().setType(Path.FILE_TYPE);
                    }
                    else {
                        log.warn("Ignored type: " + line);
                        break;
                    }
                    if(name.contains(String.valueOf(DELIMITER))) {
                        if(!name.startsWith(this.getAbsolute() + Path.DELIMITER)) {
                            // Workaround for #2434.
                            log.warn("Skip listing entry with delimiter:" + name);
                            continue;
                        }
                    }
                    if(!success) {
                        if("dir".equals(facts.get("type").toLowerCase()) && this.getName().equals(name)) {
                            log.warn("Possibly bogus response:" + line);
                        }
                        else {
                            success = true;
                        }
                    }
                    if(facts.containsKey("sizd")) {
                        parsed.attributes().setSize(Long.parseLong(facts.get("sizd")));
                    }
                    if(facts.containsKey("size")) {
                        parsed.attributes().setSize(Long.parseLong(facts.get("size")));
                    }
                    if(facts.containsKey("unix.uid")) {
                        parsed.attributes().setOwner(facts.get("unix.uid"));
                    }
                    if(facts.containsKey("unix.owner")) {
                        parsed.attributes().setOwner(facts.get("unix.owner"));
                    }
                    if(facts.containsKey("unix.gid")) {
                        parsed.attributes().setGroup(facts.get("unix.gid"));
                    }
                    if(facts.containsKey("unix.group")) {
                        parsed.attributes().setGroup(facts.get("unix.group"));
                    }
                    if(facts.containsKey("unix.mode")) {
                        try {
                            parsed.attributes().setPermission(new Permission(Integer.parseInt(facts.get("unix.mode"))));
                        }
                        catch(NumberFormatException e) {
                            log.error("Failed to parse fact:" + facts.get("unix.mode"));
                        }
                    }
                    if(facts.containsKey("modify")) {
                        parsed.attributes().setModificationDate(this.parseTimestamp(facts.get("modify")));
                    }
                    if(facts.containsKey("create")) {
                        parsed.attributes().setCreationDate(this.parseTimestamp(facts.get("create")));
                    }
                    if(facts.containsKey("charset")) {
                        if(!facts.get("charset").toLowerCase().equals(this.getSession().getEncoding().toLowerCase())) {
                            log.error("Incompatible charset " + facts.get("charset")
                                    + " but session is configured with "
                                    + this.getSession().getEncoding());
                        }
                    }
                    children.add(parsed);
                }
            }
        }
        return success;
    }

    /**
     * @param children
     * @param parser
     * @param replies
     * @return
     * @throws IOException
     */
    protected boolean parseListResponse(final AttributedList<Path> children, FTPFileEntryParser parser, List<String> replies)
            throws IOException {
        if(null == replies) {
            // This is an empty directory
            return false;
        }
        boolean success = false;
        for(String line : replies) {
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
                if(name.contains(String.valueOf(DELIMITER))) {
                    if(!name.startsWith(this.getAbsolute() + Path.DELIMITER)) {
                        // Workaround for #2434.
                        log.warn("Skip listing entry with delimiter:" + name);
                        continue;
                    }
                }
            }
            success = true;
            if(name.equals(".") || name.equals("..")) {
                continue;
            }
            final Path parsed = PathFactory.createPath(this.getSession(), this.getAbsolute(),
                    StringUtils.removeStart(name, this.getAbsolute() + Path.DELIMITER),
                    f.getType() == FTPFile.DIRECTORY_TYPE ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
            parsed.setParent(this);
            switch(f.getType()) {
                case FTPFile.SYMBOLIC_LINK_TYPE:
                    parsed.setSymlinkTarget(f.getLink());
                    parsed.attributes().setType(Path.SYMBOLIC_LINK_TYPE | Path.FILE_TYPE);
                    break;
            }
            parsed.attributes().setSize(f.getSize());
            parsed.attributes().setOwner(f.getUser());
            parsed.attributes().setGroup(f.getGroup());
            if(this.getSession().isPermissionSupported(parser)) {
                parsed.attributes().setPermission(new Permission(
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
                parsed.attributes().setModificationDate(timestamp.getTimeInMillis());
            }
            children.add(parsed);
        }
        return success;
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        this.getName()));

                if(this.getSession().getClient().makeDirectory(this.getAbsolute())) {
                    if(Preferences.instance().getBoolean("queue.upload.changePermissions")) {
                        if(Preferences.instance().getBoolean("queue.upload.permissions.useDefault")) {
                            this.writeUnixPermissionImpl(new Permission(
                                    Preferences.instance().getInteger("queue.upload.permissions.folder.default")), false);
                        }
                    }
                    this.cache().put(this.getReference(), AttributedList.<Path>emptyList());
                    // The directory listing is no more current
                    this.getParent().invalidate();
                }
                else {
                    throw new FTPException(this.getSession().getClient().getReplyString());
                }
            }
            catch(IOException e) {
                this.error("Cannot create folder {0}", e);
            }
        }
    }

    @Override
    public void rename(AbstractPath renamed) {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            if(this.getSession().getClient().rename(this.getAbsolute(), renamed.getAbsolute())) {
                // The directory listing of the target is no more current
                renamed.getParent().invalidate();
                // The directory listing of the source is no more current
                this.getParent().invalidate();
            }
            else {
                throw new FTPException(this.getSession().getClient().getReplyString());
            }
        }
        catch(IOException e) {
            this.error("Cannot rename {0}", e);
        }
    }

    @Override
    public void readSize() {
        if(this.attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                        this.getName()));

                if(this.getSession().getClient().isFeatureSupported(FTPClient.SIZE)) {
                    if(!getSession().getClient().setFileType(FTPClient.BINARY_FILE_TYPE)) {
                        throw new FTPException(getSession().getClient().getReplyString());
                    }
                    this.attributes().setSize(this.getSession().getClient().getSize(this.getAbsolute()));
                }
                if(-1 == attributes().getSize()) {
                    // Read the size from the directory listing
                    final AttributedList<AbstractPath> l = this.getParent().children();
                    if(l.contains(this.getReference())) {
                        attributes().setSize(l.get(this.getReference()).attributes().getSize());
                    }
                }
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
    }

    /**
     * Format to interpret MTDM timestamp
     */
    private SimpleDateFormat tsFormatSeconds =
            new SimpleDateFormat("yyyyMMddHHmmss");

    {
        tsFormatSeconds.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Format to interpret MTDM timestamp
     */
    private SimpleDateFormat tsFormatMilliseconds =
            new SimpleDateFormat("yyyyMMddHHmmss.SSS");

    {
        tsFormatMilliseconds.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Parse the timestamp using the MTDM format
     *
     * @param timestamp
     * @return
     */
    public long parseTimestamp(final String timestamp) {
        if(null == timestamp) {
            return -1;
        }
        try {
            Date parsed = tsFormatSeconds.parse(timestamp);
            return parsed.getTime();
        }
        catch(ParseException e) {
            log.warn("Failed to parse timestamp:" + e.getMessage());
            try {
                Date parsed = tsFormatMilliseconds.parse(timestamp);
                return parsed.getTime();
            }
            catch(ParseException f) {
                log.warn("Failed to parse timestamp:" + f.getMessage());
            }
        }
        log.error("Failed to parse timestamp:" + timestamp);
        return -1;
    }

    @Override
    public void readTimestamp() {
        if(this.attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                        this.getName()));

                if(this.getSession().getClient().isFeatureSupported(FTPCommand.MDTM)) {
                    // The "pathname" specifies an object in the NVFS which may be the object of a RETR command.
                    // Attempts to query the modification time of files that exist but are unable to be
                    // retrieved may generate an error-response
                    attributes().setModificationDate(
                            this.parseTimestamp(this.getSession().getClient().getModificationTime(this.getAbsolute())));
                }
                if(-1 == attributes().getModificationDate()) {
                    // Read the timestamp from the directory listing
                    final AttributedList<AbstractPath> l = this.getParent().children();
                    if(l.contains(this.getReference())) {
                        attributes().setModificationDate(l.get(this.getReference()).attributes().getModificationDate());
                    }
                }
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);

            }
        }
    }

    @Override
    public void readUnixPermission() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                    this.getName()));

            // Read the permission from the directory listing
            final AttributedList<AbstractPath> l = this.getParent().children();
            if(l.contains(this.getReference())) {
                attributes().setPermission(l.get(this.getReference()).attributes().getPermission());
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void delete() {
        try {
            this.getSession().check();
            if(attributes().isFile() || attributes().isSymbolicLink()) {
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                if(!this.getSession().getClient().deleteFile(this.getAbsolute())) {
                    throw new FTPException(this.getSession().getClient().getReplyString());
                }
            }
            else if(attributes().isDirectory()) {
                for(AbstractPath file : this.children()) {
                    if(!this.getSession().isConnected()) {
                        break;
                    }
                    if(file.attributes().isFile() || file.attributes().isSymbolicLink()) {
                        this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                                file.getName()));

                        if(!this.getSession().getClient().deleteFile(file.getAbsolute())) {
                            throw new FTPException(this.getSession().getClient().getReplyString());
                        }
                    }
                    else if(file.attributes().isDirectory()) {
                        file.delete();
                    }
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                if(!this.getSession().getClient().removeDirectory(this.getAbsolute())) {
                    throw new FTPException(this.getSession().getClient().getReplyString());
                }
            }
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            this.error("Cannot delete {0}", e);
        }
    }

    @Override
    public void writeOwner(String owner, boolean recursive) {
        String command = "chown";
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Changing owner of {0} to {1}", "Status"),
                    this.getName(), owner));

            if(attributes().isFile() && !attributes().isSymbolicLink()) {
                if(!this.getSession().getClient().sendSiteCommand(command + " " + owner + " " + this.getAbsolute())) {
                    throw new FTPException(this.getSession().getClient().getReplyString());
                }
            }
            else if(attributes().isDirectory()) {
                if(!this.getSession().getClient().sendSiteCommand(command + " " + owner + " " + this.getAbsolute())) {
                    throw new FTPException(this.getSession().getClient().getReplyString());
                }
                if(recursive) {
                    for(AbstractPath child : this.children()) {
                        if(!this.getSession().isConnected()) {
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
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Changing group of {0} to {1}", "Status"),
                    this.getName(), group));

            if(attributes().isFile() && !attributes().isSymbolicLink()) {
                if(!this.getSession().getClient().sendSiteCommand(command + " " + group + " " + this.getAbsolute())) {
                    throw new FTPException(this.getSession().getClient().getReplyString());
                }
            }
            else if(attributes().isDirectory()) {
                if(!this.getSession().getClient().sendSiteCommand(command + " " + group + " " + this.getAbsolute())) {
                    throw new FTPException(this.getSession().getClient().getReplyString());
                }
                if(recursive) {
                    for(AbstractPath child : this.children()) {
                        if(!this.getSession().isConnected()) {
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
    public void writeUnixPermission(Permission perm, boolean recursive) {
        try {
            this.getSession().check();
            this.writeUnixPermissionImpl(perm, recursive);
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    private boolean chmodSupported = true;

    private void writeUnixPermissionImpl(Permission perm, boolean recursive) throws IOException {
        if(chmodSupported) {
            this.getSession().message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                    this.getName(), perm.getOctalString()));
            try {
                if(attributes().isFile() && !attributes().isSymbolicLink()) {
                    if(this.getSession().getClient().sendSiteCommand("CHMOD " + perm.getOctalString() + " " + this.getAbsolute())) {
                        this.attributes().setPermission(perm);
                    }
                    else {
                        chmodSupported = false;
                    }
                }
                else if(attributes().isDirectory()) {
                    if(this.getSession().getClient().sendSiteCommand("CHMOD " + perm.getOctalString() + " " + this.getAbsolute())) {
                        this.attributes().setPermission(perm);
                    }
                    else {
                        chmodSupported = false;
                    }
                    if(recursive) {
                        for(AbstractPath child : this.children()) {
                            if(!this.getSession().isConnected()) {
                                break;
                            }
                            ((FTPPath) child).writeUnixPermissionImpl(perm, recursive);
                        }
                    }
                }
            }
            finally {
                //this.attributes().clear(false, false, true, false);
                ;// This will force a directory listing to parse the permissions again.
                //this.getParent().invalidate();
            }
        }
    }

    @Override
    public void writeTimestamp(long created, long modified, long accessed) {
        try {
            this.writeModificationDateImpl(created, modified);
        }
        catch(IOException e) {
            this.error("Cannot change timestamp", e);
        }
    }

    private void writeModificationDateImpl(long created, long modified) throws IOException {
        this.getSession().message(MessageFormat.format(Locale.localizedString("Changing timestamp of {0} to {1}", "Status"),
                this.getName(), DateFormatterFactory.instance().getShortFormat(modified)));
        try {
            if(this.getSession().getClient().isFeatureSupported(FTPCommand.MFMT)) {
                if(this.getSession().getClient().setModificationTime(this.getAbsolute(), tsFormatSeconds.format(modified))) {
                    this.attributes().setModificationDate(modified);
                }
            }
            else {
                if(this.getSession().isUtimeSupported()) {
                    // The utime() function sets the access and modification times of the named
                    // file from the structures in the argument array timep.
                    // The access time is set to the value of the first element,
                    // and the modification time is set to the value of the second element
                    // Accessed date, modified date, created date
                    if(this.getSession().getClient().sendSiteCommand("UTIME " + this.getAbsolute()
                            + " " + tsFormatSeconds.format(new Date(modified))
                            + " " + tsFormatSeconds.format(new Date(modified))
                            + " " + tsFormatSeconds.format(new Date(created))
                            + " UTC")) {
                        this.attributes().setModificationDate(modified);
                        this.attributes().setCreationDate(created);
                    }
                    else {
                        this.getSession().setUtimeSupported(false);
                        log.warn("UTIME not supported");
                    }
                }

            }
        }
        finally {
            //this.attributes().clear(true, false, false, false);
            ;// This will force a directory listing to parse the timestamp again if MDTM is not supported.
            //this.getParent().invalidate();
        }
    }

    @Override
    protected void download(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        if(this.attributes().isFile()) {
            try {
                if(check) {
                    this.getSession().check();
                }
                this.data(new DataConnectionAction() {
                    @Override
                    public boolean run() throws IOException {
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            if(!getSession().getClient().setFileType(FTP.BINARY_FILE_TYPE)) {
                                throw new FTPException(getSession().getClient().getReplyString());
                            }
                            if(status().isResume()) {
                                // Where a server process supports RESTart in STREAM mode
                                if(!getSession().getClient().isFeatureSupported("REST STREAM")) {
                                    status().setResume(false);
                                }
                                else {
                                    getSession().getClient().setRestartOffset(
                                            status().isResume() ? getLocal().attributes().getSize() : 0
                                    );
                                }
                            }
                            in = getSession().getClient().retrieveFileStream(getAbsolute());
                            out = getLocal().getOutputStream(status().isResume());
                            try {
                                download(in, out, throttle, listener);
                            }
                            catch(ConnectionCanceledException e) {
                                // Interrupted by user
                                IOUtils.closeQuietly(in);
                                IOUtils.closeQuietly(out);
                                // Tell the server to abort the previous command and any associated
                                // transfer of data
                                if(!getSession().getClient().abort()) {
                                    log.error("Interrupting file transfer failed:" + getSession().getClient().getReplyString());
                                }
                                status().setComplete(false);
                                throw e;
                            }
                            if(status().isComplete()) {
                                IOUtils.closeQuietly(in);
                                IOUtils.closeQuietly(out);
                                if(!getSession().getClient().completePendingCommand()) {
                                    status().setComplete(false);
                                    throw new FTPException(getSession().getClient().getReplyString());
                                }
                            }
                        }
                        finally {
                            IOUtils.closeQuietly(in);
                            IOUtils.closeQuietly(out);
                        }
                        return true;
                    }
                });
            }
            catch(IOException e) {
                this.error("Download failed", e);
            }
        }
    }

    @Override
    protected void upload(final BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        if(this.attributes().isFile()) {
            try {
                if(check) {
                    this.getSession().check();
                }
                this.data(new DataConnectionAction() {
                    @Override
                    public boolean run() throws IOException {
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            if(!getSession().getClient().setFileType(FTPClient.BINARY_FILE_TYPE)) {
                                throw new FTPException(getSession().getClient().getReplyString());
                            }
                            in = getLocal().getInputStream();
                            if(status().isResume()) {
                                out = getSession().getClient().appendFileStream(
                                        getAbsolute());
                            }
                            else {
                                out = getSession().getClient().storeFileStream(getAbsolute());
                            }
                            try {
                                upload(out, in, throttle, listener);
                            }
                            catch(ConnectionCanceledException e) {
                                // Interrupted by user
                                IOUtils.closeQuietly(in);
                                IOUtils.closeQuietly(out);
                                // Tell the server to abort the previous command and any associated
                                // transfer of data
                                if(!getSession().getClient().abort()) {
                                    log.error("Interrupting file transfer failed:" + getSession().getClient().getReplyString());
                                }
                                status().setComplete(false);
                                throw e;
                            }
                            if(status().isComplete()) {
                                IOUtils.closeQuietly(in);
                                IOUtils.closeQuietly(out);
                                if(!getSession().getClient().completePendingCommand()) {
                                    status().setComplete(false);
                                    throw new FTPException(getSession().getClient().getReplyString());
                                }
                            }
                        }
                        finally {
                            IOUtils.closeQuietly(in);
                            IOUtils.closeQuietly(out);
                        }
                        return true;
                    }
                });
            }
            catch(IOException e) {
                this.error("Upload failed", e);
            }
        }
    }
}