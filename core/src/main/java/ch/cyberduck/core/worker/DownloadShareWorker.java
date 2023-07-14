package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class DownloadShareWorker<Options> extends Worker<DescriptiveUrl> {
    private static final Logger log = LogManager.getLogger(DownloadShareWorker.class);

    private final Path file;
    private final Options options;
    private final PasswordCallback callback;
    private final Share.ShareeCallback prompt;

    public DownloadShareWorker(final Path file, final Options options, final PasswordCallback callback, final Share.ShareeCallback prompt) {
        this.file = file;
        this.options = options;
        this.callback = callback;
        this.prompt = prompt;
    }

    @Override
    public DescriptiveUrl run(final Session<?> session) throws BackgroundException {
        final Share<Options, Void> provider = session.getFeature(Share.class);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run with feature %s", provider));
        }
        final Set<Share.Sharee> sharees = provider.getSharees();
        if(!sharees.stream().filter(s -> !s.equals(Share.Sharee.world)).collect(Collectors.toSet()).isEmpty()) {
            return provider.toDownloadUrl(file, prompt.prompt(sharees), options, callback);
        }
        else {
            return provider.toDownloadUrl(file, Share.Sharee.world, options, callback);
        }
    }

    @Override
    public DescriptiveUrl initialize() {
        return DescriptiveUrl.EMPTY;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Prepare {0} ({1})", "Status"),
            this.toString(Collections.singletonList(file)), LocaleFactory.localizedString("URL", "Download"));
    }
}
