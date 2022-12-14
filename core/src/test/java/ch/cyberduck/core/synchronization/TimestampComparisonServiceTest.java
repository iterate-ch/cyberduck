package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class TimestampComparisonServiceTest {

    @Test
    public void testCompareEqual() {
        TimestampComparisonService s = new TimestampComparisonService();
        final long timestamp = System.currentTimeMillis();
        assertEquals(Comparison.equal, s.compare(Path.Type.file, new PathAttributes().withModificationDate(timestamp), new PathAttributes().withModificationDate(timestamp)
        ));
    }

    @Test
    public void testCompareLocal() {
        TimestampComparisonService s = new TimestampComparisonService();
        final long timestamp = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        assertEquals(Comparison.local, s.compare(Path.Type.file,
                new PathAttributes().withModificationDate(timestamp),
                new PathAttributes() {
                    @Override
                    public long getModificationDate() {
                        final Calendar c = Calendar.getInstance(TimeZone.getDefault());
                        c.set(Calendar.HOUR_OF_DAY, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        return c.getTimeInMillis();
                    }
                }
        ));
    }
}
