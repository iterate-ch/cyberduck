package ch.cyberduck.core.io.watchservice;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation class for watch keys.
 */

abstract class AbstractWatchKey implements WatchKey {

    /**
     * Maximum size of event list (in the future this may be tunable)
     */
    private static final int MAX_EVENT_LIST_SIZE = 512;

    /**
     * Possible key states
     */
    private enum State {
        READY, SIGNALLED
    }

    // reference to watcher
    private final AbstractWatchService watcher;

    // key state
    private State state;

    // pending events
    private List<WatchEvent<?>> events;

    protected AbstractWatchKey(final AbstractWatchService watcher) {
        this.watcher = watcher;
        this.state = State.READY;
        this.events = new ArrayList<WatchEvent<?>>();
    }

    /**
     * Enqueues this key to the watch service
     */
    final void signal() {
        synchronized(this) {
            if(state == State.READY) {
                state = State.SIGNALLED;
                watcher.enqueueKey(this);
            }
        }
    }

    /**
     * Adds the event to this key and signals it.
     *
     * @param kind    event kind
     * @param context context
     */
    @SuppressWarnings("unchecked")
    final void signalEvent(WatchEvent.Kind<?> kind, Object context) {
        synchronized(this) {
            int size = events.size();
            if(size > 1) {
                // don't let list get too big
                if(size >= MAX_EVENT_LIST_SIZE) {
                    kind = StandardWatchEventKinds.OVERFLOW;
                    context = null;
                }

                // repeated event
                WatchEvent<?> prev = events.get(size - 1);
                if(kind == prev.kind()) {
                    boolean isRepeat;
                    if(context == null) {
                        isRepeat = (prev.context() == null);
                    }
                    else {
                        isRepeat = context.equals(prev.context());
                    }
                    if(isRepeat) {
                        ((Event<?>) prev).increment();
                        return;
                    }
                }
            }

            // non-repeated event
            events.add(new Event<Object>((WatchEvent.Kind<Object>) kind, context));
            signal();
        }
    }

    @Override
    public final List<WatchEvent<?>> pollEvents() {
        synchronized(this) {
            List<WatchEvent<?>> result = events;
            events = new ArrayList<WatchEvent<?>>();
            return result;
        }
    }

    @Override
    public final boolean reset() {
        synchronized(this) {
            if(state == State.SIGNALLED && isValid()) {
                if(events.isEmpty()) {
                    state = State.READY;
                }
                else {
                    // pending events so re-queue key
                    watcher.enqueueKey(this);
                }
            }
            return isValid();
        }
    }

    /**
     * WatchEvent implementation
     */
    private static class Event<T> implements WatchEvent<T> {
        private final WatchEvent.Kind<T> kind;
        private final T context;

        // synchronize on watch key to access/increment count
        private int count;

        Event(WatchEvent.Kind<T> type, T context) {
            this.kind = type;
            this.context = context;
            this.count = 1;
        }

        @Override
        public WatchEvent.Kind<T> kind() {
            return kind;
        }

        @Override
        public T context() {
            return context;
        }

        @Override
        public int count() {
            return count;
        }

        // for repeated events
        void increment() {
            count++;
        }
    }
}
