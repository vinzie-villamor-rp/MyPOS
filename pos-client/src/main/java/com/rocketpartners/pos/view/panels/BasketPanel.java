package com.rocketpartners.pos.view.panels;

import com.rocketpartners.pos.view.model.BasketRow;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BasketPanel extends JPanel {

    // Common UPCs from pricebook.tsv — label → UPC
    public static final Map<String, String> QUICK_ADD_ITEMS = new LinkedHashMap<>() {{
        put("Monster Energy", "070847811169");
        put("Red Bull 8.4z",  "611269101713");
        put("Coca-Cola 20oz", "049000007893");
        put("Pepsi 20oz",     "012000001291");
        put("Water 16.9oz",   "194283301166");
        put("Lays Classic",   "028400199148");
        put("Snickers KS",    "040000002635");
        put("Doritos Nacho",  "028400003843");
    }};

    private final DefaultTableModel tableModel;
    private final JTable basketTable;
    private final JButton voidLineButton;
    private final JButton voidBasketButton;
    private final Map<String, JButton> quickAddButtons = new LinkedHashMap<>();

    public BasketPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Basket"));

        // --- Basket table ---
        tableModel = new DefaultTableModel(new String[]{"Product", "Qty", "Line Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        basketTable = new JTable(tableModel);
        basketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        basketTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        basketTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        basketTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        add(new JScrollPane(basketTable), BorderLayout.CENTER);

        // --- Action bar ---
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));

        voidLineButton = new JButton("Void Line");
        voidLineButton.setEnabled(false);
        actionBar.add(voidLineButton);

        voidBasketButton = new JButton("Void Basket");
        voidBasketButton.setEnabled(false);
        actionBar.add(voidBasketButton);

        add(actionBar, BorderLayout.SOUTH);

        // --- Quick Add buttons ---
        JPanel quickAddPanel = new JPanel(new GridLayout(0, 2, 4, 4));
        quickAddPanel.setBorder(BorderFactory.createTitledBorder("Quick Add"));
        QUICK_ADD_ITEMS.forEach((label, upc) -> {
            JButton btn = new JButton(label);
            quickAddButtons.put(upc, btn);
            quickAddPanel.add(btn);
        });
        add(quickAddPanel, BorderLayout.EAST);
    }

    // --- Listener registration (called by Controller) ---

    public void addQuickAddListener(String upc, ActionListener listener) {
        JButton btn = quickAddButtons.get(upc);
        if (btn != null) btn.addActionListener(listener);
    }

    public void addVoidLineListener(ActionListener listener) {
        voidLineButton.addActionListener(listener);
    }

    public void addVoidBasketListener(ActionListener listener) {
        voidBasketButton.addActionListener(listener);
    }

    public void addTableSelectionListener(ListSelectionListener listener) {
        basketTable.getSelectionModel().addListSelectionListener(listener);
    }

    public int getSelectedRow() {
        return basketTable.getSelectedRow();
    }

    public void setVoidLineEnabled(boolean enabled) {
        voidLineButton.setEnabled(enabled);
    }

    // --- View refresh (called by Controller with a GUI-shaped view model) ---

    public void refresh(List<BasketRow> rows) {
        tableModel.setRowCount(0);
        for (BasketRow row : rows) {
            tableModel.addRow(new Object[]{ row.name(), row.quantity(), row.lineTotal() });
        }
        boolean hasItems = !rows.isEmpty();
        voidBasketButton.setEnabled(hasItems);
        voidLineButton.setEnabled(false);
        basketTable.clearSelection();
    }

    public void showItemNotFound(String upc) {
        JOptionPane.showMessageDialog(
            this,
            "UPC not found in pricebook: " + upc,
            "Item Not Found",
            JOptionPane.WARNING_MESSAGE
        );
    }

    public void lock() {
        voidLineButton.setEnabled(false);
        voidBasketButton.setEnabled(false);
        quickAddButtons.values().forEach(btn -> btn.setEnabled(false));
    }
}
