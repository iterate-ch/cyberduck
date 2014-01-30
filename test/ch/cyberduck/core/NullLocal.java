package ch.cyberduck.core;

import ch.cyberduck.core.exception.AccessDeniedException;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class NullLocal extends Local {

    public NullLocal(final String parent, final String name) {
        super(parent + "/" + name);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public InputStream getInputStream() throws AccessDeniedException {
        return new NullInputStream(0L);
    }

    @Override
    public OutputStream getOutputStream(boolean append) throws AccessDeniedException {
        return new NullOutputStream();
    }
}
