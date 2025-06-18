package com.restaurant;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class OrderPanel extends JPanel {
    public enum OrderType { DINE_IN, TAKE_AWAY }
    public enum PaymentMethod { CASH, BCA }
    
    private final DefaultTableModel tableModel;
    private final JTable orderTable;
    private final JLabel totalLabel;
    private final OrderDAO orderDAO;
    private Order currentOrder;
    private final NumberFormat currencyFormat;
    
    private OrderType currentOrderType = OrderType.DINE_IN;
    private PaymentMethod currentPaymentMethod = PaymentMethod.CASH;

    public OrderPanel() {
        this.orderDAO = new OrderDAO();
        this.currentOrder = new Order();
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create control panel for order type and payment method
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

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

        // Bottom panel
        totalLabel = new JLabel("Total: $0.00", JLabel.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> checkout());

        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bottomPanel.add(new JLabel());
        bottomPanel.add(totalLabel);
        bottomPanel.add(checkoutBtn);

        add(new JScrollPane(orderTable), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // Order Type Panel
        JPanel orderTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        orderTypePanel.add(new JLabel("Order Type:"));
        
        ButtonGroup orderTypeGroup = new ButtonGroup();
        JRadioButton dineInBtn = new JRadioButton("Dine In", true);
        JRadioButton takeAwayBtn = new JRadioButton("Take Away");
        
        orderTypeGroup.add(dineInBtn);
        orderTypeGroup.add(takeAwayBtn);
        
        dineInBtn.addActionListener(e -> currentOrderType = OrderType.DINE_IN);
        takeAwayBtn.addActionListener(e -> currentOrderType = OrderType.TAKE_AWAY);
        
        orderTypePanel.add(dineInBtn);
        orderTypePanel.add(takeAwayBtn);
        
        // Payment Method Panel
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.add(new JLabel("Payment:"));
        
        ButtonGroup paymentGroup = new ButtonGroup();
        JRadioButton cashBtn = new JRadioButton("Cash", true);
        JRadioButton bcaBtn = new JRadioButton("BCA");
        
        paymentGroup.add(cashBtn);
        paymentGroup.add(bcaBtn);
        
        cashBtn.addActionListener(e -> {
            currentPaymentMethod = PaymentMethod.CASH;
            updateTotalDisplay();
        });
        bcaBtn.addActionListener(e -> {
            currentPaymentMethod = PaymentMethod.BCA;
            updateTotalDisplay();
        });
        
        paymentPanel.add(cashBtn);
        paymentPanel.add(bcaBtn);
        
        panel.add(orderTypePanel);
        panel.add(paymentPanel);
        
        return panel;
    }

    private void updateTotalDisplay() {
        double total = currentOrder.getTotal();
        if (currentPaymentMethod == PaymentMethod.BCA) {
            total += 5.0; // BCA admin fee
            totalLabel.setText("Total: " + currencyFormat.format(total) + " (incl. $5 BCA fee)");
        } else {
            totalLabel.setText("Total: " + currencyFormat.format(total));
        }
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
        updateTotalDisplay();
    }

    private void checkout() {
        if (currentOrder.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Order is empty!",
                "Warning",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Calculate final total with BCA fee if applicable
        double total = currentOrder.getTotal();
        double adminFee = (currentPaymentMethod == PaymentMethod.BCA) ? 5.0 : 0.0;
        total += adminFee;

        // Set order details
        currentOrder.setOrderType(currentOrderType.name());
        currentOrder.setPaymentMethod(currentPaymentMethod.name());
        currentOrder.setAdminFee(adminFee);
        currentOrder.setTotal(total);

        if (orderDAO.saveOrder(currentOrder)) {
            JOptionPane.showMessageDialog(this,
                "Order #" + currentOrder.getId() + " saved successfully!\n" +
                "Type: " + currentOrderType + "\n" +
                "Payment: " + currentPaymentMethod + 
                (adminFee > 0 ? " (+$5 fee)" : "") + "\n" +
                "Total: " + currencyFormat.format(total),
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