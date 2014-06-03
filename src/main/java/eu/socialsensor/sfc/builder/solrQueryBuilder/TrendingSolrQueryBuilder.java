package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.client.search.solr.SolrNewsFeedHandler;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.Stopwords;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;

/**
 * @brief The class that creates the solr query based on the 
 * information of a trending dysco (keywords,entities,hashtags)
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TrendingSolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(TrendingSolrQueryBuilder.class);
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Keyword> keywords = new ArrayList<Keyword>();
	private List<Keyword> hashtags = new ArrayList<Keyword>();
	
	private List<Keyword> mikeywords = new ArrayList<Keyword>();
	private List<Entity> mientities = new ArrayList<Entity>();
	
	private Map<String,Double> vocabulary = new HashMap<String,Double>();
	
	private Dysco dysco = null;
	
	Stopwords stopwords = new Stopwords();
	
	//at this point every information that remains in the dysco will be used for the creation of the solr query
	
	public TrendingSolrQueryBuilder(Dysco dysco){
		this.dysco = dysco;
		
		addfilteredDyscoContent();
		eliminateRepeatedKeywords();
	}
	
	
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
	
	public List<Query> createPrimalSolrQueries(){
		Map<Double, List<Query>> rankedQueries = new TreeMap<Double,List<Query>>(Collections.reverseOrder());
		
		List<Query> solrQueries = new ArrayList<Query>();
		
		//create queries from hashtags
		for(Keyword hash : hashtags){
			//System.out.println("hash: "+hash);
			Query query = new Query();
			query.setName(hash.getName());
			query.setScore(hash.getScore());
			query.setType(Query.Type.Keywords);
			logger.info("Hashtag : "+hash.getName()+" Score : "+hash.getScore());
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
			logger.info("Entity : "+ent.getName()+" Score : "+ent.getCont());
			for(Keyword key : keywords){
				//System.out.println("key: "+key.getName()+" has score : "+key.getScore());
				Query query = new Query();
				logger.info("Keyword : "+key.getName()+" Score : "+key.getScore());
				String resQuery = getRightEntityKeywordCombination(ent.getName(),key.getName());
				//System.out.println("Entity - Keyword combination : "+resQuery);
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
			logger.info("Entity : "+ent.getName()+" Score : "+ent.getCont());
			
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
			int minimumKeywordLenght = 3;
			
			for(Keyword key : keywords){
				if(key.getName().split(" ").length>= minimumKeywordLenght){
					Query query = new Query();
					logger.info("Keyword : "+key.getName()+" Score : "+key.getScore());
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
		
		int limit = 5;
		for(Map.Entry<Double, List<Query>> entry : rankedQueries.entrySet()){
			
			for(Query q : entry.getValue()){
				if(solrQueries.size() == limit)
					break;
				
				solrQueries.add(q);
			}
			
		}
		
		return solrQueries;
	}
	
	
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
			//System.out.println("Entity and Keyword are partly similar");
			int lastIndex = 0;
			for(int i=0;i<entWords.length;i++){
				if(wordsFound.contains(entWords[i])){
					lastIndex = keywords.indexOf(entWords[i])+entWords[i].length() + 1;
					//System.out.println("Last Index of existed word: "+entWords[i]+" is: "+lastIndex);
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
	 * Filters dysco's content 
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
						|| stopwords.is(key)
						|| key.split(" ").length > 3){
					keywordsToFilter.remove(key);
					continue;
				}
				if(key.contains("http")){
					String newKey = key.substring(0,key.indexOf("http"));
					keywordsToFilter.put(newKey, dysco.getKeywords().get(key));
					keywordsToFilter.remove(key);
				}
				
				String keyToFilter = key;
				
				keyToFilter = keyToFilter.toLowerCase();
				
				keyToFilter = keyToFilter.replaceAll("'s", "");
				keyToFilter = keyToFilter.replaceAll("[^A-Za-z0-9 ]", "");
				keyToFilter = keyToFilter.replaceAll("\\s+", " ");
				System.out.println("keyToFilter:"+keyToFilter);
				//Create the keyword to use
				Keyword keyword = new Keyword(keyToFilter,dysco.getKeywords().get(key).floatValue());
				keywords.add(keyword);
			}
			
		}
		
		if(dysco.getHashtags() != null){
			for(String hashtag : dysco.getHashtags().keySet()){
				//Create the keyword to use
				Keyword keyword = new Keyword(hashtag,dysco.getHashtags().get(hashtag).floatValue());
				hashtags.add(keyword);
			}
		}
			
	}
	
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
	
	private void extractDyscosVocabularyWithWeights(){
		
		for(Entity entity : entities){
			if(!vocabulary.containsKey(entity.getName())){
				vocabulary.put(entity.getName(), entity.getCont()+1);
			}
		}
		
		for(Keyword hashtag : hashtags){
			if(!vocabulary.containsKey(hashtag.getName())){
				vocabulary.put(hashtag.getName(), new Double(hashtag.getScore()+1));
			}
		}
		
		for(Keyword keyword : keywords){
			
			for(Entity ent : entities){
				if(keyword.getName().equals(ent.getName())){
					double eScore = vocabulary.get(ent.getName());
				//	System.out.println("keyword equal to entity");
					vocabulary.put(ent.getName(), eScore+1);
					
				}
			}
			
			String[] ngrams = keyword.getName().split(" ");
			for(int i=0;i<ngrams.length;i++){
			//	System.out.println("Handling word : "+ngrams[i]);
				for(Entity ent : entities){
					if(ngrams[i].equals(ent.getName()) || ent.getName().contains(ngrams[i])){
						double eScore = vocabulary.get(ent.getName());
						vocabulary.put(ent.getName(), eScore + 1);
					}
				}
				
				if(!vocabulary.containsKey(ngrams[i])){
					double score = 0;
					//check if it is or contains an entity
					for(Entity ent : entities){
						if(ngrams[i].equals(ent.getName()) || ent.getName().contains(ngrams[i]))
							score += ent.getCont() + 1;
					} 
					score += keyword.getScore()+1;
					vocabulary.put(ngrams[i], score);
				}
				else{
					double score = vocabulary.get(ngrams[i]);
					score += 1;
					vocabulary.put(ngrams[i], score);
				}
				
			//	System.out.println("word : "+ngrams[i]+" is stored in vocabulary with score : "+vocabulary.get(ngrams[i]));
			}
		}
	}
	
	public void printVocabulary(){
		System.out.println("---Vocabulary---");
		for(String word : vocabulary.keySet()){
			System.out.println(word + " - "+vocabulary.get(word));
		}
	}
	
	private void selectValuableContent(){
		
		findMostImportantEntities();
		
	/*	System.out.println("---Most Important Entities---");
		for(Entity ent : mientities){
			System.out.println(ent.getName());
		}*/
		
		double maxScore = 0;
		Map<Double,List<Keyword>> scoresToKeywords = new TreeMap<Double,List<Keyword>>(Collections.reverseOrder());
		
		for(Keyword key : keywords){
			double score = 0;
			boolean isEqualToEntity = false;
			
			if(key.getName().length() == 1){
				score += vocabulary.get(key.getName());
			}else{
				String[] words = key.getName().split(" ");
				
				for(int i=0;i<words.length;i++){
					score += vocabulary.get(words[i]);
				}
			}
			
			String updatedKeywordName = key.getName();
			for(Entity ent : mientities){
				if(key.getName().equals(ent.getName())){
					isEqualToEntity = true;
					break;
				}
				if(key.getName().contains(ent.getName())){
			//		System.out.println("keyword : "+key.getName()+" contains entity : "+ent.getName());
					updatedKeywordName = key.getName().replaceAll(ent.getName(), "").trim();
				}
			}
			
			if(!isEqualToEntity){
				Keyword updatedKeyword = new Keyword(updatedKeywordName,key.getScore());
				
				if(scoresToKeywords.containsKey(score)){
					List<Keyword> alreadyIn = scoresToKeywords.get(score);
					alreadyIn.add(updatedKeyword);
					scoresToKeywords.put(score, alreadyIn);
				}else{
					List<Keyword> newElement = new ArrayList<Keyword>();
					newElement.add(updatedKeyword);
					scoresToKeywords.put(score, newElement);
				}
			}

		}
		
		//get the first score which is the maximum
		
		for(double score : scoresToKeywords.keySet()){
			maxScore = score;
			break;
		}
			
		if(scoresToKeywords.get(maxScore) != null){
			if(!(mientities.isEmpty() && hashtags.isEmpty()))
				mikeywords = scoresToKeywords.get(maxScore);
		}
		
		/*System.out.println("---Most Important Keywords---");
		if(mikeywords != null)
			for(Keyword key : mikeywords){
				System.out.println(key.getName());
			}*/
	}
	
	
	private void findMostImportantEntities(){
		
		for(Entity ent : entities){
			boolean exists = false;
			
			for(Keyword key : keywords){
				if(key.getName().contains(ent.getName()) || key.getName().equals(ent.getName())){
					//System.out.println("Keyword "+key.getName()+" contains entity "+ent.getName());
					exists = true;
					break;
				}
		
			}
			
			if(exists){
				mientities.add(ent);
			}
		}
		
	}
	
	public static void main(String[] args) {
		TrendingSolrQueryBuilder builder = new TrendingSolrQueryBuilder(null);
		
		String res  = builder.getRightEntityKeywordCombination("henrik lundqvist", "henrik lundqvist show");
		
		System.out.println("res: "+res);
	}
}
