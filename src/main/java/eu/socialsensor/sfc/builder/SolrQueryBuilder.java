package eu.socialsensor.sfc.builder;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.client.dao.TopicDAO;
import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
import eu.socialsensor.sfc.builder.solrQueryBuilder.CustomSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.TrendingSolrQueryBuilder;

public class SolrQueryBuilder {
	public final Logger logger = Logger.getLogger(SolrQueryBuilder.class);
	
	protected static final String HOST = "host";
	protected static final String DATABASE = "database";
	protected static final String COLLECTION = "collection";
	
	private TopicDAO topicDAO;
	
	private String host = null;
	private String database = null;
	private String collection = null;
	
	public SolrQueryBuilder(){
		logger.info("SolrQueryBuilder instance created");
		//this.host = inputConfig.getParameter(SolrQueryBuilder.HOST);
		//this.database = inputConfig.getParameter(SolrQueryBuilder.DATABASE);
		//this.collection = inputConfig.getParameter(SolrQueryBuilder.COLLECTION);
		
		//if(host == null || database == null || collection == null)
			//logger.error("Solr Query Builder needs to be configured!");
		
		//topicDAO = new TopicDAOImpl(host, database,collection);
		
		//Set RSS Processor
		//this.rssProcessor = new RSSProcessor();
		//this.rssProcessor.setRSSProcessor(topicDAO);
		
	}
	
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
			
			return trendingBuilder.createSolrQuery();
		}
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
		final SolrDyscoHandler handler = SolrDyscoHandler.getInstance("http://social1.atc.gr:8080/solr/dyscos");
		Dysco dysco = handler.findDyscoLight("0dbcbe53-2ceb-414b-aa9d-d477f21a2623");
		if(dysco == null)
			System.err.println("Dysco is NULL");
		System.out.println("Solr query : "+solrQueryBuilder.getSolrQuery(dysco));
			
	
		
	}

}
