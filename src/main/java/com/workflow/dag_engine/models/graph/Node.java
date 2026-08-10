package com.workflow.dag_engine.models.graph;

public class Node {

    Long id;
    String name;
    String description;
    Cost nodeCost;

    public Node(Long id, String name, String description, Cost nodeCost) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.nodeCost = nodeCost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Cost getNodeCost() {
        return nodeCost;
    }

    public void setNodeCost(Cost nodeCost) {
        this.nodeCost = nodeCost;
    }

    public double getNodeCostAt(String costName) {

        return this.nodeCost.getCostAt(costName);

    }

    @Override
    public String toString() {
        return "Node[id=" + id + ", name=" + name + ", description=" + description + ", nodeCost=" + nodeCost + "]";
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        return id.equals(other.id);
    }

}
