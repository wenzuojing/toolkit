package org.wens.toolkit.query;


import org.mybatis.dynamic.sql.SqlCriterion;
import org.wens.toolkit.query.annotation.*;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.wens.toolkit.query.annotation.Condition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author wens
 */
public class QueryHelper {


    public static  <R>   PageData<List<R>> queryForPage(QueryExpressionDSL<MyBatis3SelectModelAdapter<List<R>>> select, QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>> count, PageQuery query, SqlTable sqlTable) {
        QueryExpressionDSL<MyBatis3SelectModelAdapter<List<R>>>.QueryExpressionWhereBuilder where = select.where();
        QueryExpressionDSL<MyBatis3SelectModelAdapter<Long>>.QueryExpressionWhereBuilder countWhere = count.where();
        QueryHelper.autoWhere(countWhere, sqlTable,query);
        Long l = countWhere.build().execute();
        PageData<List<R>> pageData = new PageData(query.getPageNo(),l);
        if(l != 0 ){
            QueryHelper.autoWhere(where,sqlTable,query);
            where.limit(query.getPageSize()).offset(query.getOffset());
            if(query.getSortField() != null ){
                Field orderField = null;
                try {
                    orderField = getField(sqlTable,query.getSortField());
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("无法按"+query.getSortField()+"字段排序");
                }
                SqlColumn orderColumn  = (SqlColumn) getValue(sqlTable,orderField);
                if("desc".equalsIgnoreCase(query.getSortOrder()) || "descend".equalsIgnoreCase(query.getSortOrder())){
                    where.orderBy(orderColumn.descending());
                }else {
                    where.orderBy(orderColumn);
                }
            }
            pageData.setData(where.build().execute());
        }else{
            pageData.setData(Collections.EMPTY_LIST);
        }
        return pageData;
    }



    public static <T> void autoWhere(QueryExpressionDSL<MyBatis3SelectModelAdapter<T>>.QueryExpressionWhereBuilder queryExpressionWhereBuilder , SqlTable sqlTable , Object query ){

        Field[] fields = getFields(query.getClass());
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
        if(value instanceof String && "".endsWith((String) value)){
            return ;
        }
        org.mybatis.dynamic.sql.where.condition.IsLike<Object> condition = SqlBuilder.isLike(annotation.pattern().replace("${value}",String.valueOf(value)));
        String[] or = annotation.or();
        if(or == null){
            queryExpressionWhereBuilder.and(column,condition);
        }else{
            SqlCriterion<Object>[] subCriteria = new SqlCriterion[or.length];
            for (int i = 0; i < or.length; i++) {
                SqlColumn<Object> _column = (SqlColumn<Object>) getSqlColumn(sqlTable,or[i]);
                org.mybatis.dynamic.sql.where.condition.IsLike<Object> _condition = SqlBuilder.isLike(annotation.pattern().replace("${value}",String.valueOf(value)));
                subCriteria[i] = SqlBuilder.or(_column,_condition);
            }
            queryExpressionWhereBuilder.and(column,condition,subCriteria);
        }

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
        if(value instanceof String && "".endsWith((String) value)){
            return ;
        }
        org.mybatis.dynamic.sql.where.condition.IsEqualTo<Object> condition = SqlBuilder.isEqualTo(value);
        String[] or = annotation.or();
        if(or == null){
            queryExpressionWhereBuilder.and(column,condition);
        }else{
            SqlCriterion<Object>[] subCriteria = new SqlCriterion[or.length];
            for (int i = 0; i < or.length; i++) {
                SqlColumn<Object> _column = (SqlColumn<Object>) getSqlColumn(sqlTable,or[i]);
                org.mybatis.dynamic.sql.where.condition.IsEqualTo<Object> _condition = SqlBuilder.isEqualTo(value);
                subCriteria[i] = SqlBuilder.or(_column,_condition);
            }
            queryExpressionWhereBuilder.and(column,condition,subCriteria);
        }
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

    private static Field getField(Object object, String name) throws NoSuchFieldException {
        return object.getClass().getField(name);
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


}
