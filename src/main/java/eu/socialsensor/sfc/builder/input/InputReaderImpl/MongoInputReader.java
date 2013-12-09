package eu.socialsensor.sfc.builder.input.InputReaderImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.socialsensor.framework.client.dao.SourceDAO;
import eu.socialsensor.framework.client.dao.impl.SourceDAOImpl;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.sfc.builder.StorageInputConfiguration;
import eu.socialsensor.sfc.builder.input.InputReader;

public class MongoInputReader implements InputReader{
	
	protected static final String HOST = "host";
	protected static final String DB = "database";
	protected static final String SOURCES_COLLECTION = "collection";
	
	
	private StorageInputConfiguration storage_config;
	
	private Set<String> streams = new HashSet<String>();
	
	private String host = null;
	private String db = null;
	private String newsHoundsCollection = null;
	
	private SocialNetworkSource streamType = null;
	
	private Date sinceDate = null;
	
	private Map<String,List<Feed>> feeds = new HashMap<String,List<Feed>>();
	private Map<String, Set<String>> usersToLists = new HashMap<String, Set<String>>();
	
	public MongoInputReader(StorageInputConfiguration config){
		
		this.storage_config = config;
		
		streams.add("Twitter");
		//streams.add("Facebook");
		//streams.add("Tumblr");
		//streams.add("Instagram");
		//streams.add("GooglePlus");
		//streams.add("Youtube");
		//streams.add("Flickr");
	}
	
	@Override
	public Map<String,List<Feed>> createFeedsPerStream(){
	
		for(String stream : streams){
			List<Feed> feedsPerStream = new ArrayList<Feed>();
			if(stream.equals("Twitter"))
				this.streamType = SocialNetworkSource.Twitter;
			else if(stream.equals("Facebook"))
				this.streamType = SocialNetworkSource.Facebook;
			else if(stream.equals("Flickr"))
				this.streamType = SocialNetworkSource.Flickr;
			else if(stream.equals("GooglePlus"))
				this.streamType = SocialNetworkSource.GooglePlus;
			else if(stream.equals("Instagram"))
				this.streamType = SocialNetworkSource.Instagram;
			else if(stream.equals("Tumblr"))
				this.streamType = SocialNetworkSource.Tumblr;
			else if(stream.equals("Youtube"))
				this.streamType = SocialNetworkSource.Youtube;
			
			Map<FeedType,Object> inputData = getData();
			
			for(FeedType feedType : inputData.keySet()){
				switch(feedType){
				case SOURCE :
					@SuppressWarnings("unchecked")
					List<Source> sources = (List<Source>) inputData.get(feedType);
					for(Source source : sources){
						String feedID = UUID.randomUUID().toString();
						SourceFeed sourceFeed = new SourceFeed(source,sinceDate,feedID,null);
						feedsPerStream.add(sourceFeed);
					}
					break;
				case KEYWORDS : 
					@SuppressWarnings("unchecked")
					List<Keyword> keywords = (List<Keyword>) inputData.get(feedType);
					for(Keyword keyword : keywords){
						String feedID = UUID.randomUUID().toString();
						KeywordsFeed keywordsFeed = new KeywordsFeed(keyword,sinceDate,feedID,null);
						feedsPerStream.add(keywordsFeed);
					}
					break;
				case LOCATION :
					@SuppressWarnings("unchecked")
					List<Location> locations = (List<Location>) inputData.get(feedType);
					for(Location location : locations){
						String feedID = UUID.randomUUID().toString();
						LocationFeed locationFeed = new LocationFeed(location,sinceDate,feedID);
						feedsPerStream.add(locationFeed);
					}
					break;
				}
			}
			feeds.put(stream, feedsPerStream);
		}
		
		return feeds;
	}
	
	@Override
	public List<Feed> createFeeds(){
		return null;
	}
	
	@Override
	public Map<FeedType,Object> getData(){
		Map<FeedType,Object> inputDataPerType = new HashMap<FeedType,Object>();
		
		this.host = storage_config.getParameter(MongoInputReader.HOST);
		this.db = storage_config.getParameter(MongoInputReader.DB);
		this.newsHoundsCollection = storage_config.getParameter(MongoInputReader.SOURCES_COLLECTION, "Sources");
		
		if(host == null || db == null || newsHoundsCollection == null){
			System.out.println("News hounds collection needs to be configured correctly");
			return null;
		}
		
		//sources
		List<Source> sources = new ArrayList<Source>();
		SourceDAO sourceDao = new SourceDAOImpl(host, db, newsHoundsCollection);
		sources.addAll(sourceDao.findTopSources(5000, streamType));
		
		//Assign users to newshound lists
		for(Source source : sources) {
			String user = streamType+"#"+source.getId();
			String list = source.getList();
			if(list != null) {
				Set<String> lists = usersToLists.get(user);
				if(lists == null) {
					lists = new HashSet<String>();
				}
				lists.add(list);
				usersToLists.put(user, lists);
			}
		}
		
		if(!sources.isEmpty())
			inputDataPerType.put(FeedType.SOURCE,sources);
		
		return inputDataPerType;
	}
	
	@Override
	public void run(){
		
	}
	
	@Override
	public Map<String,Set<String>> getUsersToLists(){
		return usersToLists;
	}
	
}
