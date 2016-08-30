package ch.cyberduck.core;

public interface CharsetProvider {

    /**
     * @return The available character sets available on this platform
     */
    String[] availableCharsets();
}
