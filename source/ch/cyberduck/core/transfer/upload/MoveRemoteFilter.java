package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.DateFormatterFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public class MoveRemoteFilter extends AbstractUploadFilter {

    public MoveRemoteFilter(final UploadSymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path p) {
        if(p.getSession().isRenameSupported(p)) {
            return super.accept(p);
        }
        return false;
    }

    /**
     * Rename existing file on server if there is a conflict.
     */
    @Override
    public void prepare(final Path file) {
        Path renamed = file;
        while(renamed.exists()) {
            String proposal = MessageFormat.format(Preferences.instance().getProperty("queue.upload.file.rename.format"),
                    FilenameUtils.getBaseName(file.getName()),
                    DateFormatterFactory.instance().getLongFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                    StringUtils.isNotEmpty(file.getExtension()) ? "." + file.getExtension() : StringUtils.EMPTY);
            renamed = PathFactory.createPath(file.getSession(), renamed.getParent().getAbsolute(),
                    proposal, file.attributes().getType());
        }
        if(!renamed.equals(file)) {
            file.rename(renamed);
        }
        if(file.attributes().isFile()) {
            file.status().setResume(false);
        }
        super.prepare(file);
    }
}