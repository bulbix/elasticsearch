package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.vanroy.springdata.jest.JestElasticsearchTemplate;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@Configuration
public class ElasticsearchConfiguration {
	
		@Value("spring.data.jest.uri")
		String url;

	    @Bean
	    public JestClient client() throws Exception {
	    	
	    	JestClientFactory factory = new JestClientFactory();
	        factory.setHttpClientConfig(new HttpClientConfig.Builder("https://search-logs-le3f24h7njq2nv5irf3wpagznu.us-west-2.es.amazonaws.com")
	                .multiThreaded(true)
	                .build());
	        JestClient client = factory.getObject();

	    	return client;
	    }

	    @Bean
	    public JestElasticsearchTemplate elasticsearchTemplate() throws Exception {
	        return new JestElasticsearchTemplate(client());
	    }

	    //Embedded Elasticsearch Server
	    /*@Bean
	    public ElasticsearchOperations elasticsearchTemplate() {
	        return new ElasticsearchTemplate(nodeBuilder().local(true).node().client());
	    }*/

}
