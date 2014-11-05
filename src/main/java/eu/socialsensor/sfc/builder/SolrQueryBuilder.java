package eu.socialsensor.sfc.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.CustomDysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import eu.socialsensor.sfc.builder.solrQueryBuilder.CustomSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.KeywordsExtractor;
import eu.socialsensor.sfc.builder.solrQueryBuilder.QueryFormulator;
import eu.socialsensor.sfc.builder.solrQueryBuilder.TrendingSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.graph.GraphCreator;
/**
 * @brief Class responsible for the creation of a SolrQuery
 * on the basis of DySco content. SolrQuery can be then used for 
 * the retrieval of items/mediaitmes/webpages from Apache Solr database.
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class SolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(SolrQueryBuilder.class);

	//number of words in a solr query - the more words you include, 
	//the more complicated the query turns out to be
	private static final Integer NUMBER_OF_KEYWORDS_IN_QUERY = 4;
	
	public SolrQueryBuilder() {
		logger.info("SolrQueryBuilder instance created");
	}
	
	/**
	 * Returns one aggregated Solr query based on DySco content(trending/custom)
	 * @param dysco
	 * @return the solr query as String
	 */
	public String getSolrQueryString(Dysco dysco) {
	
		if(dysco.getDyscoType() == null) {
			logger.error("Dysco Type is not defined - Cannot extract solr query");
			return null;
		}
		
		if(dysco.getDyscoType().equals(DyscoType.CUSTOM)) {
			logger.info("Find solr query for custom dysco : " + dysco.getId());
			
			CustomDysco customDysco = (CustomDysco) dysco;
			CustomSolrQueryBuilder customBuilder = new CustomSolrQueryBuilder(customDysco);
			
			return customBuilder.createSolrQuery();
		}
		else {
			logger.info("Find solr query for trending dysco : "+dysco.getId());
			
			TrendingSolrQueryBuilder trendingBuilder = new TrendingSolrQueryBuilder(dysco);
			return trendingBuilder.createSolrQuery();
		}
	}
	
	/**
	 * Returns the Solr Queries derived from the DySco content(trending/custom)
	 * @param dysco
	 * @return List of Solr Queries
	 */
	public List<Query> getSolrQueries(Dysco dysco) {
		
		if(dysco.getDyscoType() == null) {
			logger.error("Dysco Type is not defined - Cannot extract solr query");
			return null;
		}
		
		if(dysco.getDyscoType().equals(DyscoType.CUSTOM)) {
			logger.info("Find solr query for custom dysco : " + dysco.getId());
			
			CustomDysco customDysco = (CustomDysco) dysco;
			CustomSolrQueryBuilder customBuilder = new CustomSolrQueryBuilder(customDysco);
			
			return customBuilder.createSolrQueries();
		}
		else {
			logger.info("Find solr query for trending dysco : " + dysco.getId());
			
			TrendingSolrQueryBuilder trendingBuilder = new TrendingSolrQueryBuilder(dysco);
			return trendingBuilder.createPrimalSolrQueries();
		}
	}
	
	/**
	 * Returns the Solr Queries for a DySco that resulted from a query expansion process. 
	 * The process utilizes items retrieved from primal Solr queries, derived from DySco content, 
	 * to construct a directed and weighted graph of keywords. The co-occurance of keywords adjacency 
	 * serves for adding weights to the graph. Finally the most weighted paths are selected to extract 
	 * additional solr queries, related to the topic at hand. 
	 * The Solr queries are the product of merging the primal Solr queries and the additional solr
	 * queries that were computed from the algorithm on the basis of their scores. 
	 * @param items
	 * @param queryNumberLimit
	 * @param dysco
	 * @return A list of Solr queries
	 */
	public List<Query> getExpandedSolrQueries(List<Item> items, Dysco dysco, Integer queryNumberLimit) {

		List<Query> formulatedSolrQueries = new ArrayList<Query>();
		
		List<Query> primalSolrQueries = dysco.getSolrQueries();
		for(Query pQuery : primalSolrQueries) {
			if(pQuery.getScore() == null)
				pQuery.setScore(10.0);
		}

		//Process keywords from items collection
		KeywordsExtractor keywordExtractor = new KeywordsExtractor(items);
		keywordExtractor.processItemsText();
		List<String> topKeywords = keywordExtractor.getTopKeywords();
		Set<String> contentToProcess = keywordExtractor.getTextContent();

		//Create the graph on the content Keywords Exctractor gave as output
		GraphCreator graphCreator = new GraphCreator(contentToProcess, topKeywords);
		graphCreator.setSubstituteWords(keywordExtractor.getWordsToReplace());
		graphCreator.createGraph();
		graphCreator.pruneLowConnectivityNodes();
		
		//if graph has no nodes return no queries
		if(graphCreator.getGraph().getNodes().size() == 0)
			return formulatedSolrQueries;
		
		//Track solr queries on the graph 
		QueryFormulator qFormulator = new QueryFormulator(graphCreator.getGraph(), keywordExtractor.getTopHashtags());
		qFormulator.generateKeywordQueries(NUMBER_OF_KEYWORDS_IN_QUERY);
		qFormulator.generateHashtagQueries();
	
		Map<Double, List<String>> scaledRankedKeywords = qFormulator.getRankedKeywordQueries();
		Map<Double, List<String>> scaledRankedHashtags = qFormulator.getRankedHashtagQueries();
		
		//Process formulated keywords and hashtags queries to boost repetitive queries and eliminate similar.
		//Keep the highly ranked queries.
		while(formulatedSolrQueries.size() < queryNumberLimit) {
			boolean keywordFound = false;
			boolean done = false;
			Double elementToRemove = 0.0; 
			
			if(scaledRankedKeywords.isEmpty() && scaledRankedHashtags.isEmpty())
				break;

			if(scaledRankedKeywords.isEmpty()) { 
				for(Double hashtagScore : scaledRankedHashtags.keySet()) {
					if(hashtagScore < 1.5) {
						done = true;
						break;
					}
					for(String solrQuery : scaledRankedHashtags.get(hashtagScore)) {
						formulatedSolrQueries.add(new Query(solrQuery, hashtagScore));
						if(formulatedSolrQueries.size() >= queryNumberLimit) {
							done = true;
							break;
						}
					}
					if(done)
						break;
				}
			}
			else if(scaledRankedHashtags.isEmpty()) {
				for(Double keywordScore : scaledRankedKeywords.keySet()) {
					if(keywordScore < 0.5) {
						done = true;
						break;
					}
						
					for(String solrQuery : scaledRankedKeywords.get(keywordScore)) {
						formulatedSolrQueries.add(new Query(solrQuery, keywordScore));
						if(formulatedSolrQueries.size() >= queryNumberLimit) {
							done = true;
							break;
						}
					}
					if(done)
						break;
				}
			}
			else {
				for(Double keyScore : scaledRankedKeywords.keySet()) {
					boolean hashtagFound = false;
					for(Double hashScore : scaledRankedHashtags.keySet()) {
						if(keyScore < 0.5 && hashScore < 0.5) {
							done = true;
							break;
						}
					
						if(keyScore > hashScore) {
							break;
						}
					
						for(String solrQuery : scaledRankedHashtags.get(hashScore)) {
							formulatedSolrQueries.add(new Query(solrQuery,hashScore));
							if(formulatedSolrQueries.size() >= queryNumberLimit) {
								done = true;
								break;
							}
						}
						elementToRemove = hashScore;
						hashtagFound = true;
						break;
					}
					
					if(done)
						break;
					
					if(!hashtagFound) {
						for(String solrQuery : scaledRankedKeywords.get(keyScore)) {
							formulatedSolrQueries.add(new Query(solrQuery,keyScore));
							if(formulatedSolrQueries.size() >= queryNumberLimit) {
								done = true;
								break;
							}
						}
						elementToRemove = keyScore;
						keywordFound = true;
					}
					else {
						scaledRankedHashtags.remove(elementToRemove);
					}
					break;
				}
				
				if(keywordFound) {
					scaledRankedKeywords.remove(elementToRemove);
				}
			}
			
			if(done)
				break;
		}
		
		//if no queries have been formulated return no queries
		if(formulatedSolrQueries.isEmpty())
			return formulatedSolrQueries;
		
		List<Query> processedQueries = new ArrayList<Query>();
	
		//detect dysco entities inside newly formulated queries 
		for(Query query : formulatedSolrQueries) {
			for(Entity entity : dysco.getEntities()) {
				String entityName = entity.getName();
				if(query.getName().contains(entityName) || query.getName().equals(entityName)) {
					String temp = query.getName().replace(entityName, "\"" + entityName + "\"");
					query.setName(temp);
				}
			}
			query.setIsFromExpansion(true);
			processedQueries.add(query);
		}
		
		//merge primal solr queries and additional formulated queries on the basis of their scores
		return mergeSolrQueries(primalSolrQueries, processedQueries, 3*queryNumberLimit);
	}

	/**
	 * Returns the resulted queries after merging two sets of queries on the basis of their scores. 
	 * In case there is an overlap between two queries the algorithm re-assigns queries scores to boost
	 * those that occur more often than others. 
	 * The number of resulted queries is limited by queryLimit. 
	 * @param primalQueries
	 * @param processedQueries
	 * @param queryLimit
	 * @return List of merged queries
	 */
	private List<Query> mergeSolrQueries(List<Query> primalQueries, List<Query> processedQueries, int queryLimit) {
		
		List<Query> finalSolrQueries = new ArrayList<Query>();
		
		Map<String, Query> primalSolrQueriesWeights = new HashMap<String, Query>();
		Map<String, Query> processedSolrQueriesWeights = new HashMap<String, Query>();
		Map<Double, List<Query>> allRankedQueries = new TreeMap<Double, List<Query>>(Collections.reverseOrder());
		
		for(Query q : primalQueries) {
			primalSolrQueriesWeights.put(q.getName(), q);
		}
		
		for(Query q : processedQueries) {
			processedSolrQueriesWeights.put(q.getName(), q);
		}
			
		for(Query primalQuery : primalQueries) {
			List<String> entities = new ArrayList<String>();
			
			String restPrimalQuery = primalQuery.getName();
	
			int start = 0, end = 0;
			while(start != -1 && end != -1) {
				start = restPrimalQuery.indexOf("\"");
		
				if(start == -1)
					break;
				
    			String temp = restPrimalQuery.substring(start+1);
    		
    			end = temp.indexOf("\"") + start + 1;	
    			if(end == -1)
					break;
    			
    			end += 1;
   
    			String entity = restPrimalQuery.substring(start, end);
    	
    			restPrimalQuery = restPrimalQuery.replace(entity, "").trim();
    			entities.add(entity);
			}
			
			for(Query processedQuery : processedQueries) {
	
				List<String> otherEntities = new ArrayList<String>();
				String restProcessedQuery = processedQuery.getName();
				
				start = 0;
				end = 0;
				while(start != -1 && end != -1) {
					start = restProcessedQuery.indexOf("\"");
				
					if(start == -1)
						break;
	    			
					String temp = restProcessedQuery.substring(start+1);
	    
	    			end = temp.indexOf("\"") + start + 1;
	    
	    			if(end == -1)
						break;
	    			
	    			end += 1;
	    			String entity = restProcessedQuery.substring(start, end);
	   
	    			restProcessedQuery = restProcessedQuery.replace(entity, "").trim();
	    			otherEntities.add(entity);
				}
				
				for(String ent : entities) {
					for(String oEnt : otherEntities) {
						if(ent.equals(oEnt)) {
							processedQuery.setScore(processedSolrQueriesWeights.get(processedQuery.getName()).getScore()+primalQuery.getScore());
							primalQuery.setScore(primalSolrQueriesWeights.get(primalQuery.getName()).getScore()+processedQuery.getScore());
							
							processedSolrQueriesWeights.put(processedQuery.getName(), processedQuery);
							primalSolrQueriesWeights.put(primalQuery.getName(), primalQuery);
						}
					}		
				}
				
				String[] words = restPrimalQuery.split("//s+");
				for(String word : words) {
					if(processedQuery.getName().contains(word) || processedQuery.getName().equals(word)) {
						processedQuery.setScore(processedSolrQueriesWeights.get(processedQuery.getName()).getScore()+primalQuery.getScore());
						processedSolrQueriesWeights.put(processedQuery.getName(), processedQuery);
					}
						
				}
				
				String[] otherWords = restProcessedQuery.split("//s+");
				for(String word : otherWords) {
					if(primalQuery.getName().contains(word) || primalQuery.getName().equals(word)) {
						primalQuery.setScore(primalSolrQueriesWeights.get(primalQuery.getName()).getScore()+processedQuery.getScore());
						primalSolrQueriesWeights.put(primalQuery.getName(), primalQuery);
					}		
				}
			}
		}
		
		for(Map.Entry<String, Query> entry : primalSolrQueriesWeights.entrySet()) {
			
			if(allRankedQueries.containsKey(entry.getValue().getScore())) {
				boolean exists = false;
				List<Query> alreadyIn = allRankedQueries.get(entry.getValue().getScore());
				
				for(Query inQuery : alreadyIn) {
					if(inQuery.getName().equals(entry.getKey())) {
						exists = true;
						break;
					}
				}
				
				if(!exists) {
					alreadyIn.add(entry.getValue());
					allRankedQueries.put(entry.getValue().getScore(), alreadyIn);
				}
			}
			else {
				List<Query> alreadyIn = new ArrayList<Query>();
				
				alreadyIn.add(entry.getValue());
				allRankedQueries.put(entry.getValue().getScore(), alreadyIn);
			}
		}
		
		for(Map.Entry<String, Query> entry : processedSolrQueriesWeights.entrySet()) {
		
			if(allRankedQueries.containsKey(entry.getValue().getScore())) {
				boolean exists = false;
				List<Query> alreadyIn = allRankedQueries.get(entry.getValue().getScore());
				
				for(Query inQuery : alreadyIn) {
					if(inQuery.getName().equals(entry.getKey())) {
						exists = true;
						break;
					}
				}
				
				if(!exists) {
					alreadyIn.add(entry.getValue());
					allRankedQueries.put(entry.getValue().getScore(), alreadyIn);
				}
			}
			else {
				List<Query> alreadyIn = new ArrayList<Query>();
				
				alreadyIn.add(entry.getValue());
				allRankedQueries.put(entry.getValue().getScore(), alreadyIn);
			}
		}
		
		Map<Double, List<Query>> allScaledRankedQueries = scaleKeywordsToWeight(allRankedQueries);
		for(Map.Entry<Double, List<Query>> entry : allScaledRankedQueries.entrySet()) {
			
			if(finalSolrQueries.size() == queryLimit)
				break;
			
			for(Query finalQuery : entry.getValue()) {
				if(finalSolrQueries.size() == queryLimit)
					break;
				
				finalSolrQueries.add(finalQuery);
			}
		}
		return finalSolrQueries;
	}
	
	/**
	 * Scale solr queries scores on a 0-30 scale to accommodate comparison purposes
	 * @param inputData
	 * @return Map of scaled scores to the queries they correspond to
	 */
	private Map<Double, List<Query>> scaleKeywordsToWeight(Map<Double, List<Query>> inputData) {
		Map<Double, List<Query>> scaledData = new TreeMap<Double, List<Query>>(Collections.reverseOrder());
		
		if(inputData.isEmpty())
			return scaledData;
		
		double max = 0.0;
		double min = 1000000.0;
		
		for(Double score : inputData.keySet()) {
			if(score > max) {
				max = score;
			}
			
			if(score < min) {
				min = score;
			}
		}

		for(Map.Entry<Double, List<Query>> entry : inputData.entrySet()) {
			if(min == max) {
				scaledData.put(1.0, entry.getValue());
			}
			else {
				Double value = 30 * (entry.getKey() - min)/(max - min);
				scaledData.put(value, entry.getValue());
			}
		}
		return scaledData; 
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
