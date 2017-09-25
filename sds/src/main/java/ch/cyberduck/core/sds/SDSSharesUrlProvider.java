package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.SharesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.net.URI;
import java.text.MessageFormat;

public class SDSSharesUrlProvider implements PromptUrlProvider<CreateDownloadShareRequest> {
    private static final Logger log = Logger.getLogger(SDSSharesUrlProvider.class);

    private final PathContainerService containerService
        = new SDSPathContainerService();

    private final SDSSession session;

    public SDSSharesUrlProvider(final SDSSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrl toUrl(final Path file, final CreateDownloadShareRequest options) throws BackgroundException {
        try {
            final Long fileid = Long.parseLong(new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener()));
            if(containerService.getContainer(file).getType().contains(Path.Type.vault)) {
                final FileKey key = new NodesApi(session.getClient()).getUserFileKey(StringUtils.EMPTY, fileid);
                options.fileKey(key).keyPair(session.keyPair());
            }
            final DownloadShare share = new SharesApi(session.getClient()).createDownloadShare(StringUtils.EMPTY,
                options.nodeId(fileid), null);
            final String help;
            if(null == share.getExpireAt()) {
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"));
            }
            else {
                final Long expiry = share.getExpireAt().getTime();
                help = MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3")) + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                    UserDateFormatterFactory.get().getShortFormat(expiry * 1000)
                );
            }
            return new DescriptiveUrl(
                URI.create(String.format("%s://%s/#/public/shares-downloads/%s",
                    session.getHost().getProtocol().getScheme(),
                    session.getHost().getHostname(),
                    share.getAccessKey())
                ),
                DescriptiveUrl.Type.signed, help);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map(e);
        }
    }
}
