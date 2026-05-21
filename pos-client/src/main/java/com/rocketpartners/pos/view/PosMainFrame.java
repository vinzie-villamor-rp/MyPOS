package com.rocketpartners.pos.view;

import com.rocketpartners.pos.controller.BasketController;
import com.rocketpartners.pos.controller.TotalsController;

import javax.swing.*;
import java.awt.*;

public class PosMainFrame extends JFrame {

    public PosMainFrame(BasketController basketController, TotalsController totalsController) {
        super("MyPOS");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        add(basketController.getView(), BorderLayout.CENTER);
        add(totalsController.getView(), BorderLayout.EAST);

        setVisible(true);
    }
}
