"""Download every drivable way inside a bounding box from Overpass.

Usage:  python fetch_osm.py <south> <west> <north> <east>

Downloading is kept separate from parsing so the slow network call runs once and
every later step works off the saved file.
"""

import json
import sys
import requests

from config import USER_AGENT, OSM_RAW_PATH

OVERPASS_URL = "https://overpass-api.de/api/interpreter"


def build_query(south, west, north, east):
    # the (._;>;) line recurses from the ways down to every node they reference.
    # without it the response holds way definitions whose node ids point at nothing.
    return f"""
[out:json][timeout:180];
way["highway"]({south},{west},{north},{east});
(._;>;);
out body;
"""


def fetch(south, west, north, east):
    response = requests.post(
        OVERPASS_URL,
        data={"data": build_query(south, west, north, east)},
        headers={"User-Agent": USER_AGENT},
        timeout=300,
    )
    response.raise_for_status()
    return response.json()


def summarize(data):
    elements = data.get("elements", [])
    ways = sum(1 for e in elements if e.get("type") == "way")
    nodes = sum(1 for e in elements if e.get("type") == "node")
    return len(elements), ways, nodes


def download(south, west, north, east, save_to=OSM_RAW_PATH):
    print(f"  querying Overpass for box ({south},{west},{north},{east})")
    try:
        data = fetch(south, west, north, east)
    except requests.exceptions.RequestException as error:
        print(f"  request failed: {error}")
        sys.exit(1)

    total, way_count, node_count = summarize(data)
    if way_count == 0:
        print("  no ways returned - check the bounding box")
        sys.exit(1)

    with open(save_to, "w", encoding="utf-8") as file:
        json.dump(data, file)

    print(f"  elements: {total}  ways: {way_count}  nodes: {node_count}")
    return data


def main():
    if len(sys.argv) != 5:
        print("usage: python fetch_osm.py <south> <west> <north> <east>")
        sys.exit(1)

    south, west, north, east = (float(value) for value in sys.argv[1:5])
    download(south, west, north, east)
    print(f"  saved to {OSM_RAW_PATH}")


if __name__ == "__main__":
    main()
