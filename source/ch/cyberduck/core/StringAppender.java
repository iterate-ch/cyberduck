package ch.cyberduck.core;

import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id:$
 */
public final class StringAppender {

    public StringBuilder append(final StringBuilder buffer, final String message) {
        if(StringUtils.isBlank(message)) {
            return buffer;
        }
        if(buffer.length() > 0) {
            buffer.append(" ");
        }
        buffer.append(StringUtils.trim(message));
        if(buffer.charAt(buffer.length() - 1) == '.') {
            return buffer;
        }
        return buffer.append(".");
    }
}
