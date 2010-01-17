package com.barbarysoftware.watchservice;

import java.io.File;
import java.io.IOException;

public class WatchableFile implements Watchable {

    private final File file;

    public WatchableFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>[] events,
                             WatchEvent.Modifier... modifiers)
            throws IOException {
        if (watcher == null)
            throw new NullPointerException();
        if (!(watcher instanceof AbstractWatchService))
            throw new ProviderMismatchException();
        return ((AbstractWatchService) watcher).register(this, events, modifiers);
    }

    private static final WatchEvent.Modifier[] NO_MODIFIERS = new WatchEvent.Modifier[0];

    public final WatchKey register(WatchService watcher,
                                   WatchEvent.Kind<?>... events)
            throws IOException {
        return register(watcher, events, NO_MODIFIERS);
    }

    @Override
    public String toString() {
        return "Path{" +
                "file=" + file +
                '}';
    }
}
