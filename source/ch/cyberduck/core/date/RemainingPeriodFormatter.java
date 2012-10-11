package ch.cyberduck.core.date;

import ch.cyberduck.core.i18n.Locale;

import java.math.BigDecimal;
import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public class RemainingPeriodFormatter implements PeriodFormatter {

    /**
     * @param remaining Seconds
     * @return Humean readable string for seconds in hours, minutes or seconds remaining
     */
    @Override
    public String format(final long remaining) {
        StringBuilder b = new StringBuilder();
        if(remaining < 0) {
            // File sizes larger than advertised
            return Locale.localizedString("Unknown");
        }
        if(remaining > 7200) { // More than two hours
            b.append(MessageFormat.format(Locale.localizedString("{0} hours remaining", "Status"),
                    new BigDecimal(remaining).divide(new BigDecimal(3600), 1, BigDecimal.ROUND_DOWN).toString())
            );
        }
        else if(remaining > 120) { // More than two minutes
            b.append(MessageFormat.format(Locale.localizedString("{0} minutes remaining", "Status"),
                    String.valueOf((int) (remaining / 60)))
            );
        }
        else {
            b.append(MessageFormat.format(Locale.localizedString("{0} seconds remaining", "Status"),
                    String.valueOf((int) remaining))
            );
        }
        return b.toString();
    }
}
