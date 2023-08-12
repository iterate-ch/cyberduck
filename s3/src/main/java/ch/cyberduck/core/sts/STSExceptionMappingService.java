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

import ch.cyberduck.core.ExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ExpiredTokenException;
import ch.cyberduck.core.exception.LoginFailureException;

import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.amazonaws.services.securitytoken.model.InvalidIdentityTokenException;

public class STSExceptionMappingService implements ExceptionMappingService<AWSSecurityTokenServiceException> {

    @Override
    public BackgroundException map(final AWSSecurityTokenServiceException e) {
        if(e instanceof com.amazonaws.services.securitytoken.model.ExpiredTokenException) {
            // The web identity token that was passed is expired or is not valid. Get a new identity token from the identity
            // provider and then retry the request.
            return new ExpiredTokenException(e.getErrorMessage(), e);
        }
        if(e instanceof InvalidIdentityTokenException) {
            // The web identity token that was passed could not be validated by Amazon Web Services. Get a new identity token from
            // the identity provider and then retry the request.
            return new ExpiredTokenException(e.getErrorMessage(), e);
        }
        return new LoginFailureException(e.getErrorMessage(), e);
    }
}
