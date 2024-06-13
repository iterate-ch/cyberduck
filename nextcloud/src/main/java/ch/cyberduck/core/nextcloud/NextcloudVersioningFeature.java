package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.dav.DAVExceptionMappingService;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVTimestampFeature;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.ui.comparator.TimestampComparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.sardine.DavResource;
import com.github.sardine.impl.SardineException;
import com.github.sardine.model.ObjectFactory;
import com.github.sardine.model.Prop;
import com.github.sardine.model.Propfind;
import com.github.sardine.util.SardineUtil;

public class NextcloudVersioningFeature implements Versioning {

    private final DAVSession session;

    public NextcloudVersioningFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) {
        return new VersioningConfiguration(true);
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        // To restore a version all that needs to be done is to move a version the special restore folder at /remote.php/dav/versions/USER/restore
        try {
            session.getClient().move(URIEncoder.encode(file.getAbsolute()),
                    URIEncoder.encode(String.format("%s/restore/target",
                            new NextcloudHomeFeature(session.getHost()).find(NextcloudHomeFeature.Context.versions).getAbsolute()))
            );
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Cannot revert file", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return EnumSet.of(Flags.revert);
        }
        return EnumSet.of(Flags.list);
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isDirectory()) {
            return AttributedList.emptyList();
        }
        try {
            final AttributedList<Path> versions = new AttributedList<>();
            // To obtain all the version of a file a normal PROPFIND has to be send
            // to remote.php/dav/versions/USER/versions/FILEID. This will list the versions for this file.
            Propfind body = new Propfind();
            Prop prop = new Prop();
            ObjectFactory objectFactory = new ObjectFactory();
            prop.setGetcontentlength(objectFactory.createGetcontentlength());
            prop.setGetlastmodified(objectFactory.createGetlastmodified());
            List<Element> any = prop.getAny();
            for(QName entry : Stream.of(
                            DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE,
                            DAVTimestampFeature.LAST_MODIFIED_SERVER_CUSTOM_NAMESPACE).
                    collect(Collectors.toSet())) {
                Element element = SardineUtil.createElement(entry);
                any.add(element);
            }
            body.setProp(prop);
            final Path parent = this.versions(file);
            for(DavResource resource : session.getClient().propfind(URIEncoder.encode(parent.getAbsolute()), 1, body)) {
                if(!this.filter(file, resource)) {
                    continue;
                }
                final PathAttributes attributes = new NextcloudAttributesFinderFeature(session).toAttributes(resource);
                attributes.setDuplicate(true);
                attributes.setFileId(file.attributes().getFileId());
                attributes.setVersionId(PathNormalizer.name(resource.getHref().getPath()));
                versions.add(new Path(parent, PathNormalizer.name(resource.getHref().getPath()), file.getType(), attributes));
                listener.chunk(file.getParent(), versions);
            }
            return versions.filter(new TimestampComparator(false));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, file);
        }
    }

    protected boolean filter(final Path file, final DavResource resource) {
        if(resource.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return false;
        }
        if(StringUtils.equals(file.attributes().getFileId(), PathNormalizer.name(resource.getHref().getPath()))) {
            // No version
            return false;
        }
        return true;
    }

    protected Path versions(final Path file) throws IOException, BackgroundException {
        return new Path(new NextcloudHomeFeature(session.getHost()).find(NextcloudHomeFeature.Context.versions),
                String.format("versions/%s" , file.attributes().getFileId()), EnumSet.of(Path.Type.directory));
    }
}
