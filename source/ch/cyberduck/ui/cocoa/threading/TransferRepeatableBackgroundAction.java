package ch.cyberduck.ui.cocoa.threading;

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferCollection;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.ui.cocoa.TranscriptController;
import ch.cyberduck.ui.cocoa.TransferController;
import ch.cyberduck.ui.cocoa.TransferPromptController;

import java.util.List;

/**
 * @version $Id:$
 */
public class TransferRepeatableBackgroundAction extends AlertRepeatableBackgroundAction {

    private Transfer transfer;
    private boolean resume;
    private boolean reload;

    private TranscriptController transcript;

    public TransferRepeatableBackgroundAction(final TransferController controller,
                                              final Transfer transfer,
                                              final boolean resumeRequested,
                                              final boolean reloadRequested) {
        super(controller);
        this.transcript = controller.getTranscript();
        this.transfer = transfer;
        this.resume = resumeRequested;
        this.reload = reloadRequested;
    }

    @Override
    public void run() throws BackgroundException {
        final TransferOptions options = new TransferOptions();
        options.reloadRequested = reload;
        options.resumeRequested = resume;
        transfer.start(new TransferPrompt() {
            @Override
            public TransferAction prompt() throws BackgroundException {
                return TransferPromptController.create(controller, transfer).prompt();
            }
        }, options);
    }

    @Override
    public void finish() throws BackgroundException {
        super.finish();
        for(Session s : transfer.getSessions()) {
            s.close();
            // We have our own session independent of any browser.
            s.cache().clear();
        }
    }

    @Override
    public void cleanup() {
        final TransferCollection collection = TransferCollection.defaultCollection();
        if(transfer.isComplete() && !transfer.isCanceled() && transfer.isReset()) {
            if(Preferences.instance().getBoolean("queue.removeItemWhenComplete")) {
                collection.remove(transfer);
            }
            if(Preferences.instance().getBoolean("queue.orderBackOnStop")) {
                if(!(collection.numberOfRunningTransfers() > 0)) {
                    controller.window().close();
                }
            }
        }
        collection.save();
    }

    @Override
    public List<Session<?>> getSessions() {
        return transfer.getSessions();
    }

    @Override
    public String getActivity() {
        return transfer.getName();
    }

    @Override
    public void pause() {
        transfer.fireTransferQueued();
        // Upon retry do not suggest to overwrite already completed items from the transfer
        reload = false;
        resume = true;
        super.pause();
        transfer.fireTransferResumed();
    }

    @Override
    public boolean isCanceled() {
        return transfer.isCanceled();
    }

    @Override
    public void log(final boolean request, final String message) {
        if(transcript.isOpen()) {
            controller.invoke(new WindowMainAction(controller) {
                @Override
                public void run() {
                    transcript.log(request, message);
                }
            });
        }
        super.log(request, message);
    }

    private final Object lock = new Object();

    @Override
    public Object lock() {
        // No synchronization with other tasks
        return lock;
    }
}
