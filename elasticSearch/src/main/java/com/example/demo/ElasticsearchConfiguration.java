package com.example.demo;

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
	
		@Value("${spring.data.jest.uri}")
		String url;
		
		@Value("${proxy.host}")
		String proxyHost;
		
		@Value("${proxy.port}")
		Integer proxyPort;

	    @Bean
	    public JestClient client() throws Exception {
	    	
		    	JestClientFactory factory = new JestClientFactory();
		    	factory.setHttpClientConfig(new HttpClientConfig.Builder(url)
		    			.multiThreaded(true)
		    			.proxy(new HttpHost(proxyHost, proxyPort))
		    			.build());
		    	JestClient client = factory.getObject();
	
		    	return client;
	    }

	    @Bean
	    public JestElasticsearchTemplate elasticsearchTemplate() throws Exception {
	        return new JestElasticsearchTemplate(client());
	    }

}
