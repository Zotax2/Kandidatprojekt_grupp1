package WarehouseCoOp;

import java.util.*;
import java.util.ArrayList;
//test

public class NearestNeighbor {

    private final int[][] distMatrix;

    public NearestNeighbor(int[][] distMatrix) {
        this.distMatrix = distMatrix;
        // create a copy of the array so that we can operate on this array

    }

    public ArrayList<Integer> execute() {
        ArrayList<Integer> route = new ArrayList<>();
        Boolean[] visited = new Boolean[distMatrix.length];
        Arrays.fill(visited, false);
        int CheapestNode = -1;
        route.add(0);
        do {
            visited[(route.get(route.size() - 1))] = true;
            CheapestNode = -1;
            int MinCost = Integer.MAX_VALUE;
            for (int i = 0; i < distMatrix.length - 1; i++) {

                if (distMatrix[(route.get(route.size() - 1))][i] < MinCost && !visited[i]) {
                    MinCost = distMatrix[(route.get(route.size() - 1))][i];
                    CheapestNode = i;
                }

            }
            if (CheapestNode != -1) {
                route.add(CheapestNode);

            }

        } while (CheapestNode != -1);

        route.add(distMatrix.length - 1);

        return route;

    }
}
