using System.Collections.Generic;

namespace System.Collections
{
    public static class DictionaryExtensions
    {
        public static TValue Lookup<TValue, TKey>(this IDictionary<TKey, TValue> dictionary, TKey key)
        {
            return dictionary[key];
        }
    }
}
