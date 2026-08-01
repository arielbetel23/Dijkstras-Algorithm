"""Settings shared by every step of the exporter.

There is deliberately no hardcoded bounding box here. The box is derived from
whichever two towns are asked for, so the exporter works for any pair.
"""

import math
import os

USER_AGENT = "dijkstra-astar-project/1.0 (student project)"

OSM_RAW_PATH = "osm_raw.json"
PLACES_PATH = "places.json"
GRAPH_PATH = "graph.txt"

SCRATCH_FILES = (OSM_RAW_PATH, PLACES_PATH)
GENERATED_FILES = SCRATCH_FILES + (GRAPH_PATH,)


def remove(paths, label):
    """Delete whichever of these files exist.

    Leftovers are dangerous rather than untidy: a later step would read them
    without complaint and silently build a graph for the wrong area.
    """
    removed = [path for path in paths if os.path.exists(path)]
    for path in removed:
        os.remove(path)

    if removed:
        print(f"  {label}: {', '.join(removed)}")
    else:
        print(f"  {label}: nothing to remove")
    return removed


PADDING_FRACTION = 0.35
MIN_PADDING_KM = 2.0

MAX_SEPARATION_KM = 40.0

EARTH_RADIUS_M = 6371000.0
KM_PER_LAT_DEGREE = 111.32


def haversine_km(lat1, lon1, lat2, lon2):
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    h = (math.sin(delta_phi / 2) ** 2
         + math.cos(phi1) * math.cos(phi2) * math.sin(delta_lambda / 2) ** 2)
    return 2 * EARTH_RADIUS_M * math.asin(math.sqrt(h)) / 1000.0


def bounding_box(points):
    """Smallest box containing every point, padded outward.

    Padding is converted from km to degrees separately for each axis, because a
    degree of longitude shrinks as you move away from the equator while a degree
    of latitude does not.
    """
    lats = [lat for lat, _ in points]
    lons = [lon for _, lon in points]

    span_km = haversine_km(min(lats), min(lons), max(lats), max(lons))
    padding_km = max(span_km * PADDING_FRACTION, MIN_PADDING_KM)

    mid_lat = (min(lats) + max(lats)) / 2
    lat_padding = padding_km / KM_PER_LAT_DEGREE
    lon_padding = padding_km / (KM_PER_LAT_DEGREE * math.cos(math.radians(mid_lat)))

    return (round(min(lats) - lat_padding, 5), round(min(lons) - lon_padding, 5),
            round(max(lats) + lat_padding, 5), round(max(lons) + lon_padding, 5))
