package ch.cyberduck.core.cdn;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DistributionUrlProvider implements UrlProvider {

    private final Distribution distribution;

    private final PathContainerService containerService
            = new PathContainerService();

    public DistributionUrlProvider(final Distribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        list.add(new DescriptiveUrl(this.toUrl(file, distribution.getOrigin()), DescriptiveUrl.Type.origin,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Origin", "Info"))));
        if(distribution.getUrl() != null) {
            list.add(new DescriptiveUrl(this.toUrl(file, distribution.getUrl()), DescriptiveUrl.Type.cdn,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3"))));
        }
        if(distribution.getSslUrl() != null) {
            list.add(new DescriptiveUrl(this.toUrl(file, distribution.getSslUrl()), DescriptiveUrl.Type.cdn,
                    String.format("%s (SSL)", MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
        }
        if(distribution.getStreamingUrl() != null) {
            list.add(new DescriptiveUrl(this.toUrl(file, distribution.getStreamingUrl()), DescriptiveUrl.Type.cdn,
                    String.format("%s (Streaming)", MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
        }
        if(distribution.getiOSstreamingUrl() != null) {
            list.add(new DescriptiveUrl(this.toUrl(file, distribution.getiOSstreamingUrl()), DescriptiveUrl.Type.cdn,
                    String.format("%s (iOS Streaming)", MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
        }
        list.addAll(this.toCnameUrl(file));
        return list;
    }

    /**
     * @param file   File in origin container
     * @param origin Distribution URL
     * @return URL to file in distribution
     */
    private URI toUrl(final Path file, final URI origin) {
        final StringBuilder b = new StringBuilder(String.format("%s://%s", origin.getScheme(), origin.getHost()));
        if(distribution.getMethod().equals(Distribution.CUSTOM)) {
            b.append(Path.DELIMITER).append(URIEncoder.encode(PathRelativizer.relativize(origin.getRawPath(), file.getAbsolute())));
        }
        else {
            if(StringUtils.isNotEmpty(origin.getRawPath())) {
                b.append(origin.getRawPath());
            }
            if(StringUtils.isNotEmpty(containerService.getKey(file))) {
                b.append(Path.DELIMITER).append(URIEncoder.encode(containerService.getKey(file)));
            }
        }
        return URI.create(b.toString()).normalize();
    }

    /**
     * @param file File in origin container
     * @return CNAME to distribution
     */
    private List<DescriptiveUrl> toCnameUrl(final Path file) {
        final List<DescriptiveUrl> urls = new ArrayList<DescriptiveUrl>();
        for(String cname : distribution.getCNAMEs()) {
            final StringBuilder b = new StringBuilder();
            b.append(String.format("%s://%s", distribution.getMethod().getScheme(), cname)).append(distribution.getMethod().getContext());
            if(StringUtils.isNotEmpty(containerService.getKey(file))) {
                b.append(Path.DELIMITER).append(URIEncoder.encode(containerService.getKey(file)));
            }
            urls.add(new DescriptiveUrl(URI.create(b.toString()).normalize(), DescriptiveUrl.Type.cname,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3"))));
        }
        return urls;
    }
}
