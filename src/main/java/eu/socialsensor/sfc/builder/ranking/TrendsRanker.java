package eu.socialsensor.sfc.builder.ranking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.client.dao.impl.DyscoDAOImpl;
import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.client.search.solr.SolrItemHandler;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.dysco.Entity;
import eu.socialsensor.sfc.builder.solrQueryBuilder.Calculator;

/**
 * Class responsible for calculating a score for a DySco on the basis of
 * an updated RSS collection. The score reflects the quality of the information
 * the DySco entails as a news worthy event. 
 * @author ailiakop
 * @email ailiakop@iti.gr
 */
public class TrendsRanker {
	
	public final Logger logger = Logger.getLogger(TrendsRanker.class);
	
	private static Long DAY_IN_MILLISECONDS = 86400000L;
	
	private SolrItemHandler solrItemHandler;
	
	private BoundedList<Double> dyscoScoresList = new BoundedList<Double>(200);
	private BoundedList<Double> rankerScoresList = new BoundedList<Double>(200);
	
	public TrendsRanker(String solrCollection) {
		try {
			solrItemHandler = SolrItemHandler.getInstance(solrCollection);
			logger.info("SolrItemHandler initialized.. ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the score that was calculated for a DySco from 
	 * comparing its content to an updated RSS collection
	 * @param dysco
	 * @return a double number
	 */
	public Double getContentScore(Dysco dysco) {
		Double score = 0.0;
		
		try {
			List<Query> solrQueries = dysco.getSolrQueries();
			List<Float> queriesScores = new ArrayList<Float>();
		
			for(Query sQuery : solrQueries) {
				float queryLength = sQuery.getName().length();
			
				String query = "(title : ("+sQuery.getName()+")) OR (description : ("+sQuery.getName()+"))";
		
				Map<Item,Float> itemsByRelevance = solrItemHandler.findItemsWithScore(query);
		
				float avgScore = Calculator.computeAverageFloat(itemsByRelevance.values()) * sQuery.getScore().floatValue();
				//float maxScore = Collections.max(itemsByRelevance.values()) * sQuery.getScore().floatValue();
			
				avgScore *= (queryLength/100);
	
				queriesScores.add(avgScore);
			}
		
			long dateTimeOfDysco = dysco.getCreationDate().getTime();
			long currentDateTime = System.currentTimeMillis();
		
			double timeDiff = (double) Math.abs(dateTimeOfDysco - currentDateTime)/DAY_IN_MILLISECONDS;
		
			double timeEval = Math.sqrt(20/(20 + (Math.exp(timeDiff))));
	
			score = Calculator.computeAverageFloat(queriesScores) * timeEval;
			//score = Collections.max(queriesScores) * timeEval;
		}
		catch(Exception e) {
			logger.equals(e.getMessage());
		}
		return score;
	}
	
	/**
	 * Returns the score that was calculated for a DySco from 
	 * comparing its content to an updated RSS collection
	 * @param dysco
	 * @return a double number
	 */
	public Double getContentScore(Dysco dysco, String listId) {
		Double score = 0.0;
		
		List<Query> solrQueries = dysco.getSolrQueries();
		List<Float> queriesScores = new ArrayList<Float>();
		
		try {
			for(Query sQuery : solrQueries) {
				float queryLength = sQuery.getName().length();
			
				//String query = "(title : ("+sQuery.getName()+")) OR (description : ("+sQuery.getName()+"))";
		
				String queryName = sQuery.getName();
				queryName = queryName.replaceAll("\"", " ");
				queryName = queryName.trim();
				queryName = queryName.replaceAll("\\s+", " AND ");		
				String query = "(title : (" + queryName + ")) OR (description : (" + queryName + "))";
				
				query += " AND (lists : " + listId + ")";
			
				Map<Item, Float> itemsByRelevance = solrItemHandler.findItemsWithScore(query);
				//System.out.println(query + " => " + itemsByRelevance.size());
				
				float avgScore = Calculator.computeAverageFloat(itemsByRelevance.values()) * sQuery.getScore().floatValue();
			
				//float maxScore = 0;
				//if(!itemsByRelevance.isEmpty())
				//	maxScore = Collections.max(itemsByRelevance.values()) * sQuery.getScore().floatValue();
			
				avgScore *= (queryLength/100);
				queriesScores.add(avgScore);
			
				//maxScore *= (queryLength/100);
				//queriesScores.add(maxScore);
			}
		
			long dateTimeOfDysco = dysco.getCreationDate().getTime();
			long currentDateTime = System.currentTimeMillis();
		
			double timeDiff = (double) Math.abs(dateTimeOfDysco - currentDateTime)/DAY_IN_MILLISECONDS;
			double timeEval = Math.sqrt(20 / (20 + (Math.exp(timeDiff))));
	
			score = Calculator.computeAverageFloat(queriesScores) * timeEval;
			//if(!queriesScores.isEmpty())
			//	score = Collections.max(queriesScores) * timeEval;
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		return score;
	}
	
	/**
	 * Returns a ranked lists of DyScos. The DyScos are ranked on the basis
	 * of the calculated scores from comparing their content to the updated
	 * RSS collection
	 * @param dyscos
	 * @return
	 */
	public List<Dysco> rankDyscos(List<Dysco> dyscos) {
		List<Dysco> rankedDyscos = new LinkedList<Dysco>();
		Map<Double, List<Dysco>> dyscosByValues = new TreeMap<Double, List<Dysco>>(Collections.reverseOrder());		
		
		try {
			for(Dysco dysco : dyscos) {
				Double score = dysco.getRankerScore();
				
				if(score == null)
					continue;
				
				List<Dysco> alreadyIn = dyscosByValues.get(score);
				if(alreadyIn == null) {
					alreadyIn = new ArrayList<Dysco>();
					dyscosByValues.put(score, alreadyIn);
				}
				alreadyIn.add(dysco);
			}
		
			for(Map.Entry<Double, List<Dysco>> entry : dyscosByValues.entrySet()) {
				for(Dysco dysco : entry.getValue()) {
					rankedDyscos.add(dysco);
				}
			}
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		
		return rankedDyscos;
	}
	
	/**
	 * Evaluates a set of newly created DyScos. In case they are duplicates it eliminates
	 * them using a heuristic rule. Number of duplicates are multiplied over the score 
	 * computed by comparing dyscos solr queries to the RSS collection. The returned DyScos
	 * are ranked by their updated ranker scores.
	 * @param dyscos
	 * @return the updated list of ranked DyScos.
	 */
	public List<Dysco> evaluateDyscosByContent(List<Dysco> dyscos) {
	
		Map<String,String> dyscosTitles = new HashMap<String,String>();
		Map<String,Integer> dyscosCooccurrences = new HashMap<String,Integer>();
		
		for(Dysco dysco : dyscos) {
			
			Double dyscoScore = dysco.getScore();
			dyscoScoresList.push(dyscoScore);
			
			List<Entity> entities = dysco.getEntities();
			Set<String> keywords = dysco.getKeywords().keySet();
			
			int entitiesFound = 0;
			int keywordsFound = 0;
			boolean isDuplicate = false;
			for(Map.Entry<String, String> entry : dyscosTitles.entrySet()) {
				
				for(Entity ent : entities){
					if(entry.getValue().contains(ent.getName().toLowerCase()))
						entitiesFound++;
				}
				
				for(String key : keywords) {
					if(entry.getValue().contains(key.toLowerCase()))
						keywordsFound++;
				}
			
				if((entitiesFound >=1 && keywordsFound >= 2) || dysco.getTitle().toLowerCase().equals(entry.getValue())) {
					isDuplicate = true;
					Integer newScore = dyscosCooccurrences.get(entry.getKey()) + 1;
					dyscosCooccurrences.put(entry.getKey(),newScore);
					break;
				}
			}
			
			if(!isDuplicate) {
				dyscosTitles.put(dysco.getId(),dysco.getTitle().toLowerCase());
				dyscosCooccurrences.put(dysco.getId(),1);
				continue;
			}
		}
		for(Dysco dysco : dyscos) {
			if(!dyscosCooccurrences.containsKey(dysco.getId())) {
				dysco.setRankerScore(-1.0);
			}
			else {
				Double rankerScore = getContentScore(dysco) * dyscosCooccurrences.get(dysco.getId());
				dysco.setRankerScore(rankerScore);
				rankerScoresList.push(rankerScore);	
			}
		}
		
		Double minDyscoScore = Collections.min(dyscoScoresList);
		Double minRankerScore = Collections.min(rankerScoresList);
		Double maxDyscoScore = Collections.max(dyscoScoresList);
		Double maxRankerScore = Collections.max(rankerScoresList);
		
		for(Dysco dysco : dyscos) {
			double rankerScore = dysco.getRankerScore();
			if(rankerScore != -1) {
				Double normalizedRankerScore = (rankerScore - minRankerScore) / (maxRankerScore - minRankerScore);
				dysco.setNormalizedRankerScore(normalizedRankerScore);
				
				double normalizedDyscoScore = (dysco.getScore() - minDyscoScore) / (maxDyscoScore - minDyscoScore);
				dysco.setNormalizedDyscoScore(normalizedDyscoScore);
			}
			else {
				dysco.setNormalizedRankerScore(-1);
				dysco.setNormalizedDyscoScore(-1);
			}
		}
		
		return rankDyscos(dyscos);
	}
	
	
	/**
	 * Evaluates a set of newly created DyScos. In case they are duplicates it eliminates
	 * them using a heuristic rule. Number of duplicates are multiplied over the score 
	 * computed by comparing dyscos solr queries to the RSS collection. The returned DyScos
	 * are ranked by their updated ranker scores.
	 * @param dyscos
	 * @return the updated list of ranked DyScos.
	 */
	public List<Dysco> evaluateDyscosByContent(List<Dysco> dyscos, String listId) {
	
		logger.info("Evaluate " + dyscos.size() + " dyscos from list " + listId);
		
		Map<String, String> dyscosTitles = new HashMap<String, String>();
		Map<String, Integer> dyscosCooccurrences = new HashMap<String, Integer>();
		
		for(Dysco dysco : dyscos) {
			
			Double dyscoScore = dysco.getScore();
			
			if(dyscoScore == null) {
				dysco.setScore(0.);
				dyscoScore = 0.;
			}
			
			dyscoScoresList.push(dyscoScore);
			
			String currentDyscoTitle = dysco.getTitle();
			if(currentDyscoTitle == null) {
				continue;
			}
			
			List<Entity> entities = dysco.getEntities();
			Set<String> keywords = dysco.getKeywords().keySet();
			
			int entitiesFound = 0;
			int keywordsFound = 0;
			boolean isDuplicate = false;
			
			for(Map.Entry<String, String> entry : dyscosTitles.entrySet()) {
				
				String dyscoId = entry.getKey();
				String dyscoTitle = entry.getValue();
				if(entities != null) {
					for(Entity entity : entities) {
						String entityName = entity.getName();
						if(entityName != null && dyscoTitle.contains(entityName.toLowerCase())) {
							entitiesFound++;
						}
					}
				}
				
				if(keywords != null) {
					for(String keyword : keywords) {
						if(dyscoTitle.contains(keyword.toLowerCase())) {
							keywordsFound++;
						}
					}
				}
				
				if((entitiesFound >=1 && keywordsFound >= 2) || currentDyscoTitle.toLowerCase().equals(dyscoTitle)) {
					isDuplicate = true;
					Integer pScore = dyscosCooccurrences.get(dyscoId);
					Integer newScore =  (pScore==null ? 0 : pScore) + 1;
					dyscosCooccurrences.put(dyscoId, newScore);
					break;
				}
			}
			
			if(!isDuplicate) {
				dyscosTitles.put(dysco.getId(), currentDyscoTitle.toLowerCase());
				dyscosCooccurrences.put(dysco.getId(), 1);
				continue;
			}
		}
		
		for(Dysco dysco : dyscos) {
			if(!dyscosCooccurrences.containsKey(dysco.getId())) {
				dysco.setRankerScore(-1.0);
			}
			else {
				
				Double contentScore = getContentScore(dysco, listId);
				Integer cooccurrences = dyscosCooccurrences.get(dysco.getId());
				if(cooccurrences == null)
					cooccurrences = 0;
				
				Double rankerScore =  contentScore * cooccurrences;
				dysco.setRankerScore(rankerScore);
				
				rankerScoresList.push(rankerScore);
			}
		}
		
		Double minDyscoScore = dyscoScoresList.isEmpty() ? 0d : Collections.min(dyscoScoresList);
		Double minRankerScore = rankerScoresList.isEmpty() ? 0d : Collections.min(rankerScoresList);
		Double maxDyscoScore = dyscoScoresList.isEmpty() ? 0d : Collections.max(dyscoScoresList);
		Double maxRankerScore = rankerScoresList.isEmpty() ? 0d : Collections.max(rankerScoresList);
		
		logger.info("Min Dysco Score: " + minDyscoScore);
		logger.info("Max Dysco Score: " + maxDyscoScore);
		logger.info("Min Ranker Score: " + minRankerScore);
		logger.info("Max Ranker Score: " + maxRankerScore);
		
		for(Dysco dysco : dyscos) {
			double rankerScore = dysco.getRankerScore();
			if(rankerScore >= 0) {
				Double normalizedRankerScore = 0d;
				if((maxRankerScore - minRankerScore) != 0)
					normalizedRankerScore = (rankerScore - minRankerScore) / (maxRankerScore - minRankerScore);
				dysco.setNormalizedRankerScore(normalizedRankerScore);
				
				Double normalizedDyscoScore = 0d;
				if((maxDyscoScore - minDyscoScore) != 0)
					normalizedDyscoScore = (dysco.getScore() - minDyscoScore) / (maxDyscoScore - minDyscoScore);
				dysco.setNormalizedDyscoScore(normalizedDyscoScore);
				
				logger.info("Dysco: " + dysco.getId() + ",  normalizedRankerScore= " + normalizedRankerScore + 
						",  normalizedDyscoScore=" + normalizedDyscoScore);
			}
			else {
				dysco.setNormalizedRankerScore(-1.0);
				dysco.setNormalizedDyscoScore(-1.0);
				
				logger.info("Dysco: " + dysco.getId() + ",  normalizedRankerScore=-1,  normalizedDyscoScore=-1");
			}
		}
		return rankDyscos(dyscos);
	}

	private static class BoundedList<T> extends LinkedList<T> {

	    /**
		 * 
		 */
		private static final long serialVersionUID = -8828336215826576541L;
		private final int bound;

	    public BoundedList(int bound) {
	        this.bound = bound;
	    }

	    public synchronized void push(T item) {
	        super.push(item);
	        if (super.size() > bound) {
	        	try {
	        		super.removeLast();                
	        	}
	        	catch(Exception e) {
	        		
	        	}
	        }
	    }

	    public synchronized T pop() {
	        return super.poll();
	    }

	}

	public static void main(String...args) throws Exception {
		
		SolrDyscoHandler solrDyscoHandler = SolrDyscoHandler.getInstance("http://xxx.xxx.xxx.xxx/solr/dyscos");
        TrendsRanker ranker = new TrendsRanker("http://xxx.xxx.xxx.xxx/solr/NewsFeed");
        
        Date date = new Date(System.currentTimeMillis() - 15*60000);
        TimeZone tz = TimeZone.getDefault();
	    Date gmtDate = new Date( date.getTime() - tz.getRawOffset() );
	    if ( tz.inDaylightTime( gmtDate )){
	        Date dstDate = new Date( gmtDate.getTime() - tz.getDSTSavings() );
	        if ( tz.inDaylightTime( dstDate )) {
	        	gmtDate = dstDate;
	        }
	     }
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String formattedDate = dateFormat.format(gmtDate);
		
		List<Dysco> dyscos = solrDyscoHandler.findDyscosInTimeframe(formattedDate).getResults();
        System.out.println(dyscos.size() + " dyscos");
        
		List<Dysco> rankedDyscos = ranker.evaluateDyscosByContent(dyscos, "1");
		System.out.println(rankedDyscos.size() + " ranked dyscos");
		
	}
}
