package ch.cyberduck.core;

import ch.cyberduck.core.exception.AccessDeniedException;

/**
 * @version $Id$
 */
public interface TerminalService {

    void open(Host host, Path workdir) throws AccessDeniedException;
}
