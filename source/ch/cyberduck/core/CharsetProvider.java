package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public interface CharsetProvider {

    /**
     * @return The available character sets available on this platform
     */
    String[] availableCharsets();
}
