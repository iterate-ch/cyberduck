package ch.cyberduck.core.smb;

import java.util.EnumSet;

import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;

import ch.cyberduck.core.AbstractPath;
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
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        AttributedList<Path> result = new AttributedList<>();
        for(FileIdBothDirectoryInformation f : session.share.list(directory.getAbsolute())) {
            if((f.getFileAttributes() & FileAttributes.FILE_ATTRIBUTE_DIRECTORY.getValue()) != 0) {
                result.add(new Path(directory.getAbsolute() + f.getFileName(), EnumSet.of(AbstractPath.Type.directory)));
            }
            else {
                result.add(new Path(directory.getAbsolute() + f.getFileName(), EnumSet.of(AbstractPath.Type.file)));
            }
        }
        return result;
    }
    
}
