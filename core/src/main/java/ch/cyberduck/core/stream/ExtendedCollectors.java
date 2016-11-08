package ch.cyberduck.core.stream;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

public final class ExtendedCollectors {
    /**
     * http://stackoverflow.com/a/32648397
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toList(),
                list -> {
                    Map<K, U> result = new HashMap<>();
                    for(T item : list) {
                        K key = keyMapper.apply(item);
                        if(result.putIfAbsent(key, valueMapper.apply(item)) != null) {
                            throw new IllegalStateException(String.format("Duplicate key %s", key));
                        }
                    }
                    return result;
                });
    }
}
