package ch.cyberduck.core.local;

import ch.cyberduck.ui.cocoa.application.NSImage;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.resources.IconCache;

import org.apache.commons.lang.StringUtils;
import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * @version $Id$
 */
public final class WorkspaceIconService implements IconService {

    public static void register() {
        IconServiceFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends IconServiceFactory {
        @Override
        protected IconService create() {
            return new WorkspaceIconService();
        }
    }

    private WorkspaceIconService() {
        //
    }

    private static final Object lock = new Object();

    @Override
    public boolean setIcon(final Local file, final String image) {
        final NSImage icon;
        if(StringUtils.isBlank(image)) {
            icon = null;
        }
        else {
            icon = IconCache.iconNamed(image);
        }
        return setIcon(file, icon);

    }

    protected boolean setIcon(final Local file, final NSImage icon) {
        synchronized(lock) {
            // Specify 0 if you want to generate icons in all available icon representation formats
            return NSWorkspace.sharedWorkspace().setIcon_forFile_options(icon, file.getAbsolute(), new NSUInteger(0));
        }
    }

    @Override
    public boolean setProgress(final Local file, final int progress) {
        if(-1 == progress) {
            return this.setIcon(file, (String) null);
        }
        else {
            return this.setIcon(file, String.format("download%d.icns", progress));
        }
    }
}