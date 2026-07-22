# Dijkstra's Algorithm — Shortest Path Finder

A from-scratch Java implementation of Dijkstra's algorithm, built to find the shortest path between nodes in a weighted graph.

## Design

Instead of representing the graph with an adjacency matrix or a map of IDs, each `Node` holds direct references to its neighbors through `Edge` objects. The graph itself isn't a separate structure — it emerges from the nodes pointing to each other, similar to how a linked list works, but generalized so each node can connect to any number of others.

- **`Node`** — represents a vertex. Holds its own `distance`, `visited` status, a list of outgoing `Edge`s, and a `previous` reference used to reconstruct the shortest path after the algorithm finishes.
- **`Edge`** — represents a weighted connection to another `Node`.
- **`Dijkstra`** — runs the algorithm using a `PriorityQueue<Node>`, always expanding the cheapest known unvisited node next.
- **`Main`** — builds an example 7-node graph and prints the shortest distance and path from the source to every other node.

## How it works

1. Every node starts at distance `infinity`, except the source (`0`).
2. The algorithm repeatedly pulls the cheapest unvisited node from a priority queue and finalizes it.
3. Each of its neighbors is _relaxed_ — if reaching it through the current node is cheaper than what's currently known, its distance (and predecessor) is updated, and it's re-added to the queue.
4. Once the queue is empty, every reachable node holds its true shortest distance from the source, and the full path can be reconstructed by walking backward through each node's `previous` reference.

## Example

id: 6, distance: 14, shortest path: 0 2 1 3 4 5 6

## Running it

Compile and run `Main.java` — it builds the example graph, runs the algorithm from node `A`, and prints the result.
