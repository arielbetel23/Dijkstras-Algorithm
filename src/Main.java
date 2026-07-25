import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Main {
    private static final int PATH_PREVIEW_NODES = 10;

    public static void main(String[] args) throws IOException {
        String path = args.length > 0 ? args[0] : "graph.txt";

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String origin = stdin.readLine();
        String destination = stdin.readLine();
        if(origin == null || destination == null){
            System.out.println("expected two lines on stdin: origin, then destination");
            return;
        }
        origin = origin.trim();
        destination = destination.trim();

        Map<String, Node> places = GraphLoader.load(path);
        if(places.get(origin) == null){
            System.out.println("unknown place: " + origin);
            return;
        }
        if(places.get(destination) == null){
            System.out.println("unknown place: " + destination);
            return;
        }

        // control run: a ceiling of 0 forces h = 0, so this is plain Dijkstra
        Dijkstra.run(places.get(origin), places.get(destination), 0);
        int dijkstraSettled = Dijkstra.getSettledCount();
        Node target = places.get(destination);

        if(Double.isInfinite(target.getCost())){
            System.out.println(destination + " is unreachable from " + origin);
            return;
        }

        double distanceKm = target.getPathLength() / 1000.0;
        double minutes = target.getCost() / 60.0;
        int pathSize = target.getPathSize();

        // reloaded because a node keeps its cost and visited flag from the previous run
        places = GraphLoader.load(path);
        Dijkstra.run(places.get(origin), places.get(destination), GraphLoader.getMaxSpeed());
        int aStarSettled = Dijkstra.getSettledCount();
        Node aStarTarget = places.get(destination);

        System.out.println("Route:         " + origin + " -> " + destination);
        System.out.println("Distance:      " + String.format("%.2f km", distanceKm));
        System.out.println("Travel time:   " + String.format("%.1f min", minutes));
        System.out.println("Path length:   " + pathSize + " nodes");
        System.out.println("Path:          " + preview(target));
        System.out.println();
        System.out.println("Nodes settled (Dijkstra): " + dijkstraSettled);
        System.out.println("Nodes settled (A*):       " + aStarSettled);
        System.out.println("Reduction:                "
                + String.format("%.1f%%", 100.0 * (dijkstraSettled - aStarSettled) / dijkstraSettled));
        System.out.println("Same route:               "
                + (aStarTarget.getPathSize() == pathSize
                   && Math.abs(aStarTarget.getCost() - target.getCost()) < 1e-9));
    }

    // a real route runs to hundreds of unnamed ids, so only the ends are printed
    private static String preview(Node target){
        List<Long> ids = new ArrayList<>();
        Stack<Long> stack = target.getPath();
        while(!stack.isEmpty()){
            ids.add(stack.pop());
        }
        ids.add(target.getId());

        StringBuilder text = new StringBuilder();
        if(ids.size() <= PATH_PREVIEW_NODES){
            for(long id : ids){
                text.append(id).append(" ");
            }
            return text.toString();
        }

        int ends = PATH_PREVIEW_NODES / 2;
        for(int i = 0; i < ends; i++){
            text.append(ids.get(i)).append(" ");
        }
        text.append("... ").append(ids.size() - 2 * ends).append(" more ... ");
        for(int i = ids.size() - ends; i < ids.size(); i++){
            text.append(ids.get(i)).append(" ");
        }
        return text.toString();
    }
}
