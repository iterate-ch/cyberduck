package ch.cyberduck.core.threading;

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProgressListener;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @version $Id$
 */
public class BackgroundActionPauser {
    private static final Logger log = Logger.getLogger(BackgroundActionPauser.class);

    private SessionBackgroundAction action;

    /**
     * The delay to wait before execution of the action in seconds
     */
    private int delay
            = Preferences.instance().getInteger("connection.retry.delay");

    private final String pattern
            = LocaleFactory.localizedString("Retry again in {0} seconds ({1} more attempts)", "Status");

    public BackgroundActionPauser(final SessionBackgroundAction action) {
        this.action = action;
    }

    public void await(final ProgressListener listener) {
        final Timer wakeup = new Timer();
        final CyclicBarrier wait = new CyclicBarrier(2);

        wakeup.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(0 == delay || action.isCanceled()) {
                    // Cancel the timer repetition
                    this.cancel();
                    return;
                }
                listener.message(MessageFormat.format(pattern, delay--, action.retry()));
            }

            @Override
            public boolean cancel() {
                try {
                    // Notifiy to return to caller from #pause()
                    wait.await();
                }
                catch(InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                catch(BrokenBarrierException e) {
                    log.error(e.getMessage(), e);
                }
                return super.cancel();
            }
        }, 0, 1000); // Schedule for immediate execusion with an interval of 1s
        try {
            // Wait for notify from wakeup timer
            wait.await();
        }
        catch(InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        catch(BrokenBarrierException e) {
            log.error(e.getMessage(), e);
        }
    }
}
