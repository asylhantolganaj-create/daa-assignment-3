import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public final class Algorithms {

    // ==================== DSU ====================

    /** DSU with path compression and union by size/rank; return states. */
    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        int components = n;
        int largest = n > 0 ? 1 : 0;
        List<int[]> states = new ArrayList<>();

        for (int[] op : operations) {
            int u = op[0];
            int v = op[1];

            int rootU = findRoot(parent, u);
            int rootV = findRoot(parent, v);

            if (rootU != rootV) {
                if (size[rootU] < size[rootV]) {
                    int temp = rootU;
                    rootU = rootV;
                    rootV = temp;
                }
                parent[rootV] = rootU;
                size[rootU] += size[rootV];
                if (size[rootU] > largest) {
                    largest = size[rootU];
                }
                components--;
            }
            states.add(new int[]{components, largest});
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("states", states);
        return result;
    }

    /** Iterative path compression (no recursion -> safe on 100k-deep chains). */
    private static int findRoot(int[] parent, int i) {
        int root = i;
        while (parent[root] != root) {
            root = parent[root];
        }
        // second pass: compress
        int cur = i;
        while (parent[cur] != root) {
            int next = parent[cur];
            parent[cur] = root;
            cur = next;
        }
        return root;
    }

    // ==================== Kruskal ====================

    static class Edge implements Comparable<Edge> {
        int u, v;
        long weight;
        int id;

        Edge(int u, int v, long weight, int id) {
            this.u = u;
            this.v = v;
            this.weight = weight;
            this.id = id;
        }

        @Override
        public int compareTo(Edge other) {
            int c = Long.compare(this.weight, other.weight);
            if (c != 0) return c;
            return Integer.compare(this.id, other.id); // deterministic tie-break
        }
    }

    /** Separate Kruskal implementation; return cost, edge_ids, components. */
    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<Edge> edgeList = new ArrayList<>();
        if (edges != null) {
            for (int i = 0; i < edges.length; i++) {
                edgeList.add(new Edge(edges[i][0], edges[i][1], edges[i][2], i));
            }
        }

        Collections.sort(edgeList);

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        long totalCost = 0;
        List<Integer> edgeIds = new ArrayList<>();
        int components = n;

        for (Edge edge : edgeList) {
            int rootU = findRoot(parent, edge.u);
            int rootV = findRoot(parent, edge.v);

            if (rootU != rootV) {
                parent[rootV] = rootU;
                size[rootU] += size[rootV];
                totalCost += edge.weight;
                edgeIds.add(edge.id);
                components--;
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("cost", totalCost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);
        return result;
    }

    // ==================== Prim ====================

    static class PrimEdge {
        int to, id;
        long weight;

        PrimEdge(int to, long weight, int id) {
            this.to = to;
            this.weight = weight;
            this.id = id;
        }
    }

    /** Separate Prim implementation; restart for every unvisited component. */
    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<PrimEdge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        if (edges != null) {
            for (int i = 0; i < edges.length; i++) {
                int u = edges[i][0];
                int v = edges[i][1];
                long w = edges[i][2];
                adj.get(u).add(new PrimEdge(v, w, i));
                adj.get(v).add(new PrimEdge(u, w, i));
            }
        }

        boolean[] visited = new boolean[n];
        long totalCost = 0;
        List<Integer> edgeIds = new ArrayList<>();
        int components = 0;

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                components++;
                visited[i] = true;

                java.util.PriorityQueue<PrimEdge> pq = new java.util.PriorityQueue<>(
                        (a, b) -> {
                            int c = Long.compare(a.weight, b.weight);
                            if (c != 0) return c;
                            return Integer.compare(a.id, b.id); // deterministic tie-break
                        }
                );

                for (PrimEdge edge : adj.get(i)) {
                    pq.add(edge);
                }

                while (!pq.isEmpty()) {
                    PrimEdge edge = pq.poll();
                    if (visited[edge.to]) {
                        continue;
                    }
                    visited[edge.to] = true;
                    totalCost += edge.weight;
                    edgeIds.add(edge.id);

                    for (PrimEdge nextEdge : adj.get(edge.to)) {
                        if (!visited[nextEdge.to]) {
                            pq.add(nextEdge);
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("cost", totalCost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);
        return result;
    }

    // ==================== SCC (Kosaraju) ====================

    /** Kosaraju's two DFS passes; return component and exact condensation. */
    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();
        List<List<Integer>> revAdj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            revAdj.add(new ArrayList<>());
        }

        if (edges != null) {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                adj.get(u).add(v);
                revAdj.get(v).add(u);
            }
        }

        boolean[] visited = new boolean[n];
        List<Integer> finishOrder = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                sccDfs1Iterative(i, adj, visited, finishOrder);
            }
        }

        java.util.Arrays.fill(visited, false);
        int[] component = new int[n];
        int compId = 0;

        for (int i = finishOrder.size() - 1; i >= 0; i--) {
            int u = finishOrder.get(i);
            if (!visited[u]) {
                sccDfs2Iterative(u, revAdj, visited, component, compId);
                compId++;
            }
        }

        java.util.Set<String> condEdgesSet = new java.util.HashSet<>();
        if (edges != null) {
            for (int[] edge : edges) {
                int u = edge[0];
                int v = edge[1];
                int cu = component[u];
                int cv = component[v];
                if (cu != cv) {
                    condEdgesSet.add(cu + "," + cv);
                }
            }
        }

        List<int[]> condensation = new ArrayList<>();
        for (String s : condEdgesSet) {
            String[] parts = s.split(",");
            condensation.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
        }

        condensation.sort((a, b) -> {
            if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
            return Integer.compare(a[1], b[1]);
        });

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("component", component);
        result.put("condensation", condensation);
        return result;
    }

    /** Iterative first pass (post-order finish time). */
    private static void sccDfs1Iterative(int start, List<List<Integer>> adj,
                                         boolean[] visited, List<Integer> finishOrder) {
        // stack of (node, nextIndex)
        int[] stackNode = new int[adj.size()];
        int[] stackIdx = new int[adj.size()];
        int top = 0;
        stackNode[top] = start;
        stackIdx[top] = 0;
        visited[start] = true;
        top++;

        while (top > 0) {
            int u = stackNode[top - 1];
            int idx = stackIdx[top - 1];
            List<Integer> neighbors = adj.get(u);
            if (idx < neighbors.size()) {
                stackIdx[top - 1] = idx + 1;
                int v = neighbors.get(idx);
                if (!visited[v]) {
                    visited[v] = true;
                    stackNode[top] = v;
                    stackIdx[top] = 0;
                    top++;
                }
            } else {
                finishOrder.add(u);
                top--;
            }
        }
    }

    /** Iterative second pass on transpose; fills component[] with compId. */
    private static void sccDfs2Iterative(int start, List<List<Integer>> revAdj,
                                         boolean[] visited, int[] component, int compId) {
        int[] stack = new int[revAdj.size()];
        int top = 0;
        stack[top++] = start;
        visited[start] = true;
        component[start] = compId;

        while (top > 0) {
            int u = stack[--top];
            for (int v : revAdj.get(u)) {
                if (!visited[v]) {
                    visited[v] = true;
                    component[v] = compId;
                    stack[top++] = v;
                }
            }
        }
    }

    // ==================== Topo ====================

    /** Three-state iterative DFS and reverse finish order; return acyclic and order. */
    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        if (edges != null) {
            for (int[] edge : edges) {
                adj.get(edge[0]).add(edge[1]);
            }
        }

        int[] state = new int[n]; // 0=white, 1=gray, 2=black
        List<Integer> order = new ArrayList<>();
        boolean hasCycle = false;

        for (int i = 0; i < n && !hasCycle; i++) {
            if (state[i] == 0) {
                if (topoDfsIterative(i, adj, state, order)) {
                    hasCycle = true;
                }
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (hasCycle) {
            result.put("acyclic", false);
            result.put("order", new ArrayList<>());
        } else {
            Collections.reverse(order);
            result.put("acyclic", true);
            result.put("order", order);
        }
        return result;
    }

    /** Returns true if a directed cycle was detected. */
    private static boolean topoDfsIterative(int start, List<List<Integer>> adj,
                                            int[] state, List<Integer> order) {
        int n = adj.size();
        int[] stackNode = new int[n];
        int[] stackIdx = new int[n];
        int top = 0;
        stackNode[top] = start;
        stackIdx[top] = 0;
        state[start] = 1;
        top++;

        while (top > 0) {
            int u = stackNode[top - 1];
            int idx = stackIdx[top - 1];
            List<Integer> neighbors = adj.get(u);

            if (idx < neighbors.size()) {
                stackIdx[top - 1] = idx + 1;
                int v = neighbors.get(idx);
                if (state[v] == 1) {
                    return true; // back edge -> cycle
                }
                if (state[v] == 0) {
                    state[v] = 1;
                    stackNode[top] = v;
                    stackIdx[top] = 0;
                    top++;
                }
            } else {
                state[u] = 2;
                order.add(u);
                top--;
            }
        }
        return false;
    }

    // ==================== DAG (shortest paths) ====================

    static class DagEdge {
        int to, id;
        long weight;

        DagEdge(int to, long weight, int id) {
            this.to = to;
            this.weight = weight;
            this.id = id;
        }
    }

    /** Whole-graph cycle check and DAG relaxation; return acyclic, distance, parent_edge. */
    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        List<List<DagEdge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }

        if (edges != null) {
            for (int i = 0; i < edges.length; i++) {
                int u = edges[i][0];
                int v = edges[i][1];
                long w = (edges[i].length > 2) ? edges[i][2] : 1L;
                adj.get(u).add(new DagEdge(v, w, i));
            }
        }

        int[] state = new int[n];
        List<Integer> topoOrder = new ArrayList<>();
        boolean hasCycle = false;

        for (int i = 0; i < n && !hasCycle; i++) {
            if (state[i] == 0) {
                if (dagHasCycleDfsIterative(i, adj, state, topoOrder)) {
                    hasCycle = true;
                }
            }
        }

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        if (hasCycle) {
            result.put("acyclic", false);
            result.put("distance", new ArrayList<>());
            result.put("parent_edge", new ArrayList<>());
            return result;
        }

        Collections.reverse(topoOrder);

        Long[] dist = new Long[n];
        int[] parentEdge = new int[n];
        java.util.Arrays.fill(parentEdge, -1);

        dist[source] = 0L;

        for (int u : topoOrder) {
            if (dist[u] != null) {
                for (DagEdge edge : adj.get(u)) {
                    long newDist = dist[u] + edge.weight;
                    if (dist[edge.to] == null || newDist < dist[edge.to]) {
                        dist[edge.to] = newDist;
                        parentEdge[edge.to] = edge.id;
                    }
                }
            }
        }

        List<Object> distanceList = new ArrayList<>();
        List<Integer> parentEdgeList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            distanceList.add(dist[i]);
            parentEdgeList.add(parentEdge[i]);
        }

        result.put("acyclic", true);
        result.put("distance", distanceList);
        result.put("parent_edge", parentEdgeList);
        return result;
    }

    /** Iterative cycle check; fills topoOrder in post-order (reverse at the end). */
    private static boolean dagHasCycleDfsIterative(int start, List<List<DagEdge>> adj,
                                                   int[] state, List<Integer> topoOrder) {
        int n = adj.size();
        int[] stackNode = new int[n];
        int[] stackIdx = new int[n];
        int top = 0;
        stackNode[top] = start;
        stackIdx[top] = 0;
        state[start] = 1;
        top++;

        while (top > 0) {
            int u = stackNode[top - 1];
            int idx = stackIdx[top - 1];
            List<DagEdge> neighbors = adj.get(u);

            if (idx < neighbors.size()) {
                stackIdx[top - 1] = idx + 1;
                int v = neighbors.get(idx).to;
                if (state[v] == 1) {
                    return true;
                }
                if (state[v] == 0) {
                    state[v] = 1;
                    stackNode[top] = v;
                    stackIdx[top] = 0;
                    top++;
                }
            } else {
                state[u] = 2;
                topoOrder.add(u);
                top--;
            }
        }
        return false;
    }

    // ==================== Route reconstruction helper ====================

    /**
     * Reconstructs a shortest-path route from the DAG source to `target`
     * using the `parent_edge` array produced by `dag(...)`.
     *
     * Returns the list of original edge IDs from source to target,
     * ordered from source to target.
     * - If `target == source`  -> empty list (source has parent_edge = -1).
     * - If `target` unreachable (parent_edge[target] == -1) -> empty list.
     *
     * `edges` is the same `int[][]` passed to `dag(...)` so we can map
     * parent edge IDs back to their (u, v) endpoints.
     */
    static List<Integer> reconstructRoute(int[] parentEdge, int source, int target, int[][] edges) {
        List<Integer> route = new ArrayList<>();
        if (target == source) return route;

        int cur = target;
        // guard against malformed input: bounded by number of vertices
        int guard = parentEdge.length + 1;

        while (cur != source && guard-- > 0) {
            int pe = parentEdge[cur];
            if (pe < 0) {
                // unreachable
                return new ArrayList<>();
            }
            route.add(pe);
            int from = edges[pe][0];
            // safety: if edge doesn't end at cur, still walk from 'from'
            cur = from;
        }

        if (cur != source) {
            // chain didn't reach source -> treat as unreachable / broken witness
            return new ArrayList<>();
        }

        Collections.reverse(route);
        return route;
    }
}