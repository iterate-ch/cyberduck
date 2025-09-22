package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;

public interface S3CredentialsStrategy {

    static AWSCredentialsProvider toCredentialsProvider(final Credentials credentials) {
        if(credentials.isAnonymousLogin()) {
            return new AWSStaticCredentialsProvider(new AnonymousAWSCredentials());
        }
        if(credentials.isTokenAuthentication()) {
            return new AWSStaticCredentialsProvider(
                    StringUtils.isNotBlank(credentials.getTokens().getSessionToken()) ?
                            new BasicSessionCredentials(credentials.getTokens().getAccessKeyId(), credentials.getTokens().getSecretAccessKey(), credentials.getTokens().getSessionToken()) :
                            new BasicAWSCredentials(credentials.getTokens().getAccessKeyId(), credentials.getTokens().getSecretAccessKey()));
        }
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(credentials.getUsername(), credentials.getPassword()));
    }

    /**
     * @return Retrieve credentials to sign requests
     */
    Credentials get() throws BackgroundException;
}
