package com.bancoazteca.logES;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.vanroy.springdata.jest.JestElasticsearchTemplate;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@Configuration
public class ElasticsearchConfiguration {
	
		//@Value("${spring.data.jest.uri}")
		String url="https://search-bazdigital-b3vuzcftsbpqicb3mc64eiimme.us-west-2.es.amazonaws.com";
		
		//@Value("${proxy.host}")
		String proxyHost="10.50.8.20";
		
		//@Value("${proxy.port}")
		Integer proxyPort=8080;

	    @Bean
	    public JestClient client() throws Exception {
	    	
		    	JestClientFactory factory = new JestClientFactory();
		    	factory.setHttpClientConfig(new HttpClientConfig.Builder(url)
		    			.multiThreaded(true)
		    			.proxy(new HttpHost(proxyHost, proxyPort))
		    			.readTimeout(60000)
		    			.build());
		    	JestClient client = factory.getObject();
	
		    	return client;
	    }

	    @Bean
	    public JestElasticsearchTemplate elasticsearchTemplate() throws Exception {
	        return new JestElasticsearchTemplate(client());
	    }

}
