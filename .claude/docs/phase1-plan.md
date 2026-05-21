# Phase 1 Build Plan — POS Desktop Client (Java Swing)

Each step is a vertical slice: logic + UI delivered together as a complete feature.
Complete each step and verify with `./gradlew :pos-client:build` before moving on.

---

## Conventions

- **Models** — pure data, no business logic; use `record` for immutable, Lombok `@Data @Builder(toBuilder=true) @AllArgsConstructor` for mutable
- **Numeric money values** — `double` throughout (no `BigDecimal`)
- **Event system** — hand-rolled `PosEventListener` / `PosEventDispatcher` (no external library)
- **MVC** — packages: `model`, `event`, `controller`, `view` (panels + components), `pricebook`, `scanner`, `config`
- **Business logic** — lives in `controller` or dedicated repository classes, never in models or views
- **Data access** — Hibernate 6 standalone JPA + H2 in-memory; `EntityManagerFactory` injected via constructor (DIP)
- **Naming** — data access classes are `*Repository`; logic-orchestrating classes are `*Controller`
- **Lombok** — `@RequiredArgsConstructor` for constructor injection; `@Data @AllArgsConstructor @Builder(toBuilder=true)` for mutable models; records for immutable models (no Lombok needed)

---

## Steps

| Step | Feature / Package | Status |
|------|------------------|--------|
| 1 | **Foundation** — Models + event system | ✅ Complete |
| 2 | **Pricebook** — H2 DB + Hibernate + repository | ✅ Complete |
| 3 | **Basket** — Add/void logic + `BasketPanel` | [ ] Not started |
| 4 | **Totalling** — Tax + total calculation + `TotalsPanel` | [ ] Not started |
| 5 | **Tender & Payment** — Payment logic + `TenderPanel` | [ ] Not started |
| 6 | **Receipt** — Receipt model + generation + `ReceiptDialog` | [ ] Not started |
| 7 | **Main Frame** — `PosMainFrame` assembling all panels | [ ] Not started |
| 8 | **Barcode Scanner** — Scanner interfaces + keyboard simulation wired into frame | [ ] Not started |

---

## Step 1 — Foundation ✅

### Completed classes

| Class | Package | Type | Notes |
|---|---|---|---|
| `Product` | `model` | `@Entity` + Lombok (converted in Step 2) | `upc` (`@Id`), `name`, `price (double)` |
| `LineItem` | `model` | `@Data @Builder(toBuilder=true) @AllArgsConstructor` | `product`, `quantity` — no computed fields |
| `TenderType` | `model` | `enum` | `CASH, DEBIT, CREDIT, CHECK, MOBILE_PAYMENT, GIFT_CARD` |
| `TransactionStatus` | `model` | `enum` | `IDLE, ACTIVE, TOTALLED, TENDERED` |
| `Transaction` | `model` | `@Data @Builder(toBuilder=true) @AllArgsConstructor` | `lineItems`, `status`, `subtotal`, `taxAmount`, `total`, `tenderType`, `amountTendered`, `changeDue`, `timestamp` |
| `PosEvent` | `event` | `enum` | `ITEM_SCANNED, ITEM_ADDED, ITEM_VOIDED, BASKET_VOIDED, BASKET_TOTALLED, TENDER_CASH, NEXT_DOLLAR, TENDER_DEBIT, TENDER_CREDIT, TENDER_CHECK, TENDER_MOBILE_PAYMENT, TENDER_GIFT_CARD, TRANSACTION_COMPLETE` |
| `PosEventListener` | `event` | `interface` | Single method: `onPosEvent(PosEvent, Object payload)` |
| `PosEventDispatcher` | `event` | `@RequiredArgsConstructor` | Listeners injected via constructor; immutable list; `dispatch(event, payload)` + `dispatch(event)` overload |

### Design decisions

- `NEXT_DOLLAR` is a `PosEvent` (cashier shortcut action), **not** a `TenderType` (not a payment method)
- `PosEventDispatcher` listeners are constructor-injected — follows Dependency Inversion Principle; no `addListener`/`removeListener`
- Listener registration is the sole responsibility of `PosApplication` at startup
- Line total calculation excluded from `LineItem` — belongs in `BasketController` (Step 3)

---

## Step 2 — Pricebook ✅

### Completed classes

| Class | Package | Type | Notes |
|---|---|---|---|
| `Product` | `model` | `@Entity @Data @NoArgsConstructor @AllArgsConstructor @Builder(toBuilder=true)` | Converted from record; `upc` is `@Id` (natural key) |
| `DatabaseConfig` | `config` | Singleton | Lazy-initializes `EntityManagerFactory` from `persistence.xml`; exposes `shutdown()` |
| `PricebookRepository` | `pricebook` | `interface` | `Optional<Product> findByUpc(String upc)` |
| `H2PricebookRepository` | `pricebook` | `@RequiredArgsConstructor` | Implements `PricebookRepository`; `EntityManagerFactory` injected; JPQL query; opens + closes `EntityManager` per call |
| `persistence.xml` | `resources/META-INF` | Config | Persistence unit `mypos`; H2 in-memory; `hbm2ddl.auto=create`; seeds via `import.sql` |
| `import.sql` | `resources` | SQL | 1000 `INSERT` statements generated from `pricebook.tsv` |

### Design decisions

- **Hibernate standalone** — Spring Boot is reserved for the Discount Engine (Phase 3); using Hibernate here teaches what Spring Boot auto-configures
- **`Product` converted from record to `@Entity` class** — JPA requires a no-arg constructor and mutable access; records don't support this cleanly
- **`upc` as natural `@Id`** — UPC is already unique per product; no surrogate key needed
- **`hbm2ddl.auto=create`** — schema recreated fresh on every run; `import.sql` re-seeds data each time
- **`import.sql` not `data.sql`** — Hibernate's built-in seed file convention (Spring Boot uses `data.sql`; standalone Hibernate uses `import.sql`)
- **`EntityManager` opened and closed per query** — correct for a single-user desktop app; no HikariCP needed
- **`Optional<Product>` return type** — forces callers to handle UPC-not-found explicitly; no silent nulls
- **`DatabaseConfig.shutdown()`** — must be called on JVM exit in `PosApplication`

### Carry-forward constraints

- `H2PricebookRepository` must be constructed with `DatabaseConfig.getEntityManagerFactory()` — wired in `PosApplication`
- Any new `@Entity` added in future steps must be declared in `persistence.xml` under `<class>`
- `import.sql` is the only seeding mechanism — additional seed data goes here

---

## Step 3 — Basket

### Goal
Allow the cashier to add items via Quick Add buttons, void a line, and void the basket. Line total calculation lives here. Introduces `PosMainFrame` as a skeleton and `BasketPanel` as the first visible UI component.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `BasketController` | `controller` | `@RequiredArgsConstructor` + `PosEventListener` | Handles `ITEM_SCANNED` (lookup + add), `ITEM_VOIDED`, `BASKET_VOIDED`; calculates line totals; updates `Transaction` |
| `BasketPanel` | `view/panels` | `JPanel` | Basket table (UPC, name, qty, line total) + Quick Add buttons + Void Line + Void Basket |
| `PosMainFrame` | `view` | `JFrame` | Skeleton frame; hosts `BasketPanel` initially; panels added in Steps 4–6 |

### Dependencies
- `LineItem`, `Transaction`, `TransactionStatus` (Step 1 ✅)
- `PosEvent`, `PosEventListener`, `PosEventDispatcher` (Step 1 ✅)
- `PricebookRepository` / `H2PricebookRepository` (Step 2 ✅)

### Key considerations
- Line total = `product.price() * lineItem.quantity()` — computed in `BasketController`, not in `LineItem`
- If the same UPC is scanned/added twice, increment `quantity` on the existing `LineItem` rather than adding a duplicate row
- Quick Add buttons are hardcoded to a small set of high-frequency UPCs from `pricebook.tsv`
- Void Line button disabled when no row is selected in the basket table; disabled entirely when basket is empty
- Void Basket resets `Transaction.status` to `IDLE`
- `BasketController` dispatches `ITEM_ADDED` / `ITEM_VOIDED` / `BASKET_VOIDED` after mutating `Transaction` so `BasketPanel` can refresh
- UPC not found in pricebook → show error dialog, do not add a line item
- `PosMainFrame` receives `PosEventDispatcher` and `PricebookRepository` via constructor (DIP)
- `PosApplication` updated in this step to wire: `DatabaseConfig` → `H2PricebookRepository` → `BasketController` → `PosEventDispatcher` → `PosMainFrame`

---

## Step 4 — Totalling

### Goal
Compute subtotal, tax, and grand total when Total is pressed. Lock all basket input after that point.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `TotalsController` | `controller` | `@RequiredArgsConstructor` + `PosEventListener` | Handles `BASKET_TOTALLED`; computes subtotal + tax + total; sets `Transaction.status = TOTALLED` |
| `TotalsPanel` | `view/panels` | `JPanel` | Total button + subtotal / tax / grand total display fields |

### Dependencies
- `Transaction`, `TransactionStatus` (Step 1 ✅)
- `PosEvent`, `PosEventListener`, `PosEventDispatcher` (Step 1 ✅)
- `BasketController` / `BasketPanel` / `PosMainFrame` (Step 3)

### Key considerations
- Tax rate defined as a named constant in `TotalsController` (e.g. `TAX_RATE = 0.08`) — not hardcoded inline
- Total button disabled when `Transaction.status == IDLE` (empty basket)
- After `BASKET_TOTALLED` dispatched: Quick Add, Void Line, Void Basket buttons and scanner all disabled
- `TotalsController` is the only place that sets `status = TOTALLED`

---

## Step 5 — Tender & Payment

### Goal
Allow the cashier to select a payment method and complete the transaction.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `TenderController` | `controller` | `@RequiredArgsConstructor` + `PosEventListener` | Handles `TENDER_*` and `NEXT_DOLLAR`; sets `amountTendered`, `changeDue`, `tenderType`, `status = TENDERED`; dispatches `TRANSACTION_COMPLETE` |
| `TenderPanel` | `view/panels` | `JPanel` | Pay Cash, Next Dollar, Pay Debit, Pay Credit, Pay Check, Pay Mobile, Pay Gift Card buttons + cash input field + change-due display |

### Dependencies
- `Transaction`, `TenderType`, `TransactionStatus` (Step 1 ✅)
- `PosEvent`, `PosEventListener`, `PosEventDispatcher` (Step 1 ✅)
- `TotalsController` / `TotalsPanel` (Step 4)

### Key considerations
- All tender buttons disabled until `Transaction.status == TOTALLED`
- `NEXT_DOLLAR` = cash-only shortcut; rounds total up to nearest whole dollar (e.g. $4.37 → tender $5.00 → change $0.63); uses `TenderType.CASH`
- Cash + Next Dollar show a change-due display; all other tender types do not
- Cash input field only shown/enabled when cash tender is selected
- `TenderController` dispatches `TRANSACTION_COMPLETE` with the completed `Transaction` as payload

---

## Step 6 — Receipt

### Goal
Generate an immutable receipt snapshot and display it after tender completes. Reset the system for the next transaction.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `Receipt` | `model` | `record` | Immutable snapshot: `transactionId`, `lineItems`, `subtotal`, `taxAmount`, `total`, `tenderType`, `amountTendered`, `changeDue`, `timestamp` |
| `ReceiptDialog` | `view/panels` | `JDialog` + `PosEventListener` | Listens for `TRANSACTION_COMPLETE`; displays formatted receipt; "New Transaction" button resets to `IDLE` |

### Dependencies
- All models (Step 1 ✅)
- `TenderController` (Step 5) — dispatches `TRANSACTION_COMPLETE` with `Transaction` as payload
- `PosEventListener` (Step 1 ✅)

### Key considerations
- `Receipt` is a `record` — immutable snapshot at moment of tender; resetting `Transaction` afterwards does not affect it
- `ReceiptDialog` builds `Receipt` from the `Transaction` payload of `TRANSACTION_COMPLETE` — does not hold a live reference to `Transaction`
- `lineItems` in `Receipt` must be a defensive copy (`List.copyOf`) — the source list will be cleared on reset
- "New Transaction" resets `Transaction` to a fresh instance and re-enables scanner + basket buttons
- Print receipt to `System.out` in Phase 1 as fallback (physical printer is out of scope)

---

## Step 7 — Main Frame Assembly

### Goal
Assemble all panels into the final `PosMainFrame` layout and wire `PosApplication` as the composition root.

### Work to do

| Task | Notes |
|---|---|
| Finalise `PosMainFrame` layout | Arrange `BasketPanel`, `TotalsPanel`, `TenderPanel` in a logical grid/border layout |
| Update `PosApplication` | Full wiring: `DatabaseConfig` → `H2PricebookRepository` → all controllers → `PosEventDispatcher` → `PosMainFrame` |
| End-to-end manual test | Scan items → Total → Tender → Receipt → New Transaction |

### Dependencies
- All steps 3–6

---

## Step 8 — Barcode Scanner

### Goal
Simulate a USB HID barcode scanner via keyboard input and wire it into the completed frame so scanned UPCs feed into the existing basket flow.

### Classes to create

| Class | Package | Type | Notes |
|---|---|---|---|
| `ScanListener` | `scanner` | `interface` | `void onBarcodeScanned(String upc)` |
| `BarcodeScanner` | `scanner` | `interface` | `startScanning()`, `stopScanning()` |
| `KeyboardBarcodeScanner` | `scanner` | `@RequiredArgsConstructor` | Implements `BarcodeScanner` + `KeyEventDispatcher`; buffers keystrokes, flushes UPC on Enter; dispatches `ITEM_SCANNED` |

### Dependencies
- `PosEvent.ITEM_SCANNED` (Step 1 ✅)
- `PosEventDispatcher` (Step 1 ✅)
- `PosMainFrame` / `BasketController` (Steps 3, 7)

### Key considerations
- Real USB scanners enumerate as HID keyboards — they send characters followed by `\r` or `\n`; `KeyboardBarcodeScanner` matches this behaviour
- Scanner dispatches `ITEM_SCANNED` with the raw UPC string as payload — `BasketController` owns the pricebook lookup
- Scanner must be stopped when `Transaction.status == TOTALLED` — no scanning after Total is pressed
- `ScanListener` is separate from `PosEventListener` — scanner is infrastructure, not a POS business event
- Scanner wired into `PosMainFrame` via `KeyboardFocusManager` so it captures keystrokes globally regardless of focused component

---

## Package Layout (`pos-client`)

```
com.rocketpartners.pos
├── PosApplication.java                     entry point — wires all components
├── config
│   └── DatabaseConfig.java                 ✅ Step 2
├── model
│   ├── Product.java                        ✅ Steps 1+2 (@Entity)
│   ├── LineItem.java                       ✅ Step 1
│   ├── TenderType.java                     ✅ Step 1
│   ├── TransactionStatus.java              ✅ Step 1
│   ├── Transaction.java                    ✅ Step 1
│   └── Receipt.java                        Step 6
├── event
│   ├── PosEvent.java                       ✅ Step 1
│   ├── PosEventListener.java               ✅ Step 1
│   └── PosEventDispatcher.java             ✅ Step 1
├── pricebook
│   ├── PricebookRepository.java            ✅ Step 2
│   └── H2PricebookRepository.java          ✅ Step 2
├── scanner
│   ├── ScanListener.java                   Step 8
│   ├── BarcodeScanner.java                 Step 8
│   └── KeyboardBarcodeScanner.java         Step 8
├── controller
│   ├── BasketController.java               Step 3
│   ├── TotalsController.java               Step 4
│   └── TenderController.java               Step 5
└── view
    ├── PosMainFrame.java                   Step 3 (skeleton), finalised Step 7
    ├── panels
    │   ├── BasketPanel.java                Step 3
    │   ├── TotalsPanel.java                Step 4
    │   ├── TenderPanel.java                Step 5
    │   └── ReceiptDialog.java              Step 6
    └── components
        └── ...                             reusable Swing widgets as needed

resources
├── META-INF
│   └── persistence.xml                     ✅ Step 2
└── import.sql                              ✅ Step 2 (1000 products from pricebook.tsv)
```
