package com.huanchengfly.tieba.api.models;

import java.util.Objects;

public class ParamBean {
    private String name;
    private String value;

    public ParamBean(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ParamBean setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ParamBean setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParamBean)) return false;
        ParamBean paramBean = (ParamBean) o;
        return Objects.equals(getName(), paramBean.getName()) &&
                Objects.equals(getValue(), paramBean.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }
}
