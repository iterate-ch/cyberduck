package ch.cyberduck.core.synchronization;

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class TimestampComparisonServiceTest {

    @Test
    public void testCompareEqual() {
        TimestampComparisonService s = new TimestampComparisonService();
        final long timestamp = System.currentTimeMillis();
        assertEquals(Comparison.equal, s.compare(Path.Type.file, new DefaultPathAttributes().setModificationDate(timestamp), new DefaultPathAttributes().setModificationDate(timestamp)));
        final int hashCode = s.hashCode(Path.Type.file, new DefaultPathAttributes().setModificationDate(timestamp));
        assertEquals(Comparison.equal, s.compare(Path.Type.file, new DefaultPathAttributes().setModificationDate(timestamp), new DefaultPathAttributes().setModificationDate(
                TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(timestamp))
        )));
        assertEquals(hashCode, s.hashCode(Path.Type.file, new DefaultPathAttributes().setModificationDate(
                TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(timestamp))
        )));
    }

    @Test
    public void testCompareLocal() {
        TimestampComparisonService s = new TimestampComparisonService();
        final long timestamp = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();
        assertEquals(Comparison.local, s.compare(Path.Type.file,
                new DefaultPathAttributes().setModificationDate(timestamp),
                new
                        DefaultPathAttributes() {
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
