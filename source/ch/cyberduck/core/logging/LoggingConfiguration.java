package ch.cyberduck.core.logging;

/**
 * @version $Id:$
 */
public class LoggingConfiguration {

    private boolean enabled;
    private String loggingTarget;

    public LoggingConfiguration() {
        this(false);
    }

    public LoggingConfiguration(final boolean enabled) {
        this.enabled = enabled;
    }

    public LoggingConfiguration(final boolean enabled, final String loggingTarget) {
        this.enabled = enabled;
        this.loggingTarget = loggingTarget;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getLoggingTarget() {
        return loggingTarget;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final LoggingConfiguration that = (LoggingConfiguration) o;

        if(enabled != that.enabled) {
            return false;
        }
        if(loggingTarget != null ? !loggingTarget.equals(that.loggingTarget) : that.loggingTarget != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (loggingTarget != null ? loggingTarget.hashCode() : 0);
        return result;
    }
}
