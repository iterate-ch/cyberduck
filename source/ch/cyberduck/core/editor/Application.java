package ch.cyberduck.core.editor;

/**
 * @version $Id:$
 */
public class Application {

    private String identifier;
    private String name;

    public Application(final String identifier, final String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        final Application that = (Application) o;

        if(identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }
}
