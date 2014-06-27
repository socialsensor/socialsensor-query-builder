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
	
	public TrendsRanker(String solrCollection){
		try {
			solrItemHandler = SolrItemHandler.getInstance(solrCollection);
			logger.info("SolrItemHandler initialized.. ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the score that was calculated for a DySco from 
	 * comparing its content to an updated RSS collection
	 * @param dysco
	 * @return a double number
	 */
	public Double getScore(Dysco dysco){
		Double score = 0.0;
		
		List<Query> solrQueries = dysco.getSolrQueries();
		
		List<Float> queriesScores = new ArrayList<Float>();
		
		for(Query sQuery : solrQueries){
			float queryLength = sQuery.getName().length();
			
			String query = "(title : ("+sQuery.getName()+")) OR (description : ("+sQuery.getName()+"))";
		
			Map<Item,Float> itemsByRelevance = solrItemHandler.findItemsWithScore(query);
		
			float avgScore = Calculator.computeAverageFloat(itemsByRelevance.values()) * sQuery.getScore().floatValue();
		
			avgScore *= (queryLength/10);
		
			queriesScores.add(avgScore);
		}
		
		long dateTimeOfDysco = dysco.getCreationDate().getTime();
		long currentDateTime = System.currentTimeMillis();
		
		double timeDiff = (double) Math.abs(dateTimeOfDysco - currentDateTime)/DAY_IN_MILLISECONDS;
		
		double timeEval = Math.sqrt(20/(20 + (Math.exp(timeDiff))));
	
		score = Calculator.computeAverageFloat(queriesScores) * timeEval;
	
		return score;
	}
	
	/**
	 * Returns a ranked lists of DyScos. The DyScos are ranked on the basis
	 * of the calculated scores from comparing their content to the updated
	 * RSS collection
	 * @param dyscos
	 * @return
	 */
	public List<Dysco> rankDyscos(List<Dysco> dyscos){
		List<Dysco> rankedDyscos = new LinkedList<Dysco>();
		
		Map<Double,List<Dysco>> dyscosByValues = new TreeMap<Double,List<Dysco>>(Collections.reverseOrder());
		
		for(Dysco dysco : dyscos){
			Double score = dysco.getRankerScore();
			if(dyscosByValues.get(score) == null){
				List<Dysco> alreadyIn = new ArrayList<Dysco>();
				alreadyIn.add(dysco);
				dyscosByValues.put(score, alreadyIn);
			}
			else{
				List<Dysco> alreadyIn = dyscosByValues.get(score);
				alreadyIn.add(dysco);
				dyscosByValues.put(score, alreadyIn);
			}
		}
		
		for(Map.Entry<Double, List<Dysco>> entry : dyscosByValues.entrySet()){
			for(Dysco dysco : entry.getValue()){
				rankedDyscos.add(dysco);
			}
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
	public List<Dysco> evaluateDyscosByContent(List<Dysco> dyscos){
	
		Map<String,String> dyscosTitles = new HashMap<String,String>();
		Map<String,Integer> dyscosCooccurrences = new HashMap<String,Integer>();
		
		for(Dysco dysco : dyscos){
			List<Entity> entities = dysco.getEntities();
			Set<String> keywords = dysco.getKeywords().keySet();
			
			int entitiesFound = 0;
			int keywordsFound = 0;
			boolean isDuplicate = false;
			for(Map.Entry<String, String> entry : dyscosTitles.entrySet()){
				for(Entity ent : entities){
					if(entry.getValue().contains(ent.getName().toLowerCase()))
						entitiesFound++;
				}
				for(String key : keywords){
					if(entry.getValue().contains(key.toLowerCase()))
						keywordsFound++;
				}
			
				if((entitiesFound >=1 && keywordsFound >= 2) || dysco.getTitle().toLowerCase().equals(entry.getValue())){
					isDuplicate = true;
					Integer newScore = dyscosCooccurrences.get(entry.getKey()) + 1;
					dyscosCooccurrences.put(entry.getKey(),newScore);
					break;
				}
			}
			
			if(!isDuplicate){
				dyscosTitles.put(dysco.getId(),dysco.getTitle().toLowerCase());
				dyscosCooccurrences.put(dysco.getId(),1);
				continue;
			}
		}
		for(Dysco dysco : dyscos){
			if(!dyscosCooccurrences.containsKey(dysco.getId())){
				dysco.setRankerScore(-1.0);
			}
			else{
				Double rankerScore = getScore(dysco) * dyscosCooccurrences.get(dysco.getId());
				dysco.setRankerScore(rankerScore);
			}
		}
		
		
		return rankDyscos(dyscos);
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}	

}
