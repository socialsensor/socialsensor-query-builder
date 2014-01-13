package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.dysco.Dysco;

/**
 * @brief The class that creates the solr query based on the 
 * information of a custom dysco (keywords,hashtags,contributors)
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class CustomSolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(CustomSolrQueryBuilder.class);

	private List<String> contributors = new ArrayList<String>();
	
	private Set<String> keywords =  new HashSet<String>();
	private Set<String> hashtags = new HashSet<String>();
	
	public CustomSolrQueryBuilder(Dysco dysco){
		
		this.contributors = dysco.getContributors();
		this.keywords = dysco.getKeywords().keySet();
		this.hashtags = dysco.getHashtags().keySet();
		
		//filterDyscosContent();
	}
	
	/**
	 * Creates and returns the solr query. The solr query is created by combining 
	 * the keywords, hashtags and contributors that are inserted by the user from UI when
	 * a custom dysco is created.
	 * @return
	 */
	public String createSolrQuery(){
	
		if(keywords.isEmpty() && contributors.isEmpty() && hashtags.isEmpty())
			return null;
	
		boolean first = true;
		
		String solrQuery = "title : (";
		
		if(!keywords.isEmpty()){
			for(String keyword : keywords){
				String[] parts = null;
				if(keyword.split(" ").length > 1){
					parts = keyword.split(" ");
				}
				
				if(first){
					if(parts != null){
						solrQuery +="(";
						for(int i = 0;i<parts.length;i++){
							if(i == parts.length-1)
								solrQuery += parts[i];
							else
								solrQuery += parts[i]+" AND ";
						}
						solrQuery +=")";
					}
					else
						solrQuery += keyword;
					
					first = false;
				}	
				else{
					if(parts != null){
						solrQuery +="AND (";
						for(int i = 0;i<parts.length;i++){
							if(i == parts.length-1)
								solrQuery += parts[i];
							else
								solrQuery += parts[i]+" AND ";
						}
						solrQuery +=")";
					}
					else
						solrQuery += " AND " + keyword;
				}	
			}
		}
		
		if(!hashtags.isEmpty()){
			for(String hashtag : hashtags){
				if(first){
					solrQuery += hashtag;
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag;
			}
		}
		solrQuery += ")";
		first = true;
		solrQuery += " OR description : (";
		
		if(!keywords.isEmpty()){
			for(String keyword : keywords){
				String[] parts = null;
				if(keyword.split(" ").length > 1){
					parts = keyword.split(" ");
				}
				
				if(first){
					if(parts != null){
						solrQuery +="(";
						for(int i = 0;i<parts.length;i++){
							if(i == parts.length-1)
								solrQuery += parts[i];
							else
								solrQuery += parts[i]+" AND ";
						}
						solrQuery +=")";
					}
					else
						solrQuery += keyword;
					
					first = false;
				}	
				else{
					if(parts != null){
						solrQuery +="(";
						for(int i = 0;i<parts.length;i++){
							if(i == parts.length-1)
								solrQuery += parts[i];
							else
								solrQuery += parts[i]+" AND ";
						}
						solrQuery +=")";
					}
					else
						solrQuery += " AND " + keyword;
				}	
			}
		}
		
		if(!hashtags.isEmpty()){
			for(String hashtag : hashtags){
				if(first){
					solrQuery += hashtag;
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag;
			}
		}
		
		solrQuery += ")";
		first = true;
		solrQuery += " OR tags : (";
		
		if(!keywords.isEmpty()){
			for(String keyword : keywords){
				String[] parts = null;
				if(keyword.split(" ").length > 1){
					parts = keyword.split(" ");
				}
				
				if(first){
					if(parts != null){
						solrQuery +="(";
						for(int i = 0;i<parts.length;i++){
							if(i == parts.length-1)
								solrQuery += parts[i];
							else
								solrQuery += parts[i]+" AND ";
						}
						solrQuery +=")";
					}
					else
						solrQuery += keyword;
					
					first = false;
				}	
				else{
					if(parts != null){
						solrQuery +="(";
						for(int i = 0;i<parts.length;i++){
							if(i == parts.length-1)
								solrQuery += parts[i];
							else
								solrQuery += parts[i]+" AND ";
						}
						solrQuery +=")";
					}
					else
						solrQuery += " AND " + keyword;
				}	
			}
		}
		
		if(!hashtags.isEmpty()){
			for(String hashtag : hashtags){
				if(first){
					solrQuery += hashtag;
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag;
			}
		}
		
		solrQuery += ")";
		
		if(!contributors.isEmpty()){
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
		}	
		
		return solrQuery;
	}
	
	public String createUpdatedSolrQuery(){
		String solrQuery = "";
		String query = "";
		
		if(keywords.isEmpty() && contributors.isEmpty() && hashtags.isEmpty())
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
	
	private void filterDyscosContent(){
		Set<String> filteredHashtags = hashtags;
		Set<String> filteredKeywords = keywords;
		
		List<String> filteredContributors = contributors;
		
		
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
				contributors.remove(contributor);
			}
		}
	}
}
