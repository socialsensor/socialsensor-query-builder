package eu.socialsensor.sfc.builder.evaluation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import eu.socialsensor.framework.client.dao.impl.DyscoDAOImpl;
import eu.socialsensor.framework.client.search.SearchEngineResponse;
import eu.socialsensor.framework.client.search.solr.SolrHandler;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;

public class SolrQueryBuilderEvaluator {

	public static void main(String...args) throws Exception {
		
		String solrItemsCollection = "http://xxx.xxx.xxx.xxx/solr/items";
		String solrMediaItemsCollection = "http://xxx.xxx.xxx.xxx/solr/MediaItems";
		String solrWebPagesCollection = "http://xxx.xxx.xxx.xxx/solr/WebPages";
		String solrDyscosCollection = "http://xxx.xxx.xxx.xxx/solr/dyscos";
		
		String mongoHost = "xxx.xxx.xxx.xxx";
		
		String visualIndexHost = "http://xxx.xxx.xxx.xxx:8080/VisualIndexService";
		
		SolrHandler handler = new SolrHandler(solrDyscosCollection, solrItemsCollection);
		
		DyscoDAOImpl dyscoDao = new DyscoDAOImpl(mongoHost, 
					"WebPagesDB", "WebPages", "MediaItemsDB", "MediaItems",
	                solrDyscosCollection, solrItemsCollection,
	                solrMediaItemsCollection,
	                solrWebPagesCollection,
	                visualIndexHost, "Prototype");

		List<String> filters = new ArrayList<String>();
		List<String> facets = new ArrayList<String>();
		String orderBy = "publicationTime";
		long now = System.currentTimeMillis();
		long window = 4 * 60L * 60L * 1000L;
		Map<String, String> params = new HashMap<String, String>();
		filters.add("publicationTime:[" + (now - window) + " TO " + now + " ]");
		
		String list = "3";
		
		SearchEngineResponse<Dysco> response = handler.findDyscosLight("*:* AND dyscoType:TRENDING AND NOT(new:old)", "2HOURS", list, 30, 0.2);
		
		FileWriter writer = new FileWriter("/home/manosetro/QueriesEvaluation_v2/" + list + ".txt");
		for(Dysco dysco : response.getResults()) {
			writer.write("====================================================================================\n");
			
			writer.write(" - Dysco ID: " + dysco.getId()+"\n");
			writer.write(" - Dysco Title: " + dysco.getTitle()+"\n");
			
			SearchEngineResponse<Item> itemsResponse = dyscoDao.findItems(dysco, filters, facets, orderBy, params, 20);
			SearchEngineResponse<MediaItem> mediaResponse = dyscoDao.findImages(dysco, filters, facets, orderBy, 20);
			List<Item> items = itemsResponse.getResults();
			List<MediaItem> mediaItems = mediaResponse.getResults();
			writer.write(" - Dysco Items (" + items.size() + "): \n");
			for(Item item : items) {
				writer.write("\t +++++++++++++++++++++++++++++++++++++++++++++\n");
				writer.write("\t   Item Title: " + item.getTitle().replaceAll("\n", "") + "\n");
				writer.write("\t   Item Author: " + item.getAuthorScreenName() + "\n");
			}
			
			writer.write("\n - Dysco Media Items (" + items.size() + "): \n");
			for(MediaItem mItem : mediaItems) {
				writer.write("\t +++++++++++++++++++++++++++++++++++++++++++++\n");
				writer.write("\t   Media Item Title: " + mItem.getTitle().replaceAll("\n", "") + "\n");
				writer.write("\t   Tags: " + 
						(mItem.getTags()==null?"none":StringUtils.join(mItem.getTags(), ", ")) + "\n");
			}
			
			writer.write("\n - Dysco Entities: \n");
			for(Entity entity : dysco.getEntities()) {
				writer.write("\t " + entity.getType() + ": " + entity.getName()+"\n");
			}
			
			writer.write("\n - Dysco Hashtags: \n");
			for(Entry<String, Double> hashtag : dysco.getHashtags().entrySet()) {
				writer.write("\t " + hashtag.getKey() + ": " + hashtag.getValue()+"\n");
			}
			
			writer.write("\n - Dysco Keywords: \n");
			for(Entry<String, Double> keyword : dysco.getKeywords().entrySet()) {
				writer.write("\t " + keyword.getKey() + ": " + keyword.getValue()+"\n");
			}
			
			dyscoDao.postProcess(dysco);
			writer.write("\n - Dysco Queries: \n");
			for(Query query : dysco.getSolrQueries()) {
				writer.write("\t " + query.getName() + ": " + query.getScore()+"\n");
			}
			
			String solrQuery = dyscoDao.buildKeywordSolrQuery(dysco.getSolrQueries(), "OR");
			writer.write("\n - Solr Queries: \n");
			writer.write("\t "+solrQuery + "\n");
			
			writer.write("\n");
		}
		writer.close();
	       
	        
	}
}
