package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id:$
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
        String region = StringUtils.EMPTY;
        String version = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(path.attributes().getRegion())) {
            region = path.attributes().getRegion();
        }
        if(StringUtils.isNotBlank(path.attributes().getVersionId())) {
            version = path.attributes().getVersionId();
        }
        return String.format("%s%s%s", path.getAbsolute(), version, region);
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
