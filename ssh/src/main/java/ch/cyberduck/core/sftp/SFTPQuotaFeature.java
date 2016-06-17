package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Quota;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SFTPQuotaFeature implements Quota {
    private static final Logger log = Logger.getLogger(SFTPQuotaFeature.class);

    public final SFTPCommandFeature command;

    public SFTPQuotaFeature(final SFTPSession session) {
        command = new SFTPCommandFeature(session);
    }

    @Override
    public Space get() throws BackgroundException {
        final ThreadLocal<Space> quota = new ThreadLocal<Space>() {
            @Override
            protected Space initialValue() {
                return new Space(0L, Long.MAX_VALUE);
            }
        };
        command.send("df -k . | awk '{print $3, $4}'", new DisabledProgressListener(),
                new TranscriptListener() {
                    @Override
                    public void log(final boolean request, final String output) {
                        if(!request) {
                            final String[] numbers = StringUtils.split(output, ' ');
                            if(numbers.length == 2) {
                                try {
                                    quota.set(new Space(Long.valueOf(numbers[0]) * 1000L, Long.valueOf(numbers[1]) * 1000L));
                                }
                                catch(NumberFormatException e) {
                                    log.warn(String.format("Ignore line %s", output));
                                }
                            }
                            else {
                                log.warn(String.format("Ignore line %s", output));
                            }
                        }
                    }
                });
        return quota.get();
    }
}
