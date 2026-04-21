package com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.service;

import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.domain.IsolationInventoryItem;
import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.dto.IsolationLevelObservation;
import com.goorm.springmissionsplayground.mission05_spring_db.task07_transaction_isolation_level.repository.IsolationInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IsolationLevelStudyService {

    private final IsolationInventoryRepository isolationInventoryRepository;
    private final IsolationLevelWriteService isolationLevelWriteService;

    public IsolationLevelStudyService(
            IsolationInventoryRepository isolationInventoryRepository,
            IsolationLevelWriteService isolationLevelWriteService
    ) {
        this.isolationInventoryRepository = isolationInventoryRepository;
        this.isolationLevelWriteService = isolationLevelWriteService;
    }

    public Long createSampleItem(String productName, int quantity) {
        return isolationInventoryRepository.save(productName, quantity);
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            isolation = Isolation.READ_COMMITTED
    )
    public IsolationLevelObservation observeReadCommitted(Long itemId, int updatedQuantity) {
        return observeQuantityTwice("READ_COMMITTED", itemId, updatedQuantity);
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            isolation = Isolation.REPEATABLE_READ
    )
    public IsolationLevelObservation observeRepeatableRead(Long itemId, int updatedQuantity) {
        return observeQuantityTwice("REPEATABLE_READ", itemId, updatedQuantity);
    }

    @Transactional(
            transactionManager = "task07TransactionManager",
            readOnly = true
    )
    public IsolationInventoryItem findItem(Long itemId) {
        return isolationInventoryRepository.findById(itemId);
    }

    private IsolationLevelObservation observeQuantityTwice(String isolationLevel, Long itemId, int updatedQuantity) {
        int firstQuantity = isolationInventoryRepository.findQuantityById(itemId);
        isolationLevelWriteService.updateQuantityInNewTransaction(itemId, updatedQuantity);
        int secondQuantity = isolationInventoryRepository.findQuantityById(itemId);
        return new IsolationLevelObservation(isolationLevel, firstQuantity, secondQuantity);
    }
}
