package eu.socialsensor.sfc.builder;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
import eu.socialsensor.sfc.builder.solrQueryBuilder.CustomSolrQueryBuilder;
import eu.socialsensor.sfc.builder.solrQueryBuilder.TrendingSolrQueryBuilder;

public class SolrQueryBuilder {
	public final Logger logger = Logger.getLogger(SolrQueryBuilder.class);
	
	protected static final String HOST = "host";
	protected static final String DATABASE = "database";
	protected static final String COLLECTION = "collection";
	
	public SolrQueryBuilder(){
		logger.info("SolrQueryBuilder instance created");
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

		
	}

}
