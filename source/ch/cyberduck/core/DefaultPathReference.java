package ch.cyberduck.core;

import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id$
 */
public class DefaultPathReference implements PathReference<String> {

    private Path path;

    public DefaultPathReference(final Path path) {
        this.path = path;
    }

    /**
     * Obtain a string representation of the path that is unique for versioned files.
     *
     * @return The absolute path with version ID and checksum if any.
     */
    @Override
    public String unique() {
        String qualifier = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(path.attributes().getRegion())) {
            qualifier += path.attributes().getRegion();
        }
        if(StringUtils.isNotBlank(path.attributes().getVersionId())) {
            qualifier += path.attributes().getVersionId();
        }
        if(StringUtils.isNotBlank(qualifier)) {
            return String.format("%s-%s", path.getAbsolute(), qualifier);
        }
        return path.getAbsolute();
    }

    @Override
    public int hashCode() {
        return this.unique().hashCode();
    }

    @Override
    public String toString() {
        return this.unique();
    }

    /**
     * Comparing the hashcode.
     *
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof PathReference) {
            return this.hashCode() == other.hashCode();
        }
        return false;
    }
}
