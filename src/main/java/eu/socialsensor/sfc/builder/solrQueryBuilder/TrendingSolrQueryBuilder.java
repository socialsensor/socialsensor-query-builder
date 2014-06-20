package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.Stopwords;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;

/**
 * @brief The class that creates the solr query based on the 
 * the content of a trending DySco (keywords,entities,hashtags)
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TrendingSolrQueryBuilder {
	public static final int NUMBER_OF_QUERIES = 5;
	public static final int MIN_KEYWORD_LENGTH = 3;
	
	public final Logger logger = Logger.getLogger(TrendingSolrQueryBuilder.class);
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Keyword> keywords = new ArrayList<Keyword>();
	private List<Keyword> hashtags = new ArrayList<Keyword>();
	
	private List<Keyword> mikeywords = new ArrayList<Keyword>();
	private List<Entity> mientities = new ArrayList<Entity>();
	
	private Dysco dysco = null;
	
	Stopwords stopwords = new Stopwords();
	
	public TrendingSolrQueryBuilder(Dysco dysco){
		this.dysco = dysco;
		
		addfilteredDyscoContent();
		eliminateRepeatedKeywords();
	}
	
	/**
	 * Formulates one solr query connected with AND's and OR's
	 * ready to be used directly for retrieval from solr.
	 * @return
	 */
	public String createSolrQuery(){
		String solrQuery = "";
		String query = "";
		
		if(mikeywords.isEmpty() && mientities.isEmpty() && hashtags.isEmpty())
			return solrQuery;
		
		boolean first = true;

		if(!mientities.isEmpty()){
			for(Entity entity : mientities){
				for(Keyword key : mikeywords){
					if(first){
						query += "(\""+entity.getName()+"\" AND "+ key.getName()+")";
						first = false;
					}
					else
						query += " OR (\"" + entity.getName()+"\" AND "+ key.getName()+")";
				}
				
				if(mikeywords.isEmpty()){
					if(first){
						query += "\""+entity.getName()+"\"";
						first = false;
					}	
					else
						query += " OR \"" + entity.getName()+"\"";
				}
				
				
			}
		}
		
		if(!hashtags.isEmpty()){
			for(Keyword hashtag : hashtags){
				for(Keyword key : mikeywords){
					if(first){
						query += "("+hashtag.getName()+" AND "+ key.getName()+")";
						first = false;
					}
					else
						query += " OR (" + hashtag.getName()+" AND "+ key.getName()+")";
				}
				
				if(mikeywords.isEmpty()){
					if(first){
						query += hashtag.getName();
						first = false;
					}	
					else
						query += " OR " + hashtag.getName()+"";
				}
				
				
			}
		}
	
		if(mientities.isEmpty() && hashtags.isEmpty()){
			for(Keyword key : mikeywords){
				if(first){
					query += key.getName();
					first = false;
				}
				else
					query += " OR " + key.getName()+"";
			}
		}
		//Final formulation of solr query
		
		if(!query.equals("")){
			solrQuery += "(title : "+query+") OR (description:"+query+") OR (tags:"+query+")";
		}
		
		return solrQuery;
	}
	
	/**
	 * Formulates primal solr queries out of DySco content (keywords/entities/hashtags).
	 * The formulated queries are the product of the combination of keywords and entities, whereas
	 * hashtags are used independently. Queries are ranked by their calculated scores, which are produced 
	 * by processing the frequency scores of keywords,entities and hashtags in the DySco. The formulated queries
	 * need to be aggregated to be used for solr retrieval.
	 * @return the list of queries
	 */
	public List<Query> createPrimalSolrQueries(){
		Map<Double, List<Query>> rankedQueries = new TreeMap<Double,List<Query>>(Collections.reverseOrder());
		
		List<Query> solrQueries = new ArrayList<Query>();
		
		//create queries from hashtags
		for(Keyword hash : hashtags){
			Query query = new Query();
			query.setName(hash.getName());
			query.setScore(hash.getScore());
			query.setType(Query.Type.Keywords);

			if(rankedQueries.get(hash.getScore()) == null){
				List<Query> alreadyIn = new ArrayList<Query>();
				alreadyIn.add(query);
				
				rankedQueries.put(hash.getScore(), alreadyIn);
			}else{
				List<Query> alreadyIn = rankedQueries.get(hash.getScore());
				alreadyIn.add(query);
				
				rankedQueries.put(hash.getScore(), alreadyIn);
			}
			
		}
		
		//create queries from entities - keywords combination
		for(Entity ent : entities){
			for(Keyword key : keywords){	
				Query query = new Query();
			
				String resQuery = getRightEntityKeywordCombination(ent.getName(),key.getName());
		
				query.setName(resQuery);
				double aggScore = ent.getCont()+key.getScore();
				query.setScore(aggScore);
				query.setType(Query.Type.Keywords);

				if(rankedQueries.get(aggScore) == null){
					List<Query> alreadyIn = new ArrayList<Query>();
					alreadyIn.add(query);
					
					rankedQueries.put(aggScore, alreadyIn);
				}else{
					List<Query> alreadyIn = rankedQueries.get(aggScore);
					alreadyIn.add(query);
					
					rankedQueries.put(aggScore, alreadyIn);
				}
				
			}

			Query query = new Query();
	
			query.setName("\""+ent.getName()+"\"");
			
			query.setScore(ent.getCont());
			query.setType(Query.Type.Keywords);

			if(rankedQueries.get(ent.getCont()) == null){
				List<Query> alreadyIn = new ArrayList<Query>();
				alreadyIn.add(query);
				
				rankedQueries.put(ent.getCont(), alreadyIn);
			}else{
				List<Query> alreadyIn = rankedQueries.get(ent.getCont());
				alreadyIn.add(query);
				
				rankedQueries.put(ent.getCont(), alreadyIn);
			}
		}
		
		
		if(entities.isEmpty()){
		
			for(Keyword key : keywords){
				if(key.getName().split("//s+").length>= TrendingSolrQueryBuilder.MIN_KEYWORD_LENGTH){
					Query query = new Query();
					query.setName(key.getName());
					query.setScore(key.getScore());
					query.setType(Query.Type.Keywords);

					if(rankedQueries.get(key.getScore()) == null){
						List<Query> alreadyIn = new ArrayList<Query>();
						alreadyIn.add(query);
						
						rankedQueries.put(key.getScore(), alreadyIn);
					}else{
						List<Query> alreadyIn = rankedQueries.get(key.getScore());
						alreadyIn.add(query);
						
						rankedQueries.put(key.getScore(), alreadyIn);
					}
					
				}
				
			}
		}
		
		for(Map.Entry<Double, List<Query>> entry : rankedQueries.entrySet()){
			
			for(Query q : entry.getValue()){
				if(solrQueries.size() == TrendingSolrQueryBuilder.NUMBER_OF_QUERIES)
					break;
				
				solrQueries.add(q);
			}
			
		}
		
		return solrQueries;
	}
	
	/**
	 * Combines an entity string with a keywords string, detecting an overlap between the two
	 * if exists.
	 * @param ent
	 * @param keywords
	 * @return the combination of the entity and the keyword as string
	 */
	private String getRightEntityKeywordCombination(String ent, String keywords){
		String combination = "";
		
		List<String> splittedKeywords = new ArrayList<String>();
		
		ent = ent.toLowerCase();
		keywords = keywords.toLowerCase();
		
		for(String key : keywords.split(" "))
			splittedKeywords.add(key);
		//System.out.println("Entity: "+ent+" Keyword: "+keywords);
		String[] entWords = ent.split(" "); 
			
		List<String> wordsFound = new ArrayList<String>();
		for(String eWord : entWords){
			if(splittedKeywords.contains(eWord)){
				wordsFound.add(eWord);
			}
		}
		
		if(wordsFound.size()==entWords.length){
			String resQuery = keywords.replace(ent, "");
			if(resQuery.length() == 0)
				combination = "\""+ent +"\"";
			else
				combination = "\""+ent +"\""+resQuery;
		}
		else if(wordsFound.isEmpty()){
			combination = "\""+ent +"\" " + keywords;
		}
		else{

			int lastIndex = 0;
			for(int i=0;i<entWords.length;i++){
				if(wordsFound.contains(entWords[i])){
					lastIndex = keywords.indexOf(entWords[i])+entWords[i].length() + 1;
				}
				else{
					if(lastIndex == 0 || lastIndex > keywords.length()){
						keywords = "\""+entWords[i]+"\" "+keywords;
					}
					else{
						String part1 = keywords.substring(0,lastIndex);
						String part2 = keywords.substring(lastIndex+1);
						part1 +="\""+entWords[i]+"\" ";
						keywords = part1 + part2;
					}
					
				}
			}
			combination = keywords;
		}
		
		return combination;
	}
	
	/**
	 * Filters DySco's content from stopwords, urls, emails and
	 * other unnecessary features.
	 */
	private void addfilteredDyscoContent(){
		
		List<Entity> filteredEntities = new ArrayList<Entity>();
		
		//Filter entities
		if(dysco.getEntities() != null){
			filteredEntities.addAll(dysco.getEntities());
			for(Entity entity : dysco.getEntities()){
				
				int r_entity = -1;
				for(Entity f_entity : filteredEntities){
					if(f_entity.getName().contains(entity.getName()) && !f_entity.getName().equals(entity.getName())){
						r_entity = filteredEntities.indexOf(entity);
						break;
					}
					else if(entity.getName().contains(f_entity.getName()) && !f_entity.getName().equals(entity.getName())){
						r_entity = filteredEntities.indexOf(f_entity);;
						break;
					}
						
				}
				
				if(r_entity != -1){
					filteredEntities.remove(r_entity);
				}
				
				int index = filteredEntities.indexOf(entity);
				if(index != -1){
					if(entity.getName().contains("#") 
							|| Stopwords.isStopword(entity.getName().toLowerCase())
							|| entity.getName().split(" ").length > 3){
						filteredEntities.remove(entity);
						continue;
					}
					if(entity.getName().contains("http")){
						String newEntity = entity.getName().substring(0,entity.getName().indexOf("http")-1);
						filteredEntities.get(index).setName(newEntity);
					}
					if(entity.getName().contains("@")){
						String newEntity = entity.getName().replace("@", "");
						filteredEntities.get(index).setName(newEntity);
					}
					
						
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().toLowerCase());
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("'s", ""));
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("[^A-Za-z0-9 ]", ""));
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("\\s+", " "));
	       		 	
				}
			}
			
			entities.addAll(filteredEntities);
		}
			
		//Filter keywords
		if(dysco.getKeywords() != null){
			Map<String,Double> keywordsToFilter = new HashMap<String,Double>();
			
			keywordsToFilter.putAll(dysco.getKeywords());
			
		
			for(String key : dysco.getKeywords().keySet()){
			
				if(key.contains("@")||key.contains("#") 
						|| stopwords.is(key) || key.equals("http")
						|| key.split(" ").length > 3){
					keywordsToFilter.remove(key);
					continue;
				}
				
				if(key.contains("http")){
					String newKey = key.replaceAll("http","");
					keywordsToFilter.put(newKey, dysco.getKeywords().get(key));
					keywordsToFilter.remove(key);
				}
				
				String keyToFilter = key;
				
				keyToFilter = keyToFilter.toLowerCase();
				
				keyToFilter = keyToFilter.replaceAll("'s", "");
				keyToFilter = keyToFilter.replaceAll("[^A-Za-z0-9 ]", "");
				keyToFilter = keyToFilter.replaceAll("\\s+", " ");
			
				//Create the keyword to use
				Keyword keyword = new Keyword(keyToFilter,dysco.getKeywords().get(key).floatValue());
				keywords.add(keyword);
			}
			
		}
		
		if(dysco.getHashtags() != null){
			for(String hashtag : dysco.getHashtags().keySet()){
				//Create the keyword to use
				Keyword keyword = new Keyword(hashtag.replace("#", ""),dysco.getHashtags().get(hashtag).floatValue());
				hashtags.add(keyword);
			}
		}
			
	}
	/**
	 * Eliminates duplicate keywords that may exist both in hashtag or entity list 
	 * and the keywords list 
	 */
	private void eliminateRepeatedKeywords(){
		List<Keyword> keywordsToEliminate = new ArrayList<Keyword>();
		for(Keyword key : keywords){
			for(Entity ent : entities){
				if(ent.getName().equals(key.getName())){
					keywordsToEliminate.add(key);
					ent.setCont(ent.getCont()+key.getScore());
				}
			}
			for(Keyword hash : hashtags){
				if(hash.getName().equals(key.getName())){
					keywordsToEliminate.add(key);
					hash.setScore(hash.getScore()+key.getScore());
				}
			}
		}
		
		for(Keyword key : keywordsToEliminate)
			keywords.remove(key);
	}
	

	public static void main(String[] args) {
	
	}
}
