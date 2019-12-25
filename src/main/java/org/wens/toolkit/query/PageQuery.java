package org.wens.toolkit.query;

import com.sun.istack.internal.NotNull;

/**
 * @author wens
 */
public class PageQuery implements Query{

    @NotNull
    private Integer pageNo ;

    @NotNull
    private Integer pageSize ;

    private String sortField ;

    private String sortOrder ;

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
