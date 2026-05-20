# Solution Design - "MyPOS: Mock Point of Sale System"

---

## Solution Background

### Context

A Point of Sale (POS) system is the central hub through which retail and service businesses execute sales transactions, manage inventory, and generate customer receipts. As part of the Rocket Partners new hire onboarding program, junior developers are tasked with building a mock POS system from the ground up — progressing through three phases that span desktop application development, network communication, and cloud-based REST services.

This initiative matters because it provides a structured, hands-on way for developers to demonstrate competency across a wide range of real-world engineering disciplines: desktop GUI development, event-driven architecture, client-server socket communication, RESTful API design, containerization, and cloud deployment.

The project is divided into three phases:

- **Phase 1** — A fully functional desktop POS client built with Java Swing
- **Phase 2** — A Virtual Journal server that receives and displays transaction logs over sockets
- **Phase 3** — A Discount Engine REST API built with Spring Boot and deployed to AWS via Docker

---

### Scope

#### In Scope

| ID | Feature / Functionality |
| --- | --- |
| F-01 | Desktop POS application built with Java Swing |
| F-02 | Quick Add buttons for adding products to the basket |
| F-03 | Void Line — remove a single line item from the active basket |
| F-04 | Void Basket — clear all line items from the active transaction |
| F-05 | Total button to finalize the basket (locks further input after press) |
| F-06 | Pay Cash tender flow |
| F-07 | Pay Debit/Credit tender flow |
| F-08 | In-memory pricebook storing product UPCs, names, and prices |
| F-09 | Transaction processing logic (add items, compute subtotal, tax, total) |
| F-10 | Receipt generation at end of transaction |
| F-11 | Virtual Journal server that receives log events over a TCP socket |
| F-12 | POS client emitting transaction log events to the Virtual Journal |
| F-13 | Discount Engine as a Spring Boot REST API |
| F-14 | Discount rules stored in a database (percentage off, fixed amount off, BOGO) |
| F-15 | Docker containerization of the Discount Engine |
| F-16 | Deployment of the Discount Engine to AWS |
| F-17 | Unit tests for core business logic (JUnit) |

#### Out of Scope

| ID | Feature / Functionality | Exclusion Reason |
| --- | --- | --- |
| O-01 | Real barcode scanner hardware integration | Hardware dependency; UPCs are entered manually or via Quick Add buttons |
| O-02 | Persistent transaction storage / database for Phase 1 | Out of scope for the desktop client phase; in-memory state is sufficient |
| O-03 | Customer management (loyalty programs, profiles) | Not required by the onboarding spec |
| O-04 | Inventory tracking / stock depletion | Not required by the onboarding spec |
| O-05 | Real payment gateway integration (Stripe, Square, etc.) | Mock tender flows only; no actual payment processing |
| O-06 | Multi-terminal / multi-lane POS synchronization | Single-terminal emulation only |
| O-07 | Returns and refund flows | Voiding covers in-transaction correction; post-tender returns are out of scope |
| O-08 | Mobile or web-based POS UI | Desktop (Java Swing) only for Phase 1 |

---

### Key Functional Requirements

| ID | Functional Requirement |
| --- | --- |
| FR-01 | As a cashier, I want to add products to the basket via Quick Add buttons, so that I can quickly ring up common items without manually entering a UPC. |
| FR-02 | As a cashier, I want to void a specific line item from the active basket, so that I can correct scanning errors before finalizing the transaction. |
| FR-03 | As a cashier, I want to void the entire basket, so that I can cancel a transaction and start over. |
| FR-04 | As a cashier, I want to press a Total button to finalize the basket, so that the system computes the subtotal, tax, and grand total and prevents further item additions. |
| FR-05 | As a cashier, I want to tender a transaction with cash, so that the system records the payment method and calculates change due. |
| FR-06 | As a cashier, I want to tender a transaction with debit or credit, so that the system records the card payment and completes the transaction. |
| FR-07 | As a cashier, I want a receipt to be generated at the end of each transaction, so that the customer has a printed or digital proof of purchase. |
| FR-08 | As a system, I want to look up product details (name, price) from a pricebook by UPC, so that correct pricing is applied to every line item. |
| FR-09 | As a store manager, I want every transaction event to be logged in the Virtual Journal, so that I have a real-time audit trail of POS activity. |
| FR-10 | As a developer, I want the POS client to send log messages to the Virtual Journal server over TCP sockets, so that transaction events are captured remotely. |
| FR-11 | As the discount engine, I want to receive a transaction payload via REST and return applicable discounts, so that promotions are calculated consistently across channels. |
| FR-12 | As a store manager, I want discount rules (percentage off, fixed amount off, BOGO) to be stored in a database and served by the Discount Engine, so that promotions can be updated without redeploying code. |

---

### Key Design Decisions

| Decision | Rationale | Alternative Considered |
| --- | --- | --- |
| Use Java Swing for the desktop client | Directly specified in the onboarding project requirements; teaches lightweight desktop GUI development | JavaFX — more modern but adds complexity beyond the learning objectives |
| Event-driven architecture using `PosEventListener` / `PosEventDispatcher` interfaces | Decouples business logic from the UI layer; allows components to communicate without tight coupling; recommended explicitly in the spec | Direct method calls between UI and service classes — simpler but creates brittle dependencies |
| Separate business logic from the view layer (MVC-like) | Enables independent unit testing of transaction logic; aligns with the spec's architecture requirement | Putting all logic in Swing event handlers — faster initially but untestable and hard to maintain |
| In-memory pricebook for Phase 1 | Keeps Phase 1 self-contained; no external DB dependency during the learning phase | File-based or SQLite pricebook — adds I/O complexity not needed at this stage |
| TCP sockets for the Virtual Journal (Phase 2) | Directly specified; teaches fundamentals of network I/O and client-server communication | HTTP/REST for logging — higher-level abstraction that bypasses socket learning objectives |
| Spring Boot for the Discount Engine (Phase 3) | Industry-standard framework; directly specified; simplifies REST API scaffolding | Plain Java servlet or Micronaut — less standardized tooling in enterprise contexts |
| Docker + AWS for Phase 3 deployment | Teaches containerization and cloud deployment; specified in the onboarding project | Local-only deployment — does not meet the cloud deployment learning objective |
| JUnit for unit testing | De facto standard Java testing library; widely supported and referenced in the spec | TestNG — equally capable but less commonly used in the target enterprise environment |

---

## Solution Architecture

### Platform Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                        Developer Machine                             │
│                                                                      │
│   ┌─────────────────────────┐     ┌────────────────────────────┐    │
│   │   POS Desktop Client    │     │   Virtual Journal Server   │    │
│   │     (Java Swing)        │────▶│   (Java TCP Socket Server) │    │
│   │      Phase 1 + 2        │     │         Phase 2            │    │
│   └──────────┬──────────────┘     └────────────────────────────┘    │
│              │ HTTP REST                                             │
└──────────────┼───────────────────────────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                          AWS Cloud                                   │
│                                                                      │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │                 ECS / EC2 (Docker Container)                 │   │
│   │                                                             │   │
│   │   ┌──────────────────────────┐    ┌─────────────────────┐  │   │
│   │   │  Discount Engine API     │    │  Relational DB      │  │   │
│   │   │  (Spring Boot REST API)  │───▶│  (Discount Rules)   │  │   │
│   │   │       Phase 3            │    │  RDS / H2           │  │   │
│   │   └──────────────────────────┘    └─────────────────────┘  │   │
│   └─────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

### Logical Diagram

#### Phase 1 — Transaction Flow (Sequence Diagram)

```
Cashier          POS UI (Swing)        PosEventDispatcher     TransactionService      Pricebook
  │                    │                       │                      │                   │
  │── Quick Add ──────▶│                       │                      │                   │
  │                    │── dispatch(ADD_ITEM) ▶│                      │                   │
  │                    │                       │── onAddItem(upc) ───▶│                   │
  │                    │                       │                      │── lookup(upc) ───▶│
  │                    │                       │                      │◀─ Product ────────│
  │                    │                       │                      │ add LineItem       │
  │                    │◀─ updateBasketView ───────────────────────────│                   │
  │                    │                       │                      │                   │
  │── Void Line ───────▶│                      │                      │                   │
  │                    │── dispatch(VOID_LINE)▶│                      │                   │
  │                    │                       │── onVoidLine(idx) ──▶│                   │
  │                    │                       │                      │ remove LineItem    │
  │                    │◀─ updateBasketView ───────────────────────────│                   │
  │                    │                       │                      │                   │
  │── Total ───────────▶│                      │                      │                   │
  │                    │── dispatch(TOTAL) ───▶│                      │                   │
  │                    │                       │── onTotal() ────────▶│                   │
  │                    │                       │                      │ compute totals     │
  │                    │                       │                      │ lock basket        │
  │                    │◀─ showTotals / enable tender buttons ─────────│                   │
  │                    │                       │                      │                   │
  │── Pay Cash ────────▶│                      │                      │                   │
  │                    │── dispatch(TENDER) ──▶│                      │                   │
  │                    │                       │── onTender(CASH) ───▶│                   │
  │                    │                       │                      │ finalize txn       │
  │                    │                       │                      │ generate receipt   │
  │                    │◀─ showReceipt ────────────────────────────────│                   │
```

#### Phase 2 — Virtual Journal Log Flow

```
TransactionService          JournalClient              JournalServer
       │                         │                           │
       │── emit log event ──────▶│                           │
       │   (TXN_START,           │── TCP Socket write ──────▶│
       │    ITEM_ADDED,          │   (log message string)    │ print / display log
       │    ITEM_VOIDED,         │                           │
       │    TXN_TOTAL,           │                           │
       │    TXN_TENDER,          │                           │
       │    TXN_COMPLETE)        │                           │
```

#### Phase 3 — Discount Engine Request Flow

```
POS Client                  Discount Engine API              Database
    │                               │                            │
    │── POST /discounts/calculate ─▶│                            │
    │   { transactionId,            │── query discount rules ───▶│
    │     lineItems: [...] }        │◀─ DiscountRule[] ──────────│
    │                               │ apply rules to line items  │
    │◀─ 200 OK ─────────────────────│                            │
    │   { discounts: [...],         │                            │
    │     discountedTotal }         │                            │
```

#### Phase 1 — Transaction State Machine

```
        ┌────────────┐
        │   IDLE     │◀─────────────────────────────┐
        └─────┬──────┘                              │
              │ First item added                    │
              ▼                                     │
        ┌────────────┐  Void Basket                 │
        │   ACTIVE   │──────────────────────────────┤
        └─────┬──────┘                              │
              │ Total pressed                       │
              ▼                                     │
        ┌────────────┐                              │
        │  TOTALLED  │                              │
        └─────┬──────┘                              │
              │ Tender selected                     │
              ▼                                     │
        ┌────────────┐  Transaction complete        │
        │  TENDERED  │─────────────────────────────▶│
        └────────────┘  (receipt generated,         │
                         reset to IDLE)             │
```

---

## Other Considerations

### Data Models

#### `Product` (Pricebook entry)
| Field | Type | Description |
| --- | --- | --- |
| upc | String | Universal Product Code (unique identifier) |
| name | String | Display name of the product |
| unitPrice | BigDecimal | Price per unit |

#### `LineItem` (item in the active basket)
| Field | Type | Description |
| --- | --- | --- |
| product | Product | Reference to the product in the pricebook |
| quantity | int | Number of units of this product |
| lineTotal | BigDecimal | unitPrice × quantity |

#### `Transaction`
| Field | Type | Description |
| --- | --- | --- |
| transactionId | String / UUID | Unique identifier for the transaction |
| lineItems | List\<LineItem\> | All items in the basket |
| subtotal | BigDecimal | Sum of all line totals |
| taxAmount | BigDecimal | Computed tax on the subtotal |
| total | BigDecimal | subtotal + taxAmount |
| tenderType | TenderType (enum) | CASH, DEBIT_CREDIT |
| amountTendered | BigDecimal | Amount the customer paid (cash flow) |
| changeDue | BigDecimal | amountTendered − total (cash flow) |
| status | TransactionStatus (enum) | IDLE, ACTIVE, TOTALLED, TENDERED |
| timestamp | LocalDateTime | When the transaction was completed |

#### `DiscountRule` (Phase 3 — stored in DB)
| Field | Type | Description |
| --- | --- | --- |
| ruleId | Long | Primary key |
| name | String | Human-readable rule name |
| type | DiscountType (enum) | PERCENTAGE_OFF, FIXED_AMOUNT_OFF, BOGO |
| applicableUpc | String | UPC this rule applies to (null = all items) |
| discountValue | BigDecimal | Percentage or fixed amount |
| active | boolean | Whether the rule is currently enabled |

#### `Receipt`
| Field | Type | Description |
| --- | --- | --- |
| transactionId | String | Reference to the completed transaction |
| lineItems | List\<LineItem\> | Items purchased |
| discounts | List\<String\> | Applied discount descriptions |
| subtotal | BigDecimal | Pre-tax total |
| taxAmount | BigDecimal | Tax charged |
| total | BigDecimal | Grand total |
| tenderType | TenderType | Payment method used |
| amountTendered | BigDecimal | Amount paid |
| changeDue | BigDecimal | Change returned |
| timestamp | LocalDateTime | Transaction timestamp |

---

### Error Handling

#### Phase 1 — Desktop Client

| Scenario | Handling Strategy |
| --- | --- |
| UPC not found in pricebook | Show an error dialog; do not add any line item to the basket |
| Void called with no items in basket | Disable Void Line / Void Basket buttons when basket is empty |
| Total pressed on empty basket | Disable Total button when basket has zero line items |
| Cash tendered less than total | Reject the tender; show an error message prompting for sufficient amount |
| Any action attempted after Total is pressed | All input buttons disabled post-Total; enforced at the UI layer |

#### Phase 2 — Virtual Journal

| Scenario | Handling Strategy |
| --- | --- |
| Journal server not reachable at startup | Log a warning to console; POS continues to function without journal logging |
| Socket write failure mid-transaction | Catch `IOException`; log locally; attempt reconnect on next event |
| Server receives malformed log message | Log and discard the message; do not crash the server |
| Client reconnection after drop | Implement retry logic with exponential backoff (max 3 attempts) |

#### Phase 3 — Discount Engine API

| Scenario | HTTP Status | Response Body |
| --- | --- | --- |
| Invalid or missing transaction payload | 400 Bad Request | `{ "error": "Invalid request payload" }` |
| UPC not found in discount rules | 200 OK | Return transaction total with no discounts applied |
| Database unavailable | 503 Service Unavailable | `{ "error": "Discount service temporarily unavailable" }` |
| Unexpected server error | 500 Internal Server Error | `{ "error": "Internal server error" }` |
