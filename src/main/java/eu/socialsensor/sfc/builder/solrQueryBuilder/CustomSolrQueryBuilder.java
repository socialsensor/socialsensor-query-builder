package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.dysco.Dysco;

public class CustomSolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(CustomSolrQueryBuilder.class);
	
	private Dysco dysco = null;

	private List<String> contributors = new ArrayList<String>();
	
	private Set<String> keywords =  new HashSet<String>();
	private Set<String> hashtags = new HashSet<String>();
	
	public CustomSolrQueryBuilder(Dysco dysco){
		this.dysco = dysco;
		
		if(dysco.getContributors() != null)
			this.contributors = dysco.getContributors();
		
		if(dysco.getKeywords() != null)
			this.keywords = dysco.getKeywords().keySet();
		
		if(dysco.getHashtags() != null)
			this.hashtags = dysco.getHashtags().keySet();
		
		filterContent();
	}
	
	public String createSolrQuery(){
		logger.info("Creating solr query");
		logger.info("Keywords size : "+keywords.size());
		logger.info("Contributors size : "+contributors.size());
		logger.info("Hashtags size : "+hashtags.size());
		
		String solrQuery = "keywords : (";
		
		boolean first = true;
		//add keywords to query
		for(String keyword : keywords){
			if(first){
				solrQuery += keyword;
				first = false;
			}	
			else
				solrQuery += " OR " + keyword;
		}
		solrQuery += ") OR hashtags : (";
		
		first = true;
		//add hashtags to query
		for(String hashtag : hashtags){
			if(first){
				solrQuery += hashtag;
				first = false;
			}	
			else
				solrQuery += " AND " + hashtag;
		}
		solrQuery += ") OR contributors : (";
		
		first = true;
		//add contributors to query
		for(String contributor : contributors){
			if(first){
				solrQuery += contributor;
				first = false;
			}	
			else
				solrQuery += " OR " + contributor;
		}
		solrQuery += ")";
		
		
		return solrQuery;
	}
	
	/**
	 * Filters dysco's content 
	 */
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
	}
}
