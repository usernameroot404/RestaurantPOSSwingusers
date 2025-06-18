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
    
    @Column(name = "order_type")
    private String orderType = "DINE_IN";
    
    @Column(name = "payment_method")
    private String paymentMethod = "CASH";
    
    @Column(name = "admin_fee")
    private double adminFee = 0.0;
    
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

    // Getters and Setters
    public int getId() { return id; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
    public String getOrderType() { return orderType; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getAdminFee() { return adminFee; }
    public List<OrderItem> getItems() { return items; }

    public void setTotal(double total) { this.total = total; }
    public void setStatus(String status) { this.status = status; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setAdminFee(double adminFee) { this.adminFee = adminFee; }
}