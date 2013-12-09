package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.socialsensor.framework.common.domain.Stopwords;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;

public class TrendingSolrQueryBuilder {
	
	private static final int MIN_RSS_THRESHOLD = 5;
	private static final int KEYWORDS_LIMIT = 3;
	
	private Map<String,Set<String>> wordsToRSSItems;
	private Map<String,Double> dyscoAttributesWithScore = new HashMap<String,Double>();
	private Map<String,Double> rssTopicsScore = new HashMap<String,Double>();
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<String> keywords = new ArrayList<String>();
	private List<String> hashtags = new ArrayList<String>();
	
	private List<String> topKeywords = new ArrayList<String>();
	
	private static List<String> mostSimilarRSSTopics = new ArrayList<String>();
	
	private Dysco dysco = null;
	
	private RSSProcessor rssProcessor = null;
	
	private Set<Entity> mostImportantEntities = new HashSet<Entity>();
	
	Stopwords stopwords = new Stopwords();
	
	
	public TrendingSolrQueryBuilder(Dysco dysco,RSSProcessor rssProcessor){
		this.dysco = dysco;
		this.rssProcessor = rssProcessor;
		
		this.wordsToRSSItems = rssProcessor.getWordsToRSSItems();
	}
	
	public String createSolrQuery(){
		
		
		String solrQuery = "keywords : (";
		
		boolean first = true;
		//add keywords to query
		for(String keyword : topKeywords){
			if(first){
				solrQuery += keyword;
				first = false;
			}	
			else
				solrQuery += "OR" + keyword;
		}
		solrQuery += ") OR hashtags : (";
		
		first = true;
		//add hashtags to query
		for(String hashtag : hashtags){
			if(first){
				solrQuery += hashtag;
				first = false;
			}	
			else
				solrQuery += "OR" + hashtag;
		}
		
		solrQuery += ")";
		return solrQuery;
	}
	
	public void selectUsefulContent(){
		List<String> mostSimilarRSSTopics = extractSimilarRSSForDysco(dysco);
		
		//add most important entities first
		if(getMostImportantEntities() != null){
			for(Entity ent : getMostImportantEntities())
				topKeywords.add(ent.getName());
		}
		
		//if there is need for more keywords add those that were found to be relevant from similar rss topics
		if(topKeywords.size()<KEYWORDS_LIMIT){
			
    		List<String> processorKeywords = rssProcessor.getTopKeywordsFromSimilarRSS(mostSimilarRSSTopics, dysco);
    		
    		Set<String> keywordsToAdd = new HashSet<String>();
    		
    		//remove possible duplicates
			for(String p_key : processorKeywords){
				boolean exists = false;
				for(String key : topKeywords){
				
					if(key.toLowerCase().equals(p_key.toLowerCase()) || key.toLowerCase().contains(p_key.toLowerCase())){
							exists = true;
							break;
					}
				}
				if(!exists){
				
					keywordsToAdd.add(p_key);
				}
			}

			for(String keyToAdd : keywordsToAdd){
				boolean exists = false;
				for(String keyToAdd_ : keywordsToAdd){
					
					if(!keyToAdd.equals(keyToAdd_)){
						if(keyToAdd_.contains(keyToAdd)){
							exists = true;
						}
					}
				}
				
				if(!exists)
					topKeywords.add(keyToAdd);
			}
			
		}
		
	}
	
	/**
	 * Sets the best (most representative) keywords 
	 * @param topKeywords
	 */
	public void setTopKeywords(List<String> topKeywords){
		this.topKeywords = topKeywords;
	}
	/**
	 * Returns the best (most representative) keywords 
	 * @param topKeywords
	 * @return List of strings
	 */
	public List<String> getTopKeywords(){
		return topKeywords;
	}
	/**
	 * Returns the most important entities for the dysco
	 * 
	 * @return List of Entity
	 */
	public Set<Entity> getMostImportantEntities(){
		
		return mostImportantEntities;
	}
	
	/**
	 * Finds the most similar rss topics to the dysco
	 * @param dysco
	 * @return
	 */
	public List<String> extractSimilarRSSForDysco(Dysco dysco){
		
		filterContent();
		extractDyscosAttributes();
		computeTopSimilarRSS();
		findMostSimilarRSS();
		
		if(mostSimilarRSSTopics.isEmpty())
			System.out.println("Dysco has no similar RSS Topic - Can't extract usefull keywords");
		
		return mostSimilarRSSTopics;
	}
	
	/**
	 * Sets the importance of its dysco word according to 
	 * its type and frequency in the dysco
	 */
	private void extractDyscosAttributes(){
		for(Entity entity : entities){
			Double score = entity.getCont();
			if(dysco.getTitle().contains(entity.getName().toLowerCase()))
				score *= 2;
			if(entity.getType().equals(Entity.Type.ORGANIZATION))
				score *= 2;
			if(entity.getType().equals(Entity.Type.PERSON))
				score *= 4;
			dyscoAttributesWithScore.put(entity.getName().toLowerCase(), score*entity.getCont());
		}
		
		for(String keyword : keywords){
			Double score = 1.0;
			if(dysco.getTitle().contains(keyword.toLowerCase())){
				score *= 2;
			}
			dyscoAttributesWithScore.put(keyword.toLowerCase(), score);
		}
		
	}
	
	/**
	 * Computes the similarities scores of the rss topics
	 * to the dysco based on their common entities and keywords
	 */
	private void computeTopSimilarRSS(){
		Set<String> similarRSS = new HashSet<String>();
		
		//find most important entity
		Double maxEntityScore = 0.0;
		for(Entity entity : entities){
			//find max score in entities
 			if(entity.getCont() > maxEntityScore){
				maxEntityScore = entity.getCont();
			}
		}
		for(Entity entity : entities){
			if(entity.getCont() == maxEntityScore)
				mostImportantEntities.add(entity);
		}
		
		if(!mostImportantEntities.isEmpty()){
			
			for(Entity imp_entity : mostImportantEntities){
				
				if(wordsToRSSItems.get(imp_entity.getName().toLowerCase())!=null)
					similarRSS.addAll(wordsToRSSItems.get(imp_entity.getName().toLowerCase()));
			}
				
			if(similarRSS.isEmpty())
				System.out.println("No similar RSS Items for important entities");
			else{
				for(Entity entity : entities){
					if(wordsToRSSItems.containsKey(entity.getName().toLowerCase())){
						Double score = dyscoAttributesWithScore.get(entity.getName().toLowerCase());
						for(String rss : similarRSS){
							if(wordsToRSSItems.get(entity.getName().toLowerCase()).contains(rss)){
								if(rssTopicsScore.containsKey(rss)){
									rssTopicsScore.put(rss, (rssTopicsScore.get(rss)+1)*score);
								}
								else{
									rssTopicsScore.put(rss, score);
								}
							}
							
						}
					}
				}
				
				for(String keyword : keywords){
					if(wordsToRSSItems.containsKey(keyword.toLowerCase())){
						Double score = dyscoAttributesWithScore.get(keyword.toLowerCase());
						for(String rss : similarRSS){
							if(wordsToRSSItems.get(keyword.toLowerCase()).contains(rss)){
								if(rssTopicsScore.containsKey(rss)){
									rssTopicsScore.put(rss, (rssTopicsScore.get(rss)+1)*score);
								}
								else{
									rssTopicsScore.put(rss, score);
								}
							}
							
						}
					}
				}
			}
		}
		
		if(similarRSS.isEmpty()){
			
			for(String keyword : keywords){
				
				if(wordsToRSSItems.containsKey(keyword.toLowerCase())){
					Double score = dyscoAttributesWithScore.get(keyword.toLowerCase());
					similarRSS = wordsToRSSItems.get(keyword.toLowerCase());
					for(String rss : similarRSS){
						if(rssTopicsScore.containsKey(rss)){
							rssTopicsScore.put(rss, (rssTopicsScore.get(rss)+1)*score);
						}
						else{
							rssTopicsScore.put(rss, score);
						}
					}
				}
			}
			
			boolean isSimilar = false;
			for(Double score : rssTopicsScore.values()){
				if(score >= MIN_RSS_THRESHOLD)
					isSimilar = true;
			}
			
			if(!isSimilar)
				rssTopicsScore.clear();
		}
	}
	
	/**
	 * Finds the rss topics that have the maximum 
	 * similarity score to the dysco
	 */
	private void findMostSimilarRSS(){
		Double maxScore = 0.0;
	
		for(String rss : rssTopicsScore.keySet()){
			if(rssTopicsScore.get(rss) > maxScore)
				maxScore = rssTopicsScore.get(rss);
		}
		mostSimilarRSSTopics.clear();
		for(Map.Entry<String, Double> entry : rssTopicsScore.entrySet()){
			if(entry.getValue() == maxScore){
				mostSimilarRSSTopics.add(entry.getKey());
			}
		}
	}
	
	/**
	 * Filters dysco's content 
	 */
	private void filterContent(){
		
		List<Entity> filteredEntities = new ArrayList<Entity>();
		List<String> filteredKeywords = new ArrayList<String>();
		
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
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("\\(", ""));
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("\\)", ""));
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("'", ""));
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("[:.,?!;&'#-]+",""));
					filteredEntities.get(index).setName(filteredEntities.get(index).getName().replaceAll("\\s+", " "));
	       		 	
				}
			}
			
			entities.addAll(filteredEntities);
		}
			
		//Filter keywords
		if(dysco.getKeywords() != null){
			filteredKeywords.addAll(dysco.getKeywords().keySet());
			for(String key : dysco.getKeywords().keySet()){
				int index = filteredKeywords.indexOf(key);
			
				if(key.contains("@")||key.contains("#") 
						|| stopwords.is(key)
						|| key.split(" ").length > 3){
					filteredKeywords.remove(key);
					continue;
				}
				if(key.contains("http")){
					String newKey = key.substring(0,key.indexOf("http"));
					filteredKeywords.get(index).replace(filteredKeywords.get(index), newKey);
				}
				
				filteredKeywords.get(index).toLowerCase();
				filteredKeywords.get(index).replaceAll("'s", "");
				filteredKeywords.get(index).replaceAll("\\(", "");
				filteredKeywords.get(index).replaceAll("\\)", "");
				filteredKeywords.get(index).replaceAll("'", "");
				filteredKeywords.get(index).replaceAll("[:.,?!;&'#]+-","");
				filteredKeywords.get(index).replaceAll("\\s+", " ");
       		 	
			}
			
			keywords.addAll(filteredKeywords);
		}
		
		if(dysco.getHashtags() != null){
			hashtags.addAll(dysco.getHashtags().keySet());
		}
			
	}
	
	public static void main(String[] args) {
		
	}
}
