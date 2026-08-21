package ch.cyberduck.ui.cocoa.controller;

import ch.cyberduck.binding.application.NSToolbar;
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.TransferCollection;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.ui.cocoa.datasource.TransferTableDataSource;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TransferControllerTest {

    @Test
    public void testInvalidateRemovesCollectionListener() throws Exception {
        final TransferCollection collection = TransferCollection.defaultCollection();
        final int baseline = listeners(collection).size();
        final TransferController controller = new TransferController();
        try {
            assertEquals(baseline + 1, listeners(collection).size());
            setField(controller, "toolbar", mock(NSToolbar.class));
            setField(controller, "transferTableModel", mock(TransferTableDataSource.class));
            controller.invalidate();
            assertEquals(baseline, listeners(collection).size());
        }
        finally {
            if(listeners(collection).size() != baseline) {
                setField(controller, "toolbar", mock(NSToolbar.class));
                setField(controller, "transferTableModel", mock(TransferTableDataSource.class));
                controller.invalidate();
            }
        }
    }

    @Test
    public void testTransferTableDataSourceInvalidateRemovesCollectionListener() throws Exception {
        final TransferCollection collection = TransferCollection.defaultCollection();
        final int baseline = listeners(collection).size();
        final TransferTableDataSource dataSource = new TransferTableDataSource();
        try {
            assertEquals(baseline + 1, listeners(collection).size());
            dataSource.invalidate();
            assertEquals(baseline, listeners(collection).size());
        }
        finally {
            if(listeners(collection).size() != baseline) {
                dataSource.invalidate();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<?> listeners(final Collection<Transfer> collection) throws Exception {
        final Field field = Collection.class.getDeclaredField("listeners");
        field.setAccessible(true);
        return (Set<?>) field.get(collection);
    }

    private void setField(final Object target, final String name, final Object value) throws Exception {
        final Field field = TransferController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
