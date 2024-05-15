package WarehouseCoOp;

public class Edge {

    private final String id;
    private final Vertex source;
    private final Vertex destination;
    private final int weight;
    private final Robot_Command command;

    public Edge(String id, Vertex source, Vertex destination, int weight, Robot_Command command) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }

    public int getWeight() {
        return weight;
    }

    public Robot_Command getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }

}
