package org.wens.toolkit.query;


import org.wens.toolkit.query.annotation.*;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.wens.toolkit.query.annotation.Condition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author wens
 */
public class QueryHelper {

    public static <T> void autoSet(QueryExpressionDSL<MyBatis3SelectModelAdapter<T>>.QueryExpressionWhereBuilder queryExpressionWhereBuilder , SqlTable sqlTable , Object query ){

        Field[] fields = query.getClass().getDeclaredFields();

        for (Field field : fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations){

                if(!annotation.annotationType().isAnnotationPresent(Condition.class)){
                    continue;
                }
                if(annotation instanceof IsEqualTo){
                    isEqualTo(queryExpressionWhereBuilder, sqlTable, query, field, (IsEqualTo)annotation);
                }else if(annotation instanceof IsIn){
                    isIn(queryExpressionWhereBuilder, sqlTable, query, field, (IsIn)annotation);
                }else if(annotation instanceof IsBetween){
                    isBetween(queryExpressionWhereBuilder, sqlTable, query, field, (IsBetween)annotation);
                }else if(annotation instanceof IsLike){
                    isLike(queryExpressionWhereBuilder, sqlTable, query, field, (IsLike)annotation);
                }
            }




        }

    }

    private static <T> void isLike(QueryExpressionDSL<MyBatis3SelectModelAdapter<T>>.QueryExpressionWhereBuilder queryExpressionWhereBuilder, SqlTable sqlTable, Object query, Field field, IsLike annotation) {
        String name = annotation.value();
        if("".equals(name)){
            name = field.getName();
        }
        SqlColumn<Object> column = (SqlColumn<Object>) getSqlColumn(sqlTable,name);
        Object value = getValue(query, field);
        if(value == null ){
            return;
        }
        org.mybatis.dynamic.sql.where.condition.IsLike<Object> condition = SqlBuilder.isLike(annotation.pattern().replace("${value}",String.valueOf(value)));
        queryExpressionWhereBuilder.and(column,condition);
    }

    private static <T> void isBetween(QueryExpressionDSL<MyBatis3SelectModelAdapter<T>>.QueryExpressionWhereBuilder queryExpressionWhereBuilder, SqlTable sqlTable, Object query, Field field, IsBetween annotation) {
        String name = annotation.value();
        if("".equals(name)){
            name = field.getName();
        }
        SqlColumn<Object> column = (SqlColumn<Object>) getSqlColumn(sqlTable,name);
        Object value = getValue(query, field);
        if(value == null ){
            return;
        }
        Object[] twoValue = null ;
        try{
            twoValue= (Object[]) value;
        }catch (ClassCastException e){
            twoValue= ((Collection<Object>) value).toArray();
        }
        org.mybatis.dynamic.sql.where.condition.IsBetween<Object> condition = SqlBuilder.isBetween(twoValue[0]).and(twoValue[1]);
        queryExpressionWhereBuilder.and(column,condition);
    }

    private static <T> void isIn(QueryExpressionDSL<MyBatis3SelectModelAdapter<T>>.QueryExpressionWhereBuilder queryExpressionWhereBuilder, SqlTable sqlTable, Object query, Field field, IsIn annotation) {
        String name = annotation.value();
        if("".equals(name)){
            name = field.getName();
        }
        SqlColumn<Object> column = (SqlColumn<Object>) getSqlColumn(sqlTable,name);
        Object value = getValue(query, field);
        if(value == null ){
            return ;
        }
        org.mybatis.dynamic.sql.where.condition.IsIn<Object> condition = null;
        try{
            condition = SqlBuilder.isIn((Collection<Object>) value);
        }catch (ClassCastException e){
            condition = SqlBuilder.isIn((Object[]) value);
        }

        queryExpressionWhereBuilder.and(column,condition);
    }

    private static <T> void isEqualTo(QueryExpressionDSL<MyBatis3SelectModelAdapter<T>>.QueryExpressionWhereBuilder queryExpressionWhereBuilder, SqlTable sqlTable, Object query, Field field, IsEqualTo annotation) {
        String name = annotation.value();
        if("".equals(name)){
            name = field.getName();
        }
        SqlColumn<Object> column = (SqlColumn<Object>) getSqlColumn(sqlTable,name);
        Object value = getValue(query, field);
        if(value == null ){
            return ;
        }
        org.mybatis.dynamic.sql.where.condition.IsEqualTo<Object> condition = SqlBuilder.isEqualTo(value);
        queryExpressionWhereBuilder.and(column,condition);

    }

    private static SqlColumn<?> getSqlColumn(Object object, String fieldName) {
        try {
            Field field = object.getClass().getField(fieldName);
            return (SqlColumn<?>) getValue(object, field);
        } catch (NoSuchFieldException e ) {
            return null;
        }
    }

    private static Object getValue(Object object, Field field) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            return null;
        }
    }


}
