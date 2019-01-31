package ch.cyberduck.ui.browser;

import ch.cyberduck.core.Path;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class PathTooltipServiceTest {

    @Test
    public void testGetTooltip() {
        final PathTooltipService s = new PathTooltipService();
        assertEquals("/p", s.getTooltip(new Path("/p", EnumSet.of(Path.Type.file))));
    }
}
