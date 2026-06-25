import java.util.*;
import java.io.*;

public class TimeA {
    static int[] SIZES = {1_000, 10_000, 100_000};
    static int OPS = 1_000_000;
    static final Random RAND = new Random(42);
    static long sink = 0;

    static int MEM_N = 1_000_000;
    static int WARMUP = 10_000;
    static final int MEM_WAIT_MS = 200;
    static final String BASE_PATH = System.getProperty("user.dir") + File.separator;

    static void updateWarmup() {
        // keep warmup reasonable relative to OPS (e.g. max OPS/10, at least 1)
        int cap = Math.max(1, OPS / 10);
        WARMUP = Math.max(1, Math.min(WARMUP, cap));
        // avoid excessively large warmups
        WARMUP = Math.min(WARMUP, 100_000);
    }

    // Adjust OPS based on size to keep runtime reasonable
    static int getOpsForSize(int n) {
        if (n >= 100_000) return 50_000;      // 100k+ sizes: fewer ops
        if (n >= 10_000)  return 100_000;     // 10k sizes: moderate ops
        return 1_000_000;                      // 1k sizes: full ops
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        for (String a : args) {
            if ("--fast".equals(a)) {
                OPS = 100_000;
                MEM_N = 100_000;
                WARMUP = 1_000;
                SIZES = new int[]{1_000};
            }
        }
        updateWarmup();

        // ---- Part A: timing ----
        BufferedWriter csv = new BufferedWriter(new FileWriter(BASE_PATH + "timeA.csv"));
        csv.write("Structure,Operation,Size,NanosecondsPerOp");
        csv.newLine();
        System.out.printf("%-15s %-15s %-10s %-20s %-10s\n", "Structure", "Operation", "Size", "ns/op", "Ratio");
        for (TestCase test : TestCase.values()) {
            long prev = -1;
            for (int n : SIZES) {
                if (!test.shouldRun(n)) {
                    System.out.printf("%-15s %-15s %-10d (skipped - O(n) at large size)\n", test.structure, test.operation, n);
                    prev = -1;
                    continue;
                }
                // Set OPS for this size and update warmup
                int originalOps = OPS;
                OPS = getOpsForSize(n);
                updateWarmup();
                
                long ns = test.run(n, csv);
                
                OPS = originalOps;
                updateWarmup();
                
                double ratio = prev > 0 ? (double) ns / prev : 0;
                String ratioStr = prev > 0 ? String.format("%.2f", ratio) : "-";
                System.out.printf("%-15s %-15s %-10d %-20d %-10s\n", test.structure, test.operation, n, ns, ratioStr);
                prev = ns;
            }
            System.out.println();
        }
        csv.flush();
        csv.close();
        System.out.println("timeA.csv written");
        System.out.println("sink=" + sink);

        // ---- Part B: memory ----
        BufferedWriter memCsv = new BufferedWriter(new FileWriter(BASE_PATH + "memoryB.csv"));
        memCsv.write("Structure,Elements,BytesPerElement,UsedBefore,UsedAfter");
        memCsv.newLine();
        System.out.printf("\n%-15s %-12s %-18s %-12s %-12s\n", "Structure", "Elements", "BytesPerElement", "UsedBefore", "UsedAfter");

        measureMemoryArrayList(memCsv);
        measureMemoryLinkedList(memCsv);
        measureMemoryArrayDeque(memCsv);
        measureMemoryHashSet(memCsv);
        measureMemoryTreeSet(memCsv);
        measureMemoryHashMap(memCsv);
        measureMemoryTreeMap(memCsv);
        measureMemoryPriorityQueue(memCsv);

        memCsv.flush();
        memCsv.close();
        System.out.println("memoryB.csv written");

        // ---- Part D: compare my collections vs JDK ----
        BufferedWriter compCsv = new BufferedWriter(new FileWriter(BASE_PATH + "compareD.csv"));
        compCsv.write("Structure,Operation,Size,NanosecondsPerOp");
        compCsv.newLine();

        int[] compSizes = {1_000, 10_000, 100_000, 1_000_000};

        System.out.println("\n---- Part D: MyArrayList vs ArrayList ----");
        for (int n : compSizes) {
            // JDK ArrayList get
            ArrayList<Integer> jdkList = new java.util.ArrayList<Integer>(n);
            for (int i = 0; i < n; i++) jdkList.add(RAND.nextInt());
            warmup(() -> jdkList.get(RAND.nextInt(n)));
            long t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) sink += jdkList.get(RAND.nextInt(n));
            long ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("ArrayList,get,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("ArrayList        get          n=%-10d %d ns/op%n", n, ns);

            // MyArrayList get
            MyArrayList<Integer> myList = new MyArrayList<>();
            for (int i = 0; i < n; i++) myList.add(RAND.nextInt());
            warmup(() -> myList.get(RAND.nextInt(n)));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) sink += myList.get(RAND.nextInt(n));
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("MyArrayList,get,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("MyArrayList      get          n=%-10d %d ns/op%n", n, ns);

            // JDK ArrayList add
            ArrayList<Integer> jdkList2 = new java.util.ArrayList<Integer>(n);
            for (int i = 0; i < n; i++) jdkList2.add(RAND.nextInt());
            warmup(() -> jdkList2.add(RAND.nextInt()));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) jdkList2.add(RAND.nextInt());
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("ArrayList,add,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("ArrayList        add          n=%-10d %d ns/op%n", n, ns);

            // MyArrayList add
            MyArrayList<Integer> myList2 = new MyArrayList<>();
            for (int i = 0; i < n; i++) myList2.add(RAND.nextInt());
            warmup(() -> myList2.add(RAND.nextInt()));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) myList2.add(RAND.nextInt());
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("MyArrayList,add,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("MyArrayList      add          n=%-10d %d ns/op%n", n, ns);
        }

        System.out.println("\n---- Part D: MyHashMap vs HashMap ----");
        for (int n : compSizes) {
            // JDK HashMap get
            HashMap<Integer, Integer> jdkMap = new HashMap<>(n);
            for (int i = 0; i < n; i++) jdkMap.put(RAND.nextInt(), RAND.nextInt());
            warmup(() -> jdkMap.get(RAND.nextInt()));
            long t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) { Integer v = jdkMap.get(RAND.nextInt()); if (v != null) sink += v; }
            long ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("HashMap,get,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("HashMap          get          n=%-10d %d ns/op%n", n, ns);

            // MyHashMap get
            MyHashMap<Integer, Integer> myMap = new MyHashMap<>();
            for (int i = 0; i < n; i++) myMap.put(RAND.nextInt(), RAND.nextInt());
            warmup(() -> myMap.get(RAND.nextInt()));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) { Integer v = myMap.get(RAND.nextInt()); if (v != null) sink += v; }
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("MyHashMap,get,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("MyHashMap        get          n=%-10d %d ns/op%n", n, ns);

            // JDK HashMap put
            HashMap<Integer, Integer> jdkMap2 = new HashMap<>(n);
            for (int i = 0; i < n; i++) jdkMap2.put(RAND.nextInt(), RAND.nextInt());
            warmup(() -> jdkMap2.put(RAND.nextInt(), RAND.nextInt()));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) jdkMap2.put(RAND.nextInt(), RAND.nextInt());
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("HashMap,put,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("HashMap          put          n=%-10d %d ns/op%n", n, ns);

            // MyHashMap put
            MyHashMap<Integer, Integer> myMap2 = new MyHashMap<>();
            for (int i = 0; i < n; i++) myMap2.put(RAND.nextInt(), RAND.nextInt());
            warmup(() -> myMap2.put(RAND.nextInt(), RAND.nextInt()));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) myMap2.put(RAND.nextInt(), RAND.nextInt());
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("MyHashMap,put,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("MyHashMap        put          n=%-10d %d ns/op%n", n, ns);

            // JDK HashMap containsKey
            warmup(() -> jdkMap.containsKey(RAND.nextInt()));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) sink += jdkMap.containsKey(RAND.nextInt()) ? 1 : 0;
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("HashMap,containsKey,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("HashMap          containsKey  n=%-10d %d ns/op%n", n, ns);

            // MyHashMap containsKey
            warmup(() -> myMap.containsKey(RAND.nextInt()));
            t0 = System.nanoTime();
            for (int i = 0; i < OPS; i++) sink += myMap.containsKey(RAND.nextInt()) ? 1 : 0;
            ns = (System.nanoTime() - t0) / OPS;
            compCsv.write(String.format("MyHashMap,containsKey,%d,%d", n, ns));
            compCsv.newLine();
            System.out.printf("MyHashMap        containsKey  n=%-10d %d ns/op%n", n, ns);
        }

        compCsv.flush();
        compCsv.close();
        System.out.println("compareD.csv written");
    }

    enum TestCase {
        ARRAYLIST_GET("ArrayList", "get(index)") {
            long run(int n, BufferedWriter csv) throws IOException {
                ArrayList<Integer> list = new java.util.ArrayList<Integer>(n);
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.get(RAND.nextInt(n)));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += list.get(RAND.nextInt(n));
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("ArrayList,get(index),%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        ARRAYLIST_ADD_END("ArrayList", "add-at-end") {
            long run(int n, BufferedWriter csv) throws IOException {
                ArrayList<Integer> list = new java.util.ArrayList<Integer>(n);
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("ArrayList,add-at-end,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        ARRAYLIST_ADD_FRONT("ArrayList", "add-at-front") {
            long run(int n, BufferedWriter csv) throws IOException {
                ArrayList<Integer> list = new java.util.ArrayList<Integer>(n);
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.add(0, RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.add(0, RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("ArrayList,add-at-front,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        LINKEDLIST_GET("LinkedList", "get(index)") {
            long run(int n, BufferedWriter csv) throws IOException {
                LinkedList<Integer> list = new LinkedList<>();
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.get(RAND.nextInt(n)));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += list.get(RAND.nextInt(n));
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("LinkedList,get(index),%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        LINKEDLIST_ADD_END("LinkedList", "add-at-end") {
            long run(int n, BufferedWriter csv) throws IOException {
                LinkedList<Integer> list = new LinkedList<>();
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("LinkedList,add-at-end,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        LINKEDLIST_ADD_FRONT("LinkedList", "add-at-front") {
            long run(int n, BufferedWriter csv) throws IOException {
                LinkedList<Integer> list = new LinkedList<>();
                for (int i = 0; i < n; i++) list.add(RAND.nextInt());
                warmup(() -> list.addFirst(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) list.addFirst(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("LinkedList,add-at-front,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        ARRAYDEQUE_ADD_END("ArrayDeque", "add-at-end") {
            long run(int n, BufferedWriter csv) throws IOException {
                ArrayDeque<Integer> dq = new ArrayDeque<>(n);
                for (int i = 0; i < n; i++) dq.add(RAND.nextInt());
                warmup(() -> dq.addLast(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) dq.addLast(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("ArrayDeque,add-at-end,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        ARRAYDEQUE_ADD_FRONT("ArrayDeque", "add-at-front") {
            long run(int n, BufferedWriter csv) throws IOException {
                ArrayDeque<Integer> dq = new ArrayDeque<>(n);
                for (int i = 0; i < n; i++) dq.add(RAND.nextInt());
                warmup(() -> dq.addFirst(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) dq.addFirst(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("ArrayDeque,add-at-front,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        HASHSET_CONTAINS("HashSet", "contains") {
            long run(int n, BufferedWriter csv) throws IOException {
                HashSet<Integer> set = new HashSet<>(n);
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.contains(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += set.contains(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("HashSet,contains,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        HASHSET_ADD("HashSet", "add") {
            long run(int n, BufferedWriter csv) throws IOException {
                HashSet<Integer> set = new HashSet<>(n);
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) set.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("HashSet,add,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        TREESET_CONTAINS("TreeSet", "contains") {
            long run(int n, BufferedWriter csv) throws IOException {
                TreeSet<Integer> set = new TreeSet<>();
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.contains(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += set.contains(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("TreeSet,contains,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        TREESET_ADD("TreeSet", "add") {
            long run(int n, BufferedWriter csv) throws IOException {
                TreeSet<Integer> set = new TreeSet<>();
                for (int i = 0; i < n; i++) set.add(RAND.nextInt());
                warmup(() -> set.add(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) set.add(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("TreeSet,add,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        HASHMAP_PUT("HashMap", "put") {
            long run(int n, BufferedWriter csv) throws IOException {
                HashMap<Integer, Integer> map = new HashMap<>(n);
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.put(RAND.nextInt(), RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) map.put(RAND.nextInt(), RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("HashMap,put,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        HASHMAP_GET("HashMap", "get") {
            long run(int n, BufferedWriter csv) throws IOException {
                HashMap<Integer, Integer> map = new HashMap<>(n);
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.get(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.get(RAND.nextInt()) != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("HashMap,get,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        HASHMAP_CONTAINS("HashMap", "containsKey") {
            long run(int n, BufferedWriter csv) throws IOException {
                HashMap<Integer, Integer> map = new HashMap<>(n);
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.containsKey(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.containsKey(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("HashMap,containsKey,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        TREEMAP_PUT("TreeMap", "put") {
            long run(int n, BufferedWriter csv) throws IOException {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.put(RAND.nextInt(), RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) map.put(RAND.nextInt(), RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("TreeMap,put,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        TREEMAP_GET("TreeMap", "get") {
            long run(int n, BufferedWriter csv) throws IOException {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.get(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.get(RAND.nextInt()) != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("TreeMap,get,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        TREEMAP_CONTAINS("TreeMap", "containsKey") {
            long run(int n, BufferedWriter csv) throws IOException {
                TreeMap<Integer, Integer> map = new TreeMap<>();
                for (int i = 0; i < n; i++) map.put(RAND.nextInt(), RAND.nextInt());
                warmup(() -> map.containsKey(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += map.containsKey(RAND.nextInt()) ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("TreeMap,containsKey,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        PRIORITYQUEUE_OFFER("PriorityQueue", "offer") {
            long run(int n, BufferedWriter csv) throws IOException {
                PriorityQueue<Integer> pq = new PriorityQueue<>();
                for (int i = 0; i < n; i++) pq.offer(RAND.nextInt());
                warmup(() -> pq.offer(RAND.nextInt()));
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) pq.offer(RAND.nextInt());
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("PriorityQueue,offer,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        PRIORITYQUEUE_POLL("PriorityQueue", "poll") {
            long run(int n, BufferedWriter csv) throws IOException {
                PriorityQueue<Integer> pq = new PriorityQueue<>();
                for (int i = 0; i < n; i++) pq.offer(RAND.nextInt());
                warmup(() -> pq.poll());
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += pq.poll() != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("PriorityQueue,poll,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        },
        PRIORITYQUEUE_PEEK("PriorityQueue", "peek") {
            long run(int n, BufferedWriter csv) throws IOException {
                PriorityQueue<Integer> pq = new PriorityQueue<>();
                for (int i = 0; i < n; i++) pq.offer(RAND.nextInt());
                warmup(() -> pq.peek());
                long t0 = System.nanoTime();
                for (int i = 0; i < OPS; i++) sink += pq.peek() != null ? 1 : 0;
                long t1 = System.nanoTime();
                long ns = (t1 - t0) / OPS;
                csv.write(String.format("PriorityQueue,peek,%d,%d", n, ns));
                csv.newLine();
                return ns;
            }
        };

        final String structure, operation;

        TestCase(String structure, String operation) {
            this.structure = structure;
            this.operation = operation;
        }

        // Skip O(n) operations at larger sizes to keep runtime reasonable
        boolean shouldRun(int n) {
            // O(n) operations only at smallest size
            if (n > 1_000) {
                switch (this) {
                    case LINKEDLIST_GET:
                    case LINKEDLIST_ADD_END:
                    case LINKEDLIST_ADD_FRONT:
                    case ARRAYLIST_ADD_FRONT:
                        return false;
                }
            }
            return true;
        }

        abstract long run(int n, BufferedWriter csv) throws IOException;
    }

    static void warmup(Runnable op) {
        for (int i = 0; i < OPS; i++) op.run();
    }

    static long usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    static void gcAndWait() {
        System.gc();
        System.runFinalization();
        try {
            Thread.sleep(MEM_WAIT_MS);
        } catch (InterruptedException e) { /* ignore */ }
    }

    static void measureMemoryArrayList(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        ArrayList<Integer> list = new ArrayList<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) list.add(i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "ArrayList", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("ArrayList,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        list = null;
        gcAndWait();
    }

    static void measureMemoryLinkedList(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < MEM_N; i++) list.add(i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "LinkedList", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("LinkedList,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        list = null;
        gcAndWait();
    }

    static void measureMemoryArrayDeque(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        ArrayDeque<Integer> dq = new ArrayDeque<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) dq.add(i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "ArrayDeque", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("ArrayDeque,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        dq = null;
        gcAndWait();
    }

    static void measureMemoryHashSet(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        HashSet<Integer> set = new HashSet<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) set.add(i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "HashSet", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("HashSet,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        set = null;
        gcAndWait();
    }

    static void measureMemoryTreeSet(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 0; i < MEM_N; i++) set.add(i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "TreeSet", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("TreeSet,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        set = null;
        gcAndWait();
    }

    static void measureMemoryHashMap(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        HashMap<Integer, Integer> map = new HashMap<>(MEM_N);
        for (int i = 0; i < MEM_N; i++) map.put(i, i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "HashMap", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("HashMap,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        map = null;
        gcAndWait();
    }

    static void measureMemoryTreeMap(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        TreeMap<Integer, Integer> map = new TreeMap<>();
        for (int i = 0; i < MEM_N; i++) map.put(i, i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "TreeMap", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("TreeMap,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        map = null;
        gcAndWait();
    }

    static void measureMemoryPriorityQueue(BufferedWriter memCsv) throws IOException {
        gcAndWait();
        long before = usedMemory();
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        for (int i = 0; i < MEM_N; i++) pq.add(i);
        gcAndWait();
        long after = usedMemory();
        double bytesPer = (after - before) / (double) MEM_N;
        System.out.printf("%-15s %-12d %-18.2f %-12d %-12d\n", "PriorityQueue", MEM_N, bytesPer, before, after);
        memCsv.write(String.format("PriorityQueue,%d,%.2f,%d,%d", MEM_N, bytesPer, before, after));
        memCsv.newLine();
        pq = null;
        gcAndWait();
    }
}