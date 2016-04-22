package ch.cyberduck.core.ftp;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.io.IOException;
import java.util.List;

public interface FTPDataResponseReader {

    AttributedList<Path> read(Path parent, List<String> replies, ListProgressListener listener)
            throws IOException, FTPInvalidListException, ConnectionCanceledException;
}
