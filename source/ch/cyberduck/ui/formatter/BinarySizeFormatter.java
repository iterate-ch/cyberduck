package ch.cyberduck.ui.formatter;

/**
 * @version $Id:$
 */
public class BinarySizeFormatter extends AbstractSizeFormatter {

    private static final long KILO = 1024; //2^10
    private static final long MEGA = 1048576; // 2^20
    private static final long GIGA = 1073741824; // 2^30

    public BinarySizeFormatter() {
        super(KILO, MEGA, GIGA);
    }
}
