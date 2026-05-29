import java.util.*;
import java.io.*;

public class TimeA {
    static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000, 10_000_000};
    static final int OPS = 1_000_000;
    static final Random RAND = new Random(42);
    static long sink = 0;

    public static void main(String[] args) throws IOException {
        PrintWriter csv = new PrintWriter(new FileWriter("timeA.csv"));
        csv.println("Structure,Operation,Size,NanosecondsPerOp");
        System.out.printf("%-15s %-15s %-10s %-20s %-10s\n", "Structure", "Operation", "Size", "ns/op", "Ratio");
        for (TestCase test : TestCase.values()) {
            long prev = -1;
            for (int n : SIZES) {
                long ns = test.run(n, csv);
                double ratio = prev > 0 ? (double) ns / prev : 0;
                String ratioStr = prev > 0 ? String.format("%.2f", ratio) : "-";
                System.out.printf("%-15s %-15s %-10d %-20d %-10s\n", test.structure, test.operation, n, ns, ratioStr);
                prev = ns;
            }
            System.out.println();
        }
        csv.close();
        System.out.println("sink=" + sink);
    }

    enum TestCase {
        ARRAYLIST_GET("ArrayList", "get(index)") {
            long run(int n, PrintWriter csv) {
                ArrayList<Integer> list = new ArrayList<>(n);
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.get(RAND.nextInt(n)));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += list.get(RAND.nextInt(n));
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("ArrayList,get(index),%d,%d\n", n, ns);
                return ns;
            }
        },
        ARRAYLIST_ADD_END("ArrayList", "add-at-end") {
            long run(int n, PrintWriter csv) {
                ArrayList<Integer> list = new ArrayList<>(n);
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("ArrayList,add-at-end,%d,%d\n", n, ns);
                return ns;
            }
        },
        ARRAYLIST_ADD_FRONT("ArrayList", "add-at-front") {
            long run(int n, PrintWriter csv) {
                ArrayList<Integer> list = new ArrayList<>(n);
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.add(0, RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.add(0, RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("ArrayList,add-at-front,%d,%d\n", n, ns);
                return ns;
            }
        },
        LINKEDLIST_GET("LinkedList", "get(index)") {
            long run(int n, PrintWriter csv) {
                LinkedList<Integer> list = new LinkedList<>();
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.get(RAND.nextInt(n)));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += list.get(RAND.nextInt(n));
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("LinkedList,get(index),%d,%d\n", n, ns);
                return ns;
            }
        },
        LINKEDLIST_ADD_END("LinkedList", "add-at-end") {
            long run(int n, PrintWriter csv) {
                LinkedList<Integer> list = new LinkedList<>();
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("LinkedList,add-at-end,%d,%d\n", n, ns);
                return ns;
            }
        },
        LINKEDLIST_ADD_FRONT("LinkedList", "add-at-front") {
            long run(int n, PrintWriter csv) {
                LinkedList<Integer> list = new LinkedList<>();
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.addFirst(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.addFirst(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("LinkedList,add-at-front,%d,%d\n", n, ns);
                return ns;
            }
        },
        ARRAYDEQUE_ADD_END("ArrayDeque", "add-at-end") {
            long run(int n, PrintWriter csv) {
                ArrayDeque<Integer> dq = new ArrayDeque<>(n);
                for (int i = 0; i < n; i++) dq.add(RAND.nextInt());
                warmup(() -> dq.addLast(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) dq.addLast(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("ArrayDeque,add-at-end,%d,%d\n", n, ns);
                return ns;
            }
        },
        ARRAYDEQUE_ADD_FRONT("ArrayDeque", "add-at-front") {
            long run(int n, PrintWriter csv) {
                ArrayDeque<Integer> dq = new ArrayDeque<>(n);
                for (int i = 0; i < n; i++) dq.add(RAND.nextInt());
                warmup(() -> dq.addFirst(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) dq.addFirst(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("ArrayDeque,add-at-front,%d,%d\n", n, ns);
                return ns;
            }
        },
        HASHSET_CONTAINS("HashSet", "contains") {
            long run(int n, PrintWriter csv) {
                HashSet<Integer> set = new HashSet<>(n);
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.contains(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += set.contains(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("HashSet,contains,%d,%d\n", n, ns);
                return ns;
            }
        },
        HASHSET_ADD("HashSet", "add") {
            long run(int n, PrintWriter csv) {
                HashSet<Integer> set = new HashSet<>(n);
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) set.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("HashSet,add,%d,%d\n", n, ns);
                return ns;
            }
        },
        TREESET_CONTAINS("TreeSet", "contains") {
            long run(int n, PrintWriter csv) {
                TreeSet<Integer> set = new TreeSet<>();
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.contains(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += set.contains(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("TreeSet,contains,%d,%d\n", n, ns);
                return ns;
            }
        },
        TREESET_ADD("TreeSet", "add") {
            long run(int n, PrintWriter csv) {
                TreeSet<Integer> set = new TreeSet<>();
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) set.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("TreeSet,add,%d,%d\n", n, ns);
                return ns;
            }
        },
        HASHMAP_PUT("HashMap", "put") {
            long run(int n, PrintWriter csv) {
                HashMap<Integer, Integer> map = new HashMap<>(n);
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.put(RAND.nextInt(), RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) map.put(RAND.nextInt(), RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("HashMap,put,%d,%d\n", n, ns);
                return ns;
            }
        },
        HASHMAP_GET("HashMap", "get") {
            long run(int n, PrintWriter csv) {
                HashMap<Integer, Integer> map = new HashMap<>(n);
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.get(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.get(RAND.nextInt()) != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("HashMap,get,%d,%d\n", n, ns);
                return ns;
            }
        },
        HASHMAP_CONTAINS("HashMap", "containsKey") {
            long run(int n, PrintWriter csv) {
                HashMap<Integer, Integer> map = new HashMap<>(n);
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.containsKey(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.containsKey(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("HashMap,containsKey,%d,%d\n", n, ns);
                return ns;
            }
        },
        TREEMAP_PUT("TreeMap", "put") {
            long run(int n, PrintWriter csv) {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.put(RAND.nextInt(), RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) map.put(RAND.nextInt(), RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("TreeMap,put,%d,%d\n", n, ns);
                return ns;
            }
        },
        TREEMAP_GET("TreeMap", "get") {
            long run(int n, PrintWriter csv) {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.get(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.get(RAND.nextInt()) != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("TreeMap,get,%d,%d\n", n, ns);
                return ns;
            }
        },
        TREEMAP_CONTAINS("TreeMap", "containsKey") {
            long run(int n, PrintWriter csv) {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.containsKey(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.containsKey(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("TreeMap,containsKey,%d,%d\n", n, ns);
                return ns;
            }
        },
        PRIORITYQUEUE_OFFER("PriorityQueue", "offer") {
            long run(int n, PrintWriter csv) {
                PriorityQueue<Integer> pq = new PriorityQueue<>();
                for (int i = 0; i < n; i++) pq.offer(RAND.nextInt());
                warmup(() -> pq.offer(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) pq.offer(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("PriorityQueue,offer,%d,%d\n", n, ns);
                return ns;
            }
        },
        PRIORITYQUEUE_POLL("PriorityQueue", "poll") {
            long run(int n, PrintWriter csv) {
                PriorityQueue<Integer> pq = new PriorityQueue<>();
                for (int i = 0; i < n; i++) pq.offer(RAND.nextInt());
                warmup(() -> pq.poll());
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += pq.poll() != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("PriorityQueue,poll,%d,%d\n", n, ns);
                return ns;
            }
        },
        PRIORITYQUEUE_PEEK("PriorityQueue", "peek") {
            long run(int n, PrintWriter csv) {
                PriorityQueue<Integer> pq = new PriorityQueue<>();
                for (int i = 0; i < n; i++) pq.offer(RAND.nextInt());
                warmup(() -> pq.peek());
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += pq.peek() != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.printf("PriorityQueue,peek,%d,%d\n", n, ns);
                return ns;
            }
        };

        final String structure, operation;
        TestCase(String structure, String operation) {
            this.structure = structure;
            this.operation = operation;
        }
        abstract long run(int n, PrintWriter csv);
    }

    static void warmup(Runnable op) {
        for (int i = 0; i < OPS; i++) op.run();
    }
}
