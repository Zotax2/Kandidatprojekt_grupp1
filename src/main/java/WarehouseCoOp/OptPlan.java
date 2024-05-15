/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package WarehouseCoOp;

import java.util.*;

public class OptPlan {

    private final DataStore ds;
    int gridsizeX;
    int gridsizeY;
    ArrayList<Robot_Command> route_cmds;
    HashMap<String, Vertex> all_vertex = new HashMap<String, Vertex>();
    private ArrayList<String> shelf_nodes = new ArrayList<>();
    private List<Vertex> nodes;
    private List<Edge> edges;
    private List<Vertex> nodesTSP;
    private DijkstraAlgorithm dijkstra;
    private ArrayList<Integer> DesiredShelves = new ArrayList<>();

//Konstruktor för OptPlan
    public OptPlan(DataStore ds) {
        this.ds = ds;
        this.gridsizeX = ds.gridSizeX;
        this.gridsizeY = ds.gridSizeY;
        this.route_cmds = ds.route_cmds;
        dijkstra = createDijkstra();

    }

// Metod för att jämföra element
    public boolean contains(int[] array, int target) {
        for (int element : array) {
            if (element == target) {
                return true;
            }
        }
        return false;
    }

    //Hämta nod index från koordinater
    public int getNodeIndexFromGrid(int x, int y) {
        int node = gridsizeX * y + x;
        return node;

    }

    //Hämta X-position för en nod
    public int getX(int node) {
        return (node - 1) % gridsizeX;

    }

    //Hämta Y-position för en nod
    public int getY(int node) {
        return (node - 1) / gridsizeX;

    }

    //Hämta nod id
    public String getNodeId(int x, int y, String dir) {
        return ((getNodeIndexFromGrid(x, y) + 1) + dir);
    }

    //Kolla ifall x och y är i bana
    public boolean checkXY(int x, int y) {
        if (x < 0 || x >= gridsizeX) {
            return false;
        }
        return y >= 0 && y < gridsizeY;
    }

    //Beräkna ruttlängden
    private static int calculateTourLength(ArrayList<Integer> cities, int[][] distMatrix) {
        int length = 0;
        int k = 0;
        for (int i = 0; i < cities.size(); i++) {
            length += distMatrix[k][cities.get(i)];
            k = cities.get(i);
        }
        length += distMatrix[k][distMatrix.length - 1];
        return length;
    }

    // Hämta önskade hyllor inför förhandlingen
    public ArrayList<Integer> getDesiredShelves() {
        ds.DesiredShelves = DesiredShelves;
        return DesiredShelves;
    }

    //Skapar nätverket med noder och bågar i ett dijkstra object
    public DijkstraAlgorithm createDijkstra() {
        nodes = new ArrayList<Vertex>();
        edges = new ArrayList<Edge>();

        //Skapar noderna av typen vertex och lägger till dem i en lista med vertexes(nodes)
        String[] Dir = {"N", "E", "S", "W"};
        for (int i = 1; i <= gridsizeX * gridsizeY; i++) {

            for (String D : Dir) {
                Vertex location = new Vertex(i + D, "Nod #" + i + D);
                all_vertex.put(i + D, location);
                nodes.add(location);

            }

        }

        //Hinder noder
        int[] obsNodes = new int[100];

        for (int n = 0; n < ds.noObstacles; n++) {
            obsNodes[n] = (ds.obsX[n] / 30 + (ds.obsY[n] / 30) * gridsizeX) + 1;
            //System.out.println("Obstacle node: " + (obsNodes[n] + 1));
        }

        //Hyll noder
        int[] shelfNodes = new int[100];

        for (int n = 0; n < ds.noShelves; n++) {
            shelfNodes[n] = (ds.shelfX[n] / 30 + (ds.shelfY[n] / 30) * gridsizeX) + 1;
        }

        //Här skapas bågarna mellan noderna av typen Edge som även sparas i en lista edges
        int arcmove_cost = 1; //Kostnad att åka till nästa nod
        int arcturn_cost = 1; //Kostnad att byta riktning
        int arc = 0;

        for (int y = 0; y < gridsizeY; y++) {
            for (int x = 0; x < gridsizeX; x++) {
                arc += 3;

                int source = getNodeIndexFromGrid(x, y) + 1;
                if (contains(obsNodes, source) || contains(shelfNodes, source)) {
                    continue;
                }
                String dest_right = "";
                String dest_left = "";
                for (String D : Dir) {
                    int dest_x = x;
                    int dest_y = y;
                    switch (D) {
                        case "N":
                            dest_y = y + 1;
                            dest_right = "E";
                            dest_left = "W";
                            break;
                        case "S":
                            dest_y = y - 1;
                            dest_right = "W";
                            dest_left = "E";
                            break;
                        case "E":
                            dest_x = x + 1;

                            dest_right = "S";
                            dest_left = "N";
                            break;
                        case "W":
                            dest_x = x - 1;
                            dest_right = "N";
                            dest_left = "S";
                            break;
                        default:
                            break;
                    }

                    if (checkXY(dest_x, dest_y)) {
                        int destination = getNodeIndexFromGrid(dest_x, dest_y) + 1;
                        if (!contains(obsNodes, destination) && !contains(shelfNodes, destination)) {
                            //Skapar framåtbågar för att åka till nästa nod
                            Edge arcmove = new Edge(arc + D, all_vertex.get(source + D), all_vertex.get(getNodeId(dest_x, dest_y, D)), arcmove_cost, Robot_Command.MOVE);
                            edges.add(arcmove);
                        }
                    }
                    //Skapar svängbågar för att byta riktning
                    Edge arcleft = new Edge((arc + 1) + D, all_vertex.get(source + D), all_vertex.get(getNodeId(x, y, dest_left)), arcturn_cost, Robot_Command.TURN_LEFT);
                    Edge arcright = new Edge((arc + 2) + D, all_vertex.get(source + D), all_vertex.get(getNodeId(x, y, dest_right)), arcturn_cost, Robot_Command.TURN_RIGHT);
                    edges.add(arcleft);
                    edges.add(arcright);
                }
            }
        }
        //Skapar en graph utav noderna och bågarna för att skapa ett dijkstrasalgorithm objekt
        Graph graph = new Graph(nodes, edges);
        return new DijkstraAlgorithm(graph);
    }

    //Skapar distansmatrisen ifall vi startar ifrån en annan startposition än ursprunglig startposition (skillnaden är att den tar in orgstartnod)
    public int[][] createDistmatrix(ArrayList<Integer> shelfNodes, ArrayList<Boolean> activeShelf, int startnode, int orgstartnod, DijkstraAlgorithm dijkstra) {

        //Hitta alla lastnoder
        ArrayList<String> loadNodes = new ArrayList<String>();
        for (int n = 0; n < shelfNodes.size(); n++) {
            if (!activeShelf.get(n)) {
                continue;
            }

            int x = getX(shelfNodes.get(n));
            int y = getY(shelfNodes.get(n));
            String loaddir = "";
            switch (ds.shelfDir[n]) {
                case "N":
                    y = y + 1;
                    loaddir = "S";
                    break;
                case "S":
                    y = y - 1;
                    loaddir = "N";
                    break;
                case "E":
                    x = x + 1;
                    loaddir = "W";
                    break;
                case "W":
                    x = x - 1;
                    loaddir = "E";
                    break;

            }
            if (checkXY(x, y)) {
                loadNodes.add(getNodeId(x, y, loaddir));
            }

        }
        if (loadNodes.size() == ds.noShelves) {
            shelf_nodes = loadNodes;
        }
        LinkedList<Vertex> path;
        nodesTSP = new ArrayList<>();
        nodesTSP.add(new Vertex(startnode + "" + ds.direction, "Nod #" + startnode + "" + ds.direction));
        for (int n = 0; n < loadNodes.size(); n++) {

            Vertex node = new Vertex(loadNodes.get(n), "Nod #" + (loadNodes.get(n)), n);

            nodesTSP.add(node);

        }
        nodesTSP.add(new Vertex(ds.slutNod + "W", "Nod #" + ds.slutNod + "W"));
        nodesTSP.add(new Vertex(orgstartnod + "E", "Nod #" + orgstartnod + "E"));
        int[][] distMatrix = new int[nodesTSP.size()][nodesTSP.size()];

        for (int SourceNode = 0; SourceNode < nodesTSP.size(); SourceNode++) {
            for (int DestinationNode = 0; DestinationNode < nodesTSP.size(); DestinationNode++) {
                if (SourceNode == DestinationNode) {
                    continue;
                }
                dijkstra.execute(nodesTSP.get(SourceNode));
                path = dijkstra.getPath(nodesTSP.get(DestinationNode));
                //Edge laneforward = new Edge("", nodesTSP.get(SourceNode), nodesTSP.get(DestinationNode), path.size()-1);
                distMatrix[SourceNode][DestinationNode] = path.size() - 1;

            }
        }
        return distMatrix;
    }

    //Skapar distansmatrisen ifall vi startar ifrån startposition (skillnaden är att den bara tar in startnode)
    public int[][] createDistmatrix(ArrayList<Integer> shelfNodes, ArrayList<Boolean> activeShelf, int startnode, DijkstraAlgorithm dijkstra) {

        //Hitta alla lastnoder alltså positionen hyllorna ska plockas ifrån
        ArrayList<String> loadNodes = new ArrayList<>();
        for (int n = 0; n < shelfNodes.size(); n++) {
            if (!activeShelf.get(n)) {
                continue;
            }

            int x = getX(shelfNodes.get(n));
            int y = getY(shelfNodes.get(n));
            String loaddir = "";
            switch (ds.shelfDir[n]) {
                case "N":
                    y = y + 1;
                    loaddir = "S";
                    break;
                case "S":
                    y = y - 1;
                    loaddir = "N";
                    break;
                case "E":
                    x = x + 1;
                    loaddir = "W";
                    break;
                case "W":
                    x = x - 1;
                    loaddir = "E";
                    break;

            }
            if (checkXY(x, y)) {
                loadNodes.add(getNodeId(x, y, loaddir));
            }

        }
        if (loadNodes.size() == ds.noShelves) {
            shelf_nodes = loadNodes;
        }
        LinkedList<Vertex> path;
        nodesTSP = new ArrayList<>();
        nodesTSP.add(new Vertex(startnode + "E", "Nod #" + startnode + "E"));
        for (int n = 0; n < loadNodes.size(); n++) {

            Vertex node = new Vertex(loadNodes.get(n), "Nod #" + (loadNodes.get(n)), n);

            nodesTSP.add(node);

        }
        nodesTSP.add(new Vertex(ds.slutNod + "W", "Nod #" + ds.slutNod + "W"));
        int[][] distMatrix = new int[nodesTSP.size()][nodesTSP.size()];

        for (int SourceNode = 0; SourceNode < nodesTSP.size(); SourceNode++) {
            for (int DestinationNode = 0; DestinationNode < nodesTSP.size(); DestinationNode++) {
                if (SourceNode == DestinationNode) {
                    continue;
                }
                dijkstra.execute(nodesTSP.get(SourceNode));
                path = dijkstra.getPath(nodesTSP.get(DestinationNode));

                distMatrix[SourceNode][DestinationNode] = path.size() - 1;

            }
        }
        return distMatrix;
    }

    //Skapar den initiala planen med samtliga hyllor inför förhandlingen
    public void createPlan() {

        ArrayList<Integer> shelfNodes = new ArrayList<>();
        ArrayList<Boolean> activeShelves = new ArrayList<>();
        for (int n = 0; n < ds.noShelves; n++) {
            shelfNodes.add((ds.shelfX[n] / 30 + (ds.shelfY[n] / 30) * gridsizeX) + 1);
            activeShelves.add(true);
        }

        //Skapar en initial distansmatris
        int[][] dist_agv = createDistmatrix(shelfNodes, activeShelves, ds.startNod, dijkstra);

        //Skapar en inital lösning där samtliga hyllor plockas.
        NearestNeighbor neighbor = new NearestNeighbor(dist_agv);
        ArrayList<Integer> initalpath = neighbor.execute();

        //Sätter alla hyllor till inaktiva
        for (int t = 0; t < activeShelves.size(); t++) {
            activeShelves.set(t, false);
        }
        ArrayList<Integer> DividedPath = initalpath;
        //Tar bort startnoden
        DividedPath.removeFirst();
        //Tar bort avlämningsplatsen
        DividedPath.removeLast();
        //mitten av plockhyllorna
        int middle = DividedPath.size() / 2;
        //if(DividedPath.size()%2==1){
        //    middle+=1;
        //}
        //Delar upp rutten och tar första hälften
        for (int i = 0; i < middle; i++) {
            activeShelves.set((nodesTSP.get(DividedPath.get(i)).getShelfindex()), true);
            System.out.println("(Dessa hyllor plockas) Shelfindex: " + nodesTSP.get(initalpath.get(i)).getShelfindex());

        }

        //Skapar nya distansmatriser med de valda hyllorna
        int[][] dist_agv1 = createDistmatrix(shelfNodes, activeShelves, 133, dijkstra);
        List<Vertex> nodes_agv1 = nodesTSP;
        int[][] dist_agv2 = createDistmatrix(shelfNodes, activeShelves, 45, dijkstra);
        List<Vertex> nodes_agv2 = nodesTSP;

        NearestNeighbor neighbor1 = new NearestNeighbor(dist_agv1);
        ArrayList<Integer> nnsolve1 = neighbor1.execute();

        NearestNeighbor neighbor2 = new NearestNeighbor(dist_agv2);
        ArrayList<Integer> nnsolve2 = neighbor2.execute();
        ArrayList<Integer> Bestsolution;

        int nn2cost = calculateTourLength(nnsolve2, dist_agv2);
        int nn1cost = calculateTourLength(nnsolve1, dist_agv1);
        //Jämför AGV1 och AGV2 kostnad att ta de första hälften av hyllorna
        if (nn2cost >= nn1cost) {
            Bestsolution = nnsolve1;
            nodesTSP = nodes_agv1;
        } else {
            Bestsolution = nnsolve2;
            nodesTSP = nodes_agv2;

        }
        //Om vi är AGV1 eller AGV2 och vem som hade den kortaste lösningen av att ta den första hälften
        //Om vi inte är den som får kortaste av att ta den första hälften får vi resterande hyllor
        if ((Bestsolution == nnsolve1 && ds.AGVid == 2) || (Bestsolution == nnsolve2 && ds.AGVid == 1)) {
            for (int i = 0; i < activeShelves.size(); i++) {
                if (activeShelves.get(i)) {
                    activeShelves.set(i, false);
                } else {
                    activeShelves.set(i, true);
                }
            }

        }

        DesiredShelves.clear();
        //Fyller på önskelistan med noderna
        for (int i = 0; i < activeShelves.size(); i++) {
            if (activeShelves.get(i)) {
                DesiredShelves.add(i + 1);
            }

        }

        ds.DesiredShelves = DesiredShelves;
        //finalPlan(DesiredShelves);

    }
    //skapar den slutgiltiga planen som även genererar agv instruktioner

    public void finalPlan(ArrayList<Integer> ShelvesToPick, int startnod) {
        DijkstraAlgorithm finaldijkstra = createDijkstra();
        //Samtliga hyllor
        ArrayList<Integer> shelfNodes = new ArrayList<>();
        //Aktiva hyllor som ska plockas
        ArrayList<Boolean> activeShelves = new ArrayList<>();
        LinkedList<Vertex> path;

        for (int n = 0; n < ds.noShelves; n++) {
            shelfNodes.add((ds.shelfX[n] / 30 + (ds.shelfY[n] / 30) * gridsizeX) + 1);
            activeShelves.add(false);

        }
        for (Integer integer : ShelvesToPick) {
            activeShelves.set(integer - 1, true);
        }
        ArrayList<Integer> Route;
        List<String> PathList = new ArrayList<>();
        int[][] dist_OwnAgv;

        if (ds.startNod == startnod) {
            dist_OwnAgv = createDistmatrix(shelfNodes, activeShelves, startnod, finaldijkstra);
        } else {
            dist_OwnAgv = createDistmatrix(shelfNodes, activeShelves, startnod, ds.startNod, finaldijkstra);
        }
        NearestNeighbor OwnNN = new NearestNeighbor(dist_OwnAgv);
        Route = OwnNN.execute();
        //PlockOrdning för att ha koll på vilka hyllor vi plockat och vilken ordning vi ska plocka dem
        ArrayList<Integer> plockOrdning = new ArrayList<>();

        //Omvandling från distansmatris index till faktiska noder
        for (int t : Route) {

            PathList.add(nodesTSP.get(t).getId());

        }
        //Om startnoden inte är samma som ursprungliga startnoden tar vi bort avlastning och startnoden och lägger till dem i slutet igen
        if (ds.startNod != startnod) {
            PathList.remove(ds.startNod + "E");
            PathList.remove("89W");
            PathList.add("89W");
        }

        String start_string;
        if (ds.startNod == 45) {
            start_string = "45E";
        } else {
            start_string = "133E";
        }
        PathList.add(start_string);

        for (int k = 0; k < PathList.size(); k++) {
            for (int i = 0; i < shelf_nodes.size(); i++) {
                //  System.out.println("Hyllnod"+shelf_nodes.get(i));
                //System.out.println("Ruttnod"+PathList.get(k));

                if (PathList.get(k).equals(shelf_nodes.get(i))) {
                    plockOrdning.add(i + 1);

                }
            }
        }
        ds.PlockOrdning = plockOrdning;
        //for(int i=0;i<plockOrdning.size();i++) {
        //System.out.println("Plockordning");
        //System.out.println(plockOrdning.get(i));
        //}
        //System.out.println("Vi besöker noderna i denna ordning");
        //PathList.forEach(System.out::println);

        int pathIndex = 0;
        ds.pathLength = 0;

        ArrayList<Robot_Command> truck_cmds = new ArrayList<>();
        int LengthofPath = 0;

        for (int k = 1; k < PathList.size(); k++) {

            finaldijkstra.execute(all_vertex.get(PathList.get(k - 1))); // Förra hyllan
            path = finaldijkstra.getPath(all_vertex.get(PathList.get(k))); // Nästa hylla

            LengthofPath += path.size() - 1;

            for (int t = 0; t < path.size() - 1; t++) {
                ds.linkStarts[t + pathIndex] = Integer.parseInt(path.get(t).getId().replaceAll("\\D+", ""));
                ds.linkEnds[t + pathIndex] = Integer.parseInt(path.get(t + 1).getId().replaceAll("\\D+", ""));

                truck_cmds.add(finaldijkstra.getEdge(path.get(t), path.get(t + 1)).getCommand());

            }
            //Lägger till pick i slutet vid varje hylla
            if (k != PathList.size() - 1) {
                truck_cmds.add(Robot_Command.PICK);
            }

            pathIndex = pathIndex + path.size();
            ds.pathLength = ds.pathLength + path.size();

        }
        ds.route_cmds = truck_cmds;
        //System.out.println("Här kommer route cmds");
        //for(int i=0;i<ds.route_cmds.size();i++){
        //  System.out.println(ds.route_cmds.get(i));
        //}

        //System.out.println("length of the path " + LengthofPath);
    }
}
