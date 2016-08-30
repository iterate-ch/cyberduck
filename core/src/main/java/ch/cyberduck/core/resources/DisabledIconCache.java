package ch.cyberduck.core.resources;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Application;

public class DisabledIconCache<I> extends AbstractIconCache {

    @Override
    public Object iconNamed(final String name, final Integer width, final Integer height) {
        return null;
    }

    @Override
    public Object documentIcon(final String extension, final Integer size) {
        return null;
    }

    @Override
    public Object documentIcon(final String extension, final Integer size, final Object badge) {
        return null;
    }

    @Override
    public Object folderIcon(final Integer size) {
        return null;
    }

    @Override
    public Object folderIcon(final Integer size, final Object badge) {
        return null;
    }

    @Override
    public Object fileIcon(final Path item, final Integer size) {
        return null;
    }

    @Override
    public Object applicationIcon(final Application app, final Integer size) {
        return null;
    }

    @Override
    protected Object badge(final Object badge, final Object icon) {
        return null;
    }
}
