package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.dysco.Dysco;

public class CustomSolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(CustomSolrQueryBuilder.class);

	private List<String> contributors = new ArrayList<String>();
	
	private Set<String> keywords =  new HashSet<String>();
	private Set<String> hashtags = new HashSet<String>();
	
	public CustomSolrQueryBuilder(Dysco dysco){
		
		this.contributors = dysco.getContributors();
		this.keywords = dysco.getKeywords().keySet();
		this.hashtags = dysco.getHashtags().keySet();
		
	}
	
	public String createSolrQuery(){
	
		if(keywords.isEmpty() && contributors.isEmpty() && hashtags.isEmpty())
			return null;
	
		boolean first = true;
		
		String solrQuery = "title : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(String keyword : keywords){
				if(first){
					solrQuery += keyword;
					first = false;
				}	
				else
					solrQuery += " AND " + keyword;
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
		
		solrQuery += " OR description : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(String keyword : keywords){
				if(first){
					solrQuery += keyword;
					first = false;
				}	
				else
					solrQuery += " AND " + keyword;
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
		
		solrQuery += " OR tags : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(String keyword : keywords){
				if(first){
					solrQuery += keyword;
					first = false;
				}	
				else
					solrQuery += " AND " + keyword;
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
	
/*	*//**
	 * Filters dysco's content 
	 *//*
	private void filterContent(){
		for(String d_keyword : keywords){
			d_keyword = d_keyword.toLowerCase();
			d_keyword = d_keyword.replaceAll("'s", "");
			d_keyword = d_keyword.replaceAll("'", "");
			d_keyword = d_keyword.replaceAll("[:.,?!;&'#]+","");
			d_keyword = d_keyword.replaceAll("\\s+", " ");
			
		}
		
		for(String d_tag : hashtags){
			d_tag = d_tag.toLowerCase();
			d_tag = d_tag.replaceAll("'s", "");
			d_tag = d_tag.replaceAll("'", "");
			d_tag = d_tag.replaceAll("[:.,?!;&']+","");
			d_tag = d_tag.replaceAll("\\s+", " ").trim();
			
		}
	}*/
}
