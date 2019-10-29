package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Touch;
import ch.cyberduck.core.local.features.Trash;
import ch.cyberduck.core.preferences.SupportDirectoryFinder;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;

import org.junit.Test;

import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkspaceTrashFeatureTest {

    @Test
    public void testTrash() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        new WorkspaceTrashFeature().trash(l);
        assertFalse(l.exists());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testTrashNotfound() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertFalse(l.exists());
        new WorkspaceTrashFeature().trash(l);
    }

    @Test
    public void testTrashRepeated() throws Exception {
        final WorkspaceTrashFeature f = new WorkspaceTrashFeature();
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        f.trash(l);
        assertFalse(l.exists());
    }

    @Test
    public void testTrashNonEmpty() throws Exception {
        final Trash trash = new WorkspaceTrashFeature();
        final SupportDirectoryFinder finder = new TemporarySupportDirectoryFinder();

        final Local temp = finder.find();
        final Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
        directory.mkdir();
        final Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
        sub.mkdir();
        final Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
        final Touch touch = LocalTouchFactory.get();
        touch.touch(file);

        trash.trash(directory);
    }

    public void testTrashOpenFile() throws Exception {
        final Trash trash = new WorkspaceTrashFeature();
        final SupportDirectoryFinder finder = new TemporarySupportDirectoryFinder();

        final Local temp = finder.find();
        final Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
        directory.mkdir();
        final Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
        sub.mkdir();
        final Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
        final Touch touch = LocalTouchFactory.get();
        touch.touch(file);

        try (final OutputStream stream = file.getOutputStream(false)) {
            trash.trash(directory);
        }
    }

    public void testTrashOpenDirectoryEnumeration() throws Exception {
        final Trash trash = new WorkspaceTrashFeature();
        final SupportDirectoryFinder finder = new TemporarySupportDirectoryFinder();

        final Local temp = finder.find();
        final Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
        directory.mkdir();
        final Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
        sub.mkdir();
        final Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
        final Touch touch = LocalTouchFactory.get();
        touch.touch(file);

        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sub.getAbsolute()))) {
            trash.trash(directory);
        }
    }
}
