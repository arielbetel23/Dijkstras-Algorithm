public class Edge {
    private Node target;
    private double length;
    private double maxSpeed;

    public Edge(Node target, double length, double maxSpeed){
        this.target = target;
        this.length = length;
        this.maxSpeed = maxSpeed;
    }

    public Node getTarget() {
        return target;
    }

    public double getLength() {
        return length;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getTravelTime() {
        return length / maxSpeed;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
