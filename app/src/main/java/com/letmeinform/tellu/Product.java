package com.letmeinform.tellu;

import androidx.annotation.NonNull;

import java.util.Date;

public class Product {
    public int id;

    public String name;
    public Date deadline;

    public Product(int id, String name, Date deadline) {
        this.id = id;
        this.name = name;
        this.deadline = deadline;
    }

    @NonNull
    @Override
    public String toString() {
        return id + " " + name + " " + deadline.toString();
    }
}
