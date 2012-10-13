package ch.cyberduck.core.editor;

import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class WatchEditorFactory extends EditorFactory {
    private final List<Application> editors = new ArrayList<Application>();

    public static void register() {
        EditorFactory.addFactory(WatchEditorFactory.NATIVE_PLATFORM, new WatchEditorFactory());
    }

    protected WatchEditorFactory() {
        editors.add(new Application("de.codingmonkeys.SubEthaEdit", "SubEthaEdit"));
        editors.add(new Application("com.apple.TextEdit", "TextEdit"));
        editors.add(new Application("com.apple.Xcode", "Xcode"));
    }

    @Override
    public List<Application> getConfigured() {
        return editors;
    }

    @Override
    public Editor create(final Controller c, final Application application, final Path path) {
        return new WatchEditor(c, application, path);
    }

    @Override
    protected Editor create() {
        throw new FactoryException("Not supported");
    }
}