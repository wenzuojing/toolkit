package org.wens.toolkit.query.mybatis;

import org.wens.toolkit.query.annotation.*;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author wens
 */
public class ExampleUtils {

    private static final String CONDITION_PKG = Condition.class.getName().substring(0, Condition.class.getName().lastIndexOf("."));

    private static final String methodFormat = "and%s%s";

    private static ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Future<Method>>> classMethods = new ConcurrentHashMap<>();

    public static void autoFillCriteria(Object criteria, Object query) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ExecutionException, InterruptedException {

        for (Field field : getFields(query.getClass())) {

            Annotation annotation = getAnnotation(field);
            if (annotation == null) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(query);
            if (value == null && !(annotation instanceof IsNull) && !(annotation instanceof IsNotNull)) {
                continue;
            }

            if (annotation instanceof EqualTo) {
                setSimpleValue(criteria, value, field, upperCaseFirst(defaultString(((EqualTo) annotation).value(), field.getName())), EqualTo.class.getSimpleName());
            } else if (annotation instanceof NotEqualTo) {
                setSimpleValue(criteria, value, field, upperCaseFirst(defaultString(((NotEqualTo) annotation).value(), field.getName())), NotEqualTo.class.getSimpleName());
            } else if (annotation instanceof GreaterThan) {
                setSimpleValue(criteria, value, field, upperCaseFirst(defaultString(((GreaterThan) annotation).value(), field.getName())), GreaterThan.class.getSimpleName());
            } else if (annotation instanceof GreaterThanOrEqualTo) {
                setSimpleValue(criteria, value, field, upperCaseFirst(defaultString(((GreaterThanOrEqualTo) annotation).value(), field.getName())), GreaterThanOrEqualTo.class.getSimpleName());
            } else if (annotation instanceof LessThan) {
                setSimpleValue(criteria, value, field, upperCaseFirst(defaultString(((LessThan) annotation).value(), field.getName())), LessThan.class.getSimpleName());
            } else if (annotation instanceof LessThanOrEqualTo) {
                setSimpleValue(criteria, value, field, upperCaseFirst(defaultString(((LessThanOrEqualTo) annotation).value(), field.getName())), LessThanOrEqualTo.class.getSimpleName());
            } else if (annotation instanceof In) {
                setCollectOrArrayValue(criteria, value, field, upperCaseFirst(defaultString(((In) annotation).value(), field.getName())), In.class.getSimpleName());
            } else if (annotation instanceof NotIn) {
                setCollectOrArrayValue(criteria, value, field, upperCaseFirst(defaultString(((NotIn) annotation).value(), field.getName())), NotIn.class.getSimpleName());
            } else if (annotation instanceof Between) {
                setTwoValue(criteria, value, field, upperCaseFirst(defaultString(((Between) annotation).value(), field.getName())), Between.class.getSimpleName());
            } else if (annotation instanceof NotBetween) {
                setTwoValue(criteria, value, field, upperCaseFirst(defaultString(((NotBetween) annotation).value(), field.getName())), NotBetween.class.getSimpleName());
            } else if (annotation instanceof IsNull) {
                setNoValue(criteria, upperCaseFirst(defaultString(((IsNull) annotation).value(), field.getName())), IsNull.class.getSimpleName());
            } else if (annotation instanceof IsNotNull) {
                setNoValue(criteria, upperCaseFirst(defaultString(((IsNotNull) annotation).value(), field.getName())), IsNotNull.class.getSimpleName());
            }

        }

    }

    private static void setTwoValue(Object criteria, Object fieldValue, Field field, String filedName, String conditionName) throws ExecutionException, InterruptedException, IllegalAccessException, InvocationTargetException {
        Method method = findMethod(criteria.getClass(), String.format(methodFormat, filedName, conditionName));
        if (method == null) {
            throw new RuntimeException("Not found method " + criteria.getClass().getName() + "." + String.format(methodFormat, filedName, conditionName));
        }

        if (!field.getType().isArray() && !Collection.class.isAssignableFrom(field.getType())) {
            throw new RuntimeException("Only support Array or Collect type for " + conditionName);
        }


        List<Object> twoValues = new ArrayList<>(2);

        if (field.getType().isArray()) {
            Object[] array = (Object[]) fieldValue;
            if (array.length != 2) {
                throw new RuntimeException("Must has two value for " + conditionName);
            }
            for (int i = 0; i < 2; i++) {
                twoValues.add(toCase(array[i], method.getParameterTypes()[i]));
            }

        } else {
            Collection collection = (Collection) fieldValue;
            if (collection.size() != 2) {
                throw new RuntimeException("Must has two value for " + conditionName);
            }

            int i = 0;
            for (Object v : collection) {
                twoValues.add(toCase(v, method.getParameterTypes()[i++]));
            }
        }

        method.invoke(criteria, twoValues.get(0), twoValues.get(1));
    }

    private static void setCollectOrArrayValue(Object criteria, Object fieldValue, Field field, String filedName, String conditionName) throws ExecutionException, InterruptedException, IllegalAccessException, InvocationTargetException {
        Method method = findMethod(criteria.getClass(), String.format(methodFormat, filedName, conditionName));
        if (method == null) {
            throw new RuntimeException("Not found method " + criteria.getClass().getName() + "." + String.format(methodFormat, filedName, conditionName));
        }

        if (!field.getType().isArray() && !Collection.class.isAssignableFrom(field.getType())) {
            throw new RuntimeException("Only support Array or Collect type for " + conditionName);
        }

        Object value = getValue(fieldValue, field, method.getParameterTypes()[0]);
        method.invoke(criteria, value);
    }

    private static void setNoValue(Object criteria, String filedName, String conditionName) throws ExecutionException, InterruptedException, IllegalAccessException, InvocationTargetException {
        Method method = findMethod(criteria.getClass(), String.format(methodFormat, filedName, conditionName));
        if (method == null) {
            throw new RuntimeException("Not found method " + criteria.getClass().getName() + "." + String.format(methodFormat, filedName, conditionName));
        }
        method.invoke(criteria);
    }

    private static void setSimpleValue(Object criteria, Object fieldValue, Field field, String filedName, String conditionName) throws ExecutionException, InterruptedException, IllegalAccessException, InvocationTargetException {
        Method method = findMethod(criteria.getClass(), String.format(methodFormat, filedName, conditionName));
        if (method == null) {
            throw new RuntimeException("Not found method " + criteria.getClass().getName() + "." + String.format(methodFormat, filedName, conditionName));
        }

        if (field.getType().isArray() || Collection.class.isAssignableFrom(field.getType())) {
            throw new RuntimeException("Not support Array or Collect type for " + conditionName);
        }

        Object value = getValue(fieldValue, field, method.getParameterTypes()[0]);
        method.invoke(criteria, value);
    }

    private static Object getValue(Object value, Field field, Class<?> aClass) throws IllegalAccessException {
        if (field.getType().isArray() || Collection.class.isAssignableFrom(field.getType())) {
            if (field.getType().isArray()) {
                if (Collection.class.isAssignableFrom(aClass)) {
                    Object[] array = (Object[]) value;
                    Collection<Object> collection = null;
                    if (aClass.isAssignableFrom(ArrayList.class)) {
                        collection = new ArrayList<>(array.length);
                    } else if (aClass.isAssignableFrom(LinkedList.class)) {
                        collection = new LinkedList<>();
                    } else {
                        throw new RuntimeException("Not support convert array to " + aClass.getName());
                    }
                    for (Object v : array) {
                        collection.add(v);
                    }
                    return collection;
                } else {
                    return ConvertUtils.convert(value, aClass);
                }
            } else {
                return ConvertUtils.convert(value, aClass);
            }
        } else {
            return toCase(value, aClass);
        }

    }

    private static Object toCase(Object value, Class<?> aClass) {
        if (value.getClass() == aClass) {
            return value;
        } else {
            return ConvertUtils.convert(value, aClass);
        }
    }

    private static String upperCaseFirst(String str) {
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static String defaultString(String str, String defaultStr) {
        if (str == null || "".equals(str)) {
            return defaultStr;
        } else {
            return str;
        }
    }

    private static Method findMethod(Class<?> aClass, String name) throws ExecutionException, InterruptedException {
        ConcurrentHashMap<String, Future<Method>> methodsOfClass = classMethods.get(aClass);

        if (methodsOfClass == null) {

            methodsOfClass = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, Future<Method>> preMethodsOfClass = classMethods.putIfAbsent(aClass, methodsOfClass);
            if (preMethodsOfClass != null) {
                methodsOfClass = preMethodsOfClass;
            }
        }

        Future<Method> methodFuture = methodsOfClass.get(name);
        if (methodFuture != null) {
            return methodFuture.get();
        }
        methodFuture = new FutureTask<>(() -> {
            for (Method method : aClass.getMethods()) {
                if (method.getName().equals(name)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            return null;
        });

        Future<Method> preMethod = methodsOfClass.putIfAbsent(name, methodFuture);

        if (preMethod != null) {
            return preMethod.get();
        }
        ((FutureTask<Method>) methodFuture).run();

        return methodFuture.get();
    }

    private static Field[] getFields(Class<?> aClass) {

        List<Field> fields = new ArrayList<>(aClass.getDeclaredFields().length * 2);
        while (true) {
            fields.addAll(Arrays.asList(aClass.getDeclaredFields()));
            aClass = aClass.getSuperclass();
            if (aClass == null) {
                break;
            }
        }
        return fields.toArray(new Field[fields.size()]);
    }

    private static Annotation getAnnotation(Field field) {
        Annotation[] annotations = field.getAnnotations();
        if (annotations == null) {
            return null;
        }
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType().getName().startsWith(CONDITION_PKG)) {
                return annotations[i];
            }
        }
        return null;
    }

}
