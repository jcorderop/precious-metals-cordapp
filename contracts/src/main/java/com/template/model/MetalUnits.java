package com.template.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MetalUnits {
    KILOS("K"),
    GRAMS("g"),
    OUNCES("oz");

    private final String units;

    MetalUnits(String units) {
        this.units = units;
    }

    public String getUnit() {
        return this.units;
    }

    public static final List<String> UNIT_NAMES = Arrays.stream(MetalUnits.values())
            .map(metalUnits -> metalUnits.getUnit())
            .collect(Collectors.toList());
}
