package eu.socialsensor.sfc.builder;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
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
	
	protected static final String HOST = "host";
	protected static final String DATABASE = "database";
	protected static final String COLLECTION = "collection";
	
	public SolrQueryBuilder(){
		logger.info("SolrQueryBuilder instance created");
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
			
			return trendingBuilder.createSolrQuery();
		}
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
		final SolrDyscoHandler handler = SolrDyscoHandler.getInstance("http://social1.atc.gr:8080/solr/dyscos");
		Dysco dysco = handler.findDyscoLight("7073ee25-5ee1-472b-9b1d-a840c748be15");
		if(dysco == null)
			System.err.println("Dysco is NULL");
		System.out.println("Solr query : "+solrQueryBuilder.getSolrQuery(dysco));
	
	}

}
