import java.util.PriorityQueue;


public class Dijkstra {
    public static void run(Node source){
        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        queue.add(source);
        while(!queue.isEmpty()){
            Node current = queue.poll();
            if(!current.getVisited()){
                current.setVisited(true);
            }
            
        }
    }    
}
