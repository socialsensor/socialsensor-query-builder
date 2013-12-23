package eu.socialsensor.sfc.builder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.StreamUser.Category;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.sfc.builder.input.InputReader;
import eu.socialsensor.sfc.builder.input.DataInputType;
import eu.socialsensor.sfc.builder.input.InputReaderImpl.ConfigInputReader;
import eu.socialsensor.sfc.builder.input.InputReaderImpl.DyscoInputReader;
import eu.socialsensor.sfc.builder.input.InputReaderImpl.MongoInputReader;

/**
 * @brief  The class responsible for the creation of input feeds
 * that can result either from a configuration file input, 
 * a storage input(currently only mongo db is supported) and a dysco input. 
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FeedsCreator {
	
	private InputReader reader = null;
	private InputConfiguration config = null;
	private Dysco dysco = null;
	
	public <T> FeedsCreator(DataInputType dataInputType, T inputData){
		
		switch(dataInputType){
			
			case CONFIG_FILE:
				this.config = (InputConfiguration) inputData;
				
				if(this.config == null){
					System.out.println("Input Configuration is not set");
					return;
				}
				
				
				Set<String> streamInputs = config.getStreamInputIds();
				
				if(!streamInputs.isEmpty()){
					reader = new ConfigInputReader(config);
				}
				else{
					System.err.println("Streams need to be configured");
					return;
				}
				
				break;
			case MONGO_STORAGE:
				this.config = (InputConfiguration) inputData;
				
				if(this.config == null){
					System.out.println("Input Configuration is not set");
					return;
				}
				
				Set<String> storageInputs = config.getStorageInputIds();
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
				else{
					System.err.println("Storage needs to be configured");
					return;
				}
				
				break;
				
			case DYSCO:
				
				this.dysco = (Dysco) inputData;
				reader = new DyscoInputReader(this.dysco);
				break;
			
		}
	}
	
	/**
	 * Returns the input feeds created for every stream seperately
	 * @return a map of the input feeds to each stream
	 */
	public Map<String,List<Feed>> getQueryPerStream(){
		if(reader == null)
			return null;
		
		return reader.createFeedsPerStream();
	}
	
	/**
	 * Returns the unput feeds created for all streams together
	 * @return the input feeds
	 */
	public List<Feed> getQuery(){
		if(reader == null)
			return null;
		
		return reader.createFeeds();
	}
	
	/**
	 * Returns the mapping of the users to follow to 
	 * newhounds lists
	 * @return the map of the user to the newshounds lists that 
	 * he is included
	 */
	public Map<String,Set<String>> getUsersToLists(){
		return reader.getUsersToLists();
	}
	/**
	 * Returns the mapping of the users to follow to categories
	 * @return the map of the user to the category that he corresponds to
	 */
	public Map<String,Category> getUsersToCategories(){
		return reader.getUsersToCategories();
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
			
			SolrQueryBuilder solrQueryBuilder = new SolrQueryBuilder();
			final SolrDyscoHandler handler = SolrDyscoHandler.getInstance("http://social1.atc.gr:8080/solr/dyscos");
			Dysco dysco = handler.findDyscoLight("7073ee25-5ee1-472b-9b1d-a840c748be15");
			if(dysco == null)
				System.err.println("Dysco is NULL");

			dysco.setSolrQuery(solrQueryBuilder.getSolrQuery(dysco));
			System.out.println("Solr query : "+solrQueryBuilder.getSolrQuery(dysco));
			
			long t1 = System.currentTimeMillis();
			FeedsCreator feedsCreator = new FeedsCreator(DataInputType.DYSCO,dysco);
	       
	        List<Feed> results = feedsCreator.getQuery();
	        
	        if(results == null){
	        	System.err.println("No feeds for this query");
	        	return;
	        }
	        	
	        long t2 = System.currentTimeMillis();
	        System.out.println("Running time of Query Builder : "+(t2-t1) +" miliseconds");
			for(Feed feed : results){

				if(feed.getFeedtype().equals(FeedType.SOURCE)){
					SourceFeed sFeed = (SourceFeed) feed;
					System.out.println(sFeed.toJSONString());
				}
				else if (feed.getFeedtype().equals(FeedType.KEYWORDS)){
					KeywordsFeed kFeed = (KeywordsFeed) feed;
					System.out.println(kFeed.toJSONString());
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
