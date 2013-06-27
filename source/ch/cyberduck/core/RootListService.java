package ch.cyberduck.core;

import ch.cyberduck.core.threading.BackgroundException;

import java.util.List;

/**
 * @version $Id:$
 */
public interface RootListService<S extends Session> {
    List<Path> list(S session) throws BackgroundException;
}
