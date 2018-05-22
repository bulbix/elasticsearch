package com.example.demo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticSearchApplicationTests {
	
	@Autowired LogService service;

	@Test
	public void testSearchTerm() {
		List<Map<String,Object>> documents = service.searchTerm("/api/bdm/pagoServicios/validarReferencia");
		System.out.println("Num Coincidencias: " + documents.size());
	}
	
	
	@Test
	public void testGetThread() throws IOException {
		
		List<Map<String,Object>> documents = service.searchTerm("15720184109427640");
		System.out.println("Num Coincidencias: " + documents.size());
		int index = 0;
		for(Map<String,Object> doc: documents) {
			++index;
			List<String> lines = service.getThread(doc);
			FileUtils.writeLines(new File(String.format("/Users/lpradof/Desktop/salida/thread%s.txt",index)), lines);
		}
	}
	
	@Test
	public void testDate() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		DateTime dt =new DateTime("2018-04-10T14:04:27.998Z");
		System.out.println(dt);
		System.out.println(formatter.parseDateTime("2018-04-10T14:04:27.998Z"));
	}
	

}
