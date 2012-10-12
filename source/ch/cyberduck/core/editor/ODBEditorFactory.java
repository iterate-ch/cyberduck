package ch.cyberduck.core.editor;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id:$
 */
public class ODBEditorFactory extends EditorFactory {
    private final List<Application> editors = new ArrayList<Application>();

    public static void register() {
        EditorFactory.addFactory(ODBEditorFactory.NATIVE_PLATFORM, new ODBEditorFactory());
    }

    protected ODBEditorFactory() {
        if(Preferences.instance().getBoolean("editor.odb.enable")) {
            editors.add(new Application("com.barebones.bbedit", "BBEdit"));
            editors.add(new Application("com.barebones.textwrangler", "TextWrangler"));
            editors.add(new Application("com.macromates.textmate", "TextMate"));
            editors.add(new Application("com.transtex.texeditplus", "Tex-Edit Plus"));
            editors.add(new Application("jp.co.artman21.JeditX", "Jedit X"));
            editors.add(new Application("net.mimikaki.mi", "mi"));
            editors.add(new Application("org.smultron.Smultron", "Smultron"));
            editors.add(new Application("org.fraise.Fraise", "Fraise"));
            editors.add(new Application("com.aynimac.CotEditor", "CotEditor"));
            editors.add(new Application("com.macrabbit.cssedit", "CSSEdit"));
            editors.add(new Application("com.talacia.Tag", "Tag"));
            editors.add(new Application("org.skti.skEdit", "skEdit"));
            editors.add(new Application("com.cgerdes.ji", "JarInspector"));
            editors.add(new Application("com.optima.PageSpinner", "PageSpinner"));
            editors.add(new Application("com.hogbaysoftware.WriteRoom", "WriteRoom"));
            editors.add(new Application("org.vim.MacVim", "MacVim"));
            editors.add(new Application("com.forgedit.ForgEdit", "ForgEdit"));
            editors.add(new Application("com.tacosw.TacoHTMLEdit", "Taco HTML Edit"));
            editors.add(new Application("com.macrabbit.Espresso", "Espresso"));
            editors.add(new Application("net.experiya.ScinteX", "ScinteX"));
        }
    }

    @Override
    public List<Application> getConfigured() {
        return editors;
    }

    @Override
    public Editor create(final Controller c, final Application application, final Path path) {
        return new ODBEditor(c, application, path);
    }
}
