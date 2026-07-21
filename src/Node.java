import java.util.ArrayList;
import java.util.List;

public class Node {
    private int id;
    private int distance;
     private boolean visited;
     private List<Edge> neighbors;

     public Node(int id){
         this.id = id;
         this.distance = Integer.MAX_VALUE;
         this.visited = false;
         this.neighbors = new ArrayList<>();
     }

    public int getId() {
        return this.id;
    }

    public int getDistance() {
        return this.distance;
    }

    public boolean isVisited() {
        return this.visited;
    }

    public List<Edge> getNeighbors() {
        return this.neighbors;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
