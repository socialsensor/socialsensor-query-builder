package eu.socialsensor.sfc.builder.input.InputReaderImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.NewsFeedSource;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.StreamUser.Category;
import eu.socialsensor.framework.common.domain.feeds.URLFeed;
import eu.socialsensor.sfc.builder.InputConfiguration;
import eu.socialsensor.sfc.builder.StreamInputConfiguration;
import eu.socialsensor.sfc.builder.input.InputReader;

public class FileInputReader implements InputReader{
	protected static final String DATE = "date";
	protected static final String PATH = "path";
	
	private InputConfiguration config;
	
	private Set<String> streams = null;
	
	private NewsFeedSource streamType = null;
	
	private StreamInputConfiguration stream_config = null;
	
	private Map<String,List<Feed>> feeds = new HashMap<String,List<Feed>>();
	
	public FileInputReader(InputConfiguration config){
		this.config = config;
		streams = config.getStreamInputIds();
	}
	
	public Map<FeedType,Object> getData(){
		Map<FeedType,Object> inputDataPerType = new HashMap<FeedType,Object>();
		List<String> urls = new ArrayList<String>();
		String path = stream_config.getParameter(FileInputReader.PATH);
		
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			line = reader.readLine();
			while (line != null) {
				
	        	urls.add(line);
	            line = reader.readLine();
	        }
			
			reader.close();
		} catch (IOException e) {
	
		}
		
		inputDataPerType.put(FeedType.URL, urls);
		
		return inputDataPerType;
	}
	
	
	public Map<String,List<Feed>> createFeedsPerStream(){
		for(String stream : streams){
			List<Feed> feedsPerStream = new ArrayList<Feed>();
			
			if(stream.equals("RSS"))
				this.streamType = NewsFeedSource.RSS;
			
			this.stream_config = config.getStreamInputConfig(streamType.toString());
			
			String value;
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			String since = stream_config.getParameter(FileInputReader.DATE);
			Date dateToRetrieve = null;
			if(since != null){
				try {
					dateToRetrieve = (Date) formatter.parse(since);
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			
			if(dateToRetrieve == null){
				System.err.println("No Date to retrieve from");
				return feeds;
			}
			
			Map<FeedType,Object> inputData = getData();
			System.out.println("inputData : "+inputData.keySet().size());
			for(FeedType feedType : inputData.keySet()){
				switch(feedType){
					case URL :
						
						@SuppressWarnings("unchecked")
						List<String> urls = (List<String>) inputData.get(feedType);
						
						for(String url : urls){
					
							String feedId = UUID.randomUUID().toString();
							URLFeed feed = new URLFeed(url,dateToRetrieve,feedId);
							feedsPerStream.add(feed);
						}
						break;
				}

			}
			feeds.put(stream, feedsPerStream);
		}
		
		return feeds;
	}
	
	public List<Feed> createFeeds(){
		return null;
	}
	
	public Map<String,Set<String>> getUsersToLists(){
		return null;
	}
	
	public Map<String,Category> getUsersToCategories(){
		return null;
	}
}