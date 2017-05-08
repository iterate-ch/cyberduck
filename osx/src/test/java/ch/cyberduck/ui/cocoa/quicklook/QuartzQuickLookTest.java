package ch.cyberduck.ui.cocoa.quicklook;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class QuartzQuickLookTest {

    @Test
    public void testSelect() throws Exception {
        QuickLook q = new QuartzQuickLook();
        final List<Local> files = new ArrayList<Local>();
        files.add(new NullLocal("f"));
        files.add(new NullLocal("b"));
        q.select(files);
    }
}