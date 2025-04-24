package com.restaurant;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

public class MenuPanel extends JPanel {
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderPanel orderPanel;
    private final JPanel menuGrid;

    public MenuPanel(OrderPanel orderPanel) {
        this.orderPanel = orderPanel;
        this.menuGrid = new JPanel(new GridLayout(0, 4, 10, 10));
        setLayout(new BorderLayout());

        initUI();

        // ⏱️ Auto-refresh setiap 5 menit (300.000 ms)
        Timer refreshTimer = new Timer(30_000, e -> refreshMenu());
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    private void initUI() {
        refreshMenu(); // panggil saat pertama kali
        add(new JScrollPane(menuGrid), BorderLayout.CENTER);
    }

    public void refreshMenu() {
        menuGrid.removeAll(); // hapus semua item sebelumnya
        List<MenuItem> menuItems = menuItemDAO.getAllMenuItems();

        for (MenuItem item : menuItems) {
            JButton button = new JButton("<html><center>" + item.getName() + "<br>$" + item.getPrice() + "</center></html>");
            button.setPreferredSize(new Dimension(150, 100));
            button.addActionListener(e -> orderPanel.addItemToOrder(item));
            menuGrid.add(button);
        }

        menuGrid.revalidate(); // update layout
        menuGrid.repaint();    // repaint panel
    }
}
