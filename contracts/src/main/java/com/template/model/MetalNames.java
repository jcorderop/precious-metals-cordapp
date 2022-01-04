package com.template.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MetalNames {
    GOLD_BAR("Gold Bar"),
    GOLD_COIN("Gold Coin"),
    SILVER_BAR("Silver Bar"),
    SILVER_COIN("Silver Coin");

    private final String metalName;

    MetalNames(String metalName) {
        this.metalName = metalName;
    }

    public String getMetalName() {
        return this.metalName;
    }

    public static final List<String> METAL_NAMES = Arrays.stream(MetalNames.values())
            .map(metalNames -> metalNames.getMetalName())
            .collect(Collectors.toList());
}