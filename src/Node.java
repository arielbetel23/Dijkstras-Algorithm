import java.util.ArrayList;
import java.util.List;

public class Node {
    private int id;
    private int distance;
     private boolean visited;
     List<Edge> neighbors;

     public Node(int id){
         this.id = id;
         this.distance = Integer.MAX_VALUE;
         this.visited = false;
         this.neighbors = new ArrayList<>();
     }
}
