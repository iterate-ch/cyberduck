package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.identity.IdentityConfiguration;

public class WriteIdentityWorker extends Worker<Boolean> {

    private final LoginCallback prompt;

    private final Boolean enabled;

    private final String policy;

    public WriteIdentityWorker(final LoginCallback prompt, final Boolean enabled, final String policy) {
        this.prompt = prompt;
        this.enabled = enabled;
        this.policy = policy;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final IdentityConfiguration iam = session.getFeature(IdentityConfiguration.class);
        final AnalyticsProvider analytics = session.getFeature(AnalyticsProvider.class);
        if(enabled) {
            iam.create(analytics.getName(), policy, prompt);
        }
        else {
            iam.delete(analytics.getName(), prompt);
        }
        return true;
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final WriteIdentityWorker that = (WriteIdentityWorker) o;
        if(policy != null ? !policy.equals(that.policy) : that.policy != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return policy != null ? policy.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WriteIdentityWorker{");
        sb.append("policy=").append(policy);
        sb.append('}');
        return sb.toString();
    }
}
