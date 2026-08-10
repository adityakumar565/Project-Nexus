package com.workflow.dag_engine.models.graph;

import java.util.Map;

public class Cost {

    Map<String, Double> costVector;

    Cost(Map<String, Double> costVector) {
        this.costVector = costVector;
    }

    public Map<String, Double> getCostVector() {
        return costVector;
    }

    public void setCostVector(Map<String, Double> costVector) {
        this.costVector = costVector;
    }

    public double getCost(String dimension) {
        return costVector.get(dimension);
    }

    public Double getCostAt(String costName) {

        return costVector.get(costName);

    }

    @Override
    public String toString() {
        return "Cost[costVector=" + costVector + "]";
    }

    @Override
    public int hashCode() {
        return costVector.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Cost other = (Cost) obj;
        return costVector.equals(other.costVector);
    }

}
