package ch.cyberduck.core.logging;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import org.apache.commons.lang3.StringUtils;

public class LoggingConfiguration {

    public static LoggingConfiguration empty() {
        return new LoggingConfiguration();
    }

    private boolean enabled;

    private String loggingTarget;

    public LoggingConfiguration() {
        this.enabled = false;
        this.loggingTarget = StringUtils.EMPTY;
    }

    public LoggingConfiguration(final boolean enabled) {
        this.enabled = enabled;
        this.loggingTarget = StringUtils.EMPTY;
    }

    public LoggingConfiguration(final boolean enabled, final String loggingTarget) {
        this.enabled = enabled;
        this.loggingTarget = loggingTarget;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getLoggingTarget() {
        return loggingTarget;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final LoggingConfiguration that = (LoggingConfiguration) o;

        if(enabled != that.enabled) {
            return false;
        }
        if(loggingTarget != null ? !loggingTarget.equals(that.loggingTarget) : that.loggingTarget != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (loggingTarget != null ? loggingTarget.hashCode() : 0);
        return result;
    }
}
