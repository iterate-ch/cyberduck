package ch.cyberduck.core.date;

/**
 * @version $Id:$
 */
public interface DateDomainService<T> {

    T asDate(final long timestamp, final Instant precision);

}
