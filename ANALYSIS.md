# Algorithm Analysis

## 1. DSU (Disjoint Set Union)
- **Steps**: Initialize parent and size arrays. For each operation, find roots using path compression. If roots differ, attach the smaller tree to the larger one (union by size) and update components and largest size.
- **Invariants**: `parent[x]` always points towards the root. The root is its own parent. `size[root]` stores the number of elements in the tree.
- **Correctness**: Path compression flattens the tree without changing connectivity. Union by size guarantees tree depth is O(log n), keeping operations fast.
- **Boundary cases**: Empty graph (n=0), self-union (u==v) handled correctly (no change).
- **Complexity**: Time: O(α(n)) per operation (inverse Ackermann, practically O(1)). Space: O(n).
- **State reset**: A new DSU is initialized for each request, preventing state leakage between JSON lines.

## 2. Kruskal
- **Steps**: Extract all edges, sort by weight. Initialize DSU. Iterate through sorted edges, add edge to MST if it connects two different components.
- **Invariants**: The selected edges always form a forest (no cycles). Edge weights are considered in non-decreasing order.
- **Correctness**: Standard Kruskal's theorem: adding the minimum weight edge that doesn't form a cycle yields a Minimum Spanning Forest.
- **Boundary cases**: Empty graph, isolated vertices (components count includes them), negative weights, self-loops (ignored by DSU), parallel edges.
- **Complexity**: Time: O(E log E) due to sorting. Space: O(V + E).
- **Deep graphs**: Iterative `findRoot` avoids stack overflow.

## 3. Prim
- **Steps**: Initialize adjacency list. For each unvisited vertex, start a new component. Use a priority queue to pick the minimum weight edge connecting the current tree to an unvisited vertex.
- **Invariants**: The selected edges always form a tree within the current component.
- **Correctness**: Standard Prim's theorem. Restarting the loop for unvisited vertices guarantees a Minimum Spanning Forest.
- **Boundary cases**: Same as Kruskal. Isolated vertices form components of size 1.
- **Complexity**: Time: O(E log V) with priority queue. Space: O(V + E).

## 4. SCC (Kosaraju)
- **Steps**: First DFS on original graph to compute finish order (iterative). Second DFS on transposed graph in reverse finish order to assign components. Build condensation graph by checking edges between different components.
- **Invariants**: Vertices in the same SCC have the same component label. Condensation edges go only between different components.
- **Correctness**: Kosaraju's algorithm correctly identifies SCCs because the first DFS finds the "sink" components first, and the transpose swaps source/sink.
- **Boundary cases**: Empty graph, isolated vertices, self-loops, parallel edges.
- **Complexity**: Time: O(V + E). Space: O(V + E).
- **Deep graphs**: Both DFS passes are iterative.

## 5. Topo (Topological Sort)
- **Steps**: Three-state iterative DFS. State 0 = unvisited, 1 = visiting, 2 = visited. If we encounter a state 1 node, a cycle exists.
- **Invariants**: A node is added to the order only after all its descendants are fully processed (post-order).
- **Correctness**: If no cycle is found, reversing the post-order yields a valid topological sort.
- **Boundary cases**: Empty graph (returns empty order), self-loops and cycles return `acyclic=false`.
- **Complexity**: Time: O(V + E). Space: O(V + E).
- **Deep graphs**: Iterative DFS prevents StackOverflowError on 100k nodes.

## 6. DAG (Shortest Paths)
- **Steps**: Check for cycles using iterative DFS. If acyclic, compute topological order. Relax edges in topological order from the source.
- **Invariants**: `dist[v]` is the shortest path from source to `v` using processed nodes. `parent_edge[v]` is the edge ID that achieved this distance.
- **Correctness**: Relaxing edges in topological order guarantees that when we process node `u`, its distance is already final.
- **Boundary cases**: Unreachable nodes get `null` distance and `-1` parent. Source gets `0` and `-1`. Global cycle returns `false` and empty arrays.
- **Complexity**: Time: O(V + E). Space: O(V + E).
- **Route reconstruction**: Helper walks back via `parent_edge` from target to source. Tested for reachable target, source itself, unreachable target, and tied/parallel edges.

## 7. Kruskal vs Prim Benchmark
*The benchmark was run on randomly generated graphs with a fixed seed (`Random(42)`), using 3 edges per vertex on average. The median of 5 runs is reported. Warm-up runs were performed before timing. JVM compilation time is excluded.

| Graph Size (n) | Kruskal median (ms) | Prim median (ms) |
| :--- | :--- | :--- |
| 1,000 | 1.61 | 1.43 |
| 10,000 | 14.10 | 15.20 |
| 50,000 | 65.55 | 89.63 |

**Analysis**: On smaller and medium graphs, the performance is comparable, with Prim slightly faster at n=1000 due to not requiring a full sort. However, as the graph grows to 50,000 vertices, Kruskal becomes noticeably faster. This is because Kruskal's bottleneck is sorting the edges (O(E log E)), which is highly optimized in Java, while Prim's performance heavily depends on the PriorityQueue (O(E log V)), which incurs more overhead due to object creation and comparisons in the heap.

## 8. Sources and AI Assistance

- **Sources**: The algorithms were implemented based on standard textbook descriptions (Cormen et al., "Introduction to Algorithms") and lecture materials. No external graph libraries were used; only standard Java collections (`ArrayList`, `PriorityQueue`, `HashMap`) as permitted by the assignment.
- **AI Assistance**: This was a complex and challenging assignment. AI assistance was heavily utilized throughout the process. Specifically, AI was used for:
    1. Debugging and converting recursive DFS implementations into iterative ones to prevent `StackOverflowError` on deep graphs (100k nodes).
    2. Generating custom JSONL test cases (boundaries, deep chains, cycles, tied optima) and the Python script (`run_tests.py`) to validate them against alternative correct answers.
    3. Writing the Java benchmark class to compare Kruskal and Prim performance.
    4. Formatting and structuring the `ANALYSIS.md` document and debugging compilation/execution errors.

  All final code was reviewed, understood, and tested by the student to ensure correctness.