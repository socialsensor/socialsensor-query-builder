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
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.StreamUser.Category;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.sfc.builder.input.InputReader;


/**
 * @brief The class that is responsible for the creation of input feeds
 * from a dysco
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class DyscoInputReader implements InputReader{
	
	private Dysco dysco;
	
	private String solrQuery;
	
	private Set<Keyword> dyscoKeywords = new HashSet<Keyword>();
	
	private List<String> contributors = new ArrayList<String>();
	private List<Feed> feeds = new ArrayList<Feed>();
	
	private Date date;
	private DateUtil dateUtil = new DateUtil();
	
	public DyscoInputReader(Dysco dysco){
		this.dysco = dysco;
	}
	
	@Override
	public Map<FeedType,Object> getData(){
		Map<FeedType,Object> inputDataPerType = new HashMap<FeedType,Object>();
		
		this.solrQuery = dysco.getSolrQueryString();
		
		this.date = dateUtil.addDays(dysco.getCreationDate(),-2);
		
		if(solrQuery == "")
			return null;
		
		int startOne = solrQuery.indexOf("(");
		int endOne = solrQuery.indexOf(")");
		String oneString = solrQuery.substring(startOne + ("(").length(),endOne);
		System.out.println("One String : "+oneString);
		if(oneString.contains("OR")){
			
			String[] splittedKeywords = oneString.split(" OR ");
			for(int i = 0 ; i<splittedKeywords.length ; i++){
				System.out.println("keyword : "+splittedKeywords[i]);
				dyscoKeywords.add(new Keyword(splittedKeywords[i], 0.0f));
			}
		}else{
			System.out.println("keyword : "+oneString);
			dyscoKeywords.add(new Keyword(oneString,0.0f));
		}
		
		if(solrQuery.contains(" AND ")){
			
			String tempString = solrQuery.substring(endOne + (") AND (").length());
			String secondString = tempString.substring(0, tempString.indexOf(")"));
			System.out.println("Second String : "+secondString);
			if(secondString.contains("OR")){
				String[] splittedKeywords = secondString.split(" OR ");
				for(int i = 0 ; i<splittedKeywords.length ; i++){
					System.out.println("keyword : "+splittedKeywords[i]);
					dyscoKeywords.add(new Keyword(splittedKeywords[i], 0.0f));
				}
			}else{
				System.out.println("keyword : "+secondString);
				dyscoKeywords.add(new Keyword(secondString,0.0f));
			}
			
		}
		if(!dyscoKeywords.isEmpty()){
			inputDataPerType.put(FeedType.KEYWORDS, dyscoKeywords);
		}
		
		/*if(solrQuery.contains("title")){
			int beginIndex = solrQuery.indexOf("title");
			String rawString = solrQuery.substring(beginIndex + ("title : (").length());
			//System.out.println("rawString : "+rawString);
			int endIndex = rawString.indexOf(")");
			String keywordsString = rawString.substring(0, endIndex);
			System.out.println("keywordsString : "+keywordsString);
			
			if(keywordsString.contains("OR") && keywordsString.contains("AND")){
				String[] splittedKeywords = keywordsString.split(" OR ");
				for(int i = 0 ; i<splittedKeywords.length ; i++){
					String[] doubleSplitted = splittedKeywords[i].split(" AND ");
					for(int j = 0 ; j<doubleSplitted.length ; j++){
						System.out.println("keyword : "+doubleSplitted[j]);
						dyscoKeywords.add(new Keyword(doubleSplitted[j], 0.0f));
					}
				}
			}
			else if(keywordsString.contains("OR")){
				String[] splittedKeywords = keywordsString.split(" OR ");
				for(int i = 0 ; i<splittedKeywords.length ; i++){
					System.out.println("keyword : "+splittedKeywords[i]);
					dyscoKeywords.add(new Keyword(splittedKeywords[i], 0.0f));
				}
			}
			else if(keywordsString.contains("AND")){
				String[] splittedKeywords = keywordsString.split(" AND ");
				for(int i = 0 ; i<splittedKeywords.length ; i++){
					System.out.println("keyword : "+splittedKeywords[i]);
					dyscoKeywords.add(new Keyword(splittedKeywords[i], 0.0f));
				}
			}
			else{
				dyscoKeywords.add(new Keyword(keywordsString,0.0f));
			}
			
			if(!dyscoKeywords.isEmpty()){
				inputDataPerType.put(FeedType.KEYWORDS, dyscoKeywords);
			}
		}*/
		
		/*if(solrQuery.contains("contributors")){
			int beginIndex = solrQuery.indexOf("contributors");
			String rawString = solrQuery.substring(beginIndex + ("contributors : ").length());
			int endIndex = rawString.indexOf(")");
			String contributorsString = solrQuery.substring(beginIndex, endIndex);
			
			if(contributorsString.contains("OR")){
				String[] splittedContributors = contributorsString.split(" OR ");
				for(int i = 0 ; i<splittedContributors.length ; i++){
					contributors.add(splittedContributors[i]);
				}
			}
			else{
				contributors.add(contributorsString);
			}
			
			if(!contributors.isEmpty()){
				List<Source> sources = new ArrayList<Source>();
				
				for(String contributor : contributors)
					sources.add(new Source(contributor, 0.0f)); 	
				
				inputDataPerType.put(FeedType.SOURCE, sources);
			}
		}*/
		
		
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
	
	public class DateUtil
	{
	    public Date addDays(Date date, int days)
	    {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        cal.add(Calendar.DATE, days); //minus number decrements the days
	        return cal.getTime();
	    }
	}
}
