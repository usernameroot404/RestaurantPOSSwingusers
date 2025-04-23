package com.restaurant;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

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
        };

        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(40);

        // Add button renderer and editor
        TableColumn column = orderTable.getColumnModel().getColumn(3);
        column.setCellRenderer(new ButtonRenderer());
        column.setCellEditor(new ButtonEditor(new JCheckBox()));

        // Bottom panel
        totalLabel = new JLabel("Total: $0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> checkout());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(totalLabel, BorderLayout.CENTER);
        bottomPanel.add(checkoutBtn, BorderLayout.EAST);

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
                    "Order saved successfully!",
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

    // Button renderer for table
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Button editor for table
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                if (isPushed) {
                    OrderItem item = currentOrder.getItems().get(row);
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                    } else {
                        currentOrder.getItems().remove(item);
                    }
                    // Panggil refresh nanti, setelah editing berhenti
                    SwingUtilities.invokeLater(() -> refreshOrderDisplay());
                }
                fireEditingStopped(); // HARUS tetap dipanggil dulu
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            button.setText((value == null) ? "" : value.toString());
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            isPushed = false;
            return button.getText();
        }
    }
}
