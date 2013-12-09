package eu.socialsensor.sfc.builder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.sfc.builder.input.InputReader;
import eu.socialsensor.sfc.builder.input.DataInputType;
import eu.socialsensor.sfc.builder.input.InputReaderImpl.ConfigInputReader;
import eu.socialsensor.sfc.builder.input.InputReaderImpl.DyscoInputReader;
import eu.socialsensor.sfc.builder.input.InputReaderImpl.MongoInputReader;

public class QueryBuilder {
	
	private InputReader reader = null;
	
	public <T> QueryBuilder(DataInputType dataInputType, T inputData){
		switch(dataInputType){
			
			case CONFIGURATION:
				InputConfiguration config = (InputConfiguration) inputData;
				
				if(config == null){
					System.out.println("Input Configuration is not set");
					return;
				}
				
				Set<String> storageInputs = config.getStorageInputIds();
				Set<String> streamInputs = config.getStreamInputIds();
				
				if(!storageInputs.isEmpty()){
					for(String storageId : storageInputs){
						if(storageId.equals("Mongodb")){
							StorageInputConfiguration m_conf = config.getStorageInputConfig("Mongodb");
							if(m_conf != null){
								reader = new MongoInputReader(m_conf);
							}
						}
					}
				}
				
				if(!streamInputs.isEmpty()){
					reader = new ConfigInputReader(config);
				}
				
				break;
				
			case DYSCO:
				Dysco dysco = (Dysco) inputData;
				reader = new DyscoInputReader(dysco);
				break;
			
		}
	}
	
	
	public Map<String,List<Feed>> getQueryPerStream(){
		
		return reader.createFeedsPerStream();
	}
	
	public List<Feed> getQuery(){
		
		return reader.createFeeds();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			
			File configFile;
			
			if(args.length != 1 ) {
				configFile = new File("./conf/input.conf.xml");
				
			}
			else {
				configFile = new File(args[0]);
			
			}
			
			InputConfiguration config = InputConfiguration.readFromFile(configFile);		
			long t1 = System.currentTimeMillis();
	        QueryBuilder q_builder = new QueryBuilder(DataInputType.CONFIGURATION,config);
	       
	        Map<String,List<Feed>> results = q_builder.getQueryPerStream();
	        long t2 = System.currentTimeMillis();
	        System.out.println("Running time of Query Builder : "+(t2-t1) +" miliseconds");
			for(String key : results.keySet()){
				List<Feed> feeds = results.get(key);
				System.out.println("Number of Feeds for "+key+" : "+feeds.size());
				for(Feed feed : feeds){
					if(feed.getFeedtype().equals(FeedType.SOURCE)){
						SourceFeed sFeed = (SourceFeed) feed;
						System.out.println(sFeed.toJSONString());
					}
					else if (feed.getFeedtype().equals(FeedType.KEYWORDS)){
						KeywordsFeed kFeed = (KeywordsFeed) feed;
						System.out.println(kFeed.toJSONString());
					}
				}
			}
		
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

}
