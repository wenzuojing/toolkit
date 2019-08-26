package org.wens.toolkit.query;

/**
 * @author wens
 */
public class PageQuery extends Query{

    private Integer pageNo ;

    private Integer pageSize ;

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
