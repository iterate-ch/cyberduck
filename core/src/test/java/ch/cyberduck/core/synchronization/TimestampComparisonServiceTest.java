package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class TimestampComparisonServiceTest {

    @Test
    public void testCompareEqual() throws Exception {
        ComparisonService s = new TimestampComparisonService(TimeZone.getDefault());
        final long timestamp = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        assertEquals(Comparison.equal, s.compare(new PathAttributes() {
                                                     @Override
                                                     public long getModificationDate() {
                                                         return timestamp;
                                                     }
                                                 }, new LocalAttributes("/t") {
                                                     @Override
                                                     public long getModificationDate() {
                                                         return timestamp;
                                                     }
                                                 }
        ));
    }

    @Test
    public void testCompareLocal() throws Exception {
        ComparisonService s = new TimestampComparisonService(TimeZone.getDefault());
        final long timestamp = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        assertEquals(Comparison.local, s.compare(new PathAttributes() {
                                                     @Override
                                                     public long getModificationDate() {
                                                         final Calendar c = Calendar.getInstance(TimeZone.getDefault());
                                                         c.set(Calendar.HOUR_OF_DAY, 0);
                                                         c.set(Calendar.MINUTE, 0);
                                                         c.set(Calendar.SECOND, 0);
                                                         c.set(Calendar.MILLISECOND, 0);
                                                         return c.getTimeInMillis();
                                                     }
                                                 }, new LocalAttributes("/t") {
                                                     @Override
                                                     public long getModificationDate() {
                                                         return timestamp;
                                                     }
                                                 }
        ));
    }
}
