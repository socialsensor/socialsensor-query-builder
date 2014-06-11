package eu.socialsensor.sfc.builder.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.client.search.solr.SolrItemHandler;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Query;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.sfc.builder.solrQueryBuilder.Calculator;

public class TrendsRanker {
	
	private static Long DAY_IN_MILLISECONDS = 86400000L;
	
	private SolrItemHandler solrItemHandler;
	
	private Dysco dysco;

	private List<Dysco> dyscos;
	
	public TrendsRanker(String solrCollection){
		try {
			solrItemHandler = SolrItemHandler.getInstance(solrCollection);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Double getScore(Dysco dysco){
		Double score = 0.0;
		
		List<Query> solrQueries = dysco.getSolrQueries();
		
		List<Float> queriesScores = new ArrayList<Float>();
		
		for(Query sQuery : solrQueries){
			float queryLength = sQuery.getName().length();
			//System.out.println("Query Length : "+queryLength);
			String query = "(title : ("+sQuery.getName()+")) OR (description : ("+sQuery.getName()+"))";
			//System.out.println("Query : "+query);
			Map<Item,Float> itemsByRelevance = solrItemHandler.findItemsWithScore(query);
			//System.out.println();
			float avgScore = Calculator.computeAverageFloat(itemsByRelevance.values());
			//System.out.println("Average score for query : "+query + " ->> "+avgScore);
			avgScore *= (queryLength/10);
			
			//System.out.println("Average score for query : "+query + " ->> "+avgScore);
			//System.out.println();
			queriesScores.add(avgScore);
		}
		
		long dateTimeOfDysco = dysco.getCreationDate().getTime();
		long currentDateTime = System.currentTimeMillis();
		
		double timeDiff = (double) Math.abs(dateTimeOfDysco - currentDateTime)/DAY_IN_MILLISECONDS;
		
		double timeEval = Math.sqrt(20/(20 + (Math.exp(timeDiff))));
		//System.out.println("Time diff : "+timeDiff);
		//System.out.println("Time eval : "+timeEval);
		
		score = Calculator.computeAverageFloat(queriesScores) * timeEval;
		//System.out.println("Total Average score for dysco : "+score);
		return score;
	}
	
	public List<Dysco> rankDyscos(List<Dysco> dyscos){
		List<Dysco> rankedDyscos = new LinkedList<Dysco>();
		
		Map<Double,List<Dysco>> dyscosByValues = new TreeMap<Double,List<Dysco>>(Collections.reverseOrder());
		
		for(Dysco dysco : dyscos){
			Double score = getScore(dysco);
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
	 * @param args
	 */
	public static void main(String[] args) {
	
	}	

}
