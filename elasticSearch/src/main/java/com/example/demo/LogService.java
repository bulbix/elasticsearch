package com.example.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
@Slf4j
public class LogService {
	
	@Autowired ElasticsearchTemplate template;
	
	
	
	public List<Map<String,Object>> searchTerm(String term) {
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				  .withQuery(matchPhraseQuery("message", term))
				  .withIndices("logbaz")
				  .withPageable(new PageRequest(0, 10000))
				  .build();
		List<Map<String,Object>> result = template.query(searchQuery, new ResultsExtractor<List<Map<String,Object>>>() {

			@Override
			public List<Map<String,Object>> extract(SearchResponse arg0) {
				List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
				for(SearchHit hit : arg0.getHits()) {
					result.add(hit.getSource());
				}
				
				return result;
			}
		});
		
		return result;
	}
	
	public List<String> getThread(Map<String,Object> document){
		
		String thread = document.get("thread").toString();
		//log.info(thread);
		String str_logdate = document.get("@logdate").toString();
		//log.info(str_logdate);
		String source = document.get("source").toString();
		//log.info(source);
		
		DateTime logdate = new DateTime(str_logdate).plusHours(5);
		//log.info(logdate.toString("yyyy-MM-dd'T'HH:mm:ss,SSS"));
		
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				  .withQuery(boolQuery()
						  .must(matchPhraseQuery("thread", thread))
						  .must(matchQuery("source", source))
				  		  .must(rangeQuery("@logdate")
				  				  .gte(logdate.minusSeconds(2).toString("yyyy-MM-dd'T'HH:mm:ss,SSS")).
				  				  lte(logdate.plusSeconds(2).toString("yyyy-MM-dd'T'HH:mm:ss,SSS"))))	
				  .withPageable(new PageRequest(0, 10000))
				  .withSort(SortBuilders.fieldSort("@logdate").order(SortOrder.ASC))
				  .withIndices("logbaz")
				  .build();
		
		List<String> result = template.query(searchQuery, new ResultsExtractor<List<String>>() {

			@Override
			public List<String> extract(SearchResponse arg0) {
				List<String> result = new ArrayList<String>();
				for(SearchHit hit : arg0.getHits()) {
					result.add(hit.getSource().get("message").toString());
				}
				
				result.add(arg0.getHits().getAt(0).getSource().get("source").toString());
				
				return result;
			}
		});
		
		
		
		return result;
		
	}
	
	

}
