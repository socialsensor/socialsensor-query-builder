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

		List<Query> solrQueries = dysco.getPrimalSolrQueries();
		System.out.println("SolrQueries size : "+solrQueries.size());
		for(Query q : solrQueries){
			System.out.println("Q :"+q.getName());
		}
		Set<Keyword> queryKeywords = new HashSet<Keyword>();
		
		this.date = dateUtil.addDays(dysco.getCreationDate(),-2);
		
		if(solrQueries != null){
			for(Query solrQuery : solrQueries){
				String queryName = solrQuery.getName();
				Keyword key = null;
				if(queryName.contains(" AND ")){
					key = splitQueryWithAND(queryName);
					//key.setScore(solrQuery.getScore());
				}
				else{
					key = new Keyword(queryName,0.0);
				}
				queryKeywords.add(key);
			}
		}
		inputDataPerType.put(FeedType.KEYWORDS, queryKeywords);
		return inputDataPerType;
		
	}
	
	/*@Override
	public Map<FeedType,Object> getData(){
		Map<FeedType,Object> inputDataPerType = new HashMap<FeedType,Object>();
		
		this.solrQuery = dysco.getSolrQuery();
		
		this.date = dateUtil.addDays(dysco.getCreationDate(),-2);
		
		if(solrQuery.equals(""))
			return inputDataPerType;
		else{
			System.out.println("Solr Query : "+solrQuery);
			//solrQuery = solrQuery.substring(1);
			//solrQuery = solrQuery.substring(0,solrQuery.length()-1);
			
			//System.out.println("Solr Query after removing parenthesis : "+solrQuery);
		}
		//Dysco feeds are created based on the notion that the solr query has the following form : 
		// query : (title : (Entity AND keyword) OR (Hashtag AND keyword)) or query : (title :(Entity OR Hashtag))
		//followed by the same query for description and tags // in the case of custom dysco there might be author as well
		
		String embededQuery = "";
		if(solrQuery.contains("title")){ 
			int start = solrQuery.indexOf("(title");
			int end = solrQuery.indexOf("(description");
			
			embededQuery = solrQuery.substring(start,end - (" OR ").length());
		}
		
		if(embededQuery.length() > 0){
			String queryToProcess = embededQuery;
			
			queryToProcess = queryToProcess.substring(1);
			queryToProcess = queryToProcess.substring(0,embededQuery.length()-1);
			
			System.out.println("Solr Query after removing parenthesis : "+queryToProcess);
			
			queryToProcess = queryToProcess.replace("title : ", "");
			
			System.out.println("Pure Solr Query : "+queryToProcess);
			
			String[] queries = queryToProcess.split(" OR ");
			
			if(queries.length == 0){
				 if(queryToProcess.contains(" AND ")){
                     splitANDQuery(solrQuery);
				 }else{
                     if(queryToProcess.contains("\"")){
                    	 queryToProcess = queryToProcess.substring(1);
                    	 queryToProcess = queryToProcess.substring(0,queryToProcess.length()-1);
                     }
                     
                     if(!keywordExists(queryToProcess))
                    	 dyscoKeywords.add(new Keyword(queryToProcess, 0.0f));
				 }
			}
			else{
				for(int i=0;i<queries.length;i++){
					if(queries[i].contains(" AND ")){
						splitANDQuery(queries[i]);
					}
					else if(queries[i].contains(" OR ")){
						splitORQuery(queries[i]);
					}
					else{
						if(queries[i].contains("\"")){
							queries[i] = queries[i].substring(1);
							queries[i] = queries[i].substring(0,queries[i].length()-1);
						}
						if(!keywordExists(queries[i]))
							dyscoKeywords.add(new Keyword(queries[i], 0.0f));
					}
				}
				
			}
			
		}
		
	
		if(solrQuery.contains("author")){
			int beginIndex = solrQuery.indexOf("author");
			String rawString = solrQuery.substring(beginIndex + ("author : ").length());
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
		}
		
		if(!dyscoKeywords.isEmpty()){
			inputDataPerType.put(FeedType.KEYWORDS, dyscoKeywords);
		}
		
		return inputDataPerType;
	}
	*/
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
	
	//accepts small queries in the form of  "Word1 AND Word2"
	private void splitANDQuery(String query){
		String [] words = query.split(" AND ");
		
		for(int i=0;i<words.length;i++){
			if(words[i].contains("("))
				words[i] = words[i].substring(1);
			if(words[i].contains(")"))
				words[i] = words[i].substring(0,words[i].length()-1);	
		}
		
 		//the word on the left must always be an entity or a hashtag
		if(words[0].contains("\"")){
			words[0] = words[0].substring(1);
			words[0] = words[0].substring(0,words[0].length()-1);
		}
		
		System.out.println("entity/hashtag : "+words[0]);
		System.out.println("keyword : "+words[1]);
		
		if(!keywordExists(words[0]))
			dyscoKeywords.add(new Keyword(words[0], 0.0f));
		if(!keywordExists(words[1]))
			dyscoKeywords.add(new Keyword(words[0]+" "+words[1], 0.0f));
	}
	
	private Keyword splitQueryWithAND(String query){
		String [] words = query.split(" AND ");
		
		for(int i=0;i<words.length;i++){
			if(words[i].contains("("))
				words[i] = words[i].substring(1);
			if(words[i].contains(")"))
				words[i] = words[i].substring(0,words[i].length()-1);	
			if(words[i].contains("\"")){
				words[i] = words[i].substring(1);
				words[i] = words[i].substring(0,words[i].length()-1);
			}
			
		}
		String resultKeyword = "";
		boolean first = true;
		for(int i=0;i<words.length;i++){
			if(first){
				resultKeyword += words[i];
				first = false;
			}
			else{
				resultKeyword += " "+words[i];
			}
			
		}
		
		return new Keyword(resultKeyword, 0.0);
	}
	
	
	//accepts small queries in the form of  "Word1 OR Word2"
	private void splitORQuery(String query){
		query = query.substring(1);
		query = query.substring(0,query.length()-1);
		
		System.out.println("Query after removing parenthesis : "+query);
		
		String [] words = query.split(" OR ");
		
 		//the word on the left must always be an entity or a hashtag
		System.out.println("left part : "+words[0]);
		System.out.println("right part : "+words[1]);
		
		if(words[0].contains("(")){
			splitORQuery(words[0]);
		}
		if(words[1].contains("(")){
			splitORQuery(words[1]);
		}
		if(!words[0].contains("(") && !words[1].contains("(")){
			if(words[0].contains("\"")){
				words[0] = words[0].substring(1);
				words[0] = words[0].substring(0,words[0].length()-1);
			}
			if(!keywordExists(words[0]))
				dyscoKeywords.add(new Keyword(words[0], 0.0f));
		}
		if(!words[1].contains("(") && !words[1].contains("(")){
			if(words[0].contains("\"")){
				words[0] = words[0].substring(1);
				words[0] = words[0].substring(0,words[0].length()-1);
			}
			if(!keywordExists(words[1]))
				dyscoKeywords.add(new Keyword(words[1], 0.0f));
		}

	}
	
	private boolean keywordExists(String keyword){
		for(Keyword dKey : dyscoKeywords){
			if(dKey.getName().equals(keyword))
				return true;
		}
		
		return false;
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
