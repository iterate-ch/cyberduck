package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SizeComparisonServiceTest {

    @Test
    public void testCompare() {
        ComparisonService s = new SizeComparisonService();
        assertEquals(Comparison.remote, s.compare(Path.Type.file, new PathAttributes().setSize(0L), new PathAttributes().setSize(1L)));
        assertEquals(Comparison.local, s.compare(Path.Type.file, new PathAttributes().setSize(1L), new PathAttributes().setSize(0L)));
        assertEquals(Comparison.equal, s.compare(Path.Type.file, new PathAttributes().setSize(1L), new PathAttributes().setSize(1L)));
        assertEquals(Comparison.notequal, s.compare(Path.Type.file, new PathAttributes().setSize(2L), new PathAttributes().setSize(1L)));
    }
}
