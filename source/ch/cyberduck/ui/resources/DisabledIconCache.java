package ch.cyberduck.ui.resources;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Application;

/**
 * @version $Id$
 */
public class DisabledIconCache<I> extends AbstractIconCache {
    @Override
    public Object iconNamed(final String name, final Integer width, final Integer height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object documentIcon(final String extension, final Integer size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object documentIcon(final String extension, final Integer size, final Object badge) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object folderIcon(final Integer size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object folderIcon(final Integer size, final Object badge) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object fileIcon(final Path item, final Integer size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object applicationIcon(final Application app, final Integer size) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object badge(final Object badge, final Object icon) {
        throw new UnsupportedOperationException();
    }
}
