package org.jungppo.bambooforest.global.log.service;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jungppo.bambooforest.global.log.dto.RequestLogDto;
import org.springframework.aop.framework.ProxyFactory;

@RequiredArgsConstructor
public class ConnectionProxyInterceptor implements MethodInterceptor {

    private static final String JDBC_PREPARE_STATEMENT_METHOD_NAME = "prepareStatement";
    private static final String HIKARI_CONNECTION_NAME = "HikariProxyConnection";

    private final Object connection;
    private final RequestLogDto requestLogDto;

    @Nullable
    @Override
    public Object invoke(@Nonnull final MethodInvocation invocation) throws Throwable {
        final Object result = invocation.proceed();

        if (hasConnection(result) && hasPreparedStatementInvoked(invocation)) {
            final ProxyFactory proxyFactory = new ProxyFactory(result);
            proxyFactory.addAdvice(new PreparedStatementProxyInterceptor(requestLogDto));
            return proxyFactory.getProxy();
        }

        return result;
    }

    private boolean hasPreparedStatementInvoked(final MethodInvocation invocation) {
        final Object targetObject = invocation.getThis();
        if (targetObject == null) {
            return false;
        }
        final Class<?> targetClass = targetObject.getClass();
        final Method targetMethod = invocation.getMethod();
        return targetClass.getName().contains(HIKARI_CONNECTION_NAME) &&
                targetMethod.getName().equals(JDBC_PREPARE_STATEMENT_METHOD_NAME);
    }

    private boolean hasConnection(final Object result) {
        return result != null;
    }

    public Object getProxy() {
        final ProxyFactory proxyFactory = new ProxyFactory(connection);
        proxyFactory.addAdvice(this);
        return proxyFactory.getProxy();
    }
}
