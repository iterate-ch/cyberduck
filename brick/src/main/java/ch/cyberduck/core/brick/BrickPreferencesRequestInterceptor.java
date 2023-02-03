package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class BrickPreferencesRequestInterceptor implements HttpRequestInterceptor {

    private final Preferences preferences = PreferencesFactory.get();

    @Override
    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
        final String value = this.toValue(preferences);
        if(StringUtils.isNotBlank(value)) {
            request.addHeader(new BasicHeader("X-User-Agent", value));
        }
    }

    protected String toValue(final Preferences preferences) {
        final Map<String, String> configuration = new LinkedHashMap<>();
        if(preferences.getProperty("fs.sync.mode") != null) {
            configuration.put("sync", preferences.getProperty("fs.sync.mode"));
        }
        if(preferences.getBoolean("fs.sync.indexer.enable")) {
            configuration.put("index-files", StringUtils.EMPTY);
        }
        if(preferences.getBoolean("fs.lock.enable")) {
            configuration.put("lock-files", StringUtils.EMPTY);
        }
        if(preferences.getBoolean("fs.buffer.enable")) {
            configuration.put("enable-cache", StringUtils.EMPTY);
        }
        return this.toString(configuration);
    }

    protected String toString(final Map<String, String> configuration) {
        return String.join(", ", configuration.entrySet().stream().map(entry -> String.format("%s%s",
            entry.getKey(), StringUtils.isNotBlank(entry.getValue()) ? String.format("=%s", entry.getValue()) : StringUtils.EMPTY)).collect(Collectors.toList()));
    }
}
