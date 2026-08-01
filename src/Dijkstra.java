import java.util.PriorityQueue;


public class Dijkstra {
    private static int settledCount = 0;

    public static int getSettledCount(){
        return settledCount;
    }

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


    private static void setHeuristic(Node node, Node target, double speedCeiling){
        if(node.getHeuristic() < 0){
            node.setHeuristic(speedCeiling > 0 ? Geo.haversine(node, target) / speedCeiling : 0);
        }
    }
}
