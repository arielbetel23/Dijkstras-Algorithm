import java.util.PriorityQueue;


public class Dijkstra {
    private static int settledCount = 0;

    public static int getSettledCount(){
        return settledCount;
    }

    /** One search for both algorithms.
     *
     * A speed ceiling of 0 forces h = 0, which turns this exactly back into
     * Dijkstra. Same loop, same early stop - the toggle is the only difference,
     * so the settled counts of the two runs are directly comparable.
     */
    public static void run(Node source, Node target, double speedCeiling){
        settledCount = 0;
        source.setCost(0);
        setHeuristic(source, target, speedCeiling);

        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        queue.add(source);
        while(!queue.isEmpty()){
            Node current = queue.poll();
            if(current.getVisited()){
                continue;
            }
            current.setVisited(true);
            settledCount++;

            if(current == target){
                return;
            }

            for(Edge edge : current.getNeighbors()){
                Node neighbor = edge.getTarget();
                double newCost = current.getCost() + edge.getTravelTime();

                if(newCost < neighbor.getCost()){
                    neighbor.setCost(newCost);
                    neighbor.setPrevious(current);
                    setHeuristic(neighbor, target, speedCeiling);
                    queue.add(neighbor);
                }
            }
        }
    }

    // h is filled in lazily, only for nodes the search actually reaches. the
    // straight line distance over the fastest speed in the graph can never
    // overestimate the real remaining travel time, so the result stays optimal.
    private static void setHeuristic(Node node, Node target, double speedCeiling){
        if(node.getHeuristic() < 0){
            node.setHeuristic(speedCeiling > 0 ? Geo.haversine(node, target) / speedCeiling : 0);
        }
    }
}
