namespace System.Collections.Generic;

public static class KeyValuePairPolyfills
{
    public static void Deconstruct<TKey, TValue>(this in KeyValuePair<TKey, TValue> kvp, out TKey key, out TValue value)
    {
        (key, value) = (kvp.Key, kvp.Value);
    }
}
