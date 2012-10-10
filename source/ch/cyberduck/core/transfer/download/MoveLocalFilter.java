package ch.cyberduck.core.transfer.download;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.transfer.SymlinkResolver;
import ch.cyberduck.ui.DateFormatterFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class MoveLocalFilter extends AbstractDownloadFilter {

    public MoveLocalFilter(final SymlinkResolver symlinkResolver) {
        super(symlinkResolver);
    }

    /**
     * Rename existing file on disk if there is a conflict.
     */
    @Override
    public void prepare(final Path file) {
        Local renamed = file.getLocal();
        while(renamed.exists()) {
            String proposal = MessageFormat.format(Preferences.instance().getProperty("queue.download.file.rename.format"),
                    FilenameUtils.getBaseName(file.getName()),
                    DateFormatterFactory.instance().getLongFormat(System.currentTimeMillis(), false).replace(Path.DELIMITER, ':'),
                    StringUtils.isNotEmpty(file.getExtension()) ? "." + file.getExtension() : StringUtils.EMPTY);
            renamed = LocalFactory.createLocal(renamed.getParent().getAbsolute(), proposal);
        }
        if(!renamed.equals(file.getLocal())) {
            file.getLocal().rename(renamed);
        }
        super.prepare(file);
    }
}