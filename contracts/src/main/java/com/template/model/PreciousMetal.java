package com.template.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class PreciousMetal implements Serializable {

    private final String metalName;
    private final String unit;
    private final Integer weight;

    public PreciousMetal(String metalName, String unit, Integer weight) {
        this.metalName = Optional.ofNullable(metalName)
                .orElseThrow(() -> new IllegalArgumentException("MetalName cannot be null..."));;
        this.unit = Optional.ofNullable(unit)
                .orElseThrow(() -> new IllegalArgumentException("Unit cannot be null..."));;
        this.weight = Optional.ofNullable(weight)
                .orElseThrow(() -> new IllegalArgumentException("Weight cannot be null..."));;
    }

    public String getMetalName() {
        return metalName;
    }

    public String getUnit() {
        return unit;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "PreciousMetal{" +
                "metalName='" + metalName + '\'' +
                ", unit='" + unit + '\'' +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreciousMetal that = (PreciousMetal) o;
        return metalName.equals(that.metalName) && unit.equals(that.unit) && weight.equals(that.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metalName, unit, weight);
    }
}
