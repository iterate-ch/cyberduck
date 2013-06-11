package ch.cyberduck.core.exception;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;

/**
 * @version $Id$
 */
public abstract class AbstractIOExceptionMappingService<T> implements IOExceptionMappingService<T> {

    @Override
    public IOException map(final T exception) {
        return this.map(null, exception);
    }

    protected StringBuilder append(final StringBuilder buffer, final String message) {
        if(StringUtils.isBlank(message)) {
            return buffer;
        }
        if(buffer.length() > 0) {
            buffer.append(" ");
        }
        buffer.append(message);
        if(buffer.charAt(buffer.length() - 1) == '.') {
            return buffer;
        }
        return buffer.append(".");
    }
}
