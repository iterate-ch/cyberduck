package ch.cyberduck.core.local;

import java.io.File;

import com.barbarysoftware.watchservice.WatchEvent;

/**
 * @version $Id:$
 */
public interface FileWatcherCallback {

    void callback(WatchEvent<File> event);
}
