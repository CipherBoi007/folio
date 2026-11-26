package com.folio.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
}
