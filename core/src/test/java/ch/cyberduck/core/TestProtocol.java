package ch.cyberduck.core;

public class TestProtocol extends AbstractProtocol {

    private final Scheme scheme;

    public TestProtocol() {
        this(Scheme.http);
    }

    public TestProtocol(final Scheme scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getIdentifier() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "Test";
    }

    @Override
    public Type getType() {
        switch(scheme) {
            case ftp:
            case ftps:
                return Type.ftp;
            case sftp:
                return Type.sftp;
            default:
                return Type.dav;
        }
    }

    @Override
    public Scheme getScheme() {
        return scheme;
    }

    @Override
    public String getPrefix() {
        return "ch.cyberduck.core.Null";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
