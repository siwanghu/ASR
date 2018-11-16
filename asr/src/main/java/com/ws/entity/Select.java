package com.ws.entity;

public class Select {
    private String value;
    private String label;

    public Select(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Select{" +
                "value='" + value + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
