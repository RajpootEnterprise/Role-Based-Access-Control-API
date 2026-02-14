package com.rbaciam.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponseDto {
	    private Long id;
	    private boolean status;
	    private String message;
	    
	    
	    public ApiResponseDto(boolean status,String message) {
	    	this.status=status;
	    	this.message=message;
	    }


		public ApiResponseDto(Long id, boolean status, String message) {
			super();
			this.id = id;
			this.status = status;
			this.message = message;
		}

	}



