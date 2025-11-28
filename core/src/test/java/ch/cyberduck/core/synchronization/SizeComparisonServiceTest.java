package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SizeComparisonServiceTest {

    @Test
    public void testCompare() {
        ComparisonService s = new SizeComparisonService();
        assertEquals(Comparison.remote, s.compare(Path.Type.file, new DefaultPathAttributes().setSize(0L), new DefaultPathAttributes().setSize(1L)));
        assertEquals(Comparison.local, s.compare(Path.Type.file, new DefaultPathAttributes().setSize(1L), new DefaultPathAttributes().setSize(0L)));
        assertEquals(Comparison.equal, s.compare(Path.Type.file, new DefaultPathAttributes().setSize(1L), new DefaultPathAttributes().setSize(1L)));
        assertEquals(Comparison.notequal, s.compare(Path.Type.file, new DefaultPathAttributes().setSize(2L), new DefaultPathAttributes().setSize(1L)));
    }
}
