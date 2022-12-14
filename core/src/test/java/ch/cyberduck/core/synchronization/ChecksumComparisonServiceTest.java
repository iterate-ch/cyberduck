package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChecksumComparisonServiceTest {

    @Test
    public void testCompare() {
        ComparisonService s = new ChecksumComparisonService();
        assertEquals(Comparison.equal, s.compare(Path.Type.file, new PathAttributes() {
                    @Override
                    public Checksum getChecksum() {
                        return new Checksum(HashAlgorithm.md5, "a");
                    }
                }, new PathAttributes().withChecksum(new Checksum(HashAlgorithm.md5, "a"))
        ));

        assertEquals(Comparison.notequal, s.compare(Path.Type.file, new PathAttributes() {
            @Override
            public Checksum getChecksum() {
                return new Checksum(HashAlgorithm.md5, "b");
            }
        }, new PathAttributes().withChecksum(new Checksum(HashAlgorithm.md5, "a"))));
    }

    @Test
    public void testDirectory() {
        ComparisonService s = new ChecksumComparisonService();
        assertEquals(Comparison.unknown, s.compare(Path.Type.directory, new PathAttributes(),
                new PathAttributes()));
    }
}
