# Dijkstra's Algorithm — Shortest Path on Real Map Data

A from-scratch Java implementation of Dijkstra's algorithm and A*, running on real road networks pulled from OpenStreetMap. You type two town names and it finds the fastest drive between them, then shows how much work each algorithm needed in order to find it.

## Design

Instead of representing the graph with an adjacency matrix or a map of IDs, each `Node` holds direct references to its neighbors through `Edge` objects. The graph itself isn't a separate structure, it emerges from the nodes pointing to each other, similar to how a linked list works, but generalized so each node can connect to any number of others.

- **`Node`** — a vertex. Holds its coordinates, its edge list, its current best cost, a `visited` flag, a heuristic value used only for ordering, and a `previous` reference used to reconstruct the path.
- **`Edge`** — one connection. Holds a destination `Node`, the segment's length in metres and its max speed in m/s. The travel time is derived from those two.
- **`Geo`** — the haversine formula, which gives the real distance between two points on a sphere.
- **`GraphLoader`** — reads the graph file and wires the nodes together. It uses a `HashMap` while loading and then throws it away.
- **`Dijkstra`** — the search. One loop serves both algorithms.
- **`Main`** — reads two place names from stdin and prints the comparison.

## Weights are travel time, not distance

Every edge stores a length and a max speed, so its weight is

```
w(u,v) = length / maxSpeed
```

Due to that the algorithm finds the *fastest* route and not the shortest one, and those are really not the same thing. A longer road at 90 km/h beats a shorter one at 30. The program still reports the distance in km, but that has to be summed separately by walking back along the `previous` chain, because the cost field holds seconds and knows nothing about metres.

## Dijkstra to A*

Dijkstra expands outward from the origin in every direction equally, like a circle growing on the map. It has no idea where the target is, so it spends the same effort on roads heading away from the destination as on roads heading toward it.

A* adds an estimate of the remaining cost, and the queue is ordered by

```
f(n) = g(n) + h(n)
```

where `g(n)` is the real cost so far and `h(n)` is the estimate. The estimate used here is the straight line distance to the target divided by the fastest speed anywhere in the graph:

```
h(n) = d(n,T) / vCeiling
```

This can never overestimate the real remaining time, because a road can never be shorter than the straight line and no road is faster than the ceiling. So the answer A* returns is exactly the same route Dijkstra returns. The only thing that changes is how many nodes it had to look at.

The most important detail in the whole implementation: `h` affects **only** the queue ordering, inside `compareTo`. The stored cost stays pure `g(n)`. If you accumulate `h` into the cost then the reported time is wrong and the path gets corrupted, and the annoying part is that it still looks like a perfectly reasonable answer.

Setting `vCeiling` to 0 forces `h = 0`, which turns the exact same loop back into plain Dijkstra. That is how the two are compared fairly, same loop and same early stop, one toggle.

## The graph file

Java never touches the network. It reads a plain text file with three sections, and that file is the only thing connecting the two halves of the project.

```
NODES
<id>,<lat>,<lon>,<name>

EDGES
<from_id>,<to_id>,<length_m>,<speed_mps>

PLACES
<place name>,<node_id>
```

Blank lines and lines starting with `#` are ignored. Every road appears twice in `EDGES`, once for each direction. `PLACES` is what turns a typed word like `Netanya` into an actual node, so no geocoding or coordinate searching happens on the Java side.

## The Python exporter

The exporter lives in `exporter/` and it is a separate, disposable tool. It runs once and produces `graph.txt`.

```
python export.py "Kfar Saba" "Ra'anana"
```

It geocodes both towns with Nominatim, draws a padded box around wherever they turned out to be, downloads every drivable road inside that box from the Overpass API, and writes the graph file. No API keys are needed, both services are free and open.

Two things in there are worth knowing. OpenStreetMap does not store edges at all, it stores *ways*, which are ordered lists of points describing the shape of a road. So the exporter walks each way and turns every consecutive pair into an edge. And the `maxspeed` tag is missing on about 98% of the roads, so a default table keyed on the road class fills the gap. A missing speed must never become zero, because that would make the travel time infinite.

After that, every chain of nodes with exactly two neighbours gets contracted into a single edge. Those nodes only describe the curve of the road and no decision is ever made at them, so collapsing them shrinks the graph by about 4x while the distance and the travel time come out exactly the same.

## Running it

```
javac -d out src/*.java

cd exporter
python export.py "Even Yehuda" "Netanya"
cd ..

java -cp out Main exporter/graph.txt
```

Then type the origin, press Enter, type the destination, press Enter. When you are done, `python clean.py` removes the generated files.

There is also `test_graph.txt`, a hand written 5 node graph whose answer can be worked out on paper. Run it with `Start` and `End` in order to check everything works without downloading anything.

## Results

```
Route:         Even Yehuda -> Netanya
Distance:      9.63 km
Travel time:   7.7 min
Path length:   84 nodes

Nodes settled (Dijkstra): 7829
Nodes settled (A*):       2960
Reduction:                62.2%
Same route:               true
```

| Route | Distance | Dijkstra | A* | Reduction |
|---|---|---|---|---|
| Even Yehuda → Netanya | 9.63 km | 7,829 | 2,960 | 62.2% |
| Tel Aviv → Netanya | 32.09 km | 54,055 | 21,314 | 60.6% |

Both algorithms return the identical route every time. A* just gets there after looking at a fraction of the map.

## What this is not

This is an algorithms exercise and not a navigation tool. One way streets are ignored on purpose, traffic and junction delays are not modelled at all, and the speeds are legal limits rather than real ones. So the routes are not meant to be driven, they are meant to prove the algorithm works on a real messy graph instead of a toy one.
