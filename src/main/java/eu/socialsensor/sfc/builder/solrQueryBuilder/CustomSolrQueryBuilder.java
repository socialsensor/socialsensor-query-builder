package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.CustomDysco;

/**
 * @brief The class that creates the solr query based on the 
 * the content of a custom DySco (twitter users, keywords, hashtags, mentioned users,
 * list of users and urls)
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class CustomSolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(CustomSolrQueryBuilder.class);

	private Set<String> keywords =  new HashSet<String>();
	private Set<String> hashtags = new HashSet<String>();

	public CustomSolrQueryBuilder(CustomDysco dysco){
	     
		this.keywords = dysco.getKeywords().keySet();
		this.hashtags = dysco.getHashtags().keySet();
		
	}
	/**
	 * Formulates one solr query connected with AND's and OR's
	 * ready to be used directly for retrieval from solr.
	 * @return
	 */
	public String createSolrQuery(){
		String solrQuery = "";
		String query = "";
		
		if(keywords.isEmpty() && hashtags.isEmpty())
			return solrQuery;
		
		boolean first = true;
		
		if(!hashtags.isEmpty()){
			for(String hashtag : hashtags){
				for(String key : keywords){
					if(first){
						query += "("+hashtag+" AND "+ key+")";
						first = false;
					}
					else
						query += " OR (" + hashtag+" AND "+ key+")";
				}
				
				if(keywords.isEmpty()){
					if(first){
						query += hashtag;
						first = false;
					}	
					else
						query += " OR " + hashtag+"";
				}
				
				
			}
		}
	
		if(hashtags.isEmpty() && !keywords.isEmpty()){
			for(String key : keywords){
				if(first){
					query += key;
					first = false;
				}
				else
					query += " OR " + key+"";
			}
		}
		
		
		//Final formulation of solr query
		
		if(!query.equals("")){
			solrQuery += "(title : "+query+") OR (description:"+query+") OR (tags:"+query+")";
		}
		
		return solrQuery;
	}
	/**
	 * Formulates solr queries out of DySco content (keywords and hashtags).
	 * The formulated queries are the product of using  keywords and hashtags independently.
	 * The formulated queries need to be aggregated to be used for solr retrieval.
	 * @return the list of queries
	 */
	public List<Query> createSolrQueries(){
		List<Query> solrQueries = new ArrayList<Query>();
		
		for(String key : keywords){
			Query resQuery = new Query(key,0.0);
			resQuery.setType(Query.Type.Keywords);
			solrQueries.add(resQuery);
		}
		
		for(String hash : hashtags){
			Query resQuery = new Query(hash,0.0);
			resQuery.setType(Query.Type.Keywords);
			solrQueries.add(resQuery);
		}

		return solrQueries;
	}

}
