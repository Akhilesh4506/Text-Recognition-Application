/**
@author Akhilesh Anthwal
26-Sep-2023
1:18:58 am
TextDetectionController.java

Responsible for processing incoming REST API requests, 
and returning the view to be rendered as a response
 */

package com.example.TextReco.app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.TextReco.app.constants.APIConstants;
import com.example.TextReco.app.exception.InvalidDataException;
import com.example.TextReco.app.exception.ResourceNotFoundException;
import com.example.TextReco.app.services.TextExtractionService;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;

@RestController
@RequestMapping(APIConstants.IMAGE_API_PREFIX)
@Slf4j
public class TextDetectionController {
	@Autowired
	TextExtractionService extractionService;

	/**
	 * POST Api which takes multiple Image files and extract text from it
	 * 
	 * @param file
	 * @param textLanguage
	 * @return The extracted Text
	 * @throws ResourceNotFoundException
	 * @throws TesseractException
	 * @throws InvalidDataException
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	@PostMapping()
	public Map<String, String> getTextFromImage(@RequestBody MultipartFile[] files, @RequestParam String textLanguage)
			throws ResourceNotFoundException, TesseractException, InvalidDataException, IllegalArgumentException,
			Exception {
		log.info("TextDetectionController -> getTextFromImage -> Detecting Text from file {} in {} language ", files,
				textLanguage);
		return extractionService.textExtraction(files, textLanguage);

	}

}
