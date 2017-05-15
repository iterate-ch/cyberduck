package ch.cyberduck.core;

import ch.cyberduck.binding.foundation.NSString;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class NSObjectPathReferenceTest {

    @Test
    public void testUnique() throws Exception {
        NSObjectPathReference r = new NSObjectPathReference(NSString.stringWithString("a"));
        assertEquals(r, new NSObjectPathReference(NSString.stringWithString("a")));
        assertEquals(r.toString(), new NSObjectPathReference(NSString.stringWithString("a")).toString());
        assertNotSame(r, new NSObjectPathReference(NSString.stringWithString("b")));
        assertNotSame(r.toString(), new NSObjectPathReference(NSString.stringWithString("b")).toString());
    }

    @Test
    public void testEqualConstructors() throws Exception {
        assertEquals(new NSObjectPathReference(NSString.stringWithString("[file]-/b")).hashCode(),
                NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file))).hashCode()
        );
        assertEquals(new NSObjectPathReference(NSString.stringWithString("[directory, symboliclink]-/d")).hashCode(),
                NSObjectPathReference.get(new Path("/d", EnumSet.of(Path.Type.directory, AbstractPath.Type.symboliclink))).hashCode()
        );
    }

    @Test
    public void testInterchange() throws Exception {
        assertEquals(
                new DefaultPathPredicate(new Path("/b", EnumSet.of(Path.Type.file))),
                new NSObjectPathReference(NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file))))
        );
        assertEquals(
                new NSObjectPathReference(NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file)))),
                new DefaultPathPredicate(new Path("/b", EnumSet.of(Path.Type.file)))
        );
        assertEquals(new DefaultPathPredicate(new Path("/b", EnumSet.of(Path.Type.file))).hashCode(),
                new NSObjectPathReference(NSObjectPathReference.get(new Path("/b", EnumSet.of(Path.Type.file)))).hashCode()
        );
    }

    @Test
    public void testUniquePath() throws Exception {
        Path one = new Path("a", EnumSet.of(Path.Type.file));
        Path second = new Path("a", EnumSet.of(Path.Type.file));
        assertEquals(NSObjectPathReference.get(one), NSObjectPathReference.get(second));
    }
}
