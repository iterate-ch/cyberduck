package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;

public class NextcloudAttributesFinderFeature extends DAVAttributesFinderFeature {

    public static final String CUSTOM_NAMESPACE_PREFIX = "oc";
    public static final String CUSTOM_NAMESPACE_URI = "http://owncloud.org/ns";

    public static final QName OC_FILEID_CUSTOM_NAMESPACE = new QName(CUSTOM_NAMESPACE_URI, "fileid", CUSTOM_NAMESPACE_PREFIX);
    /**
     * The value is a string containing letters for all available permissions.
     * S: Shared
     * M: Mounted
     * D: Deletable
     * NV: Updateable, Renameable, Moveable
     * W: Updateable (file)
     * CK: Creatable (folders only)
     * Z: Deniable
     * P: Trashbin Purgable
     */
    public static final QName OC_PERMISSIONS_CUSTOM_NAMESPACE = new QName(CUSTOM_NAMESPACE_URI, "permissions", CUSTOM_NAMESPACE_PREFIX);
    /**
     * Similar to getcontentlength but it also works for folders.
     */
    public static final QName OC_SIZE_CUSTOM_NAMESPACE = new QName(CUSTOM_NAMESPACE_URI, "size", CUSTOM_NAMESPACE_PREFIX);
    /**
     * <oc:checksum>
     * SHA1:1c68ea370b40c06fcaf7f26c8b1dba9d9caf5dea MD5:2205e48de5f93c784733ffcca841d2b5 ADLER32:058801ab
     * </oc:checksum>
     * Due to a bug in the very early development of ownCloud, this value is not an array, but a string separated by whitespaces.
     */
    public static final QName OC_CHECKSUMS_CUSTOM_NAMESPACE = new QName(CUSTOM_NAMESPACE_URI, "checksums", CUSTOM_NAMESPACE_PREFIX);

    private final DAVSession session;

    public NextcloudAttributesFinderFeature(DAVSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        final PathAttributes attr = super.find(file, listener);
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return attr.withVersionId(file.attributes().getVersionId()).withFileId(file.attributes().getFileId());
        }
        return attr;
    }

    @Override
    protected PathAttributes head(final Path file) {
        return PathAttributes.EMPTY;
    }

    @Override
    protected List<DavResource> list(final Path file) throws IOException {
        final String url;
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            url = String.format("%sversions/%s/%s",
                    new DAVPathEncoder().encode(new NextcloudHomeFeature(session.getHost()).find(NextcloudHomeFeature.Context.versions)),
                    file.attributes().getFileId(), file.attributes().getVersionId());
        }
        else {
            url = new DAVPathEncoder().encode(file);
        }
        return session.getClient().list(url, 0,
                Stream.of(OC_FILEID_CUSTOM_NAMESPACE, OC_CHECKSUMS_CUSTOM_NAMESPACE, OC_SIZE_CUSTOM_NAMESPACE,
                        DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE,
                        DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE).collect(Collectors.toSet()));
    }

    @Override
    public PathAttributes toAttributes(final DavResource resource) {
        final PathAttributes attributes = super.toAttributes(resource);
        final Map<QName, String> properties = resource.getCustomPropsNS();
        if(null != properties) {
            if(properties.containsKey(OC_FILEID_CUSTOM_NAMESPACE)) {
                final String value = properties.get(OC_FILEID_CUSTOM_NAMESPACE);
                attributes.setFileId(value);
            }
            if(resource.isDirectory()) {
                if(properties.containsKey(OC_SIZE_CUSTOM_NAMESPACE)) {
                    final String value = properties.get(OC_SIZE_CUSTOM_NAMESPACE);
                    attributes.setSize(Long.parseLong(value));
                }
            }
            if(properties.containsKey(OC_CHECKSUMS_CUSTOM_NAMESPACE)) {
                for(String v : StringUtils.split(properties.get(OC_CHECKSUMS_CUSTOM_NAMESPACE), StringUtils.SPACE)) {
                    final String hash = StringUtils.lowerCase(StringUtils.split(v, ":")[1]);
                    final Checksum checksum = Checksum.parse(StringUtils.lowerCase(StringUtils.split(v, ":")[1]));
                    if(Checksum.NONE != checksum) {
                        attributes.setChecksum(checksum);
                    }
                }
            }
        }
        return attributes;
    }
}
