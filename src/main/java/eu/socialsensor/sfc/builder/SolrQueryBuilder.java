package eu.socialsensor.sfc.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.client.search.solr.SolrNewsFeedHandler;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
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
			
			List<Query> queries = trendingBuilder.createPrimalSolrQueries();
			
			
			
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
			
			updatedDysco.setSolrQuery(solrQuery);
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
			if(solrNewsFeedHandler != null)
				trendingBuilder.setHandler(solrNewsFeedHandler);
			return trendingBuilder.createPrimalSolrQueries();
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
	
	public List<Query> getFurtherProcessedSolrQueries(List<Item> items,Integer queryNumberLimit){
		List<Query> formulatedSolrQueries = new ArrayList<Query>();
		KeywordsExtractor extractor = new KeywordsExtractor(items);
		extractor.processMediaItemsText();
		
		List<String> topKeywords = extractor.getTopKeywords();
		
		Set<String> contentToProcess = extractor.getTextContent();
		
		GraphCreator graphCreator = new GraphCreator(contentToProcess,topKeywords);
		graphCreator.setSubstituteWords(extractor.getWordsToReplace());
		graphCreator.createGraph();
	
		graphCreator.pruneLowConnectivityNodes();
		System.out.println(" Nodes created : "+graphCreator.getGraph().getNodes().size());
		QueryFormulator qFormulator = new QueryFormulator(graphCreator.getGraph(),extractor.getTopHashtags());
		
		qFormulator.generateKeywordQueries(NUMBER_OF_KEYWORDS_IN_QUERY);
		qFormulator.generateHashtagQueries();
		
		qFormulator.printRankedKeywordQueries();
		qFormulator.printRankedHashtagQueries();
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
		
		while(formulatedSolrQueries.size() < queryNumberLimit){
			boolean keyFound = false;
			boolean done = false;
			Double elementToRemove = 0.0; 
			
			if(scaledRankedKeywords.isEmpty() && scaledRankedHashtags.isEmpty())
				break;
			
			for(Double keyScore : scaledRankedKeywords.keySet()){
				boolean hashFound = false;
				for(Double hashScore : scaledRankedHashtags.keySet()){
					//System.out.println("keyScore: "+keyScore+" ** hashScore: "+hashScore);
					if(keyScore > hashScore){
						break;
					}
					//System.out.println("Number of queries that have "+hashScore+" score are: "+scaledRankedHashtags.get(hashScore).size());
					for(String solrQuery : scaledRankedHashtags.get(hashScore)){
						//System.out.println("Add solrquery: "+solrQuery);
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
					//System.out.println("Number of queries that have "+keyScore+" score are: "+scaledRankedKeywords.get(keyScore).size());
					for(String solrQuery : scaledRankedKeywords.get(keyScore)){
						//System.out.println("Add solrquery: "+solrQuery);
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
					//System.out.println("Remove hash score: "+elementToRemove);
					scaledRankedHashtags.remove(elementToRemove);
					
				}
				break;
			}
			if(keyFound){
				//System.out.println("Remove key score: "+elementToRemove);
				scaledRankedKeywords.remove(elementToRemove);
			}
				
		}
		
		for(Query query : formulatedSolrQueries){
			System.out.println("Selected query : "+query.getName()+" with score : "+query.getScore());
		}
		
		return formulatedSolrQueries;
	}

	
	private Map<Double,List<String>> scaleKeywordsToWeight(Map<Double,List<String>> inputData){
		Map<Double,List<String>> scaledData = new TreeMap<Double,List<String>>(Collections.reverseOrder());
		
		if(inputData.isEmpty())
			return scaledData;
		
		double max = 0.0;
		double min = 100000000000000000.0;
		
		for(Double score : inputData.keySet()){
			if(score>max){
				max = score;
			}
			if(score<min){
				min = score;
			}
		}
		
		for(Map.Entry<Double, List<String>> entry : inputData.entrySet()){
			Double value = (entry.getKey() - min)/(max - min);
			scaledData.put(value, entry.getValue());
		
		}
		
		return scaledData; 
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 SolrDyscoHandler solrdyscoHandler = SolrDyscoHandler.getInstance("http://social1.atc.gr:8080/solr/dyscos");
	       
	       Dysco dysco = solrdyscoHandler.findDyscoLight("6932fe50-7317-45f7-9001-2b7069bf6afc");
	       SolrQueryBuilder builder = null;
		try {
			builder = new SolrQueryBuilder();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        System.out.println(" query : "+builder.getSolrQuery(dysco));
	}

}
