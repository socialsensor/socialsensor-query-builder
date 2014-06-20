package eu.socialsensor.sfc.builder.input.InputReaderImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.StreamUser.Category;
import eu.socialsensor.framework.common.domain.dysco.CustomDysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Dysco.DyscoType;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.ListFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.sfc.builder.input.InputReader;


/**
 * @brief The class that is responsible for the creation of input feeds
 * from DySco content
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class DyscoInputReader implements InputReader{
	
	private Dysco dysco;
	
	private CustomDysco customDysco;
	
	private List<Feed> feeds = new ArrayList<Feed>();
	
	private Date date;
	private DateUtil dateUtil = new DateUtil();
	
	public DyscoInputReader(Dysco dysco){
		this.dysco = dysco;

		if(dysco.getDyscoType().equals(DyscoType.CUSTOM)){
			this.customDysco = (CustomDysco) dysco;
		}
	
	}
	
	@Override
	public Map<FeedType,Object> getData(){
		Map<FeedType,Object> inputDataPerType = new HashMap<FeedType,Object>();
		
		this.date = dateUtil.addDays(dysco.getCreationDate(),-2);
		
		//standard for trending dysco 
		Set<Keyword> queryKeywords = new HashSet<Keyword>();
		
		List<Query> solrQueries = dysco.getSolrQueries();
		
		if(customDysco != null){
			
			Set<Source> querySources = new HashSet<Source>();
			Set<Location> queryLocations = new HashSet<Location>();
			Set<String> queryLists = new HashSet<String>();
			
			List<String> twitterUsers = customDysco.getTwitterUsers();
			List<String> mentionedUsers = customDysco.getMentionedUsers();
			List<String> listsOfUsers = customDysco.getListsOfUsers();
			List<Location> locations = customDysco.getNearLocations();
	    	
			Map<String,String> otherUsers = customDysco.getOtherSocialNetworks();
			
			if(twitterUsers != null || otherUsers != null){
				for(String user : twitterUsers){
					Source source = new Source(user,0f);
					source.setNetwork("Twitter");
					querySources.add(source);
				}
				
				for(String user : otherUsers.keySet()){
					Source source = new Source(user,0f);
					source.setNetwork(otherUsers.get(user));
					querySources.add(source);
				}
			}
			if(mentionedUsers != null){
				for(String user : mentionedUsers){
					
					Keyword key = new Keyword(user,0f);
					queryKeywords.add(key);
				}
				
			}
			
			if(listsOfUsers != null){
				for(String list : listsOfUsers){
					queryLists.add(list);
				}
			}
			if(locations != null){
				for(Location location : locations){
					queryLocations.add(location);
				}
			}
			
			if(!querySources.isEmpty())
				inputDataPerType.put(FeedType.SOURCE, querySources);
			if(!queryLocations.isEmpty())
				inputDataPerType.put(FeedType.LOCATION, queryLocations);
			if(!queryLists.isEmpty())
				inputDataPerType.put(FeedType.LIST, queryLists);
			
		}
		
		if(solrQueries != null){
			for(Query solrQuery : solrQueries){
				String queryName = solrQuery.getName();
				double score = 0.0;
				if(solrQuery.getScore() != null)
					score = solrQuery.getScore();
				
				Keyword key = new Keyword(queryName,score);
			
				queryKeywords.add(key);
			}
		}
		
		if(!queryKeywords.isEmpty())
			inputDataPerType.put(FeedType.KEYWORDS, queryKeywords);
		
		
		return inputDataPerType;
		
	}

	@Override
	public Map<String,List<Feed>> createFeedsPerStream(){

		return null;
	}

	@Override
	public List<Feed> createFeeds(){
		
		Map<FeedType,Object> inputData = getData();
		
		for(FeedType feedType : inputData.keySet()){
			switch(feedType){
			case SOURCE :
				@SuppressWarnings("unchecked")
				List<Source> sources = (List<Source>) inputData.get(feedType);
				for(Source source : sources){
					String feedID = UUID.randomUUID().toString();
					SourceFeed sourceFeed = new SourceFeed(source,date,feedID);
					feeds.add(sourceFeed);
				}
				break;
			case KEYWORDS : 
				@SuppressWarnings("unchecked")
				Set<Keyword> keywords = (Set<Keyword>) inputData.get(feedType);
				for(Keyword keyword : keywords){
					String feedID = UUID.randomUUID().toString();
					KeywordsFeed keywordsFeed = new KeywordsFeed(keyword,date,feedID);
					feeds.add(keywordsFeed);
				}
				break;
			case LOCATION :
				@SuppressWarnings("unchecked")
				List<Location> locations = (List<Location>) inputData.get(feedType);
				for(Location location : locations){
					String feedID = UUID.randomUUID().toString();
					LocationFeed locationFeed = new LocationFeed(location,date,feedID);
					feeds.add(locationFeed);
				}
				break;
			case LIST :
				@SuppressWarnings("unchecked")
				List<String> lists = (List<String>) inputData.get(feedType);
				for(String list : lists){
					String feedID = UUID.randomUUID().toString();
					ListFeed listFeed = new ListFeed(list,date,feedID);
					feeds.add(listFeed);
				}
			default:
				break;
			}
		}
		
		return feeds;
	}

	
	@Override
	public Map<String,Set<String>> getUsersToLists(){
		return null;
	}
	
	@Override
	public Map<String,Category> getUsersToCategories(){
		return null;
	}
	
	/**
	 * Class for handling date util
	 * @author ailiakop
	 *
	 */
	public class DateUtil
	{
	    public Date addDays(Date date, int days)
	    {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        cal.add(Calendar.DATE, days); //negative number decrements the days
	        return cal.getTime();
	    }
	}
}
