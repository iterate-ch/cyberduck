package ch.cyberduck.core.threading;

import ch.cyberduck.core.exception.ConnectionCanceledException;

public interface CancelCallback {
    void verify() throws ConnectionCanceledException;
}
