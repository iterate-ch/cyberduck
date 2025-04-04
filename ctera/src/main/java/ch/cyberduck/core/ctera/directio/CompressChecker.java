package ch.cyberduck.core.ctera.directio;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CompressChecker {

    private static final byte[] GZIP_MAGIC = {0x1F, (byte) 0x8B};
    private static final byte[] SNAPPY_MAGIC = {-126, 83, 78, 65, 80, 80, 89, 0};


    public static Pair<InputStream, CompressChecker.CompressionType> detectCompressionType(InputStream inputStream)
            throws IOException {

        InputStream inputStreamWithMark;
        if(!inputStream.markSupported()) {
            inputStreamWithMark = new BufferedInputStream(inputStream);
        }
        else {
            inputStreamWithMark = inputStream;
        }
        inputStreamWithMark.mark(SNAPPY_MAGIC.length); // Mark the current position in the stream

        byte[] header = new byte[SNAPPY_MAGIC.length];
        int bytesRead = inputStreamWithMark.read(header);

        inputStreamWithMark.reset(); // Reset the stream to the marked position

        CompressionType compType = CompressionType.UNKNOWN;
        if(bytesRead < GZIP_MAGIC.length) {
            return new ImmutablePair<>(null, null);
        }

        if(startsWith(header, GZIP_MAGIC)) {
            compType = CompressionType.GZIP;
        }
        else if(startsWith(header, SNAPPY_MAGIC)) {
            compType = CompressionType.SNAPPY;
        }
        else {
            compType = CompressionType.UNKNOWN;
        }
        return new ImmutablePair<>(inputStreamWithMark, compType);
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if(data.length < prefix.length) {
            return false;
        }
        for(int i = 0; i < prefix.length; i++) {
            if(data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    enum CompressionType {
        GZIP,
        SNAPPY,
        UNKNOWN
    }
}


