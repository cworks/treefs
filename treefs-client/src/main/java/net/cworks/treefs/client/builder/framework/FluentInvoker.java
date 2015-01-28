package net.cworks.treefs.client.builder.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class FluentInvoker implements InvocationHandler {

    private Object delegate = null;
    private Class interFace = null;

    public FluentInvoker(Object delegate, Class<?> startClass) {
        this.delegate = delegate;
        this.interFace = startClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null) {
            args = new Object[0];
        }

        MethodMeta methodMeta = method.getAnnotation(MethodMeta.class);

        // do the actual work
        return invokeAndReturn(method, args, proxy, methodMeta);
    }

    @SuppressWarnings("unchecked")
    public final <T> T proxy() {
        return (T) Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            new Class[]{interFace},
            this);
    }

    @SuppressWarnings("unchecked")
    private final <T> T proxy(Class<?> clazz) {
        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{clazz},
                this);
    }

    /**
     *
     * @param method
     * @param originalArgs
     * @param proxy
     * @param methodMeta
     * @return
     */
    private Object invokeAndReturn(Method method, Object[] originalArgs, Object proxy, MethodMeta methodMeta) {

        // create the new arguments and types arrays
        Class<?>[] originalTypes = method.getParameterTypes();

        Method helperMethod;
        // find the helper method
        try {
            helperMethod = delegate.getClass().getMethod(method.getName(), originalTypes);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("internal error", ex);
        }

        // make accessible if not (debatable as to whether this is a good idea)
        if (!helperMethod.isAccessible()) {
            helperMethod.setAccessible(true);
        }

        // invoke method
        Object result;
        try {
            result = helperMethod.invoke(delegate, originalArgs);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) ex.getTargetException();
            } else {
                throw new RuntimeException(ex.getTargetException());
            }
        }

        Object returnValue = computeReturn(method, proxy, methodMeta, result);

        // need to return next fluent interface or return value
        return returnValue;
    }

    private Object computeReturn(Method method, Object proxy, MethodMeta methodMeta, Object result) {

        switch(methodMeta.type()) {
            case Recursive: {
                return proxy;
            }
            case Lateral: {
                return proxy(method.getReturnType());
            }
            case Terminal: {
                return result;
            }
        }

        return null;
    }
}
