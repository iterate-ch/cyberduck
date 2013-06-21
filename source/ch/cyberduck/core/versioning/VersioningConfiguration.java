package ch.cyberduck.core.versioning;

/**
 * @version $Id:$
 */
public class VersioningConfiguration {

    private boolean enabled;

    /**
     * Authentication
     */
    private boolean multifactor;

    public VersioningConfiguration() {
        this(false);
    }

    public VersioningConfiguration(final boolean enabled) {
        this.enabled = enabled;
    }

    public VersioningConfiguration(final boolean enabled, final boolean multifactor) {
        this.enabled = enabled;
        this.multifactor = multifactor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMultifactor() {
        return multifactor;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final VersioningConfiguration that = (VersioningConfiguration) o;
        if(enabled != that.enabled) {
            return false;
        }
        if(multifactor != that.multifactor) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (multifactor ? 1 : 0);
        return result;
    }
}
