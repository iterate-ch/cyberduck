package ch.cyberduck.core;

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

import org.apache.commons.text.CharacterPredicate;
import org.apache.commons.text.RandomStringGenerator;

public class AlphanumericRandomStringService implements RandomStringService {

    private final int length;
    private final RandomStringGenerator random;

    public AlphanumericRandomStringService() {
        this(8);
    }

    public AlphanumericRandomStringService(final int length) {
        this(length, new RandomStringGenerator.Builder().withinRange('0', 'z').filteredBy(new CharacterPredicate() {
            @Override
            public boolean test(final int codePoint) {
                return Character.isAlphabetic(codePoint) || Character.isDigit(codePoint);
            }
        }).build());
    }

    public AlphanumericRandomStringService(final int length, final RandomStringGenerator random) {
        this.length = length;
        this.random = random;
    }

    @Override
    public String random() {
        return random.generate(length);
    }
}
