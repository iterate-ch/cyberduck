package ch.cyberduck.core;

import ch.cyberduck.core.test.Depends;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class NSObjectPathReferenceTest extends AbstractTestCase {

    @Test
    public void testUnique() throws Exception {
        NSObjectPathReference r = new NSObjectPathReference(NSString.stringWithString("a"));
        assertEquals(r, new NSObjectPathReference(NSString.stringWithString("a")));
        assertEquals(r.unique(), new NSObjectPathReference(NSString.stringWithString("a")).unique());
        assertNotSame(r, new NSObjectPathReference(NSString.stringWithString("b")));
        assertNotSame(r.unique(), new NSObjectPathReference(NSString.stringWithString("b")).unique());
    }

    @Test
    public void testEqualConstructors() throws Exception {
        assertEquals(new NSObjectPathReference(NSString.stringWithString("[file]-/b")), new NSObjectPathReference(
                new Path("/b", EnumSet.of(Path.Type.file))
        ));
        assertEquals(new NSObjectPathReference(NSString.stringWithString("[directory, symboliclink]-/d")), new NSObjectPathReference(
                new Path("/d", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink))
        ));
    }

    @Test
    public void testUniquePath() throws Exception {
        Path one = new Path("a", EnumSet.of(Path.Type.file));
        Path second = new Path("a", EnumSet.of(Path.Type.file));
        assertEquals(new NSObjectPathReference(one), new NSObjectPathReference(second));
    }
}
