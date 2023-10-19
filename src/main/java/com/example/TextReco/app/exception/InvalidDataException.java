/**
@author Akhilesh Anthwal
26-Sep-2023
1:32:40 am
InvalidDataException.java
Exception Handling class used to throw InvalidDataException
 */

package com.example.TextReco.app.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidDataException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidDataException(String messsage) {
		super(messsage);
		log.error(messsage);
	}

	public InvalidDataException(String message, String logMessage, Object... logMessageArguments) {
		super(message);
		log.error(logMessage, logMessageArguments, this);
	}

}
