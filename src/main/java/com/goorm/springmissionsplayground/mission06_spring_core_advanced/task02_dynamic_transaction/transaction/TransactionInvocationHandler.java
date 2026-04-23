package com.goorm.springmissionsplayground.mission06_spring_core_advanced.task02_dynamic_transaction.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionInvocationHandler implements InvocationHandler {

    private final Object target;
    private final ConsoleTransactionManager transactionManager;

    public TransactionInvocationHandler(Object target, ConsoleTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, args);
        }

        ConsoleTransactionManager.TransactionContext context = transactionManager.begin(method.getName());

        try {
            Object result = method.invoke(target, args);
            transactionManager.inspect(context, TransactionPhase.ACTIVE, true, "비즈니스 로직 실행 완료", null);
            transactionManager.commit(context);
            return result;
        } catch (InvocationTargetException exception) {
            Throwable targetException = exception.getTargetException();
            transactionManager.rollback(context, targetException);
            throw targetException;
        } catch (Throwable throwable) {
            transactionManager.rollback(context, throwable);
            throw throwable;
        }
    }
}
