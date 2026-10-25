import java.util.*;

public class Benchmark {
    public static void main(String[] args) {
        int[] sizes = {1000, 10000, 50000};

        System.out.println("Size\tKruskal median (ms)\tPrim median (ms)");

        for (int n : sizes) {
            int m = n * 3;
            List<List<Integer>> edges = generateGraph(n, m);

            Map<String, Object> req = new HashMap<>();
            req.put("n", n);
            req.put("edges", edges);

            req.put("task", "kruskal");
            Algorithms.kruskal(req);
            req.put("task", "prim");
            Algorithms.prim(req);

            long[] kTimes = new long[5];
            long[] pTimes = new long[5];

            for (int i = 0; i < 5; i++) {
                req.put("task", "kruskal");
                long start = System.nanoTime();
                Algorithms.kruskal(req);
                kTimes[i] = System.nanoTime() - start;

                req.put("task", "prim");
                start = System.nanoTime();
                Algorithms.prim(req);
                pTimes[i] = System.nanoTime() - start;
            }

            Arrays.sort(kTimes);
            Arrays.sort(pTimes);

            double kMed = kTimes[2] / 1_000_000.0;
            double pMed = pTimes[2] / 1_000_000.0;

            System.out.printf("%d\t%.2f\t\t\t%.2f%n", n, kMed, pMed);
        }
    }

    static List<List<Integer>> generateGraph(int n, int m) {
        List<List<Integer>> edges = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < m; i++) {
            int u = rand.nextInt(n);
            int v = rand.nextInt(n);
            int w = rand.nextInt(1000) - 500;
            edges.add(Arrays.asList(u, v, w));
        }
        return edges;
    }
}