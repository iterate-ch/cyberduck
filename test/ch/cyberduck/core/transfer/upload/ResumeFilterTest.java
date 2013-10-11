package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.NullSymlinkResolver;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class ResumeFilterTest extends AbstractTestCase {

    @Test
    public void testAccept() throws Exception {
        final ResumeFilter f = new ResumeFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        assertTrue(f.accept(new Path("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }

                    @Override
                    public LocalAttributes attributes() {
                        return new LocalAttributes(this.getAbsolute()) {
                            @Override
                            public long getSize() {
                                return 1L;
                            }
                        };
                    }
                };
            }
        }, new TransferStatus().exists(true)));
    }

    @Test
    public void testSkip() throws Exception {
        final Path file = new Path("t", Path.FILE_TYPE) {
            @Override
            public Local getLocal() {
                return new NullLocal(null, "a") {
                    @Override
                    public boolean exists() {
                        return true;
                    }
                };
            }
        };
        final ResumeFilter f = new ResumeFilter(new NullSymlinkResolver(), new NullSession(new Host("h")) {
            @Override
            public <T> T getFeature(final Class<T> type) {
                if(type == Find.class) {
                    return (T) new Find() {
                        @Override
                        public boolean find(final Path file) throws BackgroundException {
                            return true;
                        }
                    };
                }
                return super.getFeature(type);
            }

            @Override
            public AttributedList<Path> list(final Path parent, final ListProgressListener listener) {
                return new AttributedList<Path>(Collections.singletonList(file));
            }
        });
        file.attributes().setSize(1L);
        assertFalse(f.accept(file, new TransferStatus().exists(true)));
    }

    @Test
    public void testPrepareFalse() throws Exception {
        final ResumeFilter f = new ResumeFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        final Path t = new Path("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal(null, "t"));
        t.attributes().setSize(7L);
        final TransferStatus status = f.prepare(t, new TransferStatus().exists(true));
        assertFalse(status.isAppend());
    }

    @Test
    public void testPrepare() throws Exception {
        final ResumeFilter f = new ResumeFilter(new NullSymlinkResolver(), new NullSession(new Host("h")) {
            @Override
            public AttributedList<Path> list(final Path file, final ListProgressListener listener) {
                final Path f = new Path("t", Path.FILE_TYPE);
                f.attributes().setSize(7L);
                return new AttributedList<Path>(Collections.<Path>singletonList(f));
            }
        });
        final Path t = new Path("t", Path.FILE_TYPE) {
        };
        t.setLocal(new NullLocal(null, "t"));
        final TransferStatus status = f.prepare(t, new TransferStatus().exists(true));
        assertTrue(status.isAppend());
        assertEquals(7L, status.getCurrent());
    }

    @Test
    public void testPrepare0() throws Exception {
        final ResumeFilter f = new ResumeFilter(new NullSymlinkResolver(), new NullSession(new Host("h")));
        final Path t = new Path("t", Path.FILE_TYPE);
        t.setLocal(new NullLocal(null, "t"));
        t.attributes().setSize(0L);
        final TransferStatus status = f.prepare(t, new TransferStatus().exists(true));
        assertFalse(status.isAppend());
        assertEquals(0L, status.getCurrent());
    }
}