package ch.cyberduck.core.smb;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultTouchFeature;

public class SMBTouchFeature extends DefaultTouchFeature<Void> {

    public SMBTouchFeature(Write<Void> writer) {
        super(writer);
        //TODO Auto-generated constructor stub
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        return false;
    }
}
