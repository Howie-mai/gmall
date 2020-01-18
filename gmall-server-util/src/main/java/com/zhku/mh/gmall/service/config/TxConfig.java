package com.zhku.mh.gmall.service.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * ClassName：
 * Time：2019/12/28 12:53
 * Description：
 * Author： mh
 */
@Aspect
@Component
@Configuration
public class TxConfig {
    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.zhku.mh.gmall.*.service.impl.*.*(..))";

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Bean
    public TransactionInterceptor txAdvice() {
        DefaultTransactionAttribute txAttr_Required = new DefaultTransactionAttribute();
        txAttr_Required.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        DefaultTransactionAttribute txAttr_Required_ReadOnly = new DefaultTransactionAttribute();
        txAttr_Required_ReadOnly.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        txAttr_Required_ReadOnly.setReadOnly(true);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        source.addTransactionalMethod("add*", txAttr_Required);
        source.addTransactionalMethod("save*", txAttr_Required);
        source.addTransactionalMethod("insert*", txAttr_Required);
        source.addTransactionalMethod("create*", txAttr_Required);
        source.addTransactionalMethod("update*", txAttr_Required);
        source.addTransactionalMethod("set*", txAttr_Required);
        source.addTransactionalMethod("del*", txAttr_Required);
        source.addTransactionalMethod("query*", txAttr_Required_ReadOnly);
        source.addTransactionalMethod("search*", txAttr_Required_ReadOnly);
        source.addTransactionalMethod("get*", txAttr_Required_ReadOnly);
        source.addTransactionalMethod("list*", txAttr_Required_ReadOnly);
        source.addTransactionalMethod("count*", txAttr_Required_ReadOnly);
        return new TransactionInterceptor(transactionManager, source);
    }

    @Bean
    public Advisor txAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdvice());
    }
}
