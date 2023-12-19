package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.serializer.impl.dd.PlistSerializer;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

public class AbstractProtocolTest {

    @Test
    public void testMinimalExample() throws IOException {
        final AbstractProtocol profile = new AbstractProtocol() {
            @Override
            public String getIdentifier() {
                return "parent";
            }

            @Override
            public String getProvider() {
                return "provider";
            }

            @Override
            public String getDescription() {
                return "fancy";
            }

            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }

            @Override
            public String[] getSchemes() {
                return new String[]{Scheme.https.name(), Scheme.http.name()};
            }

        };
        final String serialized = profile.serialize(new PlistSerializer()).toXMLPropertyList();
        final String expected = IOUtils.toString(this.getClass().getResource("/AbstractProtocolTest/minimal.cyberduckprofile"));
        assertArrayEquals(expected.split("[\r]?\n"), serialized.split("[\r]?\n"));
    }

    @Test
    public void testFullExample() throws IOException {
        final AbstractProtocol profile = new AbstractProtocol() {
            @Override
            public String getOAuthClientId() {
                return "My OAuth Client ID";
            }

            @Override
            public String getOAuthClientSecret() {
                return "My OAuth Client Secret";
            }

            @Override
            public String getOAuthTokenUrl() {
                return "OAuth Token Url";
            }

            @Override
            public String getOAuthRedirectUrl() {
                return "OAuth Redirect Url";
            }

            @Override
            public String getOAuthAuthorizationUrl() {
                return "OAuth Auth Url";
            }

            @Override
            public boolean isOAuthPKCE() {
                return false;
            }

            @Override
            public List<String> getOAuthScopes() {
                return Arrays.asList("a", "b");
            }


            @Override
            public String getSTSEndpoint() {
                return "sts.iterate.ch";
            }

            @Override
            public String disk() {
                return "disk.tiff";
            }

            @Override
            public String icon() {
                return "icon.tiff";
            }

            @Override
            public String getIdentifier() {
                return "MyId";
            }

            @Override
            public String getProvider() {
                return "MyProvider";
            }

            @Override
            public boolean isBundled() {
                return true;
            }

            @Override
            public String getName() {
                return "My Name";
            }

            @Override
            public String getDescription() {
                return "My Description";
            }

            @Override
            public String getRegion() {
                return "My Region";
            }

            @Override
            public Set<Location.Name> getRegions() {
                return Collections.singleton(new Location.Name("Another Region"));
            }

            @Override
            public Scheme getScheme() {
                return Scheme.ftps;
            }

            @Override
            public String[] getSchemes() {
                return new String[]{"first", "second", "third"};
            }

            @Override
            public String getAuthorization() {
                return "My Auth";
            }

            @Override
            public String getContext() {
                return "My Context";
            }

            public String getDefaultHostname() {
                return "My Hostname";
            }

            @Override
            public int getDefaultPort() {
                return 777;
            }

            @Override
            public String getDefaultPath() {
                return "My Default Path";
            }

            @Override
            public String getDefaultNickname() {
                return "My Default Nichkname";
            }

            @Override
            public String getHostnamePlaceholder() {
                return "My Hostname Placeholder";
            }

            @Override
            public String getUsernamePlaceholder() {
                return "My Username Placeholder";
            }


            @Override
            public String getPasswordPlaceholder() {
                return "My Password Placeholder";
            }


            @Override
            public String getTokenPlaceholder() {
                return "My Token Placeholder";
            }


            @Override
            public boolean isPathConfigurable() {
                return false;
            }

            @Override
            public boolean isUsernameConfigurable() {
                return false;
            }

            @Override
            public boolean isPasswordConfigurable() {
                return false;
            }

            @Override
            public boolean isAnonymousConfigurable() {
                return true;
            }

            @Override
            public boolean isTokenConfigurable() {
                return true;
            }

            @Override
            public boolean isOAuthConfigurable() {
                return false;
            }

            @Override
            public boolean isCertificateConfigurable() {
                return true;
            }

            @Override
            public boolean isPrivateKeyConfigurable() {
                return true;
            }

            @Override
            public Map<String, String> getProperties() {
                return Collections.singletonMap("My Key", "My Value");
            }

            @Override
            public boolean isDeprecated() {
                return true;
            }

            @Override
            public String getHelp() {
                return "Help";
            }
        };
        final String serialized = profile.serialize(new PlistSerializer()).toXMLPropertyList();
        final String expected = IOUtils.toString(this.getClass().getResource("/AbstractProtocolTest/full.cyberduckprofile"));
        assertArrayEquals(expected.split("[\r]?\n"), serialized.split("[\r]?\n"));
    }

}