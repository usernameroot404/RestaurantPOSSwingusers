package com.restaurant;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class OrderPanel extends JPanel {
    private final DefaultTableModel tableModel;
    private final JTable orderTable;
    private final JLabel totalLabel;
    private final OrderDAO orderDAO;
    private Order currentOrder;
    private final NumberFormat currencyFormat;

    public OrderPanel() {
        this.orderDAO = new OrderDAO();
        this.currentOrder = new Order();
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table setup
        tableModel = new DefaultTableModel(new Object[]{"Item", "Qty", "Price", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? String.class : Object.class;
            }
        };

        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(40);
        orderTable.getTableHeader().setReorderingAllowed(false);

        // Center align all columns except Action
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 3; i++) {
            orderTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Custom renderer and editor for Action column
        TableColumn actionColumn = orderTable.getColumnModel().getColumn(3);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        // Center align header
        JTableHeader header = orderTable.getTableHeader();
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(JLabel.CENTER);

        // Bottom panel
        totalLabel = new JLabel("Total: $0.00", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> checkout());

        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottomPanel.add(new JLabel());
        bottomPanel.add(totalLabel);
        bottomPanel.add(checkoutBtn);

        add(new JScrollPane(orderTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void addItemToOrder(MenuItem menuItem) {
        for (OrderItem item : currentOrder.getItems()) {
            if (item.getMenuItem().getId() == menuItem.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                refreshOrderDisplay();
                return;
            }
        }

        OrderItem newItem = new OrderItem(menuItem, 1);
        currentOrder.addItem(newItem);
        refreshOrderDisplay();
    }

    private void refreshOrderDisplay() {
        tableModel.setRowCount(0);

        for (OrderItem item : currentOrder.getItems()) {
            double itemTotal = item.getPriceAtOrder() * item.getQuantity();
            tableModel.addRow(new Object[]{
                item.getMenuItem().getName(),
                item.getQuantity(),
                currencyFormat.format(itemTotal),
                "Remove"
            });
        }

        currentOrder.calculateTotal();
        totalLabel.setText("Total: " + currencyFormat.format(currentOrder.getTotal()));
    }

    private void checkout() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Order is empty!",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (orderDAO.saveOrder(currentOrder)) {
            JOptionPane.showMessageDialog(this,
                "Order #" + currentOrder.getId() + " saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            currentOrder = new Order();
            refreshOrderDisplay();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to save order!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Button Renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(220, 220, 220));
            setHorizontalAlignment(CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int editingRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setHorizontalAlignment(JButton.CENTER);
            button.setBackground(new Color(240, 100, 100));
            button.setForeground(Color.WHITE);
            button.addActionListener((ActionEvent e) -> {
                fireEditingStopped();
                handleButtonAction();
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            editingRow = row;
            button.setText(value == null ? "" : value.toString());
            return button;
        }

        public Object getCellEditorValue() {
            return button.getText();
        }

        private void handleButtonAction() {
            OrderItem item = currentOrder.getItems().get(editingRow);
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                currentOrder.getItems().remove(item);
            }
            refreshOrderDisplay();
        }
    }
}