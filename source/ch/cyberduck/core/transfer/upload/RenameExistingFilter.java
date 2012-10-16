package ch.cyberduck.core.transfer.upload;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class RenameExistingFilter extends AbstractUploadFilter {

    public RenameExistingFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path p) {
        if(!p.getSession().isRenameSupported(p)) {
            return false;
        }
        return super.accept(p);
    }

    /**
     * Rename existing file on server if there is a conflict.
     */
    @Override
    public TransferStatus prepare(final Path file) {
        Path renamed = file;
        while(renamed.exists()) {
            String proposal = MessageFormat.format(Preferences.instance().getProperty("queue.upload.file.rename.format"),
                    FilenameUtils.getBaseName(file.getName()),
                    UserDateFormatterFactory.get().getLongFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                    StringUtils.isNotEmpty(file.getExtension()) ? "." + file.getExtension() : StringUtils.EMPTY);
            renamed = PathFactory.createPath(file.getSession(), renamed.getParent().getAbsolute(),
                    proposal, file.attributes().getType());
        }
        if(!renamed.equals(file)) {
            file.rename(renamed);
        }
        return super.prepare(file);
    }
}