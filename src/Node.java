import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Node implements Comparable<Node> {
    private int id;
    private int distance;
    private boolean visited;
    private List<Edge> neighbors;
    private Node previous;

     public Node(int id){
         this.id = id;
         this.distance = Integer.MAX_VALUE;
         this.visited = false;
         this.neighbors = new ArrayList<>();
         this.previous = null;
     }

     @Override
     public int compareTo(Node other){
        return Integer.compare(this.distance, other.distance);
     }

     public void addNeighbor(Node target, int weight){
         neighbors.add(new Edge(target, weight));
     }

    public int getId() {
        return this.id;
    }

    public int getDistance() {
        return this.distance;
    }

    public boolean getVisited() {
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

    public Node getPrevious(){
        return this.previous;
    }
    public void setPrevious(Node node){
        this.previous = node;
    }

    public Stack<Integer> getPath(){
        Stack<Integer> stack = new Stack<>();
        if(this.previous != null){
            Node current = this.previous;
            while(current != null){
                stack.push(current.id);
                current = current.previous;
            }
        }
        return stack;
    }
    
    public String toString(){
        Stack<Integer> stack = getPath();
        String path = "";
        while(!stack.isEmpty()){
            path += String.valueOf(stack.pop()) + " ";
        }
        String ret = "id: " + this.id + ", distance: " + this.distance + ", shortest path: " + path;
        return ret;
    }
}
