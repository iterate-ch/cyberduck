package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;

public final class StringAppender {

    private final StringBuilder buffer;

    private final char suffix;

    public StringAppender() {
        this.buffer = new StringBuilder();
        this.suffix = '.';
    }

    public StringAppender(final char suffix) {
        this.buffer = new StringBuilder();
        this.suffix = suffix;
    }

    public StringAppender(final StringBuilder buffer) {
        this.buffer = buffer;
        this.suffix = '.';
    }

    public StringAppender(final StringBuilder buffer, final char suffix) {
        this.buffer = buffer;
        this.suffix = suffix;
    }

    public StringAppender append(final String message) {
        if(StringUtils.isBlank(message)) {
            return this;
        }
        if(buffer.length() > 0) {
            buffer.append(" ");
        }
        buffer.append(StringUtils.trim(message));
        if(buffer.charAt(buffer.length() - 1) == '.') {
            return this;
        }
        if(buffer.charAt(buffer.length() - 1) == ':') {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append(suffix);
//        if(StringUtils.isAlpha(String.valueOf(buffer.charAt(buffer.length() - 1)))) {
//            buffer.append(".");
//        }
        return this;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}