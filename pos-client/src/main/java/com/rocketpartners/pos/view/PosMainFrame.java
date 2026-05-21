package com.rocketpartners.pos.view;

import com.rocketpartners.pos.controller.BasketController;
import com.rocketpartners.pos.controller.TenderController;
import com.rocketpartners.pos.controller.TotalsController;
import com.rocketpartners.pos.event.PosEventDispatcher;
import com.rocketpartners.pos.view.panels.ReceiptDialog;

import javax.swing.*;
import java.awt.*;

public class PosMainFrame extends JFrame {

    private final ReceiptDialog receiptDialog;

    public PosMainFrame(BasketController basketController,
                        TotalsController totalsController,
                        TenderController tenderController,
                        PosEventDispatcher dispatcher) {
        super("MyPOS");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        add(basketController.getView(), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.add(totalsController.getView(), BorderLayout.NORTH);
        rightPanel.add(tenderController.getView(), BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        receiptDialog = new ReceiptDialog(this, dispatcher);

        setVisible(true);
    }

    public ReceiptDialog getReceiptDialog() {
        return receiptDialog;
    }
}
