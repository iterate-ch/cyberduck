package ch.cyberduck.core.sts;

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

import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.auth.profile.internal.BasicProfile;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;

/**
 * Configure credentials from AWS CLI configuration and SSO cache
 */
public abstract class AbstractSTSCredentialsConfigurator implements CredentialsConfigurator {

    protected static final Logger log = LogManager.getLogger(AbstractSTSCredentialsConfigurator.class);

    protected final X509TrustManager trust;
    protected final X509KeyManager key;
    protected final PasswordCallback prompt;
    protected AWSSecurityTokenService service;

    private final Map<String, BasicProfile> profiles = new LinkedHashMap<>();

    public AbstractSTSCredentialsConfigurator(final X509TrustManager trust, final X509KeyManager key, PasswordCallback prompt) {
        this.trust = trust;
        this.key = key;
        this.prompt = prompt;
    }

    public CredentialsConfigurator reload() {
        return this;
    }
}
