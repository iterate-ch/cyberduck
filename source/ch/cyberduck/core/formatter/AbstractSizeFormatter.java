package ch.cyberduck.core.formatter;

import ch.cyberduck.core.i18n.Locale;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * @version $Id$
 */
public class AbstractSizeFormatter implements SizeFormatter {

    private final long kilo;
    private final long mega;
    private final long giga;

    public AbstractSizeFormatter(final long kilo, final long mega, final long giga) {
        this.kilo = kilo;
        this.mega = mega;
        this.giga = giga;
    }

    @Override
    public String format(final long size) {
        return format(size, false);
    }

    @Override
    public String format(final long size, final boolean plain) {
        return format(size, plain, true);
    }


    @Override
    public String format(final long size, final boolean plain, final boolean bytes) {
        if(-1 == size) {
            return Locale.localizedString("--");
        }
        if(size < kilo) {
            return (int) size + (bytes ? " B" : " bit");
        }
        StringBuilder formatted = new StringBuilder();
        if(size < mega) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(kilo),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " KB" : " kbit");
        }
        else if(size < giga) {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(mega),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " MB" : " Mbit");
        }
        else {
            formatted.append(new BigDecimal(size).divide(new BigDecimal(giga),
                    1,
                    BigDecimal.ROUND_HALF_UP).toString()).append(bytes ? " GB" : " Gbit");
        }
        if(plain) {
            formatted.append(" (").append(NumberFormat.getInstance().format(size)).append(" bytes)");
        }
        return formatted.toString();
    }
}
