package eu.socialsensor.sfc.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.client.dao.DyscoDAO;
import eu.socialsensor.framework.client.dao.impl.DyscoDAOImpl;
import eu.socialsensor.framework.client.search.SearchEngineResponse;
import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.client.search.solr.SolrNewsFeedHandler;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import eu.socialsensor.sfc.builder.solrQueryBuilder.CustomSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.KeywordsExtractor;
import eu.socialsensor.sfc.builder.solrQueryBuilder.QueryFormulator;
import eu.socialsensor.sfc.builder.solrQueryBuilder.TrendingSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.graph.GraphCreator;
/**
 * @brief Class for the creation of a SolrQuery
 * that will be used for the retrieval of Items and MediaItems
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class SolrQueryBuilder {
	public final Logger logger = Logger.getLogger(SolrQueryBuilder.class);
	
	protected static final String NEWS_FEED_HOST = "news.feed.host";
	protected static final String NEWS_FEED_COLLECTION = "news.feed.collection";
	private static final String SOLR_SERVICE = "solr.service";
	private static final Integer NUMBER_OF_KEYWORDS_IN_QUERY = 4;
	private String solrService;
	private String newsfeedHost;
	private String newsfeedCollection;
	
	private InputConfiguration config = null;
	
	private SolrNewsFeedHandler solrNewsFeedHandler;
	
	public SolrQueryBuilder() throws Exception{
		logger.info("SolrQueryBuilder instance created");
		
//		File configFile = new File("./conf/newsfeed.conf.xml");
//		
//		try {
//			config = InputConfiguration.readFromFile(configFile);
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//		
		if(config != null){
			this.solrService = config.getParameter(SolrQueryBuilder.SOLR_SERVICE);
			this.newsfeedHost = config.getParameter(SolrQueryBuilder.NEWS_FEED_HOST);
			this.newsfeedCollection = config.getParameter(SolrQueryBuilder.NEWS_FEED_COLLECTION);
		
			this.solrNewsFeedHandler = SolrNewsFeedHandler.getInstance(newsfeedHost+"/"+solrService+"/"+newsfeedCollection);
		}
	}
	
	/**
	 * Returns the solr query based on the information of
	 * a dysco (trending/custom)
	 * @param dysco
	 * @return the solr query
	 */
	public String getSolrQuery(Dysco dysco){
	
		if(dysco.getDyscoType() == null){
			logger.error("Dysco Type is not defined - Cannot extract solr query");
			return null;
		}
		
		if(dysco.getDyscoType().equals(DyscoType.CUSTOM)){
			logger.info("Find solr query for custom dysco : "+dysco.getId());
			
			CustomSolrQueryBuilder customBuilder = new CustomSolrQueryBuilder(dysco);
			
			return customBuilder.createSolrQuery();
		}
		else{
			logger.info("Find solr query for trending dysco : "+dysco.getId());
			
			TrendingSolrQueryBuilder trendingBuilder = new TrendingSolrQueryBuilder(dysco);
			
			//List<Query> queries = trendingBuilder.createPrimalSolrQueries();
			
			
			
			return trendingBuilder.createSolrQuery();
		}
	}
	
	public Dysco getUpdatedDysco(Dysco dysco){
		Dysco updatedDysco = dysco;
		if(dysco.getDyscoType() == null){
			logger.error("Dysco Type is not defined - Cannot extract solr query");
			return null;
		}
		
		if(dysco.getDyscoType().equals(DyscoType.CUSTOM)){
			logger.info("Find solr query for custom dysco : "+dysco.getId());
			
		
			
		}
		else{
			logger.info("Find solr query for trending dysco : "+dysco.getId());
			
			TrendingSolrQueryBuilder trendingBuilder = new TrendingSolrQueryBuilder(dysco);
			
			List<Query> queries = trendingBuilder.createPrimalSolrQueries();
			
			String solrQuery = trendingBuilder.createSolrQuery();
			
			updatedDysco.setSolrQueryString(solrQuery);
			updatedDysco.setSolrQueries(queries);
		
		}
		return updatedDysco;
	}
	
	public List<Query> getSolrQueries(Dysco dysco){
		if(dysco.getDyscoType() == null){
			logger.error("Dysco Type is not defined - Cannot extract solr query");
			return null;
		}
		
		if(dysco.getDyscoType().equals(DyscoType.CUSTOM)){
			logger.info("Find solr query for custom dysco : "+dysco.getId());
			
			CustomSolrQueryBuilder customBuilder = new CustomSolrQueryBuilder(dysco);
			
			return customBuilder.createSolrQueries();
		}
		else{
			logger.info("Find solr query for trending dysco : "+dysco.getId());
			
			TrendingSolrQueryBuilder trendingBuilder = new TrendingSolrQueryBuilder(dysco);
			
			List<Query> primalSolrQueries = trendingBuilder.createPrimalSolrQueries();

			return primalSolrQueries;
				
		}
	}
	
	public Dysco updateDyscoWithPrimalQueries(Dysco dysco) throws Exception{
		
		if(dysco.getDyscoType() == null)
			throw new Exception("Dysco Type is not defined - Cannot extract solr query");

		
		if(dysco.getDyscoType().equals(DyscoType.CUSTOM)){
			logger.info("Find solr query for custom dysco : "+dysco.getId());
			
			CustomSolrQueryBuilder customBuilder = new CustomSolrQueryBuilder(dysco);
			//needs to be implemented
		}
		else{
			logger.info("Find solr query for trending dysco : "+dysco.getId());
			
			TrendingSolrQueryBuilder trendingBuilder = new TrendingSolrQueryBuilder(dysco);
			List<Query> queries = trendingBuilder.createPrimalSolrQueries();
			dysco.setSolrQueries(queries);
		}
		
		return dysco;
	}
	
	public List<Query> getFurtherProcessedSolrQueries(List<Item> items,Integer queryNumberLimit,Dysco dysco){
		List<Query> formulatedSolrQueries = new ArrayList<Query>();
		KeywordsExtractor extractor = new KeywordsExtractor(items);
		extractor.processItemsText();
		
		List<String> topKeywords = extractor.getTopKeywords();
		//System.out.println("Additional Queries - Processed keywords from media items : "+topKeywords.size());
		Set<String> contentToProcess = extractor.getTextContent();
		//System.out.println("Additional Queries - Before Graph Creation");
		GraphCreator graphCreator = new GraphCreator(contentToProcess,topKeywords);
		graphCreator.setSubstituteWords(extractor.getWordsToReplace());
		graphCreator.createGraph();
		//System.out.println("Additional Queries - After Graph Creation");
		graphCreator.pruneLowConnectivityNodes();
		//System.out.println("Additional Queries -  Graph Pruning done");
		//System.out.println("Additional Queries -  Nodes in the graph : "+graphCreator.getGraph().getNodes().size());
		graphCreator.exportGephiGraphToFile(dysco.getId());
		
		if(graphCreator.getGraph().getNodes().size() == 0)
			return formulatedSolrQueries;
		
		QueryFormulator qFormulator = new QueryFormulator(graphCreator.getGraph(),extractor.getTopHashtags());
		
		qFormulator.generateKeywordQueries(NUMBER_OF_KEYWORDS_IN_QUERY);
		//System.out.println("Additional Queries -  Keywords Generated");
		qFormulator.generateHashtagQueries();
		//System.out.println("Additional Queries -  Hashtags Generated");
		
		qFormulator.printRankedKeywordQueries();
		//qFormulator.printRankedHashtagQueries();
		Map<Double, List<String>> scaledRankedKeywords = scaleKeywordsToWeight(qFormulator.getRankedKeywordQueries());
		Map<Double, List<String>> scaledRankedHashtags = scaleKeywordsToWeight(qFormulator.getRankedHashtagQueries());
		
		System.out.println("***Scaled Ranked Keyword Queries ***");
		System.out.println();
		
		for(Double value : scaledRankedKeywords.keySet()){
			System.out.println("---- SCALED SCORE "+value+" ----");
			System.out.println();
			for(String rQuery : scaledRankedKeywords.get(value)){
				System.out.println("Q : "+rQuery);
			}
			System.out.println();
		}
		
		System.out.println("***Scaled Ranked Hashtag Queries ***");
		System.out.println();
		
		for(Double value : scaledRankedHashtags.keySet()){
			System.out.println("---- SCALED SCORE "+value+" ----");
			System.out.println();
			for(String rQuery : scaledRankedHashtags.get(value)){
				System.out.println("Q : "+rQuery);
			}
			System.out.println();
		}
		logger.info("Additional Queries -  Generating final Queries");
		while(formulatedSolrQueries.size() < queryNumberLimit){
			boolean keyFound = false;
			boolean done = false;
			Double elementToRemove = 0.0; 
			
			if(scaledRankedKeywords.isEmpty() && scaledRankedHashtags.isEmpty())
				break;
			
			
			if(scaledRankedKeywords.isEmpty()){
				for(Double hashScore : scaledRankedHashtags.keySet()){
					
					if(hashScore<1.5){
						done = true;
						break;
					}
					
					System.out.println("Number of hashtag queries that have "+hashScore+" score are: "+scaledRankedHashtags.get(hashScore).size());
					for(String solrQuery : scaledRankedHashtags.get(hashScore)){
						System.out.println("Add hashtag query: "+solrQuery);
						formulatedSolrQueries.add(new Query(solrQuery,hashScore));
						if(formulatedSolrQueries.size() >= queryNumberLimit){
							done = true;
							break;
						}
					}
					if(done)
						break;
				}
			}
			else if(scaledRankedHashtags.isEmpty()){
				for(Double keyScore : scaledRankedKeywords.keySet()){
					
					if(keyScore<1.5){
						done = true;
						break;
					}
						
					System.out.println("Number of keyword queries that have "+keyScore+" score are: "+scaledRankedKeywords.get(keyScore).size());
					for(String solrQuery : scaledRankedKeywords.get(keyScore)){
						System.out.println("Add key query: "+solrQuery);
						formulatedSolrQueries.add(new Query(solrQuery,keyScore));
						if(formulatedSolrQueries.size() >= queryNumberLimit){
							done = true;
							break;
						}
					}
					if(done)
						break;
				}
			}
			else{
				for(Double keyScore : scaledRankedKeywords.keySet()){
					boolean hashFound = false;
					for(Double hashScore : scaledRankedHashtags.keySet()){
						if(keyScore<1.5 && hashScore<1.5){
							done = true;
							break;
						}
						System.out.println("keyScore: "+keyScore+" ** hashScore: "+hashScore);
						if(keyScore > hashScore){
							break;
						}
						System.out.println("Number of hashtag queries that have "+hashScore+" score are: "+scaledRankedHashtags.get(hashScore).size());
						for(String solrQuery : scaledRankedHashtags.get(hashScore)){
							System.out.println("Add hashtag query: "+solrQuery);
							formulatedSolrQueries.add(new Query(solrQuery,hashScore));
							if(formulatedSolrQueries.size() >= queryNumberLimit){
								done = true;
								break;
							}
						}
						elementToRemove = hashScore;
						hashFound = true;
						break;
					}
					
					if(done)
						break;
					
					if(!hashFound){
						System.out.println("Number of keyword queries that have "+keyScore+" score are: "+scaledRankedKeywords.get(keyScore).size());
						for(String solrQuery : scaledRankedKeywords.get(keyScore)){
							System.out.println("Add keyword query: "+solrQuery);
							formulatedSolrQueries.add(new Query(solrQuery,keyScore));
							if(formulatedSolrQueries.size() >= queryNumberLimit){
								done = true;
								break;
							}
						}
						elementToRemove = keyScore;
						keyFound = true;
					}
					else{
						scaledRankedHashtags.remove(elementToRemove);
						
					}
					break;
				}
				if(keyFound){
					//System.out.println("Remove key score: "+elementToRemove);
					scaledRankedKeywords.remove(elementToRemove);
				}
			}
			
			if(done)
				break;
		}
		
		for(Query query : formulatedSolrQueries){
			System.out.println("Additional query : "+query.getName()+" with score : "+query.getScore());
		}
		
		return formulatedSolrQueries;
	}

	
	private Map<Double,List<String>> scaleKeywordsToWeight(Map<Double,List<String>> inputData){
		Map<Double,List<String>> scaledData = new TreeMap<Double,List<String>>(Collections.reverseOrder());
		
		if(inputData.isEmpty())
			return scaledData;
		
		double max = 0.0;
		double min = 1000000.0;
		
		for(Double score : inputData.keySet()){
			if(score>max){
				max = score;
			}
			if(score<min){
				min = score;
			}
		}

		for(Map.Entry<Double, List<String>> entry : inputData.entrySet()){
			if(min == max)
				scaledData.put(1.0, entry.getValue());
			else{
				Double value = (entry.getKey() - min)/(max - min);
				scaledData.put(value, entry.getValue());
			}
		}
		
		return scaledData; 
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Dysco dysco = new Dysco();
		Map<String,Double> keywords = new HashMap<String,Double>();
		Map<String,Double> hashtags = new HashMap<String,Double>();
		List<Entity> entities = new ArrayList<Entity>();
		
		keywords.put("stop getting", 32.0);
		keywords.put("whyimvotingukip", 21.0);
		keywords.put("August", 32.0);
		keywords.put("home secretary theresa", 32.0);
		keywords.put("public funding", 32.0);
		keywords.put("mine", 21.0);
		keywords.put("wales  police federation", 32.0);
		keywords.put("people", 21.0);
		keywords.put("other countries", 21.0);
		keywords.put("main reason", 21.0);
		keywords.put("live", 21.0);
		
		hashtags.put("pfewconf14", 3.0);
		hashtags.put("whyimvotingukip", 21.0);
		
		entities.add(new Entity("Theresa May",35.0,Entity.Type.PERSON));
		entities.add(new Entity("Wales",32.0,Entity.Type.LOCATION));
		entities.add(new Entity("England",32.0,Entity.Type.LOCATION));
		
		dysco.setEntities(entities);
		dysco.setKeywords(keywords);
		dysco.setHashtags(hashtags);
		
		dysco.setDyscoType(DyscoType.TRENDING);
		
		SolrQueryBuilder builder;
		try {
			builder = new SolrQueryBuilder();
			List<Query> queries = builder.getSolrQueries(dysco);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
