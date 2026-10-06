package ch.cyberduck.core.worker;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WorkerTest {

    @Test
    public void testAbbreviate() {
        assertEquals("[]", Worker.abbreviate(Collections.emptyList()));
        assertEquals("[a, b]", Worker.abbreviate(Arrays.asList("a", "b")));
        final List<Integer> files = new ArrayList<>();
        for(int i = 0; i < 5747; i++) {
            files.add(i);
        }
        assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, … (5747 total)]", Worker.abbreviate(files));
    }
}
