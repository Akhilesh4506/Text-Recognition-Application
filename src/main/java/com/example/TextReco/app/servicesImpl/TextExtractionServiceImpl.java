/**
 * @author Akhilesh Anthwal
26-Sep-2023
1:26:12 am
TextExtractionServiceImpl.java

Service Implementation class, responsible for all the business logic for TextExtraction
 */

package com.example.TextReco.app.servicesImpl;

import java.awt.image.BufferedImage;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.TextReco.app.Enums.AppEnums.TextLanguage;
import com.example.TextReco.app.constants.AppConstants;
import com.example.TextReco.app.exception.IllegalArgumentException;
import com.example.TextReco.app.exception.InvalidDataException;
import com.example.TextReco.app.exception.ResourceNotFoundException;
import com.example.TextReco.app.services.TextExtractionService;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
@Slf4j
public class TextExtractionServiceImpl implements TextExtractionService {

	@Override
	public Map<String, String> textExtraction(MultipartFile[] files, String textLanguage)
			throws ResourceNotFoundException, TesseractException, InvalidDataException, IllegalArgumentException,
			Exception {
		if (files == null) {
			log.error(
					"TextExtractionServiceImpl -> textExtraction-> No Image File selected. Select atleast one Image file");
			throw new ResourceNotFoundException("TextExtractionServiceImpl -> textExtraction->",
					" No Image File selected. Select atleast one Image file", HttpStatus.NOT_FOUND);
		}

		// Checking if the the textLanguage is valid or not
		if (!isValidTextLanguage(textLanguage)) {
			log.error(
					"TextExtractionServiceImpl -> textExtraction-> Invalid textLanguage parameter. Allowed values are 'hin' and 'eng'.");
			throw new IllegalArgumentException(
					"TextExtractionServiceImpl -> textExtraction-> Invalid textLanguage parameter. Allowed values are 'hin' and 'eng'.",
					HttpStatus.NOT_ACCEPTABLE);
		}

		Map<String, String> result = new HashMap<>();
		log.info("TextExtractionServiceImpl -> textExtraction -> Processing Files concurrently " + files);

		// Creating a thread pool for concurrent processing
		int numOfThreads = Runtime.getRuntime().availableProcessors(); // Number of available CPU cores
		log.info("TextExtractionServiceImpl -> textExtraction -> Number of Available Threads " + numOfThreads);

		// This ExecutorService allows us to process multiple files concurrently.
		ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);

		try {
			// Creating a list of tasks to hold the all tasks
			List<Callable<Map.Entry<String, String>>> tasks = new ArrayList<>();

			// Creating a task for each file
			for (MultipartFile multipartFile : files) {
				tasks.add(() -> {
					Mat mat = preProcessImage(multipartFile);
					BufferedImage image = matToBufferedImage(mat);
					String text = textDetection(image, textLanguage).replace("\n", " ");
					return new AbstractMap.SimpleEntry<>(multipartFile.getOriginalFilename(), text);
				});
			}
			log.info("TextExtractionServiceImpl -> textExtraction -> Tasks for each file " + tasks);

			// It execute all tasks concurrently. This method returns a list of Future
			// objects,
			// each representing the result of a task.
			List<Future<Map.Entry<String, String>>> futures = executorService.invokeAll(tasks);

			// Retrieving results from completed tasks
			for (Future<Map.Entry<String, String>> future : futures) {
				Map.Entry<String, String> entry = future.get();
				result.put(entry.getKey(), entry.getValue());
			}
		} finally {
			// Shutting down the executor service
			executorService.shutdown();
		}

		return result;

	}

	/**
	 * This method is used to Implement image pre-processing techniques to enhance
	 * the quality of input images. This includes resizing, noise reduction, and
	 * contrast adjustment.
	 * 
	 * @return Mat
	 */

	public Mat preProcessImage(MultipartFile file) throws Exception {
		Mat destination = new Mat();
		Mat source = convertMultipartFileToMat(file);
		log.info("TextExtractionServiceImpl -> preProcessImage -> Pre-Processing Image " + source);
		if (source == null) {
			log.error("Error: TextExtractionServiceImpl -> preProcessImage -> Failed to convert MultipartFile to Mat.");
			throw new Exception("Failed to convert MultipartFile to Mat.");
		}
		/**
		 * It performs a series of image processing operations on an input Mat object
		 * named source and stores the result in another Mat object named destination
		 */
		for (int i = 0; i < 4; i++) {
			destination = new Mat(source.rows(), source.cols(), source.type());
			Imgproc.GaussianBlur(source, destination, new Size(0, 0), 10);
			Core.addWeighted(source, 1.5, destination, -0.5, 0, destination);
			source = destination;
		}
		log.info("TextExtractionServiceImpl -> preProcessImage -> Image Processed " + destination);
		Mat resultMat = new Mat();
		Imgproc.threshold(destination, resultMat, 55, 255, Imgproc.THRESH_BINARY);

		return resultMat;
	}

	/**
	 * This method is used to Implement an optical character recognition (OCR)
	 * module to detect and recognize text and numbers within the images.
	 * 
	 * @return String
	 * @throws TesseractException
	 * @throws InvalidDataException
	 */

	public String textDetection(BufferedImage image, String textLanguage)
			throws TesseractException, InvalidDataException, IllegalArgumentException {
		log.info("TextExtractionServiceImpl -> textDetection -> Detecting text from Image.");

		// Checking if the image is null
		if (image == null) {
			log.error("TextExtractionServiceImpl -> textDetection -> Image is null.");
			throw new InvalidDataException("TextExtractionServiceImpl -> textDetection -> ", "Image Not Found.",
					HttpStatus.NOT_ACCEPTABLE);
		}

		String extractedText = "";

		Tesseract tesseractInstance = new Tesseract();
		tesseractInstance.setDatapath(AppConstants.TESSDATA_PATH);
		tesseractInstance.setTessVariable(AppConstants.USER_DEFINED_DPI, AppConstants.DPI);
		tesseractInstance.setLanguage(textLanguage);

		try {
			extractedText = tesseractInstance.doOCR(image);
		} catch (TesseractException ex) {
			log.error("TextExtractionServiceImpl -> textDetection -> Error while Detecting text from Image."
					+ ex.getMessage());
			throw new TesseractException();
		}
		log.info(
				"TextExtractionServiceImpl -> textDetection -> Text Detected from Image successfully." + extractedText);
		return extractedText;
	}

	private boolean isValidTextLanguage(String textLanguage) {
		for (TextLanguage language : TextLanguage.values()) {
			if (language.name().equalsIgnoreCase(textLanguage)) {
				return true; // Found a valid language
			}
		}
		return false;
	}

	/**
	 * This method is used to convert the MultipartFile to Mat (Mat used to store
	 * the values of an image).
	 * 
	 * @param imageFile
	 * @return Mat
	 * @throws Exception
	 */

	private Mat convertMultipartFileToMat(MultipartFile imageFile) throws Exception {
		try {

			byte[] imageBytes = imageFile.getBytes();
			log.info("TextExtractionServiceImpl -> convertMultipartFileToMat -> converting Multipart File To Mat "
					+ imageBytes);
			// Check if the imageBytes array is empty or null
			if (imageBytes == null || imageBytes.length == 0) {
				log.error(
						"Error: TextExtractionServiceImpl -> convertMultipartFileToMat ->  Empty or null image byte array.");
				throw new InvalidDataException("Empty or null image byte array." + imageBytes);
			}

			Mat matImage = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);

			// Check if the decoding was successful
			if (matImage.empty()) {
				log.error(
						"Error: TextExtractionServiceImpl -> convertMultipartFileToMat ->  Failed to decode the image.");
				return null;
			}

			return matImage;
		} catch (Exception e) {
			log.error(
					"Error: TextExtractionServiceImpl -> convertMultipartFileToMat -> Error in converting Multipart image to Mat"
							+ e.getMessage());
			throw new Exception("Error in converting file " + e);
		}

	}

	/**
	 * This method is used to convert the Mat to BufferedImage. A BufferedImage is
	 * comprised of a ColorModel and a Raster of image data.
	 * 
	 * @param mat
	 * @return
	 * @throws InvalidDataException
	 */

	public BufferedImage matToBufferedImage(Mat mat) throws InvalidDataException {
		log.info("TextExtractionServiceImpl -> matToBufferedImage -> Converting Mat object to BufferedImage.");
		// Checking if the input Mat object is null
		if (mat == null) {
			log.error("Erroe: TextExtractionServiceImpl -> matToBufferedImage -> Mat object is null ", mat);
			throw new InvalidDataException("Empty or null Mat object.");
		}

		log.debug(" TextExtractionServiceImpl -> matToBufferedImage -> Mat of type " + mat.type());

		// The input Mat should have a type of CvType.CV_8UC3 (3-channel, 8-bit
		// unsigned)
		if (mat.type() != CvType.CV_8UC3) {
			Mat compatibleMat = new Mat();
			Imgproc.cvtColor(mat, compatibleMat, Imgproc.COLOR_BGR2RGB);
			mat = compatibleMat;
		}

		// Convert the Mat to a byte array
		byte[] bytes = new byte[mat.cols() * mat.rows() * (int) mat.elemSize()];
		mat.get(0, 0, bytes);
		log.debug(" TextExtractionServiceImpl -> matToBufferedImage -> Convert the Mat to a byte array " + bytes);
		// Create a BufferedImage from the byte array
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
		image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), bytes);
		log.info("TextExtractionServiceImpl -> matToBufferedImage -> Mat object  Converted to BufferedImage. " + image);
		return image;
	}

}
