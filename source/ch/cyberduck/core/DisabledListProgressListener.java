package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public class DisabledListProgressListener extends DisabledProgressListener implements ListProgressListener {
    @Override
    public void chunk(final AttributedList<Path> list) {
        //
    }
}
