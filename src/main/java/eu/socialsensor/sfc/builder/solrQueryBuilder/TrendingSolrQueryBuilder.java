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
	
	private TrendsRanker trendsRanker = null;
	
	Stopwords stopwords = new Stopwords();
	
	//at this point every information that remains in the dysco will be used for the creation of the solr query
	
	public TrendingSolrQueryBuilder(Dysco dysco){
		this.dysco = dysco;
		
		addfilteredDyscoContent();
		extractDyscosVocabularyWithWeights();
		//printVocabulary();
		
		selectValuableContent();
	}
	
	public void setHandler(SolrNewsFeedHandler handler){
		
		this.trendsRanker = new TrendsRanker(handler);
		
	}
	/**
	 * Creates and returns the solr query. The solr query is created by combining 
	 * the keywords , entities and hashtags that represent the topic of a trending 
	 * dysco.
	 * @return the solr querty
	 */
	public String createSolrQuery(){
		
		if(keywords.isEmpty() && entities.isEmpty() && hashtags.isEmpty())
			return null;
	
		//call here the method to select best content for quering
		
		boolean first = true;
		
		String solrQuery = "title : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(Keyword keyword : keywords){
				if(first){
					solrQuery += keyword.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + keyword.getName();
			}
		}
		
		if(!entities.isEmpty()){
			for(Entity entity : entities){
				if(first){
					solrQuery += entity.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + entity.getName();
				
			}
		}
		
		if(!hashtags.isEmpty()){
			for(Keyword hashtag : hashtags){
				if(first){
					solrQuery += hashtag.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag.getName();
			}
		}
		solrQuery += ")";
		first = true;
		solrQuery += " OR description : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(Keyword keyword : keywords){
				if(first){
					solrQuery += keyword.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + keyword.getName();
			}
		}
		
		if(!entities.isEmpty()){
			for(Entity entity : entities){
				if(first){
					solrQuery += entity.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + entity.getName();
				
			}
		}
		
		if(!hashtags.isEmpty()){
			for(Keyword hashtag : hashtags){
				if(first){
					solrQuery += hashtag.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag.getName();
			}
		}
		
		solrQuery += ")";
		first = true;
		solrQuery += " OR tags : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(Keyword keyword : keywords){
					
				if(first){
					solrQuery += keyword.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + keyword.getName();
			}
		}
		
		if(!entities.isEmpty()){
			for(Entity entity : entities){
				String[] parts = null;
				if(entity.getName().split(" ").length > 1){
					parts = entity.getName().split(" ");
				}
				if(parts != null){
					String unifiedEntity = "";
					for(int i=0; i<parts.length;i++){
						unifiedEntity += parts[i];
					}
										
					if(first){
						solrQuery += unifiedEntity;
						first = false;
					}	
					else
						solrQuery += " OR " + unifiedEntity;
				}else{
					
					if(first){
						solrQuery += entity.getName();
						first = false;
					}	
					else
						solrQuery += " OR " + entity.getName();

				}
				
			}	
				
		}
		
		if(!hashtags.isEmpty()){
			for(Keyword hashtag : hashtags){
				if(first){
					solrQuery += hashtag.getName();
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag.getName();
			}
		}
		
		solrQuery += ")";
			
		
		return solrQuery;
	}
	
	public String createUpdatedSolrQuery(){
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
	
		//Final formulation of solr query
		
		if(!query.equals(""))
			solrQuery += "("+query+")";
		
		return solrQuery;
	}
	
	/**
	 * Filters dysco's content 
	 */
	private void addfilteredDyscoContent(){
		
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
				
				//Create the keyword to use
				Keyword keyword = new Keyword(filteredKeywords.get(index).toLowerCase(),dysco.getKeywords().get(key).floatValue());
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
}
