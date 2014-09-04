package eu.socialsensor.sfc.builder.solrQueryBuilder.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import eu.socialsensor.framework.common.domain.Stopwords;

/**
 * Class responsible for the creation of the graph of keywords 
 * which will be used for the detection of additional queries 
 * for a topic (DySco)
 * @author ailiakop
 * @email ailiakop@iti.gr
 */
public class GraphCreator {
	private Graph graph = new Graph();
	
	private List<String> keywords = new ArrayList<String>();
	private Set<String> textContent = new HashSet<String>();
	
	private Map<String,String> substituteWords = new HashMap<String, String>();
	
	//gephi graph
	private ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
	private Workspace workspace;
	
	//private AttributeModel attributeModel;
	private DirectedGraph gephiGraph;
	
	private Stopwords stopwords = new Stopwords();
	
	Map<String, Integer> nodesToIndeces = new HashMap<String, Integer>();
	
	public GraphCreator(Set<String> textContent,List<String> keywords) {
		this.keywords = keywords;
		
		this.textContent = textContent;
		
	}
	
	public GraphCreator(Set<String> textContent){

		this.textContent = textContent;
	
	}
	
	public GraphCreator(Set<String> textContent, List<String> keywords, List<String> hashtags) {
		this.textContent = textContent;
		this.keywords = keywords;
	}
	
	/**
	 * Returns the graph
	 * @return the graph
	 */
	public Graph getGraph() {
		return graph;
	}
	
	/**
	 * Set mapping of substitute words to candidate node-keywords
	 * @param words
	 */
	public void setSubstituteWords(Map<String, String> words) {
		this.substituteWords = words;
	}
	
	/**
	 * Creates the graph of keywords
	 */
	public void createGraph() {
		addNodesToGraph(keywords);
		createGephiGraph();
		detectInAndOutDegrees();
		reset();
	}
	
	/**
	 * Formulates a graph based on the gephi library out of the graph structure
	 * Gephi graph makes the graph handling easier by automatically computing outgoing
	 * and incoming edges in the directed graph structure.
	 */
	public void createGephiGraph() {
		pc.newProject();
		workspace = pc.getCurrentWorkspace();
		
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		GraphFactory graphFactory = graphModel.factory();
		
		//attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		
		Vector<org.gephi.graph.api.Node> gNodes = new Vector<org.gephi.graph.api.Node>();
		Vector<org.gephi.graph.api.Edge> gEdges = new Vector<org.gephi.graph.api.Edge>();
		
		int index = 0;
		for(Node node : graph.getNodes()) {
			org.gephi.graph.api.Node gNode = graphFactory.newNode(node.getId());
			gNode.setLabel(node.getId());
			//gNode.getNodeData().setLabel(node.getId());
			gNodes.add(gNode);
			
			nodesToIndeces.put(node.getId(), index);
			index++;
		}
		
		for(Node node : graph.getNodes()) {
			int p_1 = nodesToIndeces.get(node.getId());
			for(String neighId : node.getOutNeighbors()) {
				if(graph.exists(neighId)) {
					int p_2 = nodesToIndeces.get(neighId);
					float weight = node.getOutNeighborsWeight(neighId);
					
					//org.gephi.graph.api.Edge gEdge = graphFactory.newEdge(gNodes.get(p_1), gNodes.get(p_2), weight, true);
					org.gephi.graph.api.Edge gEdge = graphFactory.newEdge(gNodes.get(p_1), gNodes.get(p_2), true);
					gEdge.setWeight(weight);
					gEdges.add(gEdge);
				}
			}
		}
		
		gephiGraph = graphModel.getDirectedGraph();
		for(int i=0; i<gNodes.size(); i++) {
			gephiGraph.addNode(gNodes.get(i));
		}
	
		for(int i=0; i<gEdges.size(); i++) {
			gephiGraph.addEdge(gEdges.get(i));
		}
	}
	
	/**
	 * Exporst the gephi graph to a file
	 * @param gephiFileName
	 */
	public void exportGephiGraphToFile(String gephiFileName) {
		exportGraphToFile(gephiFileName);
	}
	
	/**
	 * Detects the degrees of incoming neighbor nodes 
	 * and outgoing neighbor nodes for all nodes in the graph
	 */
	public void detectInAndOutDegrees() {
		for(org.gephi.graph.api.Node gNode : gephiGraph.getNodes()) {
			int inDegree = gephiGraph.getInDegree(gNode);
			int outDegree = gephiGraph.getOutDegree(gNode);
			//int mutDegree = gephiGraph.getMutualDegree(gNode);
			
			//Node node = graph.getNode(gNode.getNodeData().getId());
			Node node = graph.getNode((String) gNode.getId());
			node.setInDegree(inDegree);
			node.setOutDegree(outDegree);
			//node.setMutDegree(mutDegree);
			
			graph.addNode(node);
		}
		
	}
	
	/**
	 * @brief Method for gephi graph extraction to a file (can be gefx,pdf etc)
	 *
	 */
	private void exportGraphToFile(String gephiFileName){
	
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		
		try {
			ec.exportFile(new File("C:/Users/ailiakop/Desktop/gephiFiles/"+gephiFileName+"_gephiGraph.gexf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}
	

	/**
	 * Processes a list of words as candidate nodes to a graph. 
	 * Words are processed in combination to a text content.
	 * Neighbor keywords-nodes of the examined word are determined
	 * on the basis of their co-occurrence in the text content with the 
	 * word. The nodes and their neighbors are added to the primary
	 * graph structure.
	 * 
	 * @param words
	 */
	private void addNodesToGraph(List<String> words){
		for(String word : words){
			List<String> neighbors = new ArrayList<String>();
			for(String content : textContent){

				List<String> neighIDs = detectAdjacentWords(word,content);
				if(neighIDs != null){
					for(String neighId : neighIDs){
						if(!neighId.contains("#")){
							String rightWord = getRightWord(neighId);
							if(keywords.contains(neighId))
								neighbors.add(rightWord);
						}	
					}
				}
				
			}
			
			if(neighbors.isEmpty())
				continue;
			
			String rightWord = getRightWord(word);
			
			Node node;
			if(!graph.exists(rightWord)){
				node = new Node(rightWord);
				node.setEntityNode(true);
				
				graph.addNode(node);
				
			}
			else{
				node = graph.getNode(rightWord);
			}
					
			for(String neigh : neighbors){
				if(node.isOutNeighbor(neigh)){
					node.updateOutNeighbor(neigh, node.getOutNeighborsWeight(neigh)+1);
				}
				else{
					if(!graph.exists(neigh)){
						Node neighNode = new Node(neigh);
						graph.addNode(neighNode);
					}
					node.updateOutNeighbor(neigh, 1);
				}
				if(graph.getNode(neigh)!=null){
					Node neighbor = graph.getNode(neigh);
					neighbor.addInNeighbor(rightWord);
				}
					
			}
		}
	}
	
	/**
	 * Returns the substitute of the word if exists, else the
	 * method returns the word itself. 
	 * @param word
	 * @return a word as string
	 */
	private String getRightWord(String word) {
		String res = substituteWords.get(word);
		if(res != null)
			return res;
		
		return word;
	}
	
	/**
	 * Detects the adjacent words to a word in several pieces of texts. 
	 * @param word
	 * @param text
	 * @return list of the adjacent words
	 */
	private List<String> detectAdjacentWords(String word, String text) {
		List<String> adjacentWords = new ArrayList<String>();
		
		if(!text.contains(word))
			return null;
		
		String[] words = text.split("[^a-zA-Z0-9#'][^a-zA-Z0-9#']*");
	
		for(int i=0;i<words.length;i++) {
			if(words[i].equals(word) || words[i].contains(word)){
				if(i!=words.length-1 && words[i+1].length()>1 && !stopwords.is(words[i+1])){
					adjacentWords.add(words[i+1]);
				}
			}
		}
		
		return adjacentWords;
	}
	
	private void reset() {
		pc.cleanWorkspace(workspace);
		pc.closeCurrentWorkspace();
		pc.closeCurrentProject();
	}
	
	/**
	 * Prunes the nodes that are lightly weighted in the graph to reduce
	 * noise and increase the possibility of producing highly relevant
	 * queries
	 */
	public void pruneLowConnectivityNodes() {
		Set<String> nodesToPrune = new HashSet<String>();
		
		boolean allChecked = false;
		
		while(!allChecked){
			for(Node node : graph.getNodes()) {
				if((node.getInDegree()<2 && node.getOutDegree()<2)) {
					boolean toPrune = true;
					
					for(String neigh : node.getOutNeighbors()){
						if(node.getOutNeighborsWeight(neigh) > 3) {
							toPrune = false;
							break;
						}
					}
					
					if(!toPrune) continue;
					
					for(String inNeigh : node.getInNeighbors()) {
						if(graph.getNode(inNeigh).getOutNeighborsWeight(node.getId()) > 3) {
							toPrune = false;
							break;
						}
					}
					
					if(toPrune) {
						nodesToPrune.add(node.getId());
					}
				}
			}
			
			if(nodesToPrune.isEmpty())
				allChecked = true;
			
			for(String prunedNode : nodesToPrune) {
				for(String outNeigh : graph.getNode(prunedNode).getOutNeighbors()) {
					graph.getNode(outNeigh).removeFromInNeighbors(prunedNode);
					graph.getNode(outNeigh).setInDegree(graph.getNode(outNeigh).getInDegree() - 1);
				}
				
				for(String inNeigh : graph.getNode(prunedNode).getInNeighbors()) {
					graph.getNode(inNeigh).removeFromOutNeighbors(prunedNode);
					graph.getNode(inNeigh).setOutDegree(graph.getNode(inNeigh).getOutDegree() - 1);
				}
				
				graph.removeFromGraph(prunedNode);
			}
			
			createGephiGraph();
			detectInAndOutDegrees();
			nodesToPrune.clear();
			
			reset();
		}
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
