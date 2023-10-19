/**
@author Akhilesh Anthwal
26-Sep-2023
1:31:46 am
ResourceNotFoundException.java

Exception Handling class used to throw ResourceNotFoundException
 */

package com.example.TextReco.app.exception;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ResourceNotFoundException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * overriding super class method.
	 */

	public ResourceNotFoundException(String message, String logMessage, Object... logMessageArguments) {
		super(message);
		log.error(logMessage, logMessageArguments, this);
	}
	
	public ResourceNotFoundException(String message){
		super(message);
		log.error(message);
		}


}
