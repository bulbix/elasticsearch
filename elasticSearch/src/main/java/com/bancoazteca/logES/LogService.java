package com.bancoazteca.logES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import com.github.vanroy.springdata.jest.JestElasticsearchTemplate;
import com.github.vanroy.springdata.jest.mapper.JestResultsExtractor;

import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import io.searchbox.core.SearchResult;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Service
public class LogService {

	@Autowired JestElasticsearchTemplate template;
	
	Logger log = LoggerFactory.getLogger(LogService.class);

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
				.withQuery(matchPhraseQuery("msgbody", term))
				.withIndices(indices)
				.withPageable(new PageRequest(0, numRegistros))
				.withSort(SortBuilders.fieldSort("@logdate").order(SortOrder.ASC))
				.build();
		List<Map<String,Object>> result = template.query(searchQuery, new JestResultsExtractor<List<Map<String,Object>>>() {

			@Override
			public List<Map<String, Object>> extract(SearchResult response) {
				List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
				for(SearchResult.Hit<Map,Void> hit : response.getHits(Map.class)) {
					hit.source.put("_index", hit.index);
					result.add(hit.source);
				}

				return result;
			}
		});

		return result;
	}

	/***
	 * Busqueda por documento
	 * 
	 * @param document Mapa devuelto por metodo searchTerm
	 * @param indices arreglo de indices donde buscar
	 * @return
	 */
	public List<String> getThread(Map<String,Object> document, String... indices){

		String thread = document.get("thread").toString();
		String str_logdate = document.get("@logdate").toString();
		String indexName = document.get("_index").toString();
		String source = document.get("source").toString();
		DateTime logdate = new DateTime(str_logdate).plusHours(5);
		
		log.info(String.format("\nQueryFields:\n%s\n%s\n%s\n%s", thread,str_logdate,indexName,source));

		log.info(logdate.minusSeconds(2).toString("yyyy-MM-dd'T'HH:mm:ss,SSS"));
		
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(boolQuery()
						.must(matchPhraseQuery("thread", thread))
						.must(matchQuery("source", source))
						.must(rangeQuery("@logdate")
								.gte(logdate.minusSeconds(2).toString("yyyy-MM-dd'T'HH:mm:ss,SSS")).
								lte(logdate.plusSeconds(2).toString("yyyy-MM-dd'T'HH:mm:ss,SSS"))))	
				.withPageable(new PageRequest(0, 1000))
				.withSort(SortBuilders.fieldSort("@logdate").order(SortOrder.ASC))
				.withIndices(indices)
				.build();

		List<String> result = template.query(searchQuery, new JestResultsExtractor<List<String>>() {
			@Override
			public List<String> extract(SearchResult response) {
				List<String> result = new ArrayList<String>();
				for(SearchResult.Hit<Map,Void> hit : response.getHits(Map.class)) {
					
					String rstr_logdate = hit.source.get("@logdate").toString();
					String rloglevel = hit.source.get("loglevel").toString();
					String rthread = hit.source.get("thread").toString();
					String rclassname = hit.source.get("classname").toString();
					String rmsgbody = hit.source.get("msgbody").toString();
					DateTime rlogdate = new DateTime(rstr_logdate);
					
					String linea = String.format("[#| %s %s  %s %s - %s |#]", rlogdate.toString("yyyy-MM-dd HH:mm:ss,SSS"),rloglevel,rthread,rclassname,rmsgbody);
					result.add(linea);
				}

				result.add(indexName + "--" + source);

				return result;
			}
		});

		return result;

	}

	/***
	 * 
	 * Busqueda por linea
	 * 
	 * @param linea detalle de la linea encontrada
	 * @param indices arreglo de indices donde buscar
	 * @return
	 */
	public List<String> getThread(String linea, String... indices){
		
		Map<String,Object> source = getFieldsFromLineLog(linea);
		List<Map<String,Object>> documents = getLineMatch(source, indices);
		
		if(documents.size() > 0 ) {
			return getThread(documents.get(0), indices);
		}
		else {
			throw new IllegalAccessError("No se encontro coincidencia");
		}
	}
	
	/**
	 * Recupera un mapa con campos a partir de la linea log
	 * 
	 * @param linealog
	 * @return
	 */
	protected Map<String,Object> getFieldsFromLineLog(String linealog){
	
		GrokCompiler grokCompiler = GrokCompiler.newInstance();
		grokCompiler.registerDefaultPatterns();
		final Grok grok = grokCompiler.compile("\\[\\#\\| %{TIMESTAMP_ISO8601:logdate} %{LOGLEVEL:loglevel}  %{DATA:thread} %{DATA:classname} - %{GREEDYDATA:msgbody}");
		Match gm = grok.match(linealog);
		return gm.capture();
	}
	
	
	/***
	 * Recupera un documento a partir del mapa de campos de la liea de log
	 * @param source
	 * @param indices
	 * @return
	 */
	protected List<Map<String,Object>> getLineMatch(Map<String,Object> source, String... indices) {
		
		String str_logdate = source.get("logdate").toString();
		String loglevel = source.get("loglevel").toString();
		String thread = source.get("thread").toString();
		String classname = source.get("classname").toString();
		String msgbody = source.get("msgbody").toString();
		
		DateTimeFormatter dtf  = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS");
		DateTime logdate = dtf.parseDateTime(str_logdate).plusHours(5);
		
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(boolQuery()
						.must(matchQuery("@logdate", logdate.toString("yyyy-MM-dd'T'HH:mm:ss,SSS")))
						.must(matchPhraseQuery("loglevel", loglevel))
						.must(matchPhraseQuery("thread", thread))
						.must(matchPhraseQuery("classname", classname))
						.must(matchPhraseQuery("msgbody", msgbody)))
				.withPageable(new PageRequest(0, 1))
				.withSort(SortBuilders.fieldSort("@logdate").order(SortOrder.ASC))
				.withIndices(indices)
				.build();
		
		List<Map<String,Object>> result = template.query(searchQuery, new JestResultsExtractor<List<Map<String,Object>>>() {

			@Override
			public List<Map<String, Object>> extract(SearchResult response) {
				List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
				for(SearchResult.Hit<Map,Void> hit : response.getHits(Map.class)) {
					hit.source.put("_index", hit.index);
					result.add(hit.source);
				}

				return result;
			}
		});
		
		return result;
		
	}
	
	/***
	 * 
	 * @return arreglo de indices del cluster
	 */
	public String[] getIndices(){
		return template.getIndicesFromAlias("").toArray(new String[] {});
	}


}
