package com.ledger.backfill;

public class PlayerIDResponse {
    private String name;
    private String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PlayerIDResponse{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
