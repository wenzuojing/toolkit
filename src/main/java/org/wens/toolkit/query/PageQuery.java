package org.wens.toolkit.query;


/**
 * @author wens
 */
public class PageQuery implements Query{

    private Integer pageNo = 1;

    private Integer pageSize = 20;

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

    public Integer getOffset(){
        return ( this.pageNo - 1 ) * pageSize ;
    }
}
