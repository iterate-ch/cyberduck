package ch.cyberduck.core.threading;

/**
 * @version $Id:$
 */
public interface AlertCallback {

    void alert(RepeatableBackgroundAction action,
               BackgroundException failure, StringBuilder transcript);
}
