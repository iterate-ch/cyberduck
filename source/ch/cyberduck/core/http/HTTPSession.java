package ch.cyberduck.core.http;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.ResolveCanceledException;
import ch.cyberduck.core.Session;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import java.net.UnknownHostException;

/**
 * @version $Id$
 */
public abstract class HTTPSession extends Session {

    private Appender appender = new AppenderSkeleton() {

        private static final String IN = "<< ";

        private static final String OUT = ">> ";

        public void close() {
            ;
        }

        public boolean requiresLayout() {
            return false;
        }

        @Override
        protected void append(LoggingEvent event) {
            final String m = event.getMessage().toString();
            if(m.startsWith(IN)) {
                HTTPSession.this.log(false, m.substring(IN.length() + 1, m.length() - 1));
            }
            else if(m.startsWith(OUT)) {
                HTTPSession.this.log(true, m.substring(OUT.length() + 1, m.length() - 1));
            }
        }
    };

    protected HTTPSession(Host h) {
        super(h);
    }

    @Override
    protected void fireConnectionWillOpenEvent() throws ResolveCanceledException, UnknownHostException {
        Logger.getLogger("httpclient.wire.header").addAppender(appender);
        super.fireConnectionWillOpenEvent();
    }

    @Override
    protected void fireConnectionWillCloseEvent() {
        Logger.getLogger("httpclient.wire.header").removeAppender(appender);
        super.fireConnectionWillCloseEvent();
    }
}
