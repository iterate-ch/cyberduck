package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.utils.Mimetypes;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @version $Id$
 */
public class MappingMimeTypeService implements MimeTypeService {
    private static final Logger log = Logger.getLogger(MappingMimeTypeService.class);

    private Mimetypes types = Mimetypes.getInstance();

    public MappingMimeTypeService() {
        this.load();
    }

    private void load() {
        try {
            final Enumeration<URL> resources = getClass().getClassLoader().getResources("mime.types");
            while(resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Loading MIME types from %s", url));
                }
                types.loadAndReplaceMimetypes(url.openStream());
            }
        }
        catch(IOException e) {
            log.error("Failure loading mime.types");
        }
    }

    @Override
    public String getMime(final String filename) {
        // Reads from mime.types in classpath
        return types.getMimetype(StringUtils.lowerCase(filename));
    }
}