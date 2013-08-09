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
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;

import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class DistributionUrlProvider implements UrlProvider {

    private Distribution distribution;

    public DistributionUrlProvider(final Distribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.attributes().isFile()) {
            if(StringUtils.isNotBlank(distribution.getUrl())) {
                list.add(new DescriptiveUrl(this.getURL(file, distribution.getUrl()), DescriptiveUrl.Type.cdn,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3"))));
            }
            if(StringUtils.isNotBlank(distribution.getSslUrl())) {
                list.add(new DescriptiveUrl(this.getURL(file, distribution.getSslUrl()), DescriptiveUrl.Type.cdn,
                        String.format("%s (SSL)", MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
            }
            if(StringUtils.isNotBlank(distribution.getStreamingUrl())) {
                list.add(new DescriptiveUrl(this.getURL(file, distribution.getStreamingUrl()), DescriptiveUrl.Type.cdn,
                        String.format("%s (Streaming)", MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3")))));
            }
            list.addAll(this.getCnameURL(file));
        }
        return list;
    }

    /**
     * @param file File in origin container
     * @param base Distribution URL
     * @return URL to file in distribution
     */
    private URI getURL(final Path file, final String base) {
        final StringBuilder b = new StringBuilder(base);
        if(StringUtils.isNotEmpty(new PathContainerService().getKey(file))) {
            b.append(Path.DELIMITER).append(URIEncoder.encode(new PathContainerService().getKey(file)));
        }
        return URI.create(b.toString()).normalize();
    }

    /**
     * @param file File in origin container
     * @return CNAME to distribution
     */
    private List<DescriptiveUrl> getCnameURL(final Path file) {
        final List<DescriptiveUrl> urls = new ArrayList<DescriptiveUrl>();
        for(String cname : distribution.getCNAMEs()) {
            StringBuilder b = new StringBuilder();
            b.append(String.format("%s://%s", distribution.getMethod().getScheme(), cname)).append(distribution.getMethod().getContext());
            if(StringUtils.isNotEmpty(new PathContainerService().getKey(file))) {
                b.append(Path.DELIMITER).append(URIEncoder.encode(new PathContainerService().getKey(file)));
            }
            urls.add(new DescriptiveUrl(URI.create(b.toString()).normalize(), DescriptiveUrl.Type.cname,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString(distribution.getMethod().toString(), "S3"))));
        }
        return urls;
    }
}
