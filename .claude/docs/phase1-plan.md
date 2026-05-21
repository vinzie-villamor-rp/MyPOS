# Phase 1 Build Plan — POS Desktop Client (Java Swing)

Each step is a vertical slice: logic + UI delivered together as a complete feature.
Complete each step and verify with `./gradlew :pos-client:build` before moving on.

---

## Conventions

- **Models** — pure data, no business logic; use `record` for immutable, Lombok `@Data @Builder(toBuilder=true) @AllArgsConstructor` for mutable
- **Numeric money values** — `double` throughout (no `BigDecimal`)
- **Event system** — hand-rolled `PosEventListener` / `PosEventDispatcher` (no external library)
- **MVC** — packages: `model`, `event`, `controller`, `view` (panels + components)
- **Business logic** — lives in `controller` or dedicated service classes, never in models or views

---

## Steps

| Step | Feature / Package | What's Included |
|------|------------------|-----------------|
| 1 | **Foundation** ✅ | Models + event system (see detail below) |
| 2 | **Pricebook** | `Pricebook` interface + `InMemoryPricebook` loaded from `pricebook.tsv` |
| 3 | **Barcode Scanner** | `BarcodeScanner` interface + `KeyboardBarcodeScanner` + UPC input field in the main frame |
| 4 | **Basket** | `BasketController` (add/void-line/void-basket + line total calculation) + `BasketPanel` (table + Quick Add buttons + Void Line + Void Basket) |
| 5 | **Totalling** | `TotalsController` (subtotal + tax + grand total calculation) + Total button + `TotalsPanel` (locks input after press) |
| 6 | **Tender & Payment** | `TenderController` (cash, next-dollar shortcut, debit, credit, check, mobile, gift card) + `TenderPanel` (Pay buttons + change-due display) |
| 7 | **Receipt** | `Receipt` model + receipt generation in `TenderController` + `ReceiptDialog` shown after tender completes |

---

## Step 1 — Foundation ✅

### Completed classes

| Class | Package | Type | Notes |
|---|---|---|---|
| `Product` | `model` | `record` | `upc`, `name`, `price (double)` |
| `LineItem` | `model` | `@Data @Builder(toBuilder=true)` | `product`, `quantity` — no computed fields |
| `TenderType` | `model` | `enum` | `CASH, DEBIT, CREDIT, CHECK, MOBILE_PAYMENT, GIFT_CARD` |
| `TransactionStatus` | `model` | `enum` | `IDLE, ACTIVE, TOTALLED, TENDERED` |
| `Transaction` | `model` | `@Data @Builder(toBuilder=true)` | `lineItems`, `status`, `subtotal`, `taxAmount`, `total`, `tenderType`, `amountTendered`, `changeDue`, `timestamp` |
| `PosEvent` | `event` | `enum` | `ITEM_SCANNED, ITEM_ADDED, ITEM_VOIDED, BASKET_VOIDED, BASKET_TOTALLED, TENDER_CASH, NEXT_DOLLAR, TENDER_DEBIT, TENDER_CREDIT, TENDER_CHECK, TENDER_MOBILE_PAYMENT, TENDER_GIFT_CARD, TRANSACTION_COMPLETE` |
| `PosEventListener` | `event` | `interface` | Single method: `onPosEvent(PosEvent, Object payload)` |
| `PosEventDispatcher` | `event` | `@RequiredArgsConstructor` | Listeners injected via constructor; `dispatch(event, payload)` + `dispatch(event)` overload |

### Design decisions made in Step 1

- `NEXT_DOLLAR` is a `PosEvent` (cashier action), **not** a `TenderType` (not a payment method)
- `PosEventDispatcher` listeners are constructor-injected and immutable — follows Dependency Inversion Principle
- No `addListener`/`removeListener` — listener registration is the responsibility of `PosApplication` at startup
- Line total calculation excluded from `LineItem` — belongs in `BasketController` (Step 4)

---

## Step 2 — Pricebook

### Goal
Load the `pricebook.tsv` file into memory and expose a lookup-by-UPC API. This is a prerequisite for the scanner (Step 3) and basket (Step 4) — both need to resolve a UPC to a `Product`.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `Pricebook` | `pricebook` | `interface` | `Optional<Product> findByUpc(String upc)` |
| `InMemoryPricebook` | `pricebook` | `class` | Parses `pricebook.tsv` at construction; implements `Pricebook` |

### Dependencies
- `Product` (Step 1 ✅)

### Key considerations
- TSV format: `upc \t name \t price` — tab-separated, one product per line
- UPC values in the file vary: some are numeric strings, some have leading zeros — store as `String`, never parse to numeric
- `findByUpc` returns `Optional<Product>` — callers must handle the not-found case (scanner will show an error, basket won't add the item)
- `pricebook.tsv` will live in `src/main/resources` so it's on the classpath at runtime

---

## Step 3 — Barcode Scanner

### Goal
Simulate a USB barcode scanner (which behaves as a HID keyboard) and wire it into the main frame so scanned UPCs trigger `ITEM_SCANNED` events.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `ScanListener` | `scanner` | `interface` | `void onBarcodeScanned(String upc)` |
| `BarcodeScanner` | `scanner` | `interface` | `startScanning()`, `stopScanning()`, `addScanListener()`, `removeScanListener()` |
| `KeyboardBarcodeScanner` | `scanner` | `class` | Implements `BarcodeScanner`; buffers keystrokes, flushes on Enter via `KeyEventDispatcher` |
| `PosMainFrame` | `view` | `JFrame` | Entry point for the view; wires scanner → dispatcher |

### Dependencies
- `PosEvent` (Step 1 ✅)
- `PosEventDispatcher` (Step 1 ✅)
- `Pricebook` (Step 2)

### Key considerations
- Real USB scanners send characters followed by `\r` or `\n` — `KeyboardBarcodeScanner` must match this
- Scanner fires `ITEM_SCANNED` with the UPC string as payload; `BasketController` (Step 4) handles the lookup
- Scanner must be stopped when the transaction is `TOTALLED` — no input after Total is pressed
- `PosMainFrame` is introduced here as a skeleton; panels are added progressively in Steps 4–7

---

## Step 4 — Basket

### Goal
Allow the cashier to add items, void a line, and void the whole basket. This is where line total calculation lives.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `BasketController` | `controller` | `class` | Implements `PosEventListener`; handles `ITEM_SCANNED`, `ITEM_VOIDED`, `BASKET_VOIDED`; calculates line totals |
| `BasketPanel` | `view/panels` | `JPanel` | Basket table (product, qty, line total) + Quick Add buttons + Void Line + Void Basket |

### Dependencies
- `LineItem`, `Transaction`, `TransactionStatus` (Step 1 ✅)
- `PosEvent`, `PosEventListener`, `PosEventDispatcher` (Step 1 ✅)
- `Pricebook` (Step 2)
- `BarcodeScanner` / `KeyboardBarcodeScanner` (Step 3)

### Key considerations
- Line total = `product.price() * lineItem.quantity()` — computed in `BasketController`, not in `LineItem`
- Quick Add buttons are hardcoded to common UPCs from `pricebook.tsv`
- Void Line requires a selected row in the basket table — Void Line button disabled when nothing is selected
- Void Basket clears all items and resets `TransactionStatus` to `IDLE`
- `BasketController` updates `Transaction.lineItems` and dispatches `ITEM_ADDED` / `ITEM_VOIDED` / `BASKET_VOIDED` back so the view can refresh

---

## Step 5 — Totalling

### Goal
Compute subtotal, tax, and grand total when the cashier presses Total, and lock all basket input.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `TotalsController` | `controller` | `class` | Implements `PosEventListener`; handles `BASKET_TOTALLED`; computes subtotal + tax + total on `Transaction` |
| `TotalsPanel` | `view/panels` | `JPanel` | Total button + subtotal / tax / grand total display; disables basket input after press |

### Dependencies
- `Transaction`, `TransactionStatus` (Step 1 ✅)
- `PosEvent`, `PosEventListener`, `PosEventDispatcher` (Step 1 ✅)
- `BasketController` / `BasketPanel` (Step 4)

### Key considerations
- Tax rate is a constant (configurable later) — define as a named constant in `TotalsController`
- Total button must be disabled when basket is `IDLE` (no items)
- After `BASKET_TOTALLED` is dispatched, `BasketPanel` Quick Add buttons, Void Line, Void Basket, and scanner must all be disabled
- `TotalsController` sets `Transaction.status = TOTALLED` after computing totals

---

## Step 6 — Tender & Payment

### Goal
Allow the cashier to select a payment method and complete the transaction.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `TenderController` | `controller` | `class` | Implements `PosEventListener`; handles all `TENDER_*` and `NEXT_DOLLAR` events; sets `amountTendered`, `changeDue`, `status = TENDERED` |
| `TenderPanel` | `view/panels` | `JPanel` | Pay Cash, Next Dollar, Pay Debit, Pay Credit, Pay Check, Pay Mobile, Pay Gift Card buttons + cash input field + change-due display |

### Dependencies
- `Transaction`, `TenderType`, `TransactionStatus` (Step 1 ✅)
- `PosEvent`, `PosEventListener`, `PosEventDispatcher` (Step 1 ✅)
- `TotalsController` / `TotalsPanel` (Step 5)

### Key considerations
- Tender buttons only enabled after `BASKET_TOTALLED`
- `NEXT_DOLLAR` rounds `amountTendered` up to the nearest whole dollar (e.g. total $4.37 → tender $5.00 → change $0.63) — this is a cash-only shortcut
- Cash tender requires an input field for the tendered amount; debit/credit/check/mobile/gift card do not
- `changeDue` only meaningful for `CASH` and `NEXT_DOLLAR`
- `TenderController` dispatches `TRANSACTION_COMPLETE` after setting status to `TENDERED`

---

## Step 7 — Receipt

### Goal
Generate a receipt and display it to the cashier after the transaction completes.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `Receipt` | `model` | `record` | Snapshot of the completed transaction: `transactionId`, `lineItems`, `subtotal`, `taxAmount`, `total`, `tenderType`, `amountTendered`, `changeDue`, `timestamp` |
| `ReceiptDialog` | `view/panels` | `JDialog` | Displays formatted receipt; has a "New Transaction" button that resets state to `IDLE` |

### Dependencies
- All models (Steps 1 ✅)
- `TenderController` (Step 6) — generates the `Receipt` and dispatches `TRANSACTION_COMPLETE` with it as payload
- `PosEventListener` (Step 1 ✅) — `ReceiptDialog` listens for `TRANSACTION_COMPLETE`

### Key considerations
- `Receipt` is an immutable snapshot — a `record` — taken at the moment of tender; subsequent state resets don't affect it
- `ReceiptDialog` receives the `Receipt` object as the payload of `TRANSACTION_COMPLETE`
- "New Transaction" button resets `Transaction` to a fresh instance and re-enables all basket input
- Consider printing the receipt to console (`System.out`) as a fallback for Phase 1 (actual printer is Phase 3+ territory)

---

## Package Layout (`pos-client`)

```
com.rocketpartners.pos
├── PosApplication.java                     entry point — wires all controllers + view
├── model
│   ├── Product.java                        ✅ Step 1
│   ├── LineItem.java                       ✅ Step 1
│   ├── TenderType.java                     ✅ Step 1
│   ├── TransactionStatus.java              ✅ Step 1
│   ├── Transaction.java                    ✅ Step 1
│   └── Receipt.java                        Step 7
├── event
│   ├── PosEvent.java                       ✅ Step 1
│   ├── PosEventListener.java               ✅ Step 1
│   └── PosEventDispatcher.java             ✅ Step 1
├── pricebook
│   ├── Pricebook.java                      Step 2
│   └── InMemoryPricebook.java              Step 2
├── scanner
│   ├── ScanListener.java                   Step 3
│   ├── BarcodeScanner.java                 Step 3
│   └── KeyboardBarcodeScanner.java         Step 3
├── controller
│   ├── BasketController.java               Step 4
│   ├── TotalsController.java               Step 5
│   └── TenderController.java               Step 6
└── view
    ├── PosMainFrame.java                   Step 3 (skeleton), extended Steps 4–7
    ├── panels
    │   ├── BasketPanel.java                Step 4
    │   ├── TotalsPanel.java                Step 5
    │   ├── TenderPanel.java                Step 6
    │   └── ReceiptDialog.java              Step 7
    └── components
        └── ...                             reusable Swing widgets as needed
```

---

## Status

| Step | Status |
|------|--------|
| 1 — Foundation | ✅ Complete |
| 2 — Pricebook | [ ] Not started |
| 3 — Barcode Scanner | [ ] Not started |
| 4 — Basket | [ ] Not started |
| 5 — Totalling | [ ] Not started |
| 6 — Tender & Payment | [ ] Not started |
| 7 — Receipt | [ ] Not started |
