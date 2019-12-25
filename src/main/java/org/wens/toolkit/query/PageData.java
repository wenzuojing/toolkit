package org.wens.toolkit.query;

/**
 * @author wens
 * @param <T>
 */
public class PageData<T> {

    private Integer pageNo ;

    private Long totalCount ;

    private T data ;

    public PageData(Integer pageNo, Long totalCount) {
        this.pageNo = pageNo;
        this.totalCount = totalCount;
    }

    public PageData(Integer pageNo, Long totalCount, T data) {
        this.pageNo = pageNo;
        this.totalCount = totalCount;
        this.data = data;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
