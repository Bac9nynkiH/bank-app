# Design Choices for the Banking App

## Overview

This document outlines the design choices made for the development of the banking application. The focus is on simplicity and safety while ensuring a smooth operational workflow.

## Design Choices

### 1. Simplicity in Transaction Handling

- **No Message Brokers:**
    - To maintain simplicity, the application does not use message brokers for handling transaction requests. This decision was made to avoid the complexity of integrating and managing an additional messaging infrastructure.

- **No Tracking of Transaction Status:**
    - The application does not keep track of transactions being processed or failed. This approach simplifies the system design and reduces the overhead of managing transaction state information.

### 2. Ensuring Safety of Operations

- **Lock Mechanism in Database:**
    - To ensure the safety and integrity of operations, the application employs a lock mechanism within the database. This prevents concurrent transactions from causing inconsistencies and ensures that operations are processed sequentially and correctly.

- **Transactional Management:**
    - The application uses database transactions to manage operations atomically. This means that operations either complete successfully as a whole or are rolled back in case of errors, ensuring data consistency and integrity.

## Conclusion

The design choices made prioritize simplicity and safety. By avoiding message brokers and transaction tracking, the application remains straightforward while relying on database locks and transactions to ensure reliable and secure operations.
