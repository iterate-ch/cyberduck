package ch.cyberduck.core.formatter;

/**
 * @version $Id$
 */
public class DecimalSizeFormatter extends AbstractSizeFormatter {

    private static final long KILO = 1000; //10^3
    private static final long MEGA = 1000000; // 10^6
    private static final long GIGA = 1000000000; // 10^9

    public DecimalSizeFormatter() {
        super(KILO, MEGA, GIGA);
    }
}
