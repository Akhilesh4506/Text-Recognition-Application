/**
@author Akhilesh Anthwal
26-Sep-2023
1:29:46 am
IllegalArgumentException.java

Exception Handling class used to throw IllegalArgumentException
 */

package com.example.TextReco.app.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IllegalArgumentException extends Exception {

	/**
     * Serial Version ID
     */
    private static final long serialVersionUID = 7590870958681675504L;

    public IllegalArgumentException(String message, Object... logMessageArguments) {
        super(message);
        log.error(message, logMessageArguments, this);

    }
    
    public IllegalArgumentException(String message) {
        super(message);
        
    }

}
