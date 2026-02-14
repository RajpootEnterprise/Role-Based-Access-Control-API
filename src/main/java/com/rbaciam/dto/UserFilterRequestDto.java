package com.rbaciam.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserFilterRequestDto {
  
	   private String email;
	    private String status; 
	    private Long roleId;
	    private int page = 0;
	    private int size = 10;

	
}
