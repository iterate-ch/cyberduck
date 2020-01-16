package ch.cyberduck.core.resources;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.Application;

public class DisabledIconCache<I> implements IconCache<Void> {

    @Override
    public Void iconNamed(final String name, final Integer width, final Integer height) {
        return null;
    }

    @Override
    public Void documentIcon(final String extension, final Integer size) {
        return null;
    }

    @Override
    public Void documentIcon(final String extension, final Integer size, final Void badge) {
        return null;
    }

    @Override
    public Void folderIcon(final Integer size) {
        return null;
    }

    @Override
    public Void folderIcon(final Integer size, final Void badge) {
        return null;
    }

    @Override
    public Void fileIcon(final Path item, final Integer size) {
        return null;
    }

    @Override
    public Void aliasIcon(final String extension, final Integer size) {
        return null;
    }

    @Override
    public Void applicationIcon(final Application app, final Integer size) {
        return null;
    }
}
