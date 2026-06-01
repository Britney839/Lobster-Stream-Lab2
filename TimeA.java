import java.util.*;
import java.io.*;

public class TimeA {
    static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000, 10_000_000};
    static final int OPS = 1_000_000;
    static final Random RAND = new Random(42);
    static long sink = 0;

    // new constants for memory measurement
    static final int MEM_N = 1_000_000; // number of elements to allocate for each structure
    static final int MEM_WAIT_MS = 200;

   public static void main(String[] args) throws IOException, InterruptedException {
    // ---- Part A: timing ----
    PrintWriter csv = new PrintWriter(new FileWriter("C:\\Users\\emily.elliott\\Documents\\Java Lab 2\\Lobster-Stream-Lab2\\timeA.csv"));
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

    // ---- Part B: memory ----
    PrintWriter memCsv = new PrintWriter(new FileWriter("C:\\Users\\emily.elliott\\Documents\\Java Lab 2\\Lobster-Stream-Lab2\\memoryB.csv"));
    memCsv.println("Structure,Elements,BytesPerElement,UsedBefore,UsedAfter");
    System.out.printf("\n%-15s %-12s %-18s %-12s %-12s\n", "Structure", "Elements", "BytesPerElement", "UsedBefore", "UsedAfter");

    measureMemoryArrayList(memCsv);
    measureMemoryLinkedList(memCsv);
    measureMemoryArrayDeque(memCsv);
    measureMemoryHashSet(memCsv);
    measureMemoryTreeSet(memCsv);
    measureMemoryHashMap(memCsv);
    measureMemoryTreeMap(memCsv);
    measureMemoryPriorityQueue(memCsv);

    memCsv.close();
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

    // helper for memory measurements
    static long usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    static void gcAndWait() {
        System.gc();
        System.runFinalization();
        try { Thread.sleep(MEM_WAIT_MS); } catch (InterruptedException e) { /* ignore */ }
    }

    

    // individual measurement methods to ensure single structure at a time and allow nulling
    static void measureMemoryArrayList(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        ArrayList<Integer> list = new ArrayList<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) list.add(new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "ArrayList", MEM_N, bytesPer, before, after);
        memCsv.printf("ArrayList,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        list = null;
        gcAndWait();
    }

    static void measureMemoryLinkedList(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < MEM_N; i++) list.add(new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "LinkedList", MEM_N, bytesPer, before, after);
        memCsv.printf("LinkedList,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        list = null;
        gcAndWait();
    }

    static void measureMemoryArrayDeque(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        ArrayDeque<Integer> dq = new ArrayDeque<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) dq.add(new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "ArrayDeque", MEM_N, bytesPer, before, after);
        memCsv.printf("ArrayDeque,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        dq = null;
        gcAndWait();
    }

    static void measureMemoryHashSet(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        HashSet<Integer> set = new HashSet<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) set.add(new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "HashSet", MEM_N, bytesPer, before, after);
        memCsv.printf("HashSet,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        set = null;
        gcAndWait();
    }

    static void measureMemoryTreeSet(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 0; i < MEM_N; i++) set.add(new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "TreeSet", MEM_N, bytesPer, before, after);
        memCsv.printf("TreeSet,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        set = null;
        gcAndWait();
    }

    static void measureMemoryHashMap(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        HashMap<Integer, Integer> map = new HashMap<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) map.put(new Integer(i), new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "HashMap", MEM_N, bytesPer, before, after);
        memCsv.printf("HashMap,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        map = null;
        gcAndWait();
    }

    static void measureMemoryTreeMap(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        TreeMap<Integer, Integer> map = new TreeMap<>();
        for (int i = 0; i < MEM_N; i++) map.put(new Integer(i), new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "TreeMap", MEM_N, bytesPer, before, after);
        memCsv.printf("TreeMap,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        map = null;
        gcAndWait();
    }

    static void measureMemoryPriorityQueue(PrintWriter memCsv) {
        gcAndWait();
        long before = usedMemory();
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for (int i = 0; i < MEM_N; i++) pq.add(new Integer(i));
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "PriorityQueue", MEM_N, bytesPer, before, after);
        memCsv.printf("PriorityQueue,%d,%.2f,%d,%d\n", MEM_N, bytesPer, before, after);
        pq = null;
        gcAndWait();
    }
}