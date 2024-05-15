package WarehouseCoOp;

public class Vertex {

    final private String id;
    final private String name;
    private boolean visited;
    private int Shelfindex;

    public Vertex(String id, String name) {
        this.id = id;
        this.name = name;
        visited = false;

    }

    public Vertex(String id, String name, int shelfindex) {
        this.id = id;
        this.name = name;
        visited = false;
        this.Shelfindex = shelfindex;
    }

    public int getShelfindex() {
        return Shelfindex;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void VisitNode() {
        visited = true;

    }

    public boolean IsVisited() {
        return visited;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vertex other = (Vertex) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

}
