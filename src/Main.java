public class Main {
    public static void main(String[] args) {
        Node a = new Node(0);
        Node b = new Node(1);
        Node c = new Node(2);
        Node d = new Node(3);
        Node e = new Node(4);
        Node f = new Node(5);
        Node g = new Node(6);

        addEdge(a, b, 4);
        addEdge(a, c, 1);
        addEdge(c, b, 2);
        addEdge(b, d, 5);
        addEdge(c, d, 8);
        addEdge(c, e, 10);
        addEdge(d, e, 2);
        addEdge(d, f, 6);
        addEdge(e, f, 3);
        addEdge(e, g, 5);
        addEdge(f, g, 1);

        a.setDistance(0);

        Dijkstra.run(a);
        
    }

    static String NodeIdAndDistance(Node node){
        String ret = "id: " + node.getId() + ", distance: " + node.getDistance();
        return ret;
    }

    static void addEdge(Node a, Node b, int weight) {
        a.addNeighbor(b, weight);
        b.addNeighbor(a, weight);
    }
}
