package com.rocketpartners.pos.view;

import com.rocketpartners.pos.controller.BasketController;
import com.rocketpartners.pos.controller.TenderController;
import com.rocketpartners.pos.controller.TotalsController;

import javax.swing.*;
import java.awt.*;

public class PosMainFrame extends JFrame {

    public PosMainFrame(BasketController basketController,
                        TotalsController totalsController,
                        TenderController tenderController) {
        super("MyPOS");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        add(basketController.getView(), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.add(totalsController.getView(), BorderLayout.NORTH);
        rightPanel.add(tenderController.getView(), BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setVisible(true);
    }
}
