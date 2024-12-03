package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Profile implements Protocol {
    private static final Logger log = LogManager.getLogger(Profile.class);

    private static final StringSubstitutor substitutor = new StringSubstitutor(
            StringLookupFactory.INSTANCE.functionStringLookup(s -> PreferencesFactory.get().getProperty(s)));

    private final Deserializer<?> dict;
    /**
     * The actual protocol implementation registered
     */
    private final Protocol parent;

    public static final String OAUTH_CLIENT_ID_KEY = "OAuth Client ID";
    public static final String OAUTH_CLIENT_SECRET_KEY = "OAuth Client Secret";
    public static final String OAUTH_TOKEN_URL_KEY = "OAuth Token Url";
    public static final String OAUTH_REDIRECT_URL_KEY = "OAuth Redirect Url";
    public static final String OAUTH_AUTHORIZATION_URL_KEY = "OAuth Authorization Url";
    public static final String OAUTH_PKCE_KEY = "OAuth PKCE";
    public static final String SCOPES_KEY = "Scopes";
    public static final String STS_ENDPOINT_KEY = "STS Endpoint";

    public static final String DISK_KEY = "Disk";
    public static final String ICON_KEY = "Icon";
    public static final String PROTOCOL_KEY = "Protocol";
    public static final String VENDOR_KEY = "Vendor";
    public static final String BUNDLED_KEY = "Bundled";
    public static final String NAME_KEY = "Name";
    public static final String DESCRIPTION_KEY = "Description";
    public static final String REGION_KEY = "Region";
    public static final String REGIONS_KEY = "Regions";
    public static final String SCHEME_KEY = "Scheme";
    public static final String SCHEMES_KEY = "Schemes";
    public static final String AUTHORIZATION_KEY = "Authorization";
    public static final String CONTEXT_KEY = "Context";

    public static final String DEFAULT_HOSTNAME_KEY = "Default Hostname";
    public static final String DEFAULT_PORT_KEY = "Default Port";
    public static final String DEFAULT_PATH_KEY = "Default Path";
    public static final String DEFAULT_NICKNAME_KEY = "Default Nickname";

    public static final String HOSTNAME_PLACEHOLDER_KEY = "Hostname Placeholder";
    public static final String PATH_PLACEHOLDER_KEY = "Path Placeholder";
    public static final String USERNAME_PLACEHOLDER_KEY = "Username Placeholder";
    public static final String PASSWORD_PLACEHOLDER_KEY = "Password Placeholder";
    public static final String TOKEN_PLACEHOLDER_KEY = "Token Placeholder";

    public static final String HOSTNAME_CONFIGURABLE_KEY = "Hostname Configurable";
    public static final String PORT_CONFIGURABLE_KEY = "Port Configurable";
    public static final String PATH_CONFIGURABLE_KEY = "Path Configurable";
    public static final String USERNAME_CONFIGURABLE_KEY = "Username Configurable";
    public static final String PASSWORD_CONFIGURABLE_KEY = "Password Configurable";
    public static final String ANONYMOUS_CONFIGURABLE_KEY = "Anonymous Configurable";
    public static final String TOKEN_CONFIGURABLE_KEY = "Token Configurable";
    public static final String OAUTH_CONFIGURABLE_KEY = "OAuth Configurable";
    public static final String CERTIFICATE_CONFIGURABLE_KEY = "Certificate Configurable";
    public static final String PRIVATE_KEY_CONFIGURABLE_KEY = "Private Key Configurable";

    public static final String PROPERTIES_KEY = "Properties";
    public static final String DEPRECATED_KEY = "Deprecated";
    public static final String HELP_KEY = "Help";

    private Local disk;
    private Local icon;

    public Profile(final Protocol parent, final Deserializer<?> dict) {
        this.parent = parent;
        this.dict = dict;
        this.disk = this.write(this.value(DISK_KEY));
        this.icon = this.write(this.value(ICON_KEY));
    }

    @Override
    public <T> T serialize(final Serializer<T> serializer) {
        for(String key : dict.keys()) {
            switch(key) {
                case HOSTNAME_CONFIGURABLE_KEY:
                case PORT_CONFIGURABLE_KEY:
                case PATH_CONFIGURABLE_KEY:
                case USERNAME_CONFIGURABLE_KEY:
                case PASSWORD_CONFIGURABLE_KEY:
                case ANONYMOUS_CONFIGURABLE_KEY:
                case TOKEN_CONFIGURABLE_KEY:
                case OAUTH_CONFIGURABLE_KEY:
                case CERTIFICATE_CONFIGURABLE_KEY:
                case PRIVATE_KEY_CONFIGURABLE_KEY:
                case BUNDLED_KEY:
                case DEPRECATED_KEY:
                case OAUTH_PKCE_KEY:
                    serializer.setBooleanForKey(dict.booleanForKey(key), key);
                    break;
                case SCOPES_KEY:
                case REGIONS_KEY:
                case PROPERTIES_KEY:
                case SCHEMES_KEY:
                    serializer.setStringListForKey(dict.listForKey(key), key);
                    break;
                default:
                    serializer.setStringForKey(dict.stringForKey(key), key);
                    break;
            }
        }
        return serializer.getSerialized();
    }

    public Protocol getProtocol() {
        return parent;
    }

    @Override
    public String getPrefix() {
        return parent.getPrefix();
    }

    /**
     * @return False if missing required fields in profile.
     */
    @Override
    public boolean isEnabled() {
        if(this.isBundled()) {
            return true;
        }
        final String protocol = this.value(PROTOCOL_KEY);
        final String vendor = this.value(VENDOR_KEY);
        if(StringUtils.isNotBlank(protocol) && StringUtils.isNotBlank(vendor)) {
            final String property = PreferencesFactory.get().getProperty(StringUtils.lowerCase(String.format("profiles.%s.%s.enabled", protocol, vendor)));
            if(null == property) {
                // Not previously configured. Assume enabled
                return true;
            }
            return Boolean.parseBoolean(property);
        }
        return false;
    }

    @Override
    public boolean isDeprecated() {
        return this.bool(DEPRECATED_KEY);
    }

    @Override
    public boolean isSecure() {
        return this.getScheme().isSecure();
    }

    @Override
    public Statefulness getStatefulness() {
        return parent.getStatefulness();
    }

    @Override
    public Comparator<String> getListComparator() {
        return parent.getListComparator();
    }

    @Override
    public VersioningMode getVersioningMode() {
        return parent.getVersioningMode();
    }

    @Override
    public String getIdentifier() {
        return parent.getIdentifier();
    }

    @Override
    public Type getType() {
        return parent.getType();
    }

    @Override
    public String getHostnamePlaceholder() {
        final String v = this.value(HOSTNAME_PLACEHOLDER_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getHostnamePlaceholder();
        }
        return v;
    }

    @Override
    public String getPathPlaceholder() {
        final String v = this.value(PATH_PLACEHOLDER_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getPathPlaceholder();
        }
        return v;
    }

    @Override
    public String getUsernamePlaceholder() {
        final String v = this.value(USERNAME_PLACEHOLDER_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getUsernamePlaceholder();
        }
        return LocaleFactory.localizedString(v, "Credentials");
    }

    @Override
    public String getPasswordPlaceholder() {
        final String v = this.value(PASSWORD_PLACEHOLDER_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getPasswordPlaceholder();
        }
        return LocaleFactory.localizedString(v, "Credentials");
    }

    @Override
    public String getTokenPlaceholder() {
        final String v = this.value(TOKEN_PLACEHOLDER_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getTokenPlaceholder();
        }
        return LocaleFactory.localizedString(v, "Credentials");
    }

    @Override
    public String getDefaultHostname() {
        final String v = this.value(DEFAULT_HOSTNAME_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultHostname();
        }
        return v;
    }

    @Override
    public String getProvider() {
        final String v = this.value(VENDOR_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getProvider();
        }
        return v;
    }

    public boolean isBundled() {
        final String v = this.value(BUNDLED_KEY);
        if(StringUtils.isBlank(v)) {
            return false;
        }
        return Boolean.parseBoolean(v);
    }

    @Override
    public String getName() {
        final String v = this.value(NAME_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getName();
        }
        return v;
    }

    @Override
    public String getDescription() {
        final String v = this.value(DESCRIPTION_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getDescription();
        }
        return v;
    }

    @Override
    public int getDefaultPort() {
        final String v = this.value(DEFAULT_PORT_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultPort();
        }
        try {
            return Integer.parseInt(v);
        }
        catch(NumberFormatException e) {
            log.warn("Port {} is not a number", e.getMessage());
        }
        return parent.getDefaultPort();
    }

    @Override
    public String getDefaultPath() {
        final String v = this.value(DEFAULT_PATH_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultPath();
        }
        return v;
    }

    @Override
    public String getDefaultNickname() {
        final String v = this.value(DEFAULT_NICKNAME_KEY);
        if(StringUtils.isBlank(v)) {
            return null;
        }
        return v;
    }

    @Override
    public String getRegion() {
        final String v = this.value(REGION_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getRegion();
        }
        return v;
    }

    @Override
    public String disk() {
        if(null == disk) {
            return parent.disk();
        }
        if(!disk.exists()) {
            this.disk = this.write(this.value(DISK_KEY));
        }
        // Temporary file
        return disk.getAbsolute();
    }

    @Override
    public String icon() {
        if(null == icon) {
            if(null == disk) {
                return parent.icon();
            }
            return this.disk();
        }
        if(!icon.exists()) {
            this.icon = this.write(this.value(ICON_KEY));
        }
        // Temporary file
        return icon.getAbsolute();
    }

    @Override
    public String favicon() {
        return parent.favicon();
    }

    /**
     * Write temporary file with data
     *
     * @param icon Base64 encoded image information
     * @return Path to file
     */
    private Local write(final String icon) {
        if(StringUtils.isBlank(icon)) {
            return null;
        }
        final byte[] favicon = Base64.decodeBase64(icon);
        final Local file = TemporaryFileServiceFactory.get().create(new AlphanumericRandomStringService().random());
        try {
            try (final OutputStream out = file.getOutputStream(false)) {
                IOUtils.write(favicon, out);
            }
            return file;
        }
        catch(IOException | AccessDeniedException e) {
            log.error("Error writing temporary file", e);
        }
        return null;
    }

    @Override
    public boolean validate(final Credentials credentials, final LoginOptions options) {
        return parent.validate(credentials, options);
    }

    @Override
    public Case getCaseSensitivity() {
        return parent.getCaseSensitivity();
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return parent.getDirectoryTimestamp();
    }

    @Override
    public Scheme getScheme() {
        final String v = this.value(SCHEME_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getScheme();
        }
        try {
            return Scheme.valueOf(v);
        }
        catch(IllegalArgumentException e) {
            log.warn("Unknown scheme {}", v);
            return null;
        }
    }

    @Override
    public String[] getSchemes() {
        final List<String> values = this.list(SCHEMES_KEY);
        if(values.isEmpty()) {
            return parent.getSchemes();
        }
        return values.toArray(new String[values.size()]);
    }

    @Override
    public String getContext() {
        final String v = this.value(CONTEXT_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getContext();
        }
        return v;
    }

    @Override
    public String getAuthorization() {
        final String v = this.value(AUTHORIZATION_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getAuthorization();
        }
        return v;
    }

    @Override
    public Set<Location.Name> getRegions() {
        final List<String> regions = this.list(REGIONS_KEY);
        if(regions.isEmpty()) {
            return parent.getRegions();
        }
        return parent.getRegions(regions);
    }

    @Override
    public Set<Location.Name> getRegions(final List<String> regions) {
        return parent.getRegions(regions);
    }

    @Override
    public boolean isEncodingConfigurable() {
        return parent.isEncodingConfigurable();
    }

    @Override
    public boolean isAnonymousConfigurable() {
        if(StringUtils.isBlank(this.value(ANONYMOUS_CONFIGURABLE_KEY))) {
            return parent.isAnonymousConfigurable();
        }
        return this.bool(ANONYMOUS_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isUsernameConfigurable() {
        if(StringUtils.isBlank(this.value(USERNAME_CONFIGURABLE_KEY))) {
            return parent.isUsernameConfigurable();
        }
        return this.bool(USERNAME_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isPasswordConfigurable() {
        if(StringUtils.isBlank(this.value(PASSWORD_CONFIGURABLE_KEY))) {
            return parent.isPasswordConfigurable();
        }
        return this.bool(PASSWORD_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isTokenConfigurable() {
        if(StringUtils.isBlank(this.value(TOKEN_CONFIGURABLE_KEY))) {
            return parent.isTokenConfigurable();
        }
        return this.bool(TOKEN_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isOAuthConfigurable() {
        if(StringUtils.isNotBlank(this.value(OAUTH_CONFIGURABLE_KEY))) {
            return this.bool(OAUTH_CONFIGURABLE_KEY);
        }
        return StringUtils.isNotBlank(this.getOAuthClientId());
    }

    @Override
    public boolean isCertificateConfigurable() {
        if(StringUtils.isBlank(this.value(CERTIFICATE_CONFIGURABLE_KEY))) {
            return parent.isCertificateConfigurable();
        }
        return this.bool(CERTIFICATE_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isPrivateKeyConfigurable() {
        if(StringUtils.isBlank(this.value(PRIVATE_KEY_CONFIGURABLE_KEY))) {
            return parent.isPrivateKeyConfigurable();
        }
        return this.bool(PRIVATE_KEY_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isHostnameConfigurable() {
        if(StringUtils.isBlank(this.value(HOSTNAME_CONFIGURABLE_KEY))) {
            return parent.isHostnameConfigurable();
        }
        return this.bool(HOSTNAME_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isPortConfigurable() {
        if(StringUtils.isBlank(this.value(PORT_CONFIGURABLE_KEY))) {
            return parent.isPortConfigurable();
        }
        return this.bool(PORT_CONFIGURABLE_KEY);
    }

    @Override
    public boolean isPathConfigurable() {
        if(StringUtils.isBlank(this.value(PATH_CONFIGURABLE_KEY))) {
            return parent.isPathConfigurable();
        }
        return this.bool(PATH_CONFIGURABLE_KEY);
    }

    @Override
    public String getOAuthAuthorizationUrl() {
        final String v = this.value(OAUTH_AUTHORIZATION_URL_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getOAuthAuthorizationUrl();
        }
        return v;
    }

    @Override
    public String getOAuthTokenUrl() {
        final String v = this.value(OAUTH_TOKEN_URL_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getOAuthTokenUrl();
        }
        return v;
    }

    @Override
    public List<String> getOAuthScopes() {
        final List<String> scopes = this.list(SCOPES_KEY);
        if(scopes.isEmpty()) {
            return parent.getOAuthScopes();
        }
        return scopes;
    }

    @Override
    public String getOAuthRedirectUrl() {
        final String v = this.value(OAUTH_REDIRECT_URL_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getOAuthRedirectUrl();
        }
        return v;
    }

    @Override
    public String getOAuthClientId() {
        final String v = this.value(OAUTH_CLIENT_ID_KEY);
        if(null == v) {
            return parent.getOAuthClientId();
        }
        return v;
    }

    @Override
    public String getOAuthClientSecret() {
        final String v = this.value(OAUTH_CLIENT_SECRET_KEY);
        if(null == v) {
            return parent.getOAuthClientSecret();
        }
        return v;
    }

    @Override
    public boolean isOAuthPKCE() {
        if(StringUtils.isBlank(this.value(OAUTH_PKCE_KEY))) {
            return parent.isOAuthPKCE();
        }
        return this.bool(OAUTH_PKCE_KEY);
    }

    @Override
    public String getSTSEndpoint() {
        final String v = this.value(STS_ENDPOINT_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getSTSEndpoint();
        }
        return v;
    }

    @Override
    public Map<String, String> getProperties() {
        final List<String> properties = this.list(PROPERTIES_KEY);
        if(properties.isEmpty()) {
            return parent.getProperties();
        }
        return properties.stream().distinct().collect(Collectors.toMap(
                property -> StringUtils.contains(property, '=') ? StringUtils.substringBefore(property, '=') : property,
                property -> StringUtils.contains(property, '=') ? substitutor.replace(StringUtils.substringAfter(property, '=')) : StringUtils.EMPTY));
    }

    @Override
    public String getHelp() {
        final String v = this.value(HELP_KEY);
        if(StringUtils.isBlank(v)) {
            return parent.getHelp();
        }
        return v;
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        return parent.getFeature(type);
    }

    @Override
    public boolean isUTCTimezone() {
        return parent.isUTCTimezone();
    }

    private String value(final String key) {
        return substitutor.replace(dict.stringForKey(key));
    }

    private List<String> list(final String key) {
        final List<String> list = dict.listForKey(key);
        if(null == list) {
            return Collections.emptyList();
        }
        final ArrayList<String> substituted = new ArrayList<>(list);
        substituted.replaceAll(substitutor::replace);
        return substituted;
    }

    private boolean bool(final String key) {
        return dict.booleanForKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Protocol)) {
            return false;
        }
        Protocol protocol = (Protocol) o;
        if(this.getIdentifier() != null ? !this.getIdentifier().equals(protocol.getIdentifier()) : protocol.getIdentifier() != null) {
            return false;
        }
        if(this.getScheme() != null ? !this.getScheme().equals(protocol.getScheme()) : protocol.getScheme() != null) {
            return false;
        }
        if(this.getContext() != null ? !this.getContext().equals(protocol.getContext()) : protocol.getContext() != null) {
            return false;
        }
        if(this.getAuthorization() != null ? !this.getAuthorization().equals(protocol.getAuthorization()) : protocol.getAuthorization() != null) {
            return false;
        }
        if(this.getProvider() != null ? !this.getProvider().equals(protocol.getProvider()) : protocol.getProvider() != null) {
            return false;
        }
        if(this.getDefaultHostname() != null ? !this.getDefaultHostname().equals(protocol.getDefaultHostname()) : protocol.getDefaultHostname() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = this.getIdentifier() != null ? this.getIdentifier().hashCode() : 0;
        result = 31 * result + (this.getScheme() != null ? this.getScheme().hashCode() : 0);
        result = 31 * result + (this.getContext() != null ? this.getContext().hashCode() : 0);
        result = 31 * result + (this.getAuthorization() != null ? this.getAuthorization().hashCode() : 0);
        result = 31 * result + (this.getProvider() != null ? this.getProvider().hashCode() : 0);
        result = 31 * result + (this.getDefaultHostname() != null ? this.getDefaultHostname().hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final Protocol o) {
        return new ProtocolComparator(this).compareTo(o);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Profile{");
        sb.append("parent=").append(parent);
        sb.append(", vendor=").append(this.value(VENDOR_KEY));
        sb.append(", description=").append(this.value(DESCRIPTION_KEY));
        sb.append(", image=").append(disk);
        sb.append('}');
        return sb.toString();
    }
}
