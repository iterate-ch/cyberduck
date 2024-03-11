package ch.cyberduck.core.oauth;

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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CustomSchemeHandlerOAuth2AuthorizationCodeProviderTest {

    @Test
    public void toScheme() {
        assertEquals("x-cyberduck-action", CustomSchemeHandlerOAuth2AuthorizationCodeProvider.toScheme("x-cyberduck-action:oauth"));
        assertEquals("https", CustomSchemeHandlerOAuth2AuthorizationCodeProvider.toScheme("https://redirect/"));
    }
}