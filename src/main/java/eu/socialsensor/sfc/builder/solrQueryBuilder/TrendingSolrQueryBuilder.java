package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Stopwords;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;


public class TrendingSolrQueryBuilder {
	
	public final Logger logger = Logger.getLogger(TrendingSolrQueryBuilder.class);
	
	private static final int KEYWORDS_LIMIT = 3;
	
	private List<Entity> entities = new ArrayList<Entity>();
	private Set<String> keywords = new HashSet<String>();
	private Set<String> hashtags = new HashSet<String>();
	
	private Dysco dysco = null;
	
	Stopwords stopwords = new Stopwords();
	
	//at this point every information that remains in the dysco will be used for the creation of the solr query
	
	public TrendingSolrQueryBuilder(Dysco dysco){
		this.dysco = dysco;
		
		addfilteredDyscoContent();
	}
	
	public String createSolrQuery(){
		
		if(keywords.isEmpty() && entities.isEmpty() && hashtags.isEmpty())
			return null;
	
		boolean first = true;
		
		String solrQuery = "title : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(String keyword : keywords){
				if(first){
					solrQuery += keyword;
					first = false;
				}	
				else
					solrQuery += " AND " + keyword;
			}
		}
		
		if(!entities.isEmpty()){
			for(Entity entity : entities){
				if(first){
					solrQuery += entity.getName();
					first = false;
				}	
				else
					solrQuery += " AND " + entity.getName();
				
			}
		}
		
		if(!hashtags.isEmpty()){
			for(String hashtag : hashtags){
				if(first){
					solrQuery += hashtag;
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag;
			}
		}
		solrQuery += ")";
		first = true;
		solrQuery += " OR description : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(String keyword : keywords){
				if(first){
					solrQuery += keyword;
					first = false;
				}	
				else
					solrQuery += " AND " + keyword;
			}
		}
		
		if(!entities.isEmpty()){
			for(Entity entity : entities){
				if(first){
					solrQuery += entity.getName();
					first = false;
				}	
				else
					solrQuery += " AND " + entity.getName();
				
			}
		}
		
		if(!hashtags.isEmpty()){
			for(String hashtag : hashtags){
				if(first){
					solrQuery += hashtag;
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag;
			}
		}
		
		solrQuery += ")";
		first = true;
		solrQuery += " OR tags : (";
		
		if(!keywords.isEmpty()){
			//add keywords to query
			for(String keyword : keywords){
					
				if(first){
					solrQuery += keyword;
					first = false;
				}	
				else
					solrQuery += " OR " + keyword;
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
			for(String hashtag : hashtags){
				if(first){
					solrQuery += hashtag;
					first = false;
				}	
				else
					solrQuery += " OR " + hashtag;
			}
		}
		
		solrQuery += ")";
			
		
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
       		 	
			}
			
			keywords.addAll(filteredKeywords);
		}
		
		if(dysco.getHashtags() != null){
			hashtags.addAll(dysco.getHashtags().keySet());
		}
			
	}
	
}
