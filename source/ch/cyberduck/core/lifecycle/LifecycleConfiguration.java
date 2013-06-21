package ch.cyberduck.core.lifecycle;

/**
 * @version $Id:$
 */
public class LifecycleConfiguration {

    private Integer transition;
    private Integer expiration;

    public LifecycleConfiguration() {
        //
    }

    public LifecycleConfiguration(final Integer transition, final Integer expiration) {
        this.transition = transition;
        this.expiration = expiration;
    }

    public Integer getTransition() {
        return transition;
    }

    public Integer getExpiration() {
        return expiration;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final LifecycleConfiguration that = (LifecycleConfiguration) o;
        if(expiration != null ? !expiration.equals(that.expiration) : that.expiration != null) {
            return false;
        }
        if(transition != null ? !transition.equals(that.transition) : that.transition != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = transition != null ? transition.hashCode() : 0;
        result = 31 * result + (expiration != null ? expiration.hashCode() : 0);
        return result;
    }
}
