package eu.socialsensor.sfc.builder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InputConfiguration extends Configuration{
	private static final long serialVersionUID = 5269944272731808440L;

	@Expose
	@SerializedName(value = "storageinput")
	private Map<String, StorageInputConfiguration> storageInputMap = null;
	@Expose
	@SerializedName(value = "streaminput")
	private Map<String, StreamInputConfiguration> streamInputMap = null;
	
	public InputConfiguration() {
		storageInputMap = new HashMap<String, StorageInputConfiguration>();
		streamInputMap = new HashMap<String, StreamInputConfiguration>();
	}
	
	public void setStorageInputConfig(String storageId, StorageInputConfiguration config){
		storageInputMap.put(storageId,config);
	}
	
	public StorageInputConfiguration getStorageInputConfig(String storageId){
		return storageInputMap.get(storageId);
	}
	
	public void setStreamInputConfig(String streamId, StreamInputConfiguration config){
		streamInputMap.put(streamId,config);
	}
	
	public StreamInputConfiguration getStreamInputConfig(String streamId){
		return streamInputMap.get(streamId);
	}
	
	public void setParameter(String name, String value){
		super.setParameter(name,value);
	}
	
	public String getParameter(String name) {
		return super.getParameter(name);
	}
	
	public Set<String> getStorageInputIds() {
		return storageInputMap.keySet();
	}
	
	public Set<String> getStreamInputIds() {
		return streamInputMap.keySet();
	}
	
	public static InputConfiguration readFromFile(File file) 
			    throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		ParseHandler handler = new ParseHandler();
		parser.parse(file, handler);
		return handler.getConfig();
	}
	
	
	private static class ParseHandler extends DefaultHandler {

		private enum ParseState{
			IDLE,
			IN_CONFIG_PARAM,
			IN_CONFIG_INPUT_STORAGE,
			IN_CONFIG_INPUT_STORAGE_PARAM,
			IN_CONFIG_INPUT_STREAM,
			IN_CONFIG_INPUT_STREAM_PARAM,

		}
		
		private InputConfiguration config = new InputConfiguration();
	    private ParseState state = ParseState.IDLE;
	    private StringBuilder value = null;
	    private String name = null;
	    private StorageInputConfiguration storage_config = null;
	    private StreamInputConfiguration stream_config = null;
	    private String streamId = null, storageId = null;
		
		public InputConfiguration getConfig() {
			return config;
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			
			if (name.equalsIgnoreCase("Parameter")) {
				this.name = attributes.getValue("name");
				if (this.name == null) return;
				value = new StringBuilder();
				if (state == ParseState.IDLE) {
					state = ParseState.IN_CONFIG_PARAM;
				}else if(state == ParseState.IN_CONFIG_INPUT_STORAGE) {
					state = ParseState.IN_CONFIG_INPUT_STORAGE_PARAM;
				}else if(state == ParseState.IN_CONFIG_INPUT_STREAM) {
					state = ParseState.IN_CONFIG_INPUT_STREAM_PARAM;
				}
			
			}
			else if (name.equalsIgnoreCase("Storage")){
				storageId = attributes.getValue("id");
				value = new StringBuilder();
				if (storageId == null) return;
				storage_config = new StorageInputConfiguration();
				state = ParseState.IN_CONFIG_INPUT_STORAGE;
			}
			else if (name.equalsIgnoreCase("Stream")){
				streamId = attributes.getValue("id");
				value = new StringBuilder();
				if (streamId == null) return;
				stream_config = new StreamInputConfiguration();
				state = ParseState.IN_CONFIG_INPUT_STREAM;
			}
			
			
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (value != null){
				value.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String name)
				throws SAXException {

			if (name.equalsIgnoreCase("Parameter") && state == ParseState.IN_CONFIG_PARAM) {
				config.setParameter(this.name, value.toString().trim());
				state = ParseState.IDLE;
			}
			else if (name.equalsIgnoreCase("Parameter") && state == ParseState.IN_CONFIG_INPUT_STREAM_PARAM){
				stream_config.setParameter(this.name, value.toString().trim());
				state = ParseState.IN_CONFIG_INPUT_STREAM;
			}
			else if (name.equalsIgnoreCase("Parameter") && state == ParseState.IN_CONFIG_INPUT_STORAGE_PARAM){
				storage_config.setParameter(this.name, value.toString().trim());
				state = ParseState.IN_CONFIG_INPUT_STORAGE;
			}
			else if (name.equalsIgnoreCase("Stream")){
				config.setStreamInputConfig(streamId, stream_config);
				state = ParseState.IDLE;
			}
			else if (name.equalsIgnoreCase("Storage")){
				config.setStorageInputConfig(storageId, storage_config);
				state = ParseState.IDLE;
			}
		}
		
	}
}
