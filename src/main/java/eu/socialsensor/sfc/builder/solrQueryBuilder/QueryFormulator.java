package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import eu.socialsensor.sfc.builder.solrQueryBuilder.graph.Graph;
import eu.socialsensor.sfc.builder.solrQueryBuilder.graph.Node;


/**
 * Class responsible for formulating queries when a graph is given as input.
 * The algorithm calculates the most heavily connected nodes in the graph
 * to detect popular additional queries to a topic. 
 * @author ailiakop
 * @email ailiakop@iti.gr
 */
public class QueryFormulator {
	
	private Graph graph;
	
	private List<String> startingNodes = new ArrayList<String>();
	
	private Map<String,Double> hashtags = new HashMap<String,Double>();
	private Map<String,Double> keywordQueries = new HashMap<String,Double>();
	private Map<Double,List<String>> rankedKeywordQueries = new TreeMap<Double,List<String>>(Collections.reverseOrder());
	private Map<Double,List<String>> rankedHashtagQueries = new TreeMap<Double,List<String>>(Collections.reverseOrder());
	
	private Double maxKeywordsQueryScore = 0.0;
	private Double maxHashtagsQueryScore = 0.0;

	public QueryFormulator(Graph graph){
		this.graph = graph;
	}
	
	public QueryFormulator(Graph graph,Map<String,Double> hashtags){
		this.graph = graph;
		this.hashtags = hashtags;
	
	}
	/**
	 * Prints keyword queries produced by the algorithm
	 */
	public void printQueries(){
		System.out.println("*** Queries ***");
		for(Entry<String,Double> entry : keywordQueries.entrySet()){
			System.out.println();
			System.out.println(entry.getKey()+" : "+entry.getValue());
			System.out.println();
		}
	}
	
	/**
	 * Prints ranked keyword queries by their weight.
	 */
	public void printRankedKeywordQueries(){
		System.out.println("*** Ranked Keyword Queries ***");
		System.out.println();
		for(Double value : rankedKeywordQueries.keySet()){
			System.out.println("---- SCORE "+value+" ----");
			System.out.println();
			for(String rQuery : rankedKeywordQueries.get(value)){
				System.out.println("Q : "+rQuery);
			}
			System.out.println();
		}
	}
	/**
	 * Prints ranked hashtag queries by their weight.
	 */
	public void printRankedHashtagQueries(){
		System.out.println("*** Ranked Hashtag Queries ***");
		System.out.println();
		for(Double value : rankedHashtagQueries.keySet()){
			System.out.println("---- SCORE "+value+" ----");
			System.out.println();
			for(String rQuery : rankedHashtagQueries.get(value)){
				System.out.println("Q : "+rQuery);
			}
			System.out.println();
		}
	}
	
	public Double getMaxKeywordsQueryScore(){
		return this.maxKeywordsQueryScore;
	}
	
	public Double getMaxHashtagQueryScore(){
		return this.maxHashtagsQueryScore;
	}
	/**
	 * Prints nodes out and in degrees. 
	 */
	public void printDegrees(){
		Map<Integer,List<Node>> topInDegreeNodes = new HashMap<Integer,List<Node>>();
		Map<Integer,List<Node>> topOutDegreeNodes = new HashMap<Integer,List<Node>>();
		
		Set<Integer> topInDegrees = new TreeSet<Integer>(Collections.reverseOrder());
		Set<Integer> topOutDegrees = new TreeSet<Integer>(Collections.reverseOrder());
		
		for(Node node : graph.getNodes()){
			if(topInDegreeNodes.get(node.getInDegree())!=null){
				List<Node> alreadyIn = topInDegreeNodes.get(node.getInDegree());
				alreadyIn.add(node);
				topInDegreeNodes.put(node.getInDegree(), alreadyIn);
			}
			else{
				List<Node> alreadyIn = new ArrayList<Node>();
				alreadyIn.add(node);
				topInDegreeNodes.put(node.getInDegree(), alreadyIn);
			}
			topInDegrees.add(node.getInDegree());
			
			if(topOutDegreeNodes.get(node.getOutDegree())!=null){
				List<Node> alreadyIn = topOutDegreeNodes.get(node.getOutDegree());
				alreadyIn.add(node);
				topOutDegreeNodes.put(node.getOutDegree(), alreadyIn);
			}
			else{
				List<Node> alreadyIn = new ArrayList<Node>();
				alreadyIn.add(node);
				topOutDegreeNodes.put(node.getOutDegree(), alreadyIn);
			}
			topOutDegrees.add(node.getOutDegree());
		}
		

		System.out.println("----InDegrees----");
		for(Integer inDegree : topInDegrees){
			List<Node> inNodes = topInDegreeNodes.get(inDegree);
			System.out.print("In Degree "+inDegree+" :: ");
			for(Node node : inNodes){
				System.out.print(node.getId()+" ");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
		System.out.println("----OutDegrees----");
		for(Integer outDegree : topOutDegrees){
			List<Node> outNodes = topOutDegreeNodes.get(outDegree);
			System.out.print("Out Degree "+outDegree+" :: ");
			for(Node node : outNodes){
				System.out.print(node.getId()+" ");
			}
			System.out.println();
		}
	}
	/**
	 * Create keywords queries containing a maximum number of words
	 * by traversing the graph of keywords
	 * @param numberOfWords
	 */
	public void generateKeywordQueries(int numberOfWords){
	
		detectStartingNodes();

		for(String word : startingNodes){
			createQuery(graph.getNode(word),numberOfWords);
		}
		eliminateDoubleKeywordQueries();
		rankKeywordQueries();

	}
	/**
	 * Returns a maximum number of keyword queries produced by the algorithm 
	 * and ranked according to their weight. 
	 * @param numberOfQueries
	 * @return the list of keyword queries
	 */
	public List<String> getKeywordQueries(int numberOfQueries){
		List<String> queries = new ArrayList<String>();
		
		for(Entry<Double, List<String>> entry : rankedKeywordQueries.entrySet()){
			for(String query : entry.getValue()){
				queries.add(query);
				if(queries.size() >= numberOfQueries)
					return queries;
			}
		}
		
		return queries;
	}
	
	public Map<Double,List<String>> getRankedKeywordQueries(){
		return rankedKeywordQueries;
	}
	/**
	 * Returns a maximum number of hashtag queries produced by the algorithm
	 * and ranked according to their weight.
	 * @param numberOfQueries
	 * @return the list of hashtag queries
	 */
	public List<String> getHashtagQueries(int numberOfQueries){
		List<String> queries = new ArrayList<String>();
		
		for(Entry<Double, List<String>> entry : rankedHashtagQueries.entrySet()){
			for(String query : entry.getValue()){
				queries.add(query);
				if(queries.size() >= numberOfQueries)
					return queries;
			}
		}
		
		return queries;
	}
	
	public Map<Double,List<String>> getRankedHashtagQueries(){
		return rankedHashtagQueries;
	}
	/**
	 * Creates hashtag queries by traversing the graph and examining hashtag's connections
	 * with other words
	 */
	public void generateHashtagQueries(){

		for(String tag : hashtags.keySet()){
			double score = hashtags.get(tag);
			Set<String> embeddedWords = new HashSet<String>();
			for(Node node : graph.getNodes()){
				if(tag.contains(node.getId()))
					embeddedWords.add(node.getId());
			}
			
			for(String emWord : embeddedWords){
				Node node = graph.getNode(emWord);
				int encounters = 1;
				for(String otherEmWord : embeddedWords){
					if(!emWord.equals(otherEmWord) && node.isOutNeighbor(otherEmWord)){
						score *= node.getOutNeighborsWeight(otherEmWord);
						encounters++;
					}
				}
				score /= encounters;
			}
			
			if(score > maxHashtagsQueryScore)
				maxHashtagsQueryScore = score;
			
			if(rankedHashtagQueries.get(score)!=null){
				List<String> alreadyIn = rankedHashtagQueries.get(score);
				alreadyIn.add(tag);
				rankedHashtagQueries.put(score, alreadyIn);
				
			}
			else{
				List<String> alreadyIn = new ArrayList<String>();
				alreadyIn.add(tag);
				rankedHashtagQueries.put(score, alreadyIn);
			}
		}
	}
	/**
	 * Eliminates double keyword queries that might be produced by
	 * the keyword queries formulation process. The method detects queries 
	 * that are identical or contain the same words in a different order.
	 * It downgrades very similar queries and upgrades the original ones.
	 */
	private void eliminateDoubleKeywordQueries(){
		String[] queriesToProcess = new String[keywordQueries.size()];
		Map<String, Double> queriesToChange = new HashMap<String,Double>();
		List<String> queriesToRemove = new ArrayList<String>();
		queriesToChange.putAll(keywordQueries);
		int index=0;
		for(String query : keywordQueries.keySet()){
			queriesToProcess[index] = query;
			index++;
		}
		
		for(int i=0 ; i<queriesToProcess.length ; i++){
			for(int j=0 ; j<queriesToProcess.length ; j++){
				double similarity = 0.0;
				if(i!=j){
					String query = queriesToProcess[i];
					String otherQuery = queriesToProcess[j];
					
					String[] parts = query.split(" ");
					String[] otherParts = otherQuery.split(" ");
				
					int maxLength = otherParts.length<parts.length?parts.length:otherParts.length;
					int times = 0;
					for(int k=0;k<parts.length;k++){
						for(int l=0;l<otherParts.length;l++){
							if(parts[k].equals(otherParts[l])){
								times ++;
							}
								
						}
					}
					if(times == parts.length || times ==otherParts.length){
						similarity = 1.0;
					}
					else
						similarity /= maxLength;
					
					double score = keywordQueries.get(query);
					double otherScore = keywordQueries.get(otherQuery);
					if(score > otherScore){
				
						queriesToChange.put(otherQuery, queriesToChange.get(otherQuery) * (1-similarity));
					}
					else{
					
						queriesToChange.put(query, queriesToChange.get(query) * (1-similarity));
					}
				}
				
			}
		}
		
		for(String queryToChange : queriesToChange.keySet()){
			keywordQueries.put(queryToChange, queriesToChange.get(queryToChange));
		}
		for(String queryToRemove : queriesToRemove){
			keywordQueries.remove(queryToRemove);
		}
		
	}
	/**
	 * Ranks keyword queries by their weights
	 */
	private void rankKeywordQueries(){
		for(Entry<String,Double> entry : keywordQueries.entrySet()){
			if(entry.getValue() > maxKeywordsQueryScore)
				maxKeywordsQueryScore = entry.getValue() ;
			
			if(rankedKeywordQueries.get(entry.getValue())!=null){
				List<String> alreadyIn = rankedKeywordQueries.get(entry.getValue());
				alreadyIn.add(entry.getKey());
				rankedKeywordQueries.put(entry.getValue(), alreadyIn);
			}
			else{
				List<String> alreadyIn = new ArrayList<String>();
				alreadyIn.add(entry.getKey());
				rankedKeywordQueries.put(entry.getValue(), alreadyIn);
			}
		}
	}
	/**
	 * Detects the starting nodes in the graph to start traversing it 
	 * for queries detection
	 */
	private void detectStartingNodes(){
	
		Map<Double,List<String>> scores = new TreeMap<Double,List<String>>(Collections.reverseOrder());
		for(Node node : graph.getNodes()){
			double score = 0.0;
			double nom = 0.0, denom = 0.0;
			
			for(String outNode : node.getOutNeighbors()){
				nom += node.getOutNeighborsWeight(outNode);
			}
			
			for(String inNode : node.getInNeighbors()){
				denom += graph.getNode(inNode).getOutNeighborsWeight(node.getId());
			}
			
			if(denom != 0)
				score = nom;
			
			node.setValue(score);
			if(scores.get(score) != null){
				List<String> alreadyIn = scores.get(score);
				alreadyIn.add(node.getId());
				scores.put(score, alreadyIn);
			}
			else{
				List<String> alreadyIn = new ArrayList<String>();
				alreadyIn.add(node.getId());
				scores.put(score, alreadyIn);
			}
		
		}
		boolean finished = false;
		for(Double score : scores.keySet()){
			for(String nodeId : scores.get(score)){
				startingNodes.add(nodeId);
				
				if(startingNodes.size()>5){
					finished = true;
					break;
				}
			}
			if(finished)
				break;
		}
		
	}
	/**
	 * Traverses the directed graph to form queries on the basis of graph's directed weighted nodes
	 * @param query
	 * @param score
	 * @param currentNode
	 * @param currentSteps
	 * @param maxSteps
	 */
	private void traverseQueryGraph(String query,Double score,Node currentNode,int currentSteps,int maxSteps){
	
		for(String neighId : currentNode.getOutNeighbors()){
			String queryToProcess = query;
			Double scoreToProcess = score;
			
			if(queryToProcess.contains(neighId)){
				scoreToProcess /= currentSteps;
				keywordQueries.put(queryToProcess, scoreToProcess);
		
				continue;
			}
			if(currentNode.getOutNeighborsWeight(neighId)>1){
				queryToProcess += " "+neighId;
				scoreToProcess += currentNode.computeMaxOutNeighborsWeight();
				
			}
			if((currentSteps + 1) >= maxSteps){
				scoreToProcess /= maxSteps;
				keywordQueries.put(queryToProcess, scoreToProcess);
			
				continue;
			}
			traverseQueryGraph(queryToProcess,scoreToProcess,graph.getNode(neighId),currentSteps+1,maxSteps);	
			
		}

	}
	/**
	 * Creates all possible keyword queries starting from a cerain node in the graph
	 * for a limited number of steps, which reflects the number of words in the query.
	 * @param startNode
	 * @param numberOfSteps
	 */
	private void createQuery(Node startNode,int numberOfSteps){
		
		String query = startNode.getId();
		Double score = 0.0;
		traverseQueryGraph(query,score,startNode,1,numberOfSteps);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
