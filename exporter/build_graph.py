"""Step 3 of the exporter: turn the saved OSM response into graph.txt.

Runs entirely offline against osm_raw.json and places.json, so it can be rerun
as often as needed without touching the network.
"""

import json
import math
import sys

from config import PLACES_PATH, OSM_RAW_PATH, GRAPH_PATH

EARTH_RADIUS_M = 6371000.0


DEFAULT_SPEED_KMH = {
    "motorway": 110, "motorway_link": 110,
    "trunk": 90, "trunk_link": 90,
    "primary": 80, "primary_link": 80,
    "secondary": 70, "secondary_link": 70,
    "tertiary": 60, "tertiary_link": 60,
    "unclassified": 50,
    "residential": 30,
    "service": 20,
    "living_street": 15,
}


def haversine(lat1, lon1, lat2, lon2):
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    h = (math.sin(delta_phi / 2) ** 2
         + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2) ** 2)
    return 2 * EARTH_RADIUS_M * math.asin(math.sqrt(h))


def resolve_speed_kmh(tags):
    """maxspeed is free text and absent on ~98% of ways, so a missing or
    unparsable value falls back to a default keyed on the road class. it must
    never end up zero: that would make travel time infinite."""
    raw = tags.get("maxspeed", "").strip().lower()

    if raw.isdigit():
        value = int(raw)
        if value > 0:
            return value

    if raw.endswith("mph"):
        number = raw[:-3].strip()
        if number.isdigit() and int(number) > 0:
            return int(number) * 1.609344

    return DEFAULT_SPEED_KMH[tags["highway"]]


def load_inputs():
    with open(OSM_RAW_PATH, encoding="utf-8") as file:
        elements = json.load(file)["elements"]
    with open(PLACES_PATH, encoding="utf-8") as file:
        places = json.load(file)
    return elements, places


def build(elements):
    coords = {e["id"]: (e["lat"], e["lon"])
              for e in elements if e["type"] == "node"}

    used_nodes = set()
    edges = []
    skipped = 0

    for element in elements:
        if element["type"] != "way":
            continue
        tags = element.get("tags", {})
        if tags.get("highway") not in DEFAULT_SPEED_KMH:
            continue

        speed_mps = resolve_speed_kmh(tags) / 3.6
        node_ids = element["nodes"]

        for first, second in zip(node_ids, node_ids[1:]):
            if first not in coords or second not in coords or first == second:
                skipped += 1
                continue

            lat1, lon1 = coords[first]
            lat2, lon2 = coords[second]
            length = haversine(lat1, lon1, lat2, lon2)
            if length <= 0:
                skipped += 1
                continue

            edges.append((first, second, length, speed_mps))
            edges.append((second, first, length, speed_mps))
            used_nodes.add(first)
            used_nodes.add(second)

    nodes = {node_id: coords[node_id] for node_id in used_nodes}
    return nodes, edges, skipped


def contract(edges, protected):
    """Collapse every run of shape nodes into a single edge.

    A node with exactly two distinct neighbours offers no routing choice, so the
    chain through it can become one edge with the summed length and an effective
    speed of total_length / total_time - which reproduces the original travel
    time exactly. The snapped place nodes are protected so an origin or target
    sitting mid-street cannot be contracted away.
    """
    adj = {}
    for a, b, length, speed in edges:
        adj.setdefault(a, []).append((b, length, speed))

    kept = set(protected)
    for node, out in adj.items():
        if len({target for target, _, _ in out}) != 2:
            kept.add(node)

    contracted = []
    for start in kept:
        for target, length, speed in adj.get(start, []):
            total_length = length
            total_time = length / speed
            prev, current = start, target

            while current not in kept:

                ahead = [(l, s, t) for t, l, s in adj[current] if t != prev]
                best = min(ahead, key=lambda seg: seg[0] / seg[1])
                total_length += best[0]
                total_time += best[0] / best[1]
                prev, current = current, best[2]

            if current != start:
                contracted.append((start, current, total_length, total_length / total_time))

    return contracted


def snap(places, nodes):
    """a geocoded coordinate is almost never a graph node, so each place takes
    the id of the nearest exported node instead."""
    print("  snapping places:")
    snapped = {}
    for name, point in places.items():
        best_id = None
        best_distance = float("inf")
        for node_id, (lat, lon) in nodes.items():
            distance = haversine(point["lat"], point["lon"], lat, lon)
            if distance < best_distance:
                best_distance = distance
                best_id = node_id

        if best_id is None:
            print(f"    no node to snap {name} to")
            sys.exit(1)

        print(f"    {name} -> node {best_id} ({best_distance:.0f} m away)")
        snapped[name] = best_id
    return snapped


def write_graph(nodes, edges, places):
    with open(GRAPH_PATH, "w", encoding="utf-8") as file:
        file.write("# generated by build_graph.py from OpenStreetMap data\n")
        file.write("# NODES  id,lat,lon,name\n")
        file.write("# EDGES  from_id,to_id,length_m,speed_mps\n")
        file.write("# PLACES name,node_id\n\n")

        file.write("NODES\n")
        for node_id, (lat, lon) in sorted(nodes.items()):
            file.write(f"{node_id},{lat:.7f},{lon:.7f},\n")

        file.write("\nEDGES\n")
        for first, second, length, speed in edges:

            file.write(f"{first},{second},{length:.3f},{speed:.6f}\n")

        file.write("\nPLACES\n")
        for name, node_id in places.items():
            file.write(f"{name},{node_id}\n")


def export(elements, places):
    nodes, edges, skipped = build(elements)
    if not edges:
        print("  no drivable edges were produced - check the raw data")
        sys.exit(1)

    print(f"  nodes: {len(nodes)}  edges: {len(edges)}  skipped pairs: {skipped}")

    snapped = snap(places, nodes)
    edges = contract(edges, set(snapped.values()))

    surviving = {node for first, second, _, _ in edges for node in (first, second)}
    missing = [name for name, node_id in snapped.items() if node_id not in surviving]
    if missing:
        print(f"    {', '.join(missing)} snapped onto a node with no roads")
        sys.exit(1)

    nodes = {node_id: nodes[node_id] for node_id in surviving}
    print(f"  after contraction: {len(nodes)} nodes  {len(edges)} edges")

    write_graph(nodes, edges, snapped)
    return len(nodes), len(edges)


def main():
    elements, places = load_inputs()
    export(elements, places)
    print(f"  wrote {GRAPH_PATH}")


if __name__ == "__main__":
    main()
