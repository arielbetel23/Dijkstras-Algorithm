public class Edge {
    private Node target;
    private int weight;

    public Edge(Node target, int weight){
        this.target = target;
        this.weight = weight;
    }

    public Node getTarget() {
        return target;
    }

    public int getWeight() {
        return weight;
    }

}
