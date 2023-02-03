package ch.cyberduck.core.oauth;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;

public interface OAuth2AuthorizationCodeProvider {

    /**
     * Prompt for authentication code
     *
     * @param bookmark                    Host
     * @param prompt                      User input
     * @param authorizationCodeRequestUrl URL to query for code
     * @param redirectUri                 Redirect URI
     * @param state                       Custom state
     * @return Authentication code
     */
    String prompt(Host bookmark, final LoginCallback prompt, String authorizationCodeRequestUrl, String redirectUri, final String state) throws BackgroundException;
}
