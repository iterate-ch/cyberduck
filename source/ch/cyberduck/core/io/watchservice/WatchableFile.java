package ch.cyberduck.core.io.watchservice;

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

    public WatchKey register(final WatchService watcher,
                             final WatchEvent.Kind<?>[] events,
                             final WatchEvent.Modifier... modifiers) throws IOException {
        return watcher.register(this, events, modifiers);
    }

    private static final WatchEvent.Modifier[] NO_MODIFIERS = new WatchEvent.Modifier[0];

    public final WatchKey register(final WatchService watcher,
                                   final WatchEvent.Kind<?>... events)
            throws IOException {
        return this.register(watcher, events, NO_MODIFIERS);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WatchableFile{");
        sb.append("file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
