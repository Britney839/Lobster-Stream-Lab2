public class MyHashMap<K, V> {
    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] buckets;
    private int size;
    private int capacity;

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        capacity = INITIAL_CAPACITY;
        buckets = new Node[capacity];
        size = 0;
    }

    private int getBucket(K key) {
        return Math.abs(key.hashCode() % capacity);
    }

    public void put(K key, V value) {
        if ((double) size / capacity >= LOAD_FACTOR) {
            resize();
        }
        int idx = getBucket(key);
        Node<K, V> curr = buckets[idx];
        while (curr != null) {
            if (curr.key.equals(key)) {
                curr.value = value;
                return;
            }
            curr = curr.next;
        }
        buckets[idx] = new Node<>(key, value, buckets[idx]);
        size++;
    }

    public V get(K key) {
        int idx = getBucket(key);
        Node<K, V> curr = buckets[idx];
        while (curr != null) {
            if (curr.key.equals(key)) return curr.value;
            curr = curr.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        capacity *= 2;
        Node<K, V>[] newBuckets = new Node[capacity];
        for (int i = 0; i < buckets.length; i++) {
            Node<K, V> curr = buckets[i];
            while (curr != null) {
                Node<K, V> next = curr.next;
                int idx = Math.abs(curr.key.hashCode() % capacity);
                curr.next = newBuckets[idx];
                newBuckets[idx] = curr;
                curr = next;
            }
        }
        buckets = newBuckets;
    }
}
