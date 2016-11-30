package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.date.MDTMMillisecondsDateFormatter;
import ch.cyberduck.core.date.MDTMSecondsDateFormatter;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPMlsdListResponseReader implements FTPDataResponseReader {
    private static final Logger log = Logger.getLogger(FTPMlsdListResponseReader.class);

    public FTPMlsdListResponseReader() {
        //
    }

    @Override
    public AttributedList<Path> read(final Path directory, final List<String> replies, final ListProgressListener listener)
            throws IOException, FTPInvalidListException, ConnectionCanceledException {
        final AttributedList<Path> children = new AttributedList<Path>();
        // At least one entry successfully parsed
        boolean success = false;
        for(String line : replies) {
            final Map<String, Map<String, String>> file = this.parseFacts(line);
            if(null == file) {
                log.error(String.format("Error parsing line %s", line));
                continue;
            }
            for(Map.Entry<String, Map<String, String>> f : file.entrySet()) {
                final String name = f.getKey();
                // size       -- Size in octets
                // modify     -- Last modification time
                // create     -- Creation time
                // type       -- Entry type
                // unique     -- Unique id of file/directory
                // perm       -- File permissions, whether read, write, execute is allowed for the login id.
                // lang       -- Language of the file name per IANA [11] registry.
                // media-type -- MIME media-type of file contents per IANA registry.
                // charset    -- Character set per IANA registry (if not UTF-8)
                final Map<String, String> facts = f.getValue();
                if(!facts.containsKey("type")) {
                    log.error(String.format("No type fact in line %s", line));
                    continue;
                }
                final Path parsed;
                if("dir".equals(facts.get("type").toLowerCase(Locale.ROOT))) {
                    parsed = new Path(directory, PathNormalizer.name(f.getKey()), EnumSet.of(Path.Type.directory));
                }
                else if("file".equals(facts.get("type").toLowerCase(Locale.ROOT))) {
                    parsed = new Path(directory, PathNormalizer.name(f.getKey()), EnumSet.of(Path.Type.file));
                }
                else if(facts.get("type").toLowerCase(Locale.ROOT).matches("os\\.unix=slink:.*")) {
                    parsed = new Path(directory, PathNormalizer.name(f.getKey()), EnumSet.of(Path.Type.file, Path.Type.symboliclink));
                    // Parse symbolic link target in Type=OS.unix=slink:/foobar;Perm=;Unique=keVO1+4G4; foobar
                    final String[] type = facts.get("type").split(":");
                    if(type.length == 2) {
                        final String target = type[1];
                        if(target.startsWith(String.valueOf(Path.DELIMITER))) {
                            parsed.setSymlinkTarget(new Path(target, EnumSet.of(Path.Type.file)));
                        }
                        else {
                            parsed.setSymlinkTarget(new Path(String.format("%s/%s", directory.getAbsolute(), target), EnumSet.of(Path.Type.file)));
                        }
                    }
                    else {
                        log.warn(String.format("Missing symbolic link target for type %s in line %s", facts.get("type"), line));
                        continue;
                    }
                }
                else {
                    log.warn(String.format("Ignored type %s in line %s", facts.get("type"), line));
                    continue;
                }
                if(!success) {
                    if(parsed.isDirectory() && directory.getName().equals(name)) {
                        log.warn(String.format("Possibly bogus response line %s", line));
                    }
                    else {
                        success = true;
                    }
                }
                if(name.equals(".") || name.equals("..")) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Skip %s", name));
                    }
                    continue;
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
                    parsed.attributes().setPermission(new Permission(facts.get("unix.mode")));
                }
                else if(facts.containsKey("perm")) {
                    Permission.Action user = Permission.Action.none;
                    final String flags = facts.get("perm");
                    if(StringUtils.contains(flags, 'r') || StringUtils.contains(flags, 'l')) {
                        // RETR command may be applied to that object
                        // Listing commands, LIST, NLST, and MLSD may be applied
                        user.or(Permission.Action.read);
                    }
                    if(StringUtils.contains(flags, 'w') || StringUtils.contains(flags, 'm') || StringUtils.contains(flags, 'c')) {
                        user.or(Permission.Action.write);
                    }
                    if(StringUtils.contains(flags, 'e')) {
                        // CWD command naming the object should succeed
                        user.or(Permission.Action.execute);
                    }
                    final Permission permission = new Permission(user, Permission.Action.none, Permission.Action.none);
                    parsed.attributes().setPermission(permission);
                }
                if(facts.containsKey("modify")) {
                    // Time values are always represented in UTC
                    parsed.attributes().setModificationDate(this.parseTimestamp(facts.get("modify")));
                }
                if(facts.containsKey("create")) {
                    // Time values are always represented in UTC
                    parsed.attributes().setCreationDate(this.parseTimestamp(facts.get("create")));
                }
                children.add(parsed);
                listener.chunk(directory, children);
            }
        }
        if(!success) {
            throw new FTPInvalidListException(children);
        }
        return children;
    }

    /**
     * Parse the timestamp using the MTDM format
     *
     * @param timestamp Date string
     * @return Milliseconds
     */
    protected long parseTimestamp(final String timestamp) {
        if(null == timestamp) {
            return -1;
        }
        try {
            final Date parsed = new MDTMSecondsDateFormatter().parse(timestamp);
            return parsed.getTime();
        }
        catch(InvalidDateException e) {
            log.warn("Failed to parse timestamp:" + e.getMessage());
            try {
                final Date parsed = new MDTMMillisecondsDateFormatter().parse(timestamp);
                return parsed.getTime();
            }
            catch(InvalidDateException f) {
                log.warn("Failed to parse timestamp:" + f.getMessage());
            }
        }
        log.error(String.format("Failed to parse timestamp %s", timestamp));
        return -1;
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
     * @param line The "facts" for a file in a reply to a MLSx command
     * @return Parsed keys and values
     */
    protected Map<String, Map<String, String>> parseFacts(final String line) {
        final Pattern p = Pattern.compile("\\s?(\\S+\\=\\S+;)*\\s(.*)");
        final Matcher result = p.matcher(line);
        final Map<String, Map<String, String>> file = new HashMap<String, Map<String, String>>();
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
                facts.put(key.toLowerCase(Locale.ROOT), value);
            }
            file.put(filename, facts);
            return file;
        }
        log.warn(String.format("No match for %s", line));
        return null;
    }

}
