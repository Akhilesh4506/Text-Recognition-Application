/**
@author Akhilesh Anthwal
26-Sep-2023
1:20:50 am
TextExtractionService.java

Service layer interface used for Performing The TextExtraction 
 */

package com.example.TextReco.app.services;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.TextReco.app.exception.InvalidDataException;
import com.example.TextReco.app.exception.ResourceNotFoundException;

public interface TextExtractionService {

	/**
	 * This Method is used to Take multiple file then Pre-process those image files
	 * and finally extract the text from all the files and return the extracted text
	 * @param file
	 * @param textLanguage
	 * @return Map<String,String>
	 * @throws ResourceNotFoundException, InvalidDataException, Exception
	 */
	public Map<String,String> textExtraction(MultipartFile[] file,String textLanguage) throws ResourceNotFoundException, InvalidDataException, Exception;
	
}
