package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.local.LocalFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.symlink.SymlinkResolver;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class RenameExistingFilter extends AbstractDownloadFilter {

    public RenameExistingFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    @Override
    public boolean accept(final Path file) {
        return true;
    }

    /**
     * Rename existing file on disk if there is a conflict.
     */
    @Override
    public TransferStatus prepare(final Path file) {
        Local renamed = file.getLocal();
        while(renamed.exists()) {
            String proposal = MessageFormat.format(Preferences.instance().getProperty("queue.download.file.rename.format"),
                    FilenameUtils.getBaseName(file.getName()),
                    UserDateFormatterFactory.get().getLongFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                    StringUtils.isNotEmpty(file.getExtension()) ? "." + file.getExtension() : StringUtils.EMPTY);
            renamed = LocalFactory.createLocal(renamed.getParent().getAbsolute(), proposal);
        }
        if(!renamed.equals(file.getLocal())) {
            file.getLocal().rename(renamed);
        }
        return super.prepare(file);
    }
}