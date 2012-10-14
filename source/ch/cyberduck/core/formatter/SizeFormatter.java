package ch.cyberduck.core.formatter;

/**
 * @version $Id$
 */
public interface SizeFormatter {

    /**
     * Rounding mode to round towards "nearest neighbor" unless both
     * neighbors are equidistant, in which case round up.
     *
     * @param size Number of bytes
     * @return The size of the file using BigDecimal.ROUND_HALF_UP rounding
     */
    String format(long size);

    /**
     * @param size  Bytes
     * @param plain Include plain format of bytes
     * @return Formatted size
     */
    String format(long size, boolean plain);

    /**
     * @param size  Bytes
     * @param bytes Report file size as bytes or bits.
     * @param plain Include plain format of bytes
     * @return Formatted size
     */
    String format(long size, boolean bytes, boolean plain);
}
