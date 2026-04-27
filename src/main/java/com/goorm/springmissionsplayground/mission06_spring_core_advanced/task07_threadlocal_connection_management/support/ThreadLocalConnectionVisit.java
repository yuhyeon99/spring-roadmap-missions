package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task07_threadlocal_connection_management.support;

public class ThreadLocalConnectionVisit {

    private final String repositoryMethod;
    private final String threadName;
    private final int connectionId;
    private final String databaseProduct;

    public ThreadLocalConnectionVisit(
            String repositoryMethod,
            String threadName,
            int connectionId,
            String databaseProduct
    ) {
        this.repositoryMethod = repositoryMethod;
        this.threadName = threadName;
        this.connectionId = connectionId;
        this.databaseProduct = databaseProduct;
    }

    public String getRepositoryMethod() {
        return repositoryMethod;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public String getDatabaseProduct() {
        return databaseProduct;
    }
}
