package ch.cyberduck.core.profiles;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.TemporaryApplicationResourcesFinder;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteIndexProfilesFinder implements ProfilesFinder {
    private static final Logger log = LogManager.getLogger(RemoteIndexProfilesFinder.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final ProtocolFactory protocols;
    private final Session<?> session;
    private final Local temporary = LocalFactory.get(new TemporaryApplicationResourcesFinder().find(), "profiles");

    public RemoteIndexProfilesFinder(final Session<?> session) {
        this(ProtocolFactory.get(), session);
    }

    public RemoteIndexProfilesFinder(final ProtocolFactory protocols, final Session<?> session) {
        this.protocols = protocols;
        this.session = session;
    }

    /**
     *     {
     *       "filename": "AWS PrivateLink for Amazon S3 (VPC endpoint).cyberduckprofile",
     *       "protocol": "s3",
     *       "vendor": "s3-privatelink",
     *       "versions": [
     *         {
     *           "checksum": "255b117667ac1f0fd1ecfe25ae99440d",
     *           "modified": "2025-12-02T09:09:07Z",
     *           "version_id": "sMABDfcz3m0pwQeU85uI2.pW.JTGGT4T",
     *           "latest": true
     *         },
     *         {
     *           "checksum": "9a6fcc93e68a669952da5e47719af3c9",
     *           "modified": "2022-09-15T12:55:09Z",
     *           "version_id": ".iXSL9g6EWEIthW6gENMGgSileSI0XG8",
     *           "latest": false
     *         },
     *         {
     *           "checksum": "3fbf79c16d3187f135a5fbf32e0cbaf7",
     *           "modified": "2021-11-30T11:54:30Z",
     *           "version_id": "S71y2mc.dq8BXtF11g85Z9ZLvulhZDJk",
     *           "latest": false
     *         },
     *         {
     *           "checksum": "3fbf79c16d3187f135a5fbf32e0cbaf7",
     *           "modified": "2021-10-27T06:00:55Z",
     *           "version_id": "E.cXq21hr0xLkqtNMUZjBuZaj2HfA.n9",
     *           "latest": false
     *         },
     *         {
     *           "checksum": "7188dafb0c649fe123b70cbe5b7bfa40",
     *           "modified": "2021-10-27T06:00:49Z",
     *           "version_id": "nosIgW.rj3jrGn9jwdf_S.8fnKMO2ZW1",
     *           "latest": false
     *         }
     *       ]
     *     }
     */
    @Override
    public Set<ProfileDescription> find(final Visitor visitor) throws BackgroundException {
        log.info("Fetch profiles from {}", session.getHost());
        final Set<ProfileDescription> profiles = new HashSet<>();
        final Path directory = new DelegatingHomeFeature(new DefaultPathHomeFeature(session.getHost())).find();
        try(final InputStream in = session.getFeature(Read.class).read(new Path(directory, "index.json", EnumSet.of(Path.Type.file)),
                new TransferStatus().setLength(TransferStatus.UNKNOWN_LENGTH), ConnectionCallback.noop)) {
            final ProfileMetadataList list = mapper.readValue(in, ProfileMetadataList.class);
            for(ProfileMetadata metadata : list.profiles) {
                for(ProfileMetadataVersion version : metadata.versions) {
                    profiles.add(visitor.visit(new RemoteIndexProfileDescription(temporary, protocols, version, metadata, directory)));
                }
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return profiles;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProfileMetadataList {
        @JsonProperty("profiles")
        private ProfileMetadata[] profiles;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProfileMetadata {
        @JsonProperty("filename")
        private String filename;
        @JsonProperty("protocol")
        private String protocol;
        @JsonProperty("vendor")
        private String vendor;
        @JsonProperty("description")
        private String description;
        @JsonProperty("help")
        private String help;
        @JsonProperty("thumbnail")
        private String thumbnail;
        @JsonProperty("versions")
        private ProfileMetadataVersion[] versions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ProfileMetadataVersion {
        @JsonProperty("checksum")
        private String checksum;
        @JsonProperty("modified")
        private String modified;
        @JsonProperty("version_id")
        private String version_id;
        @JsonProperty("latest")
        private Boolean latest;
    }

    private final class RemoteIndexProfileDescription extends ProfileDescription {
        private final ProtocolFactory factory;
        private final ProfileMetadataVersion version;
        private final ProfileMetadata metadata;

        public RemoteIndexProfileDescription(final Local temporary, final ProtocolFactory factory, final ProfileMetadataVersion version, final ProfileMetadata metadata, final Path directory) {
            super(factory, protocol -> true, new LazyInitializer<Checksum>() {
                @Override
                protected Checksum initialize() {
                    return Checksum.parse(version.checksum);
                }
            }, new LazyInitializer<Local>() {
                @Override
                protected Local initialize() throws ConcurrentException {
                    try {
                        temporary.mkdir();
                        final Local local = LocalFactory.get(temporary, metadata.filename);
                        final Path file = new Path(directory, metadata.filename, EnumSet.of(Path.Type.file));
                        final Read read = session.getFeature(Read.class);
                        RemoteIndexProfilesFinder.log.info("Download profile {}", file);
// Read latest version
                        try(InputStream in = read.read(file.withAttributes(new DefaultPathAttributes(file.attributes())
                                .setVersionId(version.version_id)), new TransferStatus().setLength(TransferStatus.UNKNOWN_LENGTH), ConnectionCallback.noop); OutputStream out = local.getOutputStream(false)) {
                            IOUtils.copy(in, out);
                        }
                        return local;
                    }
                    catch(BackgroundException | IOException e) {
                        throw new ConcurrentException(e);
                    }
                }
            });
            this.factory = factory;
            this.version = version;
            this.metadata = metadata;
        }

        @Override
        public boolean isLatest() {
            return Boolean.TRUE.equals(version.latest);
        }

        @Override
        public boolean isEnabled() {
            return factory.isEnabled(metadata.protocol, metadata.vendor);
        }

        @Override
        public boolean isBundled() {
            return false;
        }

        @Override
        public String getIdentifier() {
            return metadata.protocol;
        }

        @Override
        public String getProvider() {
            return metadata.vendor;
        }

        @Override
        public String getName() {
            return factory.forName(metadata.protocol).getName();
        }

        @Override
        public String getDescription() {
            if(null == metadata.description) {
                return StringUtils.EMPTY;
            }
            return metadata.description;
        }

        @Override
        public String getHelp() {
            return metadata.help;
        }

        @Override
        public String getThumbnail() {
            if(null == metadata.thumbnail) {
                return factory.forName(metadata.protocol).disk();
            }
            return metadata.thumbnail;
        }
    }
}
