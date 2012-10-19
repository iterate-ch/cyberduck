package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;
import org.jets3t.service.utils.Mimetypes;

/**
 * @version $Id$
 */
public class MappingMimeTypeService implements MimeTypeService {

    @Override
    public String getMime(final String filename) {
        // Reads from mime.types in classpath
        return Mimetypes.getInstance().getMimetype(StringUtils.lowerCase(filename));
    }
}