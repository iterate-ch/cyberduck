package ch.cyberduck.core.transfer.normalizer;

/**
 * @version $Id$
 */
public interface RootPathsNormalizer<T> {
    public T normalize(T roots);
}
