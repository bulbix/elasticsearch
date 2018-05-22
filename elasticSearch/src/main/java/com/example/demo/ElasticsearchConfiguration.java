package com.example.demo;

import java.net.InetAddress;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

@Configuration
public class ElasticsearchConfiguration {
	
	 	@Value("${elasticsearch.host}")
	    private String EsHost;

	    @Value("${elasticsearch.port}")
	    private int EsPort;

	    @Value("${elasticsearch.clustername}")
	    private String EsClusterName;

	    @Bean
	    public Client client() throws Exception {
	    	
	    	System.setProperty("http.proxyHost", "10.50.8.20");
	    	System.setProperty("http.proxyPort", "8080");

	    	Builder builder = Settings.builder();
	    	// builder.put("client.transport.sniff", true);
	    	Settings settings = builder.put("cluster.name", EsClusterName).build();
	    	TransportClient client = new PreBuiltTransportClient(settings);
	    	InetAddress adress = InetAddress.getByName(EsHost);
	    	client.addTransportAddress(new InetSocketTransportAddress(adress, EsPort));

	    	return client;
	    }

	    @Bean
	    public ElasticsearchOperations elasticsearchTemplate() throws Exception {
	        return new ElasticsearchTemplate(client());
	    }

	    //Embedded Elasticsearch Server
	    /*@Bean
	    public ElasticsearchOperations elasticsearchTemplate() {
	        return new ElasticsearchTemplate(nodeBuilder().local(true).node().client());
	    }*/

}
