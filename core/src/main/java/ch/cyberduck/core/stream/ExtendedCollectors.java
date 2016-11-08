package ch.cyberduck.core.stream;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Created by alive on 08.11.2016.
 */
public final class ExtendedCollectors {
    private ExtendedCollectors() throws Exception {
        throw new Exception("Invalid operation");
    }

    /**
     * http://stackoverflow.com/a/32648397
     * @param keyMapper
     * @param valueMapper
     * @param <T>
     * @param <K>
     * @param <U>
     * @return
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toList(),
                list -> {
                    Map<K, U> result = new HashMap<>();
                    for (T item : list) {
                        K key = keyMapper.apply(item);
                        if (result.putIfAbsent(key, valueMapper.apply(item)) != null) {
                            throw new IllegalStateException(String.format("Duplicate key %s", key));
                        }
                    }
                    return result;
                });
    }
}
