import java.util.PriorityQueue;


public class Dijkstra {
    public static void run(Node source){
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        queue.add(source);
        while(!queue.isEmpty()){
            Node current = queue.poll();
            if(current.getVisited()){
                continue;
            }
            current.setVisited(true);

            int newDistance;
            for(int i = 0; i < current.getNeighbors().size(); i++){
                newDistance = current.getDistance() + current.getNeighbors().get(i).getWeight();

                if(newDistance < current.getNeighbors().get(i).getTarget().getDistance()){
                    current.getNeighbors().get(i).getTarget().setDistance(newDistance);
                    current.getNeighbors().get(i).getTarget().setPrevious(current);
                    queue.add(current.getNeighbors().get(i).getTarget());
                }
            }
        }
    }    
}
