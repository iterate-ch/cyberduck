package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChecksumComparisonServiceTest {

    @Test
    public void testCompare() throws Exception {
        ComparisonService s = new ChecksumComparisonService();
        assertEquals(Comparison.equal, s.compare(new PathAttributes() {
                                                     @Override
                                                     public Checksum getChecksum() {
                                                         return new Checksum(HashAlgorithm.md5, "a");
                                                     }
                                                 }, new LocalAttributes("/t") {
                                                     @Override
                                                     public Checksum getChecksum() {
                                                         return new Checksum(HashAlgorithm.md5, "a");
                                                     }
                                                 }
        ));

        assertEquals(Comparison.notequal, s.compare(new PathAttributes() {
                                                        @Override
                                                        public Checksum getChecksum() {
                                                            return new Checksum(HashAlgorithm.md5, "b");
                                                        }
                                                    }, new LocalAttributes("/t") {
                                                        @Override
                                                        public Checksum getChecksum() {
                                                            return new Checksum(HashAlgorithm.md5, "a");
                                                        }
                                                    }
        ));
    }

    @Test
    public void testDirectory() throws Exception {
        ComparisonService s = new ChecksumComparisonService();
        assertEquals(Comparison.notequal, s.compare(new PathAttributes(),
                new LocalAttributes("/t")));
    }
}
