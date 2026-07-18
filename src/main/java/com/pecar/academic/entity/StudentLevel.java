package com.pecar.academic.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StudentLevel {

    HND1("HND 1", 1),
    HND2("HND2", 2),
    B_TECH("B-TECH", 3),
    M_TECH1("M-TECH 1", 4),
    M_TECH2("M-TECH 2", 5);

    private final String label;
    private final int order;

    StudentLevel(String label, int order) {
        this.label = label;
        this.order = order;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public int getOrder() {
        return order;
    }

    public boolean isAtOrBelow(StudentLevel other) {
        return this.order <= other.order;
    }

    @JsonCreator
    public static StudentLevel fromLabel(String label) {
        for (StudentLevel level : values()) {
            if (level.label.equalsIgnoreCase(label)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown student level: " + label);
    }
}
