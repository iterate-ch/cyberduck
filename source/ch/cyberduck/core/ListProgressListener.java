package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public interface ListProgressListener extends ProgressListener {
    void chunk(AttributedList<Path> list);
}
