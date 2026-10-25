# Custom Tests

This folder contains custom tests for the graph algorithms, covering edge cases, deep graphs, and non-unique answers.

## How to run tests

From the repository root, run:
python tests/run_tests.py

This script:
1. Builds the Java project.
2. Reads requests from `tests/inputs/*.jsonl`.
3. Runs the program and saves actual outputs to `tests/actual/*.jsonl`.
4. Compares actual outputs with `tests/expected/*.jsonl` using `tools/public_validate.py` (which correctly handles alternative valid answers like different MSTs or topological orders).
5. Prints `OK` for passing tests and `FAIL` with details for failing ones.

## Test Cases Explanation

- **DSU (`my_dsu.jsonl`)**: Tests self-union (no change), empty graph (`n=0`), a growing star graph, and union by size behavior.
- **Kruskal (`my_kruskal.jsonl`)**: Tests parallel edges, self-loops, negative weights, 64-bit total cost (to ensure `long` is used), and tied optima (accepts any valid MST).
- **Prim (`my_prim.jsonl`)**: Tests the same complex graph as Kruskal and verifies Prim restarts correctly in disconnected components.
- **SCC (`my_scc.jsonl`)**: Tests a simple chain, a cycle with a tail, and self-loops with parallel edges. Accepts any valid component labeling.
- **Topo (`my_topo.jsonl`)**: Tests a 2-node cycle, an empty graph (isolated vertices), parallel edges, and a disconnected cycle. Accepts any valid topological order.
- **DAG (`my_dag.jsonl`)**: Tests unreachable vertices, tied paths, parallel edges (must pick min weight), and a global cycle (even if unreachable from source). Accepts any valid shortest-path witness.

## Test Sections
The test suite covers exactly 7 sections to ensure full coverage:
1. DSU
2. Kruskal
3. Prim
4. SCC
5. Topo
6. DAG
7. Route Helper (via `RouteHelperTest.java`)ы