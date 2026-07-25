"""Build graph.txt for any two nearby towns, anywhere in the world.

    python export.py "<origin town>" "<destination town>"

The towns are geocoded first, and the bounding box is derived from where they
turn out to be. Nothing about the area is known ahead of time.
"""

import json
import sys

import build_graph
import fetch_osm
import geocode
from config import (PLACES_PATH, GRAPH_PATH, MAX_SEPARATION_KM,
                    bounding_box, haversine_km, remove_generated)


def main():
    if len(sys.argv) != 3:
        print('usage: python export.py "Origin Town" "Destination Town"')
        sys.exit(1)

    origin, destination = sys.argv[1], sys.argv[2]

    print("[1/5] clearing old data")
    remove_generated("removed")

    print("[2/5] geocoding")
    places = geocode.resolve([origin, destination])
    with open(PLACES_PATH, "w", encoding="utf-8") as file:
        json.dump(places, file, ensure_ascii=False, indent=2)

    points = [(place["lat"], place["lon"]) for place in places.values()]
    separation = haversine_km(points[0][0], points[0][1], points[1][0], points[1][1])
    print(f"  the towns are {separation:.1f} km apart")
    if separation > MAX_SEPARATION_KM:
        print(f"  that is past the {MAX_SEPARATION_KM:.0f} km guard - the download would be very large")
        sys.exit(1)

    print("[3/5] choosing the area")
    south, west, north, east = bounding_box(points)
    print(f"  box: {south},{west},{north},{east}")

    print("[4/5] downloading roads")
    data = fetch_osm.download(south, west, north, east)

    print("[5/5] building the graph")
    node_count, edge_count = build_graph.export(data["elements"], places)

    print()
    print(f"wrote {GRAPH_PATH} with {node_count} nodes and {edge_count} edges")
    print(f'run it with:  java Main {GRAPH_PATH} "{origin}" "{destination}"')
    print("then clean up with:  python clean.py")


if __name__ == "__main__":
    main()
