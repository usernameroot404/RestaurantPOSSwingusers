package com.restaurant;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuPanel extends JPanel {
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderPanel orderPanel;
    
    public MenuPanel(OrderPanel orderPanel) {
        this.orderPanel = orderPanel;
        setLayout(new BorderLayout());
        initUI();
    }
    
    private void initUI() {
        JPanel menuGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        List<MenuItem> menuItems = menuItemDAO.getAllMenuItems();
        
        for (MenuItem item : menuItems) {
            JButton button = new JButton("<html><center>" + item.getName() + "<br>$" + item.getPrice() + "</center></html>");
            button.setPreferredSize(new Dimension(150, 100));
            button.addActionListener(e -> orderPanel.addItemToOrder(item));
            
            menuGrid.add(button);
        }
        
        add(new JScrollPane(menuGrid), BorderLayout.CENTER);
    }
}