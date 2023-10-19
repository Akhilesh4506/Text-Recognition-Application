/**
@author Akhilesh Anthwal
26-Sep-2023
1:16:41 am
AppApplication.java

Entry point of the Application
 */
package com.example.TextReco.app;

import java.awt.HeadlessException;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.sourceforge.tess4j.TesseractException;

@SpringBootApplication
public class AppApplication {

	public static void main(String[] args) throws HeadlessException, IOException, TesseractException{
		SpringApplication.run(AppApplication.class, args);
		nu.pattern.OpenCV.loadLocally();
	}

}
