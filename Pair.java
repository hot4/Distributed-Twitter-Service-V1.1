public class Pair<K, V> {

    private final K key;
    private final V value;

    /**
     * @param key: The type of key maintained by this pair
     * @param value: The type of mapped value
     * @effects Creates new Pair object using arguments
     * */
    public static <K, V> Pair<K, V> createPair(K key, V value) {
        return new Pair<K, V>(key, value);
    }

    /**
     * @param key: The type of key maintained by this pair
     * @param value: The type of mapped value
     * @effects Assigns arguments to private fields
     * @modifies key and value private fields
     * */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    /**
     * @returns Private field key
     * */
    public K getKey() {
        return key;
    }

    /**
     * @returns Private field value
     * */
    public V getValue() {
        return value;
    }

}