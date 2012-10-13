package ch.cyberduck.core.editor;

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.Path;
import ch.cyberduck.ui.Controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Id$
 */
public class MultipleEditorFactory extends EditorFactory {

    public static void register() {
        EditorFactory.addFactory(Factory.NATIVE_PLATFORM, new MultipleEditorFactory());
    }

    private EditorFactory watchEditorFactory
            = new WatchEditorFactory();

    private EditorFactory odbEditorFactory
            = new ODBEditorFactory();

    private final Set<Application> editors = new HashSet<Application>();

    protected MultipleEditorFactory() {
        editors.addAll(watchEditorFactory.getEditors());
        editors.addAll(odbEditorFactory.getEditors());
    }

    @Override
    public List<Application> getConfigured() {
        return new ArrayList<Application>(editors);
    }

    @Override
    public Editor create(final Controller c, final Application application, final Path path) {
        if(odbEditorFactory.getConfigured().contains(application)) {
            return odbEditorFactory.create(c, application, path);
        }
        return watchEditorFactory.create(c, application, path);
    }

    @Override
    protected Editor create() {
        throw new FactoryException("Not supported");
    }
}
