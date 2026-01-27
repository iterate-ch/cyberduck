package ch.cyberduck.core.ctera.model;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DirectIO {

    public EncryptInfo encrypt_info;
    public List<Chunk> chunks;

    public static final class Chunk {
        public String url;
        public long len;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Chunk{");
            sb.append("url='").append(url).append('\'');
            sb.append(", len=").append(len);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class EncryptInfo {

        public String wrapped_key;
        public boolean data_encrypted;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("EncryptInfo{");
            sb.append("wrapped_key='").append(wrapped_key).append('\'');
            sb.append(", data_encrypted=").append(data_encrypted);
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DirectIO{");
        sb.append("encrypt_info=").append(encrypt_info);
        sb.append(", chunks=").append(chunks);
        sb.append('}');
        return sb.toString();
    }
}

