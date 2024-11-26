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

import ch.cyberduck.core.DefaultPathContainerService;
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
import java.util.EnumSet;
import java.util.List;

public class DistributionUrlProvider implements UrlProvider {

    private final Distribution distribution;

    private final PathContainerService containerService
            = new DefaultPathContainerService();

    public DistributionUrlProvider(final Distribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file, final EnumSet<DescriptiveUrl.Type> types) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(types.contains(DescriptiveUrl.Type.origin)) {
            list.add(new DescriptiveUrl(this.toUrl(file, distribution.getOrigin()), DescriptiveUrl.Type.origin,
                    MessageFormat.format(LocaleFactory.localizedString("{0} {1} URL"),
                            distribution.getName(),
                            LocaleFactory.localizedString("Origin", "Info"))));
        }
        if(types.contains(DescriptiveUrl.Type.cdn)) {
            if(distribution.getUrl() != null) {
                list.add(new DescriptiveUrl(this.toUrl(file, distribution.getUrl()), DescriptiveUrl.Type.cdn,
                        MessageFormat.format(LocaleFactory.localizedString("{0} {1} URL"),
                                distribution.getName(),
                                LocaleFactory.localizedString(distribution.getMethod().toString(), "S3"))));
            }
            if(distribution.getSslUrl() != null) {
                list.add(new DescriptiveUrl(this.toUrl(file, distribution.getSslUrl()), DescriptiveUrl.Type.cdn,
                        String.format("%s (SSL)", MessageFormat.format(LocaleFactory.localizedString("{0} {1} URL"),
                                distribution.getName(),
                                LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
            }
            if(distribution.getStreamingUrl() != null) {
                list.add(new DescriptiveUrl(this.toUrl(file, distribution.getStreamingUrl()), DescriptiveUrl.Type.cdn,
                        String.format("%s (Streaming)", MessageFormat.format(LocaleFactory.localizedString("{0} {1} URL"),
                                distribution.getName(),
                                LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
            }
            if(distribution.getiOSstreamingUrl() != null) {
                list.add(new DescriptiveUrl(this.toUrl(file, distribution.getiOSstreamingUrl()), DescriptiveUrl.Type.cdn,
                        String.format("%s (iOS Streaming)", MessageFormat.format(LocaleFactory.localizedString("{0} {1} URL"),
                                distribution.getName(),
                                LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
            }
        }
        if(types.contains(DescriptiveUrl.Type.cname)) {
            list.addAll(this.toCnameUrl(file));
        }
        return list;
    }

    /**
     * @param file   File in origin container
     * @param origin Distribution URL
     * @return URL to file in distribution
     */
    private String toUrl(final Path file, final URI origin) {
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
        return b.toString();
    }

    /**
     * @param file File in origin container
     * @return CNAME to distribution
     */
    private List<DescriptiveUrl> toCnameUrl(final Path file) {
        final List<DescriptiveUrl> urls = new ArrayList<>();
        for(String cname : distribution.getCNAMEs()) {
            final StringBuilder b = new StringBuilder();
            b.append(String.format("%s://%s", distribution.getMethod().getScheme(), cname)).append(distribution.getMethod().getContext());
            if(StringUtils.isNotEmpty(containerService.getKey(file))) {
                b.append(Path.DELIMITER).append(URIEncoder.encode(containerService.getKey(file)));
            }
            urls.add(new DescriptiveUrl(b.toString(), DescriptiveUrl.Type.cname,
                    MessageFormat.format(LocaleFactory.localizedString("{0} {1} {2} URL"),
                            distribution.getName(),
                            LocaleFactory.localizedString(distribution.getMethod().toString(), "S3"),
                            LocaleFactory.localizedString("CNAME", "S3"))));
        }
        return urls;
    }
}
