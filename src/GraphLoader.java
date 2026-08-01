import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphLoader {
    private static final String NODES = "NODES";
    private static final String EDGES = "EDGES";
    private static final String PLACES = "PLACES";

    private static double maxSpeed;

    public static double getMaxSpeed(){
        return maxSpeed;
    }

    public static Map<String, Node> load(String path) throws IOException {
        maxSpeed = 0;
        Map<Long, Node> byId = new HashMap<>();
        Map<String, Long> placeIds = new HashMap<>();

        List<String[]> edgeRows = new ArrayList<>();
        String section = null;
        int lineNumber = 0;

        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while((line = reader.readLine()) != null){
            lineNumber++;
            line = line.trim();

            if(line.isEmpty() || line.startsWith("#")){
                continue;
            }
            if(line.equals(NODES) || line.equals(EDGES) || line.equals(PLACES)){
                section = line;
                continue;
            }
            if(section == null){
                reader.close();
                throw new IOException("line " + lineNumber + ": data before any section header");
            }

            String[] parts = line.split(",", -1);

            if(section.equals(NODES)){
                require(parts, 4, lineNumber);
                long id = Long.parseLong(parts[0].trim());
                double lat = Double.parseDouble(parts[1].trim());
                double lon = Double.parseDouble(parts[2].trim());
                byId.put(id, new Node(id, lat, lon, parts[3].trim()));
            }
            else if(section.equals(EDGES)){
                require(parts, 4, lineNumber);
                edgeRows.add(parts);
            }
            else {
                require(parts, 2, lineNumber);
                placeIds.put(parts[0].trim(), Long.parseLong(parts[1].trim()));
            }
        }
        reader.close();

        for(String[] parts : edgeRows){
            Node from = byId.get(Long.parseLong(parts[0].trim()));
            Node to = byId.get(Long.parseLong(parts[1].trim()));
            if(from == null || to == null){
                throw new IOException("edge " + parts[0] + "->" + parts[1] + " names a node that was never declared");
            }
            double speed = Double.parseDouble(parts[3].trim());
            maxSpeed = Math.max(maxSpeed, speed);
            from.addNeighbor(to, Double.parseDouble(parts[2].trim()), speed);
        }

        Map<String, Node> places = new HashMap<>();
        for(Map.Entry<String, Long> entry : placeIds.entrySet()){
            Node node = byId.get(entry.getValue());
            if(node == null){
                throw new IOException("place " + entry.getKey() + " points at a node that was never declared");
            }
            places.put(entry.getKey(), node);
        }
        return places;
    }

    private static void require(String[] parts, int expected, int lineNumber) throws IOException {
        if(parts.length != expected){
            throw new IOException("line " + lineNumber + ": expected " + expected + " fields but found " + parts.length);
        }
    }
}
