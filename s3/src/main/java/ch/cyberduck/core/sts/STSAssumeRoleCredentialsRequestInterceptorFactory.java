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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.http.client.HttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class STSAssumeRoleCredentialsRequestInterceptorFactory extends Factory<STSAssumeRoleCredentialsRequestInterceptor> {

    private static final Logger log = LogManager.getLogger(STSAssumeRoleCredentialsRequestInterceptorFactory.class);

    private STSAssumeRoleCredentialsRequestInterceptorFactory() {
        super("factory.stsassumerolecredentialsrequestinterceptor.class");
    }

    public static STSAssumeRoleCredentialsRequestInterceptor get(final OAuth2RequestInterceptor oauth, final S3Session session,
                                                                 final X509TrustManager trust, final X509KeyManager key,
                                                                 final LoginCallback prompt) {
        return new STSAssumeRoleCredentialsRequestInterceptorFactory().create(oauth, session, trust, key, prompt);
    }

    private STSAssumeRoleCredentialsRequestInterceptor create(final OAuth2RequestInterceptor oauth, final S3Session session,
                                                              final X509TrustManager trust, final X509KeyManager key,
                                                              final LoginCallback prompt) {
        try {
            final Constructor<? extends STSAssumeRoleCredentialsRequestInterceptor> constructor = ConstructorUtils
                    .getMatchingAccessibleConstructor(clazz, OAuth2RequestInterceptor.class, S3Session.class, X509TrustManager.class, X509KeyManager.class, LoginCallback.class);
            if(null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", HttpClient.class, Host.class, LoginCallback.class));
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(oauth, session, trust, key, prompt);
        }
        catch(InstantiationException | InvocationTargetException | IllegalAccessException
              | NoSuchMethodException e) {
            log.error(String.format("Failure loading class %s. %s", clazz, e.getMessage()));
            return new STSAssumeRoleCredentialsRequestInterceptor(oauth, session, trust, key, prompt);
        }
    }
}
