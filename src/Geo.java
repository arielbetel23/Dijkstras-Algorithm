public class Geo {
    private static final double EARTH_RADIUS_M = 6371000.0;

    // great-circle distance in metres. lat/lon are angles on a sphere, so plain
    // euclidean distance on degrees would be wrong - a degree of longitude is
    // shorter than a degree of latitude everywhere except the equator.
    public static double haversine(Node a, Node b){
        double lat1 = Math.toRadians(a.getLat());
        double lat2 = Math.toRadians(b.getLat());
        double deltaLat = Math.toRadians(b.getLat() - a.getLat());
        double deltaLon = Math.toRadians(b.getLon() - a.getLon());

        double h = Math.pow(Math.sin(deltaLat / 2), 2)
                 + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);

        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(h));
    }
}
