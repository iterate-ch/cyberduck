package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.dav.DAVPathMapper;
import ch.cyberduck.core.ocs.OcsCapabilities;
import ch.cyberduck.core.owncloud.model.Drives;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Maps virtual folders for personal space, project spaces and shares to WebDAV root of storage spaces
 */
public final class OwncloudPathMapper implements DAVPathMapper {
    private static final Logger log = LogManager.getLogger(OwncloudPathMapper.class);

    public static final Path PERSONAL_FOLDER
            = new Path(PathNormalizer.normalize(LocaleFactory.localizedString("Personal", "ownCloud")),
            EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume));

    public static final Path SPACES_FOLDER
            = new Path(PathNormalizer.normalize(LocaleFactory.localizedString("Spaces", "ownCloud")),
            EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume));

    public static final Path SHARES_FOLDER
            = new Path(PathNormalizer.normalize(LocaleFactory.localizedString("Shares", "ownCloud")),
            EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.volume));

    /**
     * Identifier of virtual space containing accepted shares
     */
    public static final String SHARE_JAIL_ID = "a0ca6a90-a365-4782-871e-d44447bbc668$a0ca6a90-a365-4782-871e-d44447bbc668";

    public static final String DRIVE_TYPE_PERSONAL = "personal";
    public static final String DRIVE_TYPE_PROJECT = "project";
    public static final String DRIVE_TYPE_VIRTUAL = "virtual";

    /**
     * WebDAV path of personal space
     */
    private String personal;
    /**
     * WebDAV path of share jail
     */
    private String shares;
    /**
     * Project space name mapped to WebDAV path
     */
    private volatile Map<String, String> projects = Collections.emptyMap();

    private final OcsCapabilities ocs;

    public OwncloudPathMapper(final OcsCapabilities ocs) {
        this(ocs, null, String.format("/dav/spaces/%s", SHARE_JAIL_ID));
    }

    /**
     * @param personal Default WebDAV path of personal space
     * @param shares   Default WebDAV path of share jail
     */
    public OwncloudPathMapper(final OcsCapabilities ocs, final String personal, final String shares) {
        this.ocs = ocs;
        this.personal = personal;
        this.shares = shares;
    }

    /**
     * Replace mapping with drives retrieved from server. Project spaces with clashing names are ignored.
     *
     * @param drives Drives of user
     * @return Project spaces available
     */
    public List<Drives.Drive> withDrives(final List<Drives.Drive> drives) {
        final List<Drives.Drive> available = drives.stream().filter(drive -> null != drive.driveType
                && null != drive.root && null == drive.root.deleted
                && StringUtils.isNotBlank(drive.root.webDavUrl)).collect(Collectors.toList());
        String personal = null;
        for(Drives.Drive drive : available) {
            switch(drive.driveType) {
                case DRIVE_TYPE_PERSONAL:
                    personal = toPath(drive.root.webDavUrl);
                    break;
                case DRIVE_TYPE_VIRTUAL:
                    if(StringUtils.contains(drive.id, SHARE_JAIL_ID)) {
                        shares = toPath(drive.root.webDavUrl);
                    }
                    break;
            }
        }
        final Map<String, List<Drives.Drive>> named = available.stream()
                .filter(drive -> StringUtils.equals(DRIVE_TYPE_PROJECT, drive.driveType))
                .filter(drive -> StringUtils.isNotBlank(drive.name))
                .collect(Collectors.groupingBy(drive -> drive.name));
        final Map<String, Drives.Drive> unique = new TreeMap<>();
        for(Map.Entry<String, List<Drives.Drive>> entry : named.entrySet()) {
            if(entry.getValue().size() > 1) {
                log.warn("Ignore project spaces {} with clashing name {}", entry.getValue(), entry.getKey());
                continue;
            }
            if(StringUtils.contains(entry.getKey(), Path.DELIMITER)) {
                log.warn("Ignore project space with invalid name {}", entry.getKey());
                continue;
            }
            unique.put(entry.getKey(), entry.getValue().get(0));
        }
        this.personal = personal;
        projects = unique.values().stream().collect(Collectors.toMap(drive -> drive.name, drive -> toPath(drive.root.webDavUrl)));
        log.debug("Mapped personal space {}, shares {} and project spaces {}", personal, shares, projects);
        return unique.values().stream().collect(Collectors.toList());
    }

    /**
     * @param file Path in browser
     * @return Path on server or unchanged if not within a known space such as for versions or when spaces are not supported
     */
    @Override
    public Path map(final Path file) {
        if(ocs.spaces) {
            final Path server = this.toServer(file);
            if(null == server) {
                log.debug("No space found for {}", file);
                return file;
            }
            return server;
        }
        return file;
    }

    /**
     * @param file Path in browser
     * @return Path on server or null if not within a known space
     */
    public Path toServer(final Path file) {
        final String absolute = file.getAbsolute();
        if(null != personal) {
            final String path = this.toServer(absolute, PERSONAL_FOLDER.getAbsolute(), personal);
            if(null != path) {
                return new Path(path, file.getType());
            }
        }
        if(null != shares) {
            final String path = this.toServer(absolute, SHARES_FOLDER.getAbsolute(), shares);
            if(null != path) {
                return new Path(path, file.getType());
            }
        }
        if(StringUtils.startsWith(absolute, SPACES_FOLDER.getAbsolute() + Path.DELIMITER)) {
            final String relative = StringUtils.removeStart(absolute, SPACES_FOLDER.getAbsolute() + Path.DELIMITER);
            final String name = StringUtils.substringBefore(relative, String.valueOf(Path.DELIMITER));
            final String root = projects.get(name);
            if(null != root) {
                return new Path(this.toServer(absolute, String.format("%s%s%s", SPACES_FOLDER.getAbsolute(), Path.DELIMITER, name), root), file.getType());
            }
        }
        return null;
    }

    private String toServer(final String absolute, final String folder, final String root) {
        if(StringUtils.equals(absolute, folder)) {
            return root;
        }
        if(StringUtils.startsWith(absolute, folder + Path.DELIMITER)) {
            return PathNormalizer.normalize(root + StringUtils.removeStart(absolute, folder));
        }
        return null;
    }

    /**
     * @param path    Path on server
     * @param aliases Additional WebDAV paths of personal space. Ignored without personal space
     * @return Path in browser or null if not within a known space
     */
    public Path toVirtual(final String path, final String... aliases) {
        final String normalized = PathNormalizer.normalize(path);
        // Most specific root first
        final Map<String, String> roots = new TreeMap<>(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));
        if(null != personal) {
            for(String alias : aliases) {
                if(StringUtils.isNotBlank(alias)) {
                    roots.put(PathNormalizer.normalize(alias), PERSONAL_FOLDER.getAbsolute());
                }
            }
            roots.put(personal, PERSONAL_FOLDER.getAbsolute());
        }
        if(null != shares) {
            roots.put(shares, SHARES_FOLDER.getAbsolute());
        }
        roots.putAll(projects.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue,
                entry -> String.format("%s%s%s", SPACES_FOLDER.getAbsolute(), Path.DELIMITER, entry.getKey()), (a, b) -> a)));
        for(Map.Entry<String, String> entry : roots.entrySet()) {
            final String virtual = this.toServer(normalized, entry.getKey(), entry.getValue());
            if(null != virtual) {
                return new Path(virtual, EnumSet.of(Path.Type.directory));
            }
        }
        return null;
    }

    public String getPersonal() {
        return personal;
    }

    public String getShares() {
        return shares;
    }

    public Map<String, String> getProjects() {
        return projects;
    }

    private static String toPath(final String url) {
        return PathNormalizer.normalize(URI.create(url).getPath());
    }
}
