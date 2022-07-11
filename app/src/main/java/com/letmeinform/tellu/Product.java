package com.letmeinform.tellu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Objects;

public class Product {
    public int id;

    public String name;
    public Date expirationDate;

    public Product(String name, Date expirationDate) {
        this.name = name;
        this.expirationDate = expirationDate;
    }

    public Product(int id, String name, Date expirationDate) {
        this.id = id;
        this.name = name;
        this.expirationDate = expirationDate;
    }

    @NonNull
    @Override
    public String toString() {
        return id + " " + name + " " + expirationDate.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id && Objects.equals(name, product.name) && Objects.equals(expirationDate, product.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, expirationDate);
    }
}
