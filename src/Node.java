import java.util.List;

public class Node {
    private int id;
    private int distance;
     private boolean visited;
     List<Edge> neighbors;

     public Node(int id, int distance, boolean visited, List<Edge> neighbors){
         this.id = id;
         this.distance = distance;
         this.visited = visited;
         this.neighbors = neighbors;
     }
}
