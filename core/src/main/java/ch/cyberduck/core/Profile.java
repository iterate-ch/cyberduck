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
    public static  final String OAUTH_CLIENT_SECRET_KEY = "OAuth Client Secret";

    private Local disk;
    private Local icon;

    public Profile(final Protocol parent, final Deserializer<?> dict) {
        this.parent = parent;
        this.dict = dict;
        this.disk = this.write(this.value("Disk"));
        this.icon = this.write(this.value("Icon"));
    }

    @Override
    public <T> T serialize(final Serializer<T> serializer) {
        for(String key : dict.keys()) {
            serializer.setStringForKey(dict.stringForKey(key), key);
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
        final String protocol = this.value("Protocol");
        final String vendor = this.value("Vendor");
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
        return this.bool("Deprecated");
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
        final String v = this.value("Hostname Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getHostnamePlaceholder();
        }
        return v;
    }

    @Override
    public String getUsernamePlaceholder() {
        final String v = this.value("Username Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getUsernamePlaceholder();
        }
        return LocaleFactory.localizedString(v, "Credentials");
    }

    @Override
    public String getPasswordPlaceholder() {
        final String v = this.value("Password Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getPasswordPlaceholder();
        }
        return LocaleFactory.localizedString(v, "Credentials");
    }

    @Override
    public String getTokenPlaceholder() {
        final String v = this.value("Token Placeholder");
        if(StringUtils.isBlank(v)) {
            return parent.getTokenPlaceholder();
        }
        return LocaleFactory.localizedString(v, "Credentials");
    }

    @Override
    public String getDefaultHostname() {
        final String v = this.value("Default Hostname");
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultHostname();
        }
        return v;
    }

    @Override
    public String getProvider() {
        final String v = this.value("Vendor");
        if(StringUtils.isBlank(v)) {
            return parent.getProvider();
        }
        return v;
    }

    public boolean isBundled() {
        final String v = this.value("Bundled");
        if(StringUtils.isBlank(v)) {
            return false;
        }
        return Boolean.parseBoolean(v);
    }

    @Override
    public String getName() {
        final String v = this.value("Name");
        if(StringUtils.isBlank(v)) {
            return parent.getName();
        }
        return v;
    }

    @Override
    public String getDescription() {
        final String v = this.value("Description");
        if(StringUtils.isBlank(v)) {
            return parent.getDescription();
        }
        return v;
    }

    @Override
    public int getDefaultPort() {
        final String v = this.value("Default Port");
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultPort();
        }
        try {
            return Integer.parseInt(v);
        }
        catch(NumberFormatException e) {
            log.warn(String.format("Port %s is not a number", e.getMessage()));
        }
        return parent.getDefaultPort();
    }

    @Override
    public String getDefaultPath() {
        final String v = this.value("Default Path");
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultPath();
        }
        return v;
    }

    @Override
    public String getDefaultNickname() {
        final String v = this.value("Default Nickname");
        if(StringUtils.isBlank(v)) {
            return parent.getDefaultNickname();
        }
        return v;
    }

    @Override
    public String getRegion() {
        final String v = this.value("Region");
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
            this.disk = this.write(this.value("Disk"));
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
            this.icon = this.write(this.value("Icon"));
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
    public boolean validate(Credentials credentials, LoginOptions options) {
        return parent.validate(credentials, options);
    }

    @Override
    public CredentialsConfigurator getCredentialsFinder() {
        return parent.getCredentialsFinder();
    }

    @Override
    public HostnameConfigurator getHostnameFinder() {
        return parent.getHostnameFinder();
    }

    @Override
    public JumphostConfigurator getJumpHostFinder() {
        return parent.getJumpHostFinder();
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
        final String v = this.value("Scheme");
        if(StringUtils.isBlank(v)) {
            return parent.getScheme();
        }
        try {
            return Scheme.valueOf(v);
        }
        catch(IllegalArgumentException e) {
            log.warn(String.format("Unknown scheme %s", v));
            return null;
        }
    }

    @Override
    public String[] getSchemes() {
        final List<String> values = this.list("Schemes");
        if(values.isEmpty()) {
            return parent.getSchemes();
        }
        return values.toArray(new String[values.size()]);
    }

    @Override
    public String getContext() {
        final String v = this.value("Context");
        if(StringUtils.isBlank(v)) {
            return parent.getContext();
        }
        return v;
    }

    @Override
    public String getAuthorization() {
        final String v = this.value("Authorization");
        if(StringUtils.isBlank(v)) {
            return parent.getAuthorization();
        }
        return v;
    }

    @Override
    public Set<Location.Name> getRegions() {
        final List<String> regions = this.list("Regions");
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
        if(StringUtils.isBlank(this.value("Anonymous Configurable"))) {
            return parent.isAnonymousConfigurable();
        }
        return this.bool("Anonymous Configurable");
    }

    @Override
    public boolean isUsernameConfigurable() {
        if(StringUtils.isBlank(this.value("Username Configurable"))) {
            return parent.isUsernameConfigurable();
        }
        return this.bool("Username Configurable");
    }

    @Override
    public boolean isPasswordConfigurable() {
        if(StringUtils.isBlank(this.value("Password Configurable"))) {
            return parent.isPasswordConfigurable();
        }
        return this.bool("Password Configurable");
    }

    @Override
    public boolean isTokenConfigurable() {
        if(StringUtils.isBlank(this.value("Token Configurable"))) {
            return parent.isTokenConfigurable();
        }
        return this.bool("Token Configurable");
    }

    @Override
    public boolean isOAuthConfigurable() {
        if(StringUtils.isNotBlank(this.value("OAuth Configurable"))) {
            return this.bool("OAuth Configurable");
        }
        return StringUtils.isNotBlank(this.getOAuthClientId());
    }

    @Override
    public boolean isCertificateConfigurable() {
        if(StringUtils.isBlank(this.value("Certificate Configurable"))) {
            return parent.isCertificateConfigurable();
        }
        return this.bool("Certificate Configurable");
    }

    @Override
    public boolean isPrivateKeyConfigurable() {
        if(StringUtils.isBlank(this.value("Private Key Configurable"))) {
            return parent.isPrivateKeyConfigurable();
        }
        return this.bool("Private Key Configurable");
    }

    @Override
    public boolean isHostnameConfigurable() {
        if(StringUtils.isBlank(this.value("Hostname Configurable"))) {
            return parent.isHostnameConfigurable();
        }
        return this.bool("Hostname Configurable");
    }

    @Override
    public boolean isPortConfigurable() {
        if(StringUtils.isBlank(this.value("Port Configurable"))) {
            return parent.isPortConfigurable();
        }
        return this.bool("Port Configurable");
    }

    @Override
    public boolean isPathConfigurable() {
        if(StringUtils.isBlank(this.value("Path Configurable"))) {
            return parent.isPathConfigurable();
        }
        return this.bool("Path Configurable");
    }

    @Override
    public String getOAuthAuthorizationUrl() {
        final String v = this.value("OAuth Authorization Url");
        if(StringUtils.isBlank(v)) {
            return parent.getOAuthAuthorizationUrl();
        }
        return v;
    }

    @Override
    public String getOAuthTokenUrl() {
        final String v = this.value("OAuth Token Url");
        if(StringUtils.isBlank(v)) {
            return parent.getOAuthTokenUrl();
        }
        return v;
    }

    @Override
    public List<String> getOAuthScopes() {
        final List<String> scopes = this.list("Scopes");
        if(scopes.isEmpty()) {
            return parent.getOAuthScopes();
        }
        return scopes;
    }

    @Override
    public String getOAuthRedirectUrl() {
        final String v = this.value("OAuth Redirect Url");
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
        if(StringUtils.isBlank(this.value("OAuth PKCE"))) {
            return parent.isOAuthPKCE();
        }
        return this.bool("OAuth PKCE");
    }

    @Override
    public String getSTSEndpoint() {
        final String v = this.value("STS Endpoint");
        if(StringUtils.isBlank(v)) {
            return parent.getSTSEndpoint();
        }
        return v;
    }

    @Override
    public Map<String, String> getProperties() {
        final List<String> properties = this.list("Properties");
        if(properties.isEmpty()) {
            return parent.getProperties();
        }
        return properties.stream().distinct().collect(Collectors.toMap(
                property -> StringUtils.contains(property, '=') ? StringUtils.substringBefore(property, '=') : property,
                property -> StringUtils.contains(property, '=') ? substitutor.replace(StringUtils.substringAfter(property, '=')) : StringUtils.EMPTY));
    }

    @Override
    public String getHelp() {
        final String v = this.value("Help");
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
        sb.append(", vendor=").append(this.value("Vendor"));
        sb.append(", description=").append(this.value("Description"));
        sb.append(", image=").append(disk);
        sb.append('}');
        return sb.toString();
    }
}
