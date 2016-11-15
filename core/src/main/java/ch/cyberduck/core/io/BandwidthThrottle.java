package ch.cyberduck.core.io;

import org.apache.log4j.Logger;

/**
 * Limits throughput of a stream to at most N bytes per T seconds.  Mutable and
 * thread-safe.<p>
 * <p/>
 * In the following example, <tt>throttle</tt> is used to send the contents of
 * <tt>buf</tt> to <tt>out</tt> at no more than <tt>N/T</tt> bytes per second:
 * <pre>
 *      BandwidthThrottle throttle=new BandwidthThrottle(N, T);
 *      OutputStream out=...;
 *      byte[] buf=...;
 *      for (int i=0; i<buf.length; ) {
 *          int allowed=throttle.request(buf.length-i);
 *          out.write(buf, i, allowed);
 *          i+=allowed;
 *      }
 * </pre>
 * <p/>
 * This class works by allowing exactly N bytes to be sent every T seconds.  If
 * the number of bytes for a given window have been exceeded, subsequent calls
 * to request(..) will block.  The default value of T is 100 milliseconds.
 * Smaller window values T allow fairer bandwidth sharing and less noticeable
 * pauses but may decrease efficiency slightly.<p>
 * <p/>
 * Note that throttles are <i>not</i> cumulative.  In the future, this may allow
 * enable fancier control.  Also, BandwidthThrottle may be able delegate to
 * other throttles.  This would allow, for example, a 15 KB/s Gnutella messaging
 * throttle, with no more than 10 KB/s devoted to uploads.<p>
 * <p/>
 * This implementation is based on the <a href="http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/freenet/freenet/src/freenet/support/io/Bandwidth.java">Bandwidth</a>
 * class from
 * the Freenet project.  It has been simplified and better documented.<p>

 */
public final class BandwidthThrottle {
    private static final Logger log = Logger.getLogger(BandwidthThrottle.class);

    /**
     * The number of windows per second.
     */
    private static final int TICKS_PER_SECOND = 10;
    /**
     * The value of T, in milliseconds.
     */
    private static final int MILLIS_PER_TICK = 1000 / TICKS_PER_SECOND;

    /**
     * The bytes to send per tick.  Modified by setThrottle.
     */
    private volatile int bytesPerTick;

    /**
     * Whether or not we're only allowing bandwidth to be used every other
     * second.
     */
    private volatile boolean switching = false;

    /**
     * The number of bytes remaining in this window.
     */
    private int availableBytes;
    /**
     * The system time when the window is reset so more bytes can be sent.
     */
    private long nextTickTime;

    /**
     * Creates a new bandwidth throttle at the given throttle rate.
     * The default windows size T is used.  The bytes per windows N
     * is calculated from bytesPerSecond.
     *
     * @param bytesPerSecond the limits in bytes (not bits!) per second
     *                       (not milliseconds!)
     */
    public BandwidthThrottle(float bytesPerSecond) {
        this.setRate(bytesPerSecond);
    }

    /**
     * Creates a new bandwidth throttle at the given throttle rate,
     * only allowing bandwidth to be used every other second if
     * switching is true.
     * The default windows size T is used.  The bytes per windows N
     * is calculated from bytesPerSecond.
     *
     * @param bytesPerSecond the limits in bytes (not bits!) per second
     *                       (not milliseconds!)
     * @param switching      true if we should only allow bandwidth to be used
     *                       every other second.
     */
    public BandwidthThrottle(float bytesPerSecond, boolean switching) {
        this.setRate(bytesPerSecond);
        this.setSwitching(switching);

    }

    /**
     * No throttling
     */
    public static final int UNLIMITED = -1;

    /**
     * Bytes per second allowed
     */
    private float rate = UNLIMITED;

    /**
     * Sets the throttle to the given throttle rate.  The default windows size
     * T is used.  The bytes per windows N is calculated from bytesPerSecond.
     *
     * @param bytesPerSecond the limits in bytes (not bits!) per second
     *                       (not milliseconds!)
     */
    public void setRate(float bytesPerSecond) {
        if(bytesPerSecond < 0) {
            rate = UNLIMITED;
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Set rate to %s bytes per second", bytesPerSecond));
            }
            rate = bytesPerSecond;
            bytesPerTick = (int) (bytesPerSecond / TICKS_PER_SECOND);
        }
        if(switching) {
            this.fixBytesPerTick(true);
        }
    }

    /**
     * @return Transfer rate in bytes per second allowed by this throttle
     */
    public float getRate() {
        return rate;
    }

    /**
     * Sets whether or not this throttle is switching bandwidth on/off.
     */
    public void setSwitching(boolean switching) {
        log.debug("setSwitching:" + switching);
        if(this.switching != switching) {
            fixBytesPerTick(switching);
        }
        this.switching = switching;
    }

    /**
     * Modifies bytesPerTick to either be double or half of what it was.
     * This is necessary because of the 'switching', which can effectively
     * reduce or raise the amount of data transferred.
     */
    private void fixBytesPerTick(boolean raise) {
        int newBytesPerTick = bytesPerTick;
        if(raise) {
            newBytesPerTick *= 2;
        }
        else {
            newBytesPerTick /= 2;
        }
        if(newBytesPerTick < 0) // overflowed?
        {
            newBytesPerTick = Integer.MAX_VALUE;
        }
        bytesPerTick = newBytesPerTick;
    }

    /**
     * Blocks until the caller can send at least one byte without violating
     * bandwidth constraints.  Records the number of byte sent.
     *
     * @param desired the number of bytes the caller would like to send
     * @return the number of bytes the sender is expected to send, which
     *         is always greater than one and less than or equal to desired
     */
    public synchronized int request(int desired) {
        if(UNLIMITED == rate) {
            return desired;
        }
        waitForBandwidth();
        int result = Math.min(desired, availableBytes);
        availableBytes -= result;
        return result;
    }

    /**
     * Waits until data is _availableBytes.
     */
    private void waitForBandwidth() {
        while(true) {
            long now = System.currentTimeMillis();
            updateWindow(now);
            if(availableBytes != 0) {
                break;
            }
            try {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Throttling bandwidth for %d milliseconds", nextTickTime - now));
                }
                Thread.sleep(nextTickTime - now);
            }
            catch(InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Updates _availableBytes and _nextTickTime if possible.
     */
    private void updateWindow(long now) {
        if(now >= nextTickTime) {
            if(!switching || ((now / 1000) % 2) == 0) {
                availableBytes = bytesPerTick;
                nextTickTime = now + MILLIS_PER_TICK;
            }
            else {
                availableBytes = 0;
                // the next tick time is the time we'll hit
                // the next second.
                long diff = 1000 - (now % 1000);
                nextTickTime = now + diff;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof BandwidthThrottle)) {
            return false;
        }
        BandwidthThrottle that = (BandwidthThrottle) o;
        if(Float.compare(that.rate, rate) != 0) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (rate != +0.0f ? Float.floatToIntBits(rate) : 0);
    }
}