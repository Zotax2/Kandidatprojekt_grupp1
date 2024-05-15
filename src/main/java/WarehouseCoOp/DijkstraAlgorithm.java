package WarehouseCoOp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class DijkstraAlgorithm {

    private final List<Vertex> nodes;
    private final List<Edge> edges;
    private Set<Vertex> settledNodes;
    private Set<Vertex> unSettledNodes;
    private Map<Vertex, Vertex> predecessors;
    private Map<Vertex, Integer> distance;
    private Map<Vertex, List<Edge>> adjacentEdges;

    public DijkstraAlgorithm(Graph graph) {
        // create a copy of the array so that we can operate on this array
        this.nodes = new ArrayList<Vertex>(graph.getVertexes());
        this.edges = new ArrayList<Edge>(graph.getEdges());

        // Speed improvement. Create a map of adjacent edges for each vertex
        this.adjacentEdges = new HashMap<Vertex, List<Edge>>();
        for (Edge edge : edges) {
            if (!adjacentEdges.containsKey(edge.getSource())) {
                adjacentEdges.put(edge.getSource(), new ArrayList<Edge>());
            }
            adjacentEdges.get(edge.getSource()).add(edge);
        }
    }

    public void execute(Vertex source) {
        settledNodes = new HashSet<Vertex>();
        unSettledNodes = new HashSet<Vertex>();
        distance = new HashMap<Vertex, Integer>();
        predecessors = new HashMap<Vertex, Vertex>();
        distance.put(source, 0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            Vertex node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(Vertex node) {
        List<Vertex> adjacentNodes = getNeighbors(node);
        for (Vertex target : adjacentNodes) {
            if (getShortestDistance(target) > getShortestDistance(node)
                    + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node)
                        + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        }

    }

    private int getDistance(Vertex node, Vertex target) {

        if (adjacentEdges.containsKey(node)) {
            List<Edge> edgesToNeighbors = adjacentEdges.get(node);

            for (Edge edge : edgesToNeighbors) {
                if (edge.getDestination().equals(target)) {
                    return edge.getWeight();
                }
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private List<Vertex> getNeighbors(Vertex node) {
        List<Vertex> neighbors = new ArrayList<Vertex>();

        List<Edge> edgesToNeighbors = null;
        if (adjacentEdges.containsKey(node)) {
            edgesToNeighbors = adjacentEdges.get(node);
        } else {
            return neighbors;
        }
        for (Edge edge : edgesToNeighbors) {
            if (!isSettled(edge.getDestination())) {
                neighbors.add(edge.getDestination());
            }
        }
        return neighbors;
    }

    private Vertex getMinimum(Set<Vertex> vertexes) {
        Vertex minimum = null;
        for (Vertex vertex : vertexes) {
            if (minimum == null) {
                minimum = vertex;
            } else {
                if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                    minimum = vertex;
                }
            }
        }
        return minimum;
    }

    private boolean isSettled(Vertex vertex) {
        return settledNodes.contains(vertex);
    }

    private int getShortestDistance(Vertex destination) {
        Integer d = distance.get(destination);
        if (d == null) {
            return Integer.MAX_VALUE;
        } else {
            return d;
        }
    }

    /* Make it easy to find the used edge between two vertex in the path*/
    public Edge getEdge(Vertex source, Vertex destination) {
        if (adjacentEdges.containsKey(source)) {
            List<Edge> edgesToNeighbors = adjacentEdges.get(source);
            for (Edge edge : edgesToNeighbors) {
                if (edge.getDestination().equals(destination)) {
                    return edge;
                }
            }
        }
        return null;
    }

    /*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
    public LinkedList<Vertex> getPath(Vertex target) {
        LinkedList<Vertex> path = new LinkedList<Vertex>();
        Vertex step = target;
        // check if a path exists
        if (predecessors.get(step) == null) {
            return null;
        }
        path.add(step);
        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }
        // Put it into the correct order
        Collections.reverse(path);
        return path;
    }

}
