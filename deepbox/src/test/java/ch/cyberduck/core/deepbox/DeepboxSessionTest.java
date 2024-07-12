package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeepboxSessionTest {

    private static Stream<Arguments> provideStringsForIsBlank() {
        return Stream.of(
                Arguments.of("api.int.deepbox.swiss", "int."),
                Arguments.of("api.deepbox.swiss", "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideStringsForIsBlank")
    public void getStage(final String hostname, final String expectedStage) {
        final DeepboxSession session = new DeepboxSession(new Host(new DeepboxProtocol(), hostname), null, null);
        assertEquals(expectedStage, session.getStage());
    }
}