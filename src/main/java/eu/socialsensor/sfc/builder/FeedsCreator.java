package eu.socialsensor.sfc.builder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.StreamUser.Category;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
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
	}

}
