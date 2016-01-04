package ch.cyberduck.core;

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @version $Id$
 */
public class NullLocal extends Local {

    public NullLocal(final String parent, final String name) throws LocalAccessDeniedException {
        super(parent, name);
    }

    public NullLocal(final Local parent, final String name) throws LocalAccessDeniedException {
        super(parent, name);
    }

    public NullLocal(final String name) throws LocalAccessDeniedException {
        super(PathNormalizer.normalize(name, false));
    }

    @Override
    public boolean isFile() {
        if(!super.exists()) {
            return true;
        }
        return super.isFile();
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
