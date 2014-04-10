package eu.socialsensor.sfc.builder;

import java.util.List;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.client.search.solr.SolrNewsFeedHandler;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
import eu.socialsensor.sfc.builder.solrQueryBuilder.CustomSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.TrendingSolrQueryBuilder;
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
			
			logger.info("---Solr Queries---");
			for(Query q : queries)
				logger.info("Query Name: "+q.getName()+" Query Score: "+q.getScore());
			
			return trendingBuilder.createSolrQuery();
		}
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
