package com.bancoazteca.logES;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

import com.bancoazteca.logES.LogService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticSearchApplicationTests {
	
	@Autowired LogService service;
	
	final String[] arrayIndexName = {"logbaz-2018.05.28"};
	final int numRegistros = 1;
	
	final String lineLog = "[#| 2018-01-24 08:52:27,995 INFO  (HTTP-CRED-242) PathInterceptor:44 - CECO: Tiempo: 1 Milisegundos  CECO: Tiempo: 1 Milisegundos   Tiempo de ejecucion Total: 1288 Milisegundos |#] ";

	@Test
	public void testSearchTerm() {
		List<Map<String,Object>> documents = service.searchTerm("CECO:",numRegistros,null,arrayIndexName);
		System.out.println("Num Coincidencias: " + documents.size());
	}
	
	
	@Test
	public void testGetThread() throws IOException {
		
		List<Map<String,Object>> documents = service.searchTerm("/saldos/consultar",numRegistros,null,arrayIndexName);
		System.out.println("Num Coincidencias: " + documents.size());
		int index = 0;
		for(Map<String,Object> doc: documents) {
			++index;
			List<String> lines = service.getThread(doc,arrayIndexName);
			FileUtils.writeLines(new File(String.format("./logBAZ/thread%s.txt",index)), lines);
		}
	}
	
	@Test
	public void testGetThreadFromLineLog() throws IOException {
		
		List<String> lines = service.getThread(lineLog, arrayIndexName);
		
		lines.stream().forEach(item->System.out.println(item));
	}
	
	@Test
	public void testGetFieldsFromLineLog() throws IOException {
		
		Map<String,Object> source = service.getFieldsFromLineLog(lineLog);
		
		String logdate = source.get("logdate").toString();
		String loglevel = source.get("loglevel").toString();
		String thread = source.get("thread").toString();
		String classname = source.get("classname").toString();
		String msgbody = source.get("msgbody").toString();
		
		System.out.println(logdate);
		System.out.println(loglevel);
		System.out.println(thread);
		System.out.println(classname);
		System.out.println(msgbody);
	}
	
	
	@Test
	public void testDate() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		DateTime dt =new DateTime("2018-04-10T14:04:27.998Z");
		System.out.println(dt);
		System.out.println(formatter.parseDateTime("2018-04-10T14:04:27.998Z"));
	}
	
	@Test
	public void testGetAliases() {
		
		System.out.println(Arrays.toString(service.getIndices()));
	}
	

}
