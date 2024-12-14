package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.shared.RootPathContainerService;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.cyberduck.core.Profile.*;

public abstract class AbstractProtocol implements Protocol {

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        if(this.getOAuthClientId() != null) {
            dict.setStringForKey(this.getOAuthClientId(), OAUTH_CLIENT_ID_KEY);
        }
        if(this.getOAuthClientSecret() != null) {
            dict.setStringForKey(this.getOAuthClientSecret(), OAUTH_CLIENT_SECRET_KEY);
        }
        if(this.getOAuthTokenUrl() != null) {
            dict.setStringForKey(this.getOAuthTokenUrl(), OAUTH_TOKEN_URL_KEY);
        }
        if(this.getOAuthRedirectUrl() != null) {
            dict.setStringForKey(this.getOAuthRedirectUrl(), OAUTH_REDIRECT_URL_KEY);
        }
        if(this.getOAuthAuthorizationUrl() != null) {
            dict.setStringForKey(this.getOAuthAuthorizationUrl(), OAUTH_AUTHORIZATION_URL_KEY);
        }
        if(!this.isOAuthPKCE()) {
            dict.setBooleanForKey(this.isOAuthPKCE(), OAUTH_PKCE_KEY);
        }
        if(!this.getOAuthScopes().isEmpty()) {
            dict.setStringListForKey(this.getOAuthScopes(), SCOPES_KEY);
        }
        if(this.getSTSEndpoint() != null) {
            dict.setStringForKey(this.getSTSEndpoint(), STS_ENDPOINT_KEY);
        }
        if(this.disk() != null) {
            dict.setStringForKey(this.disk(), DISK_KEY);
        }
        if(this.icon() != null) {
            dict.setStringForKey(this.icon(), ICON_KEY);
        }
        if(this.getIdentifier() != null) {
            dict.setStringForKey(this.getIdentifier(), PROTOCOL_KEY);
        }
        if(this.getProvider() != null) {
            dict.setStringForKey(this.getProvider(), VENDOR_KEY);
        }
        if(this.isBundled()) {
            dict.setBooleanForKey(this.isBundled(), BUNDLED_KEY);
        }
        if(this.getName() != null) {
            dict.setStringForKey(this.getName(), NAME_KEY);
        }
        if(this.getDescription() != null) {
            dict.setStringForKey(getDescription(), DESCRIPTION_KEY);
        }
        if(this.getRegion() != null) {
            dict.setStringForKey(this.getRegion(), REGION_KEY);
        }
        if(!this.getRegions().isEmpty()) {
            dict.setStringListForKey(this.getRegions().stream().map(Location.Name::getIdentifier).collect(Collectors.toList()), REGIONS_KEY);
        }
        dict.setStringForKey(this.getScheme().name(), SCHEME_KEY);
        if(this.getSchemes().length > 0) {
            dict.setStringListForKey(Arrays.asList(this.getSchemes()), SCHEMES_KEY);
        }
        if(this.getAuthorization() != null) {
            dict.setStringForKey(this.getAuthorization(), AUTHORIZATION_KEY);
        }
        if(this.getContext() != null) {
            dict.setStringForKey(this.getContext(), CONTEXT_KEY);
        }
        if(StringUtils.isNotBlank(this.getDefaultHostname())) {
            dict.setStringForKey(this.getDefaultHostname(), DEFAULT_HOSTNAME_KEY);
        }
        if(this.getDefaultPort() != this.getScheme().getPort()) {
            dict.setStringForKey(Integer.toString(this.getDefaultPort()), DEFAULT_PORT_KEY);
        }
        if(this.getDefaultPath() != null) {
            dict.setStringForKey(this.getDefaultPath(), DEFAULT_PATH_KEY);
        }
        if(this.getDefaultNickname() != null) {
            dict.setStringForKey(this.getDefaultNickname(), DEFAULT_NICKNAME_KEY);
        }
        if(StringUtils.isNotEmpty(this.getHostnamePlaceholder())) {
            dict.setStringForKey(this.getHostnamePlaceholder(), HOSTNAME_PLACEHOLDER_KEY);
        }
        if(!this.getUsernamePlaceholder().equals(LocaleFactory.localizedString("Username", "Credentials"))) {
            dict.setStringForKey(this.getUsernamePlaceholder(), USERNAME_PLACEHOLDER_KEY);
        }
        if(!this.getPasswordPlaceholder().equals(LocaleFactory.localizedString("Password", "Credentials"))) {
            dict.setStringForKey(this.getPasswordPlaceholder(), PASSWORD_PLACEHOLDER_KEY);
        }
        if(!this.getTokenPlaceholder().equals(LocaleFactory.localizedString("Password", "Credentials"))) {
            dict.setStringForKey(this.getTokenPlaceholder(), TOKEN_PLACEHOLDER_KEY);
        }
        if(StringUtils.isNotBlank(this.getDefaultHostname())) {
            dict.setBooleanForKey(this.isHostnameConfigurable(), HOSTNAME_CONFIGURABLE_KEY);
        }
        if(StringUtils.isNotBlank(this.getDefaultHostname())) {
            dict.setBooleanForKey(this.isPortConfigurable(), PORT_CONFIGURABLE_KEY);
        }
        if(!this.isPathConfigurable()) {
            dict.setBooleanForKey(this.isPathConfigurable(), PATH_CONFIGURABLE_KEY);
        }
        if(!this.isUsernameConfigurable()) {
            dict.setBooleanForKey(this.isUsernameConfigurable(), USERNAME_CONFIGURABLE_KEY);
        }
        if(!this.isPasswordConfigurable()) {
            dict.setBooleanForKey(this.isPasswordConfigurable(), PASSWORD_CONFIGURABLE_KEY);
        }
        if(this.isAnonymousConfigurable()) {
            dict.setBooleanForKey(this.isAnonymousConfigurable(), ANONYMOUS_CONFIGURABLE_KEY);
        }
        if(this.isTokenConfigurable()) {
            dict.setBooleanForKey(this.isTokenConfigurable(), TOKEN_CONFIGURABLE_KEY);
        }
        if(this.isOAuthConfigurable()) {
            dict.setBooleanForKey(this.isOAuthConfigurable(), OAUTH_CONFIGURABLE_KEY);
        }
        if(this.isCertificateConfigurable()) {
            dict.setBooleanForKey(this.isCertificateConfigurable(), CERTIFICATE_CONFIGURABLE_KEY);
        }
        if(this.isPrivateKeyConfigurable()) {
            dict.setBooleanForKey(this.isPrivateKeyConfigurable(), PRIVATE_KEY_CONFIGURABLE_KEY);
        }
        if(!this.getProperties().isEmpty()) {
            dict.setStringListForKey(this.getProperties().entrySet().stream().map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue())).collect(Collectors.toList()), PROPERTIES_KEY);
        }
        if(this.isDeprecated()) {
            dict.setBooleanForKey(this.isDeprecated(), DEPRECATED_KEY);
        }
        if(StringUtils.isNotEmpty(this.getHelp())) {
            dict.setStringForKey(this.getHelp(), HELP_KEY);
        }
        return dict.getSerialized();
    }

    @Override
    public String getProvider() {
        return this.getIdentifier();
    }

    @Override
    public boolean isBundled() {
        return false;
    }

    @Override
    public String getName() {
        return this.getScheme().name().toUpperCase(Locale.ROOT);
    }

    @Override
    public String favicon() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        // Disabled by default. Enable using profile
        return false;
    }

    @Override
    public boolean isDeprecated() {
        return false;
    }

    @Override
    public Statefulness getStatefulness() {
        return Statefulness.stateless;
    }

    @Override
    public String[] getSchemes() {
        final HashSet<String> schemes = new LinkedHashSet<>(Arrays.asList(this.getIdentifier(), this.getScheme().name()));
        return schemes.toArray(new String[schemes.size()]);
    }

    @Override
    public String toString() {
        return this.getProvider();
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", this.getIdentifier());
    }

    /**
     * @return A small icon to display
     */
    @Override
    public String icon() {
        return this.disk();
    }

    @Override
    public boolean isSecure() {
        return this.getScheme().isSecure();
    }

    @Override
    public boolean isHostnameConfigurable() {
        return StringUtils.isBlank(this.getDefaultHostname());
    }

    @Override
    public boolean isPortConfigurable() {
        return StringUtils.isBlank(this.getDefaultHostname());
    }

    @Override
    public boolean isPathConfigurable() {
        return true;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return false;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public boolean isUsernameConfigurable() {
        return true;
    }

    @Override
    public boolean isPasswordConfigurable() {
        return StringUtils.isBlank(this.getOAuthClientId());
    }

    @Override
    public boolean isTokenConfigurable() {
        return false;
    }

    @Override
    public boolean isOAuthConfigurable() {
        return StringUtils.isNotBlank(this.getOAuthClientId());
    }

    @Override
    public boolean isCertificateConfigurable() {
        return false;
    }

    @Override
    public boolean isPrivateKeyConfigurable() {
        return false;
    }

    @Override
    public boolean isUTCTimezone() {
        return true;
    }

    @Override
    public String getHostnamePlaceholder() {
        return this.getDefaultHostname();
    }

    @Override
    public String getPathPlaceholder() {
        return LocaleFactory.localizedString("Folder", "Localizable");
    }

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Username", "Credentials");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Password", "Credentials");
    }

    @Override
    public String getTokenPlaceholder() {
        return this.getPasswordPlaceholder();
    }

    @Override
    public String getOAuthClientId() {
        return null;
    }

    @Override
    public String getOAuthClientSecret() {
        return null;
    }

    @Override
    public String getOAuthRedirectUrl() {
        return null;
    }

    @Override
    public boolean isOAuthPKCE() {
        return true;
    }

    @Override
    public String getSTSEndpoint() {
        return null;
    }

    @Override
    public String getDefaultHostname() {
        // Blank by default
        return PreferencesFactory.get().getProperty("connection.hostname.default");
    }

    @Override
    public Set<Location.Name> getRegions() {
        return Collections.emptySet();
    }

    @Override
    public Set<Location.Name> getRegions(final List<String> regions) {
        return regions.stream().map(Location.Name::new).collect(Collectors.toSet());
    }

    @Override
    public int getDefaultPort() {
        return this.getScheme().getPort();
    }

    @Override
    public String getDefaultPath() {
        return null;
    }

    @Override
    public String getDefaultNickname() {
        return null;
    }

    @Override
    public String getContext() {
        return null;
    }

    @Override
    public String getAuthorization() {
        return null;
    }

    @Override
    public String getOAuthAuthorizationUrl() {
        return null;
    }

    @Override
    public String getOAuthTokenUrl() {
        return null;
    }

    @Override
    public List<String> getOAuthScopes() {
        return Collections.emptyList();
    }

    @Override
    public String getRegion() {
        return null;
    }

    @Override
    public Type getType() {
        return Type.valueOf(this.getIdentifier());
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", this.getClass().getPackage().getName(), StringUtils.capitalize(this.getType().name()));
    }

    @Override
    public boolean validate(final Credentials credentials, final LoginOptions options) {
        if(options.user) {
            if(StringUtils.isBlank(credentials.getUsername())) {
                return false;
            }
        }
        if(options.certificate) {
            if(credentials.isCertificateAuthentication()) {
                return true;
            }
        }
        if(options.publickey) {
            if(credentials.isPublicKeyAuthentication()) {
                // No password may be required to decrypt private key
                return true;
            }
            if(!options.password) {
                // Require private key
                return false;
            }
        }
        if(options.password) {
            switch(this.getType()) {
                case ftp:
                case dav:
                    return Objects.nonNull(credentials.getPassword());
                case sftp:
                    // SFTP agent auth requires no password and no private key selection
                    return true;
                default:
                    return StringUtils.isNotBlank(credentials.getPassword());
            }
        }
        if(options.oauth) {
            // Always refresh tokens in login
            return true;
        }
        if(options.token) {
            return StringUtils.isNotBlank(credentials.getToken());
        }
        return true;
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
        if(this.getProvider() != null ? !this.getProvider().equals(protocol.getProvider()) : protocol.getProvider() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(final Protocol o) {
        return new ProtocolComparator(this).compareTo(o);
    }

    @Override
    public int hashCode() {
        int result = this.getIdentifier() != null ? this.getIdentifier().hashCode() : 0;
        result = 31 * result + (this.getScheme() != null ? this.getScheme().hashCode() : 0);
        result = 31 * result + (this.getProvider() != null ? this.getProvider().hashCode() : 0);
        return result;
    }

    @Override
    public Case getCaseSensitivity() {
        return Protocol.Case.sensitive;
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.implicit;
    }

    @Override
    public Comparator<String> getListComparator() {
        return new NullComparator<>();
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.custom;
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public String getHelp() {
        return StringUtils.EMPTY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == PathContainerService.class) {
            return (T) new RootPathContainerService();
        }
        if(type == WebUrlProvider.class) {
            return (T) new DefaultWebUrlProvider();
        }
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(this);
        }
        if(type == HostnameConfigurator.class) {
            return (T) HostnameConfigurator.DISABLED;
        }
        if(type == CredentialsConfigurator.class) {
            return (T) CredentialsConfigurator.DISABLED;
        }
        if(type == JumphostConfigurator.class) {
            return (T) JumphostConfigurator.DISABLED;
        }
        return null;
    }
}
