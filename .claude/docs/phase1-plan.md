# Phase 1 Build Plan — POS Desktop Client (Java Swing)

Each step is a vertical slice: logic + UI delivered together as a complete feature.
Complete each step and verify with `./gradlew :pos-client:build` before moving on.

## Steps

| Step | Feature / Package | What's Included |
|------|------------------|-----------------|
| 1 | **Foundation** | Shared models (`Product`, `LineItem`, `Transaction`, `TransactionStatus`, `TenderType`, `Receipt`) + event system (`PosEvent` enum, `PosEventListener` interface, `PosEventDispatcher`) |
| 2 | **Pricebook** | `Pricebook` interface + `InMemoryPricebook` with seeded products — pure logic, no UI |
| 3 | **Barcode Scanner** | `BarcodeScanner` interface + `KeyboardBarcodeScanner` (simulates USB HID scanner via buffered keystrokes + Enter) + UPC input field wired into the main frame |
| 4 | **Basket** | Add-item / void-line / void-basket logic in `TransactionService` + basket table panel in the GUI (Quick Add buttons + Void Line + Void Basket) |
| 5 | **Totalling** | Total + tax calculation in `TransactionService` + Total button + totals display area in the GUI (locks further input after press) |
| 6 | **Tender & Payment** | Tender logic (cash, next dollar, debit/credit) + Pay buttons + change-due display in the GUI |
| 7 | **Receipt** | `Receipt` model + receipt generation in `TransactionService` + receipt display dialog shown after tender completes |

## Package Layout (`pos-client`)

```
com.rocketpartners.pos
├── PosApplication.java               entry point
├── event
│   ├── PosEvent.java                 (Step 1)
│   ├── PosEventListener.java         (Step 1)
│   └── PosEventDispatcher.java       (Step 1)
├── model
│   ├── Product.java                  (Step 1)
│   ├── LineItem.java                 (Step 1)
│   ├── TenderType.java               (Step 1)
│   ├── TransactionStatus.java        (Step 1)
│   ├── Transaction.java              (Step 1)
│   └── Receipt.java                  (Step 7)
├── pricebook
│   ├── Pricebook.java                (Step 2)
│   └── InMemoryPricebook.java        (Step 2)
├── scanner
│   ├── BarcodeScanner.java           (Step 3)
│   ├── ScanListener.java             (Step 3)
│   └── KeyboardBarcodeScanner.java   (Step 3)
├── service
│   └── TransactionService.java       (Steps 4, 5, 6, 7)
└── gui
    ├── PosMainFrame.java             (wired up progressively each step)
    ├── components
    │   └── ...                       (reusable Swing components)
    └── panels
        ├── BasketPanel.java          (Step 4)
        ├── TotalsPanel.java          (Step 5)
        ├── TenderPanel.java          (Step 6)
        └── ReceiptDialog.java        (Step 7)
```

## Status

| Step | Status |
|------|--------|
| 1 — Foundation | [ ] Not started |
| 2 — Pricebook | [ ] Not started |
| 3 — Barcode Scanner | [ ] Not started |
| 4 — Basket | [ ] Not started |
| 5 — Totalling | [ ] Not started |
| 6 — Tender & Payment | [ ] Not started |
| 7 — Receipt | [ ] Not started |
