import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Node implements Comparable<Node> {
    private long id;
    private double lat;
    private double lon;
    private String name;
    private double cost;
    private double heuristic;
    private boolean visited;
    private List<Edge> neighbors;
    private Node previous;

     public Node(long id, double lat, double lon, String name){
         this.id = id;
         this.lat = lat;
         this.lon = lon;
         this.name = name;
         this.cost = Double.POSITIVE_INFINITY;
         this.heuristic = -1;   // unset until the search first reaches this node
         this.visited = false;
         this.neighbors = new ArrayList<>();
         this.previous = null;
     }

     // the queue orders by f = g + h, but the stored cost stays pure g.
     // only nodes whose h has been filled in ever enter the queue.
     @Override
     public int compareTo(Node other){
        return Double.compare(this.cost + this.heuristic, other.cost + other.heuristic);
     }

     public void addNeighbor(Node target, double length, double maxSpeed){
         neighbors.add(new Edge(target, length, maxSpeed));
     }

    public long getId() {
        return this.id;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }

    public String getName() {
        return this.name;
    }

    public double getCost() {
        return this.cost;
    }

    public double getHeuristic() {
        return this.heuristic;
    }

    public boolean getVisited() {
        return this.visited;
    }

    public List<Edge> getNeighbors() {
        return this.neighbors;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setHeuristic(double heuristic) {
        this.heuristic = heuristic;
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

    public Stack<Long> getPath(){
        Stack<Long> stack = new Stack<>();
        if(this.previous != null){
            Node current = this.previous;
            while(current != null){
                stack.push(current.id);
                current = current.previous;
            }
        }
        return stack;
    }

    public double getPathLength(){
        double total = 0;
        for(Node current = this; current.previous != null; current = current.previous){
            total += current.previous.lengthTo(current);
        }
        return total;
    }

    public int getPathSize(){
        int count = 0;
        for(Node current = this; current != null; current = current.previous){
            count++;
        }
        return count;
    }

    private double lengthTo(Node target){
        double bestTime = Double.POSITIVE_INFINITY;
        double length = 0;
        for(Edge edge : this.neighbors){
            if(edge.getTarget() == target && edge.getTravelTime() < bestTime){
                bestTime = edge.getTravelTime();
                length = edge.getLength();
            }
        }
        return length;
    }
}
