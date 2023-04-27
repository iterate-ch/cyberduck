package ch.cyberduck.core.smb;

import java.util.EnumSet;

import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.exception.BackgroundException;

public class SMBListService implements ListService {

    private final SMBSession session;
    
    public SMBListService(SMBSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        String path = directory.getAbsolute();

            for (FileIdBothDirectoryInformation f : session.share.list(path)) {
                long attr = f.getFileAttributes();
                if (attr == 16) { // is a directory
                    children.add(new Path(directory, f.getFileName(), EnumSet.of(Type.directory)));
                } else if (attr == 32)  { // is a file
                    children.add(new Path(directory, f.getFileName(), EnumSet.of(Type.file)));
                }
            }

        return children;
    }
    
}
