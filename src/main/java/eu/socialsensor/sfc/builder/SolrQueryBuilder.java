package eu.socialsensor.sfc.builder;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import eu.socialsensor.framework.client.dao.TopicDAO;
import eu.socialsensor.framework.client.dao.impl.TopicDAOImpl;
import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
import eu.socialsensor.sfc.builder.solrQueryBuilder.CustomSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.RSSProcessor;
import eu.socialsensor.sfc.builder.solrQueryBuilder.TrendingSolrQueryBuilder;

public class SolrQueryBuilder {
	public final Logger logger = Logger.getLogger(SolrQueryBuilder.class);
	
	protected static final String HOST = "host";
	protected static final String DATABASE = "database";
	protected static final String COLLECTION = "collection";
	
	private RSSProcessor rssProcessor = null;
	
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
	 * Sets the RSSProcessor that is used for handling
	 * the rss topics for keywords' extraction
	 * @param topicDAO
	 */
	public void setRSSProcessor(TopicDAO topicDAO){
		rssProcessor.setRSSProcessor(topicDAO);
		rssProcessor.processRSSItems();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
		SolrDyscoHandler dyscoHandler = SolrDyscoHandler.getInstance("dyscos");
		Dysco dysco = dyscoHandler.findDyscoLight("399993f4-05d6-4a8d-94db-66b6048f7915");
		
		System.out.println("Solr query : "+solrQueryBuilder.getSolrQuery(dysco));
			
	
		
	}

}
