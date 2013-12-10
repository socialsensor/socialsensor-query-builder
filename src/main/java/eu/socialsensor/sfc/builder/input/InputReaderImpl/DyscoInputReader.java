package eu.socialsensor.sfc.builder.input.InputReaderImpl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.StreamUser.Category;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import eu.socialsensor.sfc.builder.input.InputReader;

public class DyscoInputReader implements InputReader{
	
	private Dysco dysco;
	
	private List<String> keywords;
	private List<String> people;
	private List<Entity> entities;
	private List<String> hashtags;
	private List<String> ngrams;
	private List<String> latent_keywords;
	
	public DyscoInputReader(Dysco dysco){
		this.dysco = dysco;
	}
	
	@Override
	public Map<FeedType,Object> getData(){
		//get all the necessary fields
		return null;
	}
	
	@Override
	public Map<String,List<Feed>> createFeedsPerStream(){
		
		//Call the appropriate method for feed creation according to
		//whether the dysco is custom or trending
		
		/**
		 * switch(dysco.getType) { //enum value
		 * 
		 * 		case: DYNAMIC
		 * 			return createCustomFeeds();
		 * 
		 * 		case: TRENDING
		 * 			return createTrendingFeeds();
		 * }
		 */
		
		return null;
	}
	
	@Override
	public List<Feed> createFeeds(){
		return null;
	}
	
	@Override
	public void run(){
		
		
	}
	
	@Override
	public Map<String,Set<String>> getUsersToLists(){
		return null;
	}
	
	@Override
	public Map<String,Category> getUsersToCategories(){
		return null;
	}
	
	private void formSolrQuery(){
		
	}
	
	private Map<String,List<Feed>> createCustomFeeds(){
		return null;
	}
	
	private Map<String,List<Feed>> createTrendingFeeds(){
		return null;
	}

}
