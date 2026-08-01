"""Turn a place name into a coordinate using Nominatim.

The coordinate returned here is only an anchor. It is almost never an actual node
of the road graph, so a later step snaps it to the nearest exported node.

Usage:  python geocode.py "<origin town>" "<destination town>"
"""

import json
import sys
import time
import requests

from config import USER_AGENT, PLACES_PATH

NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"

REQUEST_DELAY_SECONDS = 1.1


def geocode(name):
    params = {"q": name, "format": "json", "limit": 1}
    response = requests.get(
        NOMINATIM_URL,
        params=params,
        headers={"User-Agent": USER_AGENT},
        timeout=30,
    )
    response.raise_for_status()
    return parse_result(response.json())


def parse_result(payload):
    if not payload:
        return None
    return float(payload[0]["lat"]), float(payload[0]["lon"])


def resolve(names):
    places = {}
    for index, name in enumerate(names):
        if index > 0:
            time.sleep(REQUEST_DELAY_SECONDS)

        print(f"  looking up {name}")
        try:
            result = geocode(name)
        except requests.exceptions.RequestException as error:
            print(f"    request failed: {error}")
            sys.exit(1)

        if result is None:
            print(f"    no match found for {name}")
            sys.exit(1)

        lat, lon = result
        print(f"    {lat},{lon}")
        places[name] = {"lat": lat, "lon": lon}
    return places


def main():
    if len(sys.argv) < 3:
        print('usage: python geocode.py "Origin Town" "Destination Town"')
        sys.exit(1)

    places = resolve(sys.argv[1:3])
    with open(PLACES_PATH, "w", encoding="utf-8") as file:
        json.dump(places, file, ensure_ascii=False, indent=2)
    print(f"saved {len(places)} places to {PLACES_PATH}")


if __name__ == "__main__":
    main()
