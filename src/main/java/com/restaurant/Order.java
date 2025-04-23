package com.restaurant;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private double total;
    private String status = "pending";
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void calculateTotal() {
        this.total = items.stream()
            .mapToDouble(item -> item.getPriceAtOrder() * item.getQuantity())
            .sum();
    }
    
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        calculateTotal();
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }

    // Other getters and setters...
    public int getId() { return id; }
    public String getStatus() { return status; }

    public void setTotal(double total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
}