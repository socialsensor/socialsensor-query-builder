package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.List;
import java.util.Set;

import eu.socialsensor.framework.common.domain.dysco.Dysco;

public class CustomSolrQueryBuilder {
	
	private Dysco dysco = null;

	private List<String> contributors = null;
	
	private Set<String> keywords = null;
	private Set<String> hashtags = null;
	
	public CustomSolrQueryBuilder(Dysco dysco){
		this.dysco = dysco;
		
		this.contributors = dysco.getContributors();
		this.keywords = dysco.getKeywords().keySet();
		this.hashtags = dysco.getHashtags().keySet();
		
		filterContent();
	}
	
	public String createSolrQuery(){
		String solrQuery = "keywords : (";
		
		boolean first = true;
		//add keywords to query
		for(String keyword : keywords){
			if(first){
				solrQuery += keyword;
				first = false;
			}	
			else
				solrQuery += "OR" + keyword;
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
				solrQuery += "OR" + hashtag;
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
				solrQuery += "OR" + contributor;
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
