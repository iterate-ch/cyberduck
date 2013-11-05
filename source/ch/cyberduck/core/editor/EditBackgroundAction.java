package ch.cyberduck.core.editor;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.ui.action.SingleTransferWorker;
import ch.cyberduck.ui.action.Worker;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public class EditBackgroundAction extends Worker {
    private static final Logger log = Logger.getLogger(EditBackgroundAction.class);

    private AbstractEditor editor;

    public EditBackgroundAction(AbstractEditor editor) {
        this.editor = editor;
    }

    @Override
    public Transfer run() throws BackgroundException {
        // Delete any existing file which might be used by a watch editor already
        final TransferOptions options = new TransferOptions();
        options.quarantine = false;
        options.open = false;
        final Transfer download = new DownloadTransfer(editor.session.getHost(), editor.edited) {
            @Override
            public TransferAction action(final Session<?> session, final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt) throws BackgroundException {
                return TransferAction.trash;
            }
        };
        final SingleTransferWorker worker
                = new SingleTransferWorker(editor.session, download, options, new DisabledTransferPrompt(), new DisabledTransferErrorCallback());
        worker.run();
        if(download.isComplete()) {
            // Save checksum before edit
            editor.checksum = editor.local.attributes().getChecksum();
            final Permission permissions = editor.local.attributes().getPermission();
            // Update local permissions to make sure the file is readable and writable for editing.
            permissions.setUser(permissions.getUser().or(Permission.Action.read).or(Permission.Action.write));
            if(!permissions.equals(editor.local.attributes().getPermission())) {
                editor.local.writeUnixPermission(permissions);
            }
            try {
                editor.edit();
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        else {
            log.warn(String.format("Skip opening file %s with incomplete transfer %s", editor.edited, download));
        }
        return download;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Downloading {0}", "Status"),
                editor.edited.getName());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        EditBackgroundAction that = (EditBackgroundAction) o;
        if(editor != null ? !editor.equals(that.editor) : that.editor != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return editor != null ? editor.hashCode() : 0;
    }
}
