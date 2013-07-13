package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public class NullProtocol extends AbstractProtocol {

    @Override
    public String getIdentifier() {
        return "null";
    }

    @Override
    public String getDescription() {
        return StringUtils.EMPTY;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.http;
    }
}
