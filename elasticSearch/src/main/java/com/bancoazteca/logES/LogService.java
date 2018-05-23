package com.bancoazteca.logES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import com.github.vanroy.springdata.jest.JestElasticsearchTemplate;
import com.github.vanroy.springdata.jest.mapper.JestResultsExtractor;

import io.searchbox.core.SearchResult;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class LogService {
	
	@Autowired JestElasticsearchTemplate template;
	
	/****
	 * 
	 * 
	 * 
	 * @param term Texto Libre de busqueda
	 * @param numRegistros registros a devolver
	 * @param indices arreglos de indices donde buscar
	 * @return
	 * Mapa =>  logdate date
				loglevel text
				thread text
				classname text
				message text solo el mensaje
				msgbody text Linea completa
				source text Archivo donde encontro
	 */
	public List<Map<String,Object>> searchTerm(String term, Integer numRegistros, String... indices) {
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				  .withQuery(matchPhraseQuery("message", term))
				  .withIndices(indices)
				  .withPageable(new PageRequest(0, numRegistros))
				  .build();
		List<Map<String,Object>> result = template.query(searchQuery, new JestResultsExtractor<List<Map<String,Object>>>() {

			@Override
			public List<Map<String, Object>> extract(SearchResult response) {
				List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
				for(SearchResult.Hit<Map,Void> hit : response.getHits(Map.class)) {
					result.add(hit.source);
				}
				
				return result;
			}
		});
		
		return result;
	}
	
	/***
	 * 
	 * @param document Mapa devuelto por metodo searchTerm
	 * @param indices arreglos de indices donde buscar
	 * @return
	 */
	public List<String> getThread(Map<String,Object> document, String... indices){
		
		String thread = document.get("thread").toString();
		String str_logdate = document.get("@logdate").toString();
		String source = document.get("source").toString();
		
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
				  .withIndices(indices)
				  .build();
		
		List<String> result = template.query(searchQuery, new JestResultsExtractor<List<String>>() {
			@Override
			public List<String> extract(SearchResult response) {
				List<String> result = new ArrayList<String>();
				for(SearchResult.Hit<Map,Void> hit : response.getHits(Map.class)) {
					result.add(hit.source.get("message").toString());
				}
				
				result.add(response.getHits(Map.class).get(0).source.get("source").toString());
				
				return result;
			}
		});
		
		
		
		return result;
		
	}
	
	

}
