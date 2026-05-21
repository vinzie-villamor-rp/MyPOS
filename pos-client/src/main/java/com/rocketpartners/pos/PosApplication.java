package com.rocketpartners.pos;

import com.rocketpartners.pos.config.DatabaseConfig;
import com.rocketpartners.pos.controller.BasketController;
import com.rocketpartners.pos.controller.TotalsController;
import com.rocketpartners.pos.event.PosEventDispatcher;
import com.rocketpartners.pos.pricebook.H2PricebookRepository;
import com.rocketpartners.pos.view.PosMainFrame;
import com.rocketpartners.pos.view.panels.BasketPanel;
import com.rocketpartners.pos.view.panels.TotalsPanel;

import javax.swing.*;

public class PosApplication {

    public static void main(String[] args) {
        // 1. Data layer
        H2PricebookRepository pricebookRepository =
                new H2PricebookRepository(DatabaseConfig.getEntityManagerFactory());

        // 2. Dispatcher
        PosEventDispatcher dispatcher = new PosEventDispatcher();

        // 3. Views
        BasketPanel basketPanel = new BasketPanel();
        TotalsPanel totalsPanel = new TotalsPanel();

        // 4. Controllers
        BasketController basketController = new BasketController(
                pricebookRepository, dispatcher, basketPanel);
        TotalsController totalsController = new TotalsController(
                dispatcher, totalsPanel);

        // 5. Register listeners
        dispatcher.addListener(basketController);
        dispatcher.addListener(totalsController);

        SwingUtilities.invokeLater(() -> {
            new PosMainFrame(basketController, totalsController);
            Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConfig::shutdown));
        });
    }
}
