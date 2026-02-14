package com.rbaciam.dto;

import java.io.Serializable;
import java.util.List;

public class PaginatedResponse<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<T> data;
	private int pageNumber;
	private int pageSize;
	private int totalElements;
	private int totalPages;

	public PaginatedResponse() {
	}

	public PaginatedResponse(List<T> data, int pageNumber, int pageSize, int totalElements, int totalPages) {
		this.data = data;
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalElements = totalElements;
		this.totalPages = totalPages;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(int totalElements) {
		this.totalElements = totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

}