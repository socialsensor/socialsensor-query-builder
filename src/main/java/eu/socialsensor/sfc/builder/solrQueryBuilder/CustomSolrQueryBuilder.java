package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.Dysco;

/**
 * @brief The class that creates the solr query based on the 
 * information of a custom dysco (keywords,hashtags,contributors)
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class CustomSolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(CustomSolrQueryBuilder.class);

	
	private List<String> users = new ArrayList<String>();
	
	private Set<String> keywords =  new HashSet<String>();
	private Set<String> hashtags = new HashSet<String>();
	
	
	//new fields
	private Set<String> keyPhrases = new HashSet<String>();
	private Set<String> wordsToAvoid = new HashSet<String>();
	private Set<String> twitterUsers = new HashSet<String>();
	private Set<String> mentionedTwitterUsers = new HashSet<String>();
	
	private Set<String> urls = new HashSet<String>();
	
	private List<Location> locations = new ArrayList<Location>();
	
	public CustomSolrQueryBuilder(Dysco dysco){
		
		this.users = dysco.getContributors();
		this.keywords = dysco.getKeywords().keySet();
		this.hashtags = dysco.getHashtags().keySet();
		
		//filterDyscosContent();
	}
	
	public String createSolrQuery(){
		String solrQuery = "";
		String query = "";
		
		if(keywords.isEmpty() && users.isEmpty() && hashtags.isEmpty())
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
		/*if(!contributors.isEmpty()){
			
			if(hashtags.isEmpty() && keywords.isEmpty())
				solrQuery += "author : (";
			else
				solrQuery += " OR author : (";
			
			first = true;
			
			for(String contributor : contributors){
				if(first){
					solrQuery += contributor;
					first = false;
				}	
				else
					solrQuery += " OR " + contributor;
			}
			
			solrQuery += ")";
		}*/
		return solrQuery;
	}
	
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
		
		for(String contributor : users){
			Query resQuery = new Query(contributor,0.0);
			resQuery.setType(Query.Type.Contributors);
			solrQueries.add(resQuery);
		}
		
		return solrQueries;
	}
	
	public List<Query> createUpdatedSolrQueries(){
		List<Query> solrQueries = new ArrayList<Query>();
		
		return solrQueries;
	}
	
	private void filterDyscosContent(){
		Set<String> filteredHashtags = hashtags;
		Set<String> filteredKeywords = keywords;
		
		List<String> filteredContributors = users;
		
		
		for(String hashtag : filteredHashtags){
			if(hashtag.equals(" ")){
				hashtags.remove(hashtag);
			}
		}
		

		for(String key : filteredKeywords){
			if(key.equals(" ")){
				keywords.remove(key);
			}
		}
		
		for(String contributor : filteredContributors){
			if(contributor.equals(" ")){
				users.remove(contributor);
			}
		}
	}
}
