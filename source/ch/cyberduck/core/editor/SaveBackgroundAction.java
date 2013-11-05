package ch.cyberduck.core.editor;

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.transfer.upload.AbstractUploadFilter;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;
import ch.cyberduck.ui.action.SingleTransferWorker;
import ch.cyberduck.ui.action.Worker;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public class SaveBackgroundAction extends Worker {
    private static final Logger log = Logger.getLogger(SaveBackgroundAction.class);

    private AbstractEditor editor;

    public SaveBackgroundAction(final AbstractEditor editor) {
        this.editor = editor;
    }

    @Override
    public Transfer run() throws BackgroundException {
        final Transfer upload = new UploadTransfer(editor.session.getHost(), editor.getEdited()) {
            @Override
            public TransferAction action(final Session<?> session,
                                         final boolean resumeRequested, final boolean reloadRequested,
                                         final TransferPrompt prompt) throws BackgroundException {
                return TransferAction.overwrite;
            }

            @Override
            public AbstractUploadFilter filter(final Session<?> session, final TransferAction action) {
                return super.filter(session, action).withOptions(new UploadFilterOptions()
                        .withTemporary(Preferences.instance().getBoolean("editor.upload.temporary"))
                        .withPermission(Preferences.instance().getBoolean("editor.upload.permissions.change")));
            }
        };
        final SingleTransferWorker worker
                = new SingleTransferWorker(editor.session, upload, new TransferOptions(), new DisabledTransferPrompt(), new DisabledTransferErrorCallback());
        worker.run();
        if(upload.isComplete()) {
            // Update known remote file size
            editor.getEdited().attributes().setSize(upload.getTransferred());
            if(editor.isClosed()) {
                editor.delete();
            }
            editor.setModified(false);
        }
        return upload;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"),
                editor.getEdited().getName());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        SaveBackgroundAction that = (SaveBackgroundAction) o;
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
