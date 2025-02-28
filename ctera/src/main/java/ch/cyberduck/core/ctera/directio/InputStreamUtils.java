package ch.cyberduck.core.ctera.directio;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamUtils {

    public static int readFull(InputStream is, byte[] buf, int len) throws IOException {
        int toRead = len;
        int offset = 0;
        int numRead = 0;

        while((numRead = is.read(buf, offset, toRead)) != -1) {
            if(numRead < toRead) {
                toRead -= numRead;
                offset += numRead;
            }
            else {
                return len;
            }
        }

        // We reached EOF before reading len bytes
        if(toRead < len) {
            throw new IOException("Stream is truncated");
        }
        return -1;
    }
}
