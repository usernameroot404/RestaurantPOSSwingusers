package com.restaurant;

import javax.swing.*;

public class MainFrame extends JFrame {
    private final OrderPanel orderPanel;
    
    public MainFrame() {
        setTitle("Restaurant POS System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        this.orderPanel = new OrderPanel();
        MenuPanel menuPanel = new MenuPanel(orderPanel);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Menu", menuPanel);
        tabbedPane.addTab("Order", orderPanel);
        
        add(tabbedPane);
    }
    
    public OrderPanel getOrderPanel() {
        return orderPanel;
    }
}