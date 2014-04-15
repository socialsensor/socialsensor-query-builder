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


import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import eu.socialsensor.framework.common.domain.Stopwords;

public class GraphCreator {
	private Graph graph = new Graph();
	
//	private String model = "conf/english.all.3class.caseless.distsim.crf.ser.gz";
//	private EntitiesExtractor extractor = new EntitiesExtractor(model);
	
	private List<String> keywords = new ArrayList<String>();
	private List<String> hashtags = new ArrayList<String>();
	
	private Set<String> textContent = new HashSet<String>();
	private Set<String> entities = new HashSet<String>();
	
	private Map<String,String> substituteWords = new HashMap<String,String>();
	
	//gephi graph
	private ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
	private Workspace workspace;
	
	private GraphModel graphModel;
	private AttributeModel attributeModel;
	private DirectedGraph gephiGraph;
	
	private Stopwords stopwords = new Stopwords();
	
	Map<String,Integer> nodesToIndeces = new HashMap<String,Integer>();
	
	public GraphCreator(Set<String> textContent,List<String> keywords){
		this.keywords = keywords;
		
		this.textContent = textContent;
		
	}
	
	public GraphCreator(Set<String> textContent){

		this.textContent = textContent;
	
	}
	
	public GraphCreator(Set<String> textContent,List<String> keywords,List<String> hashtags){
		this.textContent = textContent;
		this.keywords = keywords;
		this.hashtags = hashtags;
		
	}
	
	
	public Graph getGraph(){
		return graph;
	}
	
	public void setSubstituteWords(Map<String,String> words){
		this.substituteWords = words;
	}
	
	public void createGraph(){
		
		addNodesToGraph(keywords);
		createGephiGraph();
		detectInAndOutDegrees();
		
	}
	
	
	public void createGephiGraph(){
		pc.newProject();
		workspace = pc.getCurrentWorkspace();
		
		graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		
		Vector<org.gephi.graph.api.Node> gNodes = new Vector<org.gephi.graph.api.Node>();
		Vector<org.gephi.graph.api.Edge> gEdges = new Vector<org.gephi.graph.api.Edge>();
		
		int index = 0;
		for(Node node : graph.getNodes()){
			org.gephi.graph.api.Node gNode = graphModel.factory().newNode(node.getId());
			gNode.getNodeData().setLabel(node.getId());
			gNodes.add(gNode);
			
			nodesToIndeces.put(node.getId(), index);
			index++;
		}
		
		for(Node node : graph.getNodes()){
			int p_1 = nodesToIndeces.get(node.getId());
			for(String neighId : node.getOutNeighbors()){
				if(graph.exists(neighId)){
					int p_2 = nodesToIndeces.get(neighId);
					float weight = node.getOutNeighborsWeight(neighId);
					org.gephi.graph.api.Edge gEdge = graphModel.factory().newEdge(gNodes.get(p_1),gNodes.get(p_2),weight,true);
					gEdges.add(gEdge);
				}
			}
		}
		gephiGraph = graphModel.getDirectedGraph();
		for(int i=0;i<gNodes.size();i++)
			gephiGraph.addNode(gNodes.get(i));
		for(int i=0;i<gEdges.size();i++)
			gephiGraph.addEdge(gEdges.get(i));
		
	}
	
	public void exportGephiGraphToFile(String gephiFileName){
		exportGraphToFile(gephiFileName);
	}
	
	public void detectInAndOutDegrees(){
		for(org.gephi.graph.api.Node gNode : gephiGraph.getNodes()){
			int inDegree = gephiGraph.getInDegree(gNode);
			int outDegree = gephiGraph.getOutDegree(gNode);
			int mutDegree = gephiGraph.getMutualDegree(gNode);
			
			Node node = graph.getNode(gNode.getNodeData().getId());
			node.setInDegree(inDegree);
			node.setOutDegree(outDegree);
			node.setMutDegree(mutDegree);
			
			graph.addNode(node);
		}
		
	}
	
	/**
	 * @brief Method for graph extraction to a file (can be gefx,pdf etc)
	 *
	 */
	private void exportGraphToFile(String gephiFileName){
	
		ExportController ec = Lookup.getDefault().lookup(ExportController.class);
		
		try {
			ec.exportFile(new File("C:/Users/ailiakop/Dropbox/SocialSensor/gephiFiles/"+gephiFileName+"_gephiGraph.gexf"));
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
	}
	
	/*private void extractEntities(){
		
		for(MediaItem mediaItem : mediaItems){
			if(mediaItem.getTitle() != null){
				List<Entity> extEntities = extractor.getEntities(mediaItem.getTitle().toLowerCase());
				for(Entity ent : extEntities){
					entities.add(ent.getName());
				}
			}
			
			if(mediaItem.getDescription() != null){
				List<Entity> extEntities = extractor.getEntities(mediaItem.getDescription().toLowerCase());
				for(Entity ent : extEntities){
					entities.add(ent.getName());
				}
			}
			
		}
	}*/
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
	
	private String getRightWord(String word){
		String res = substituteWords.get(word);
		if(res != null)
			return res;
		
		return word;
	}
	
	private List<String> detectAdjacentWords(String word,String text){
		List<String> adjacentWords = new ArrayList<String>();
		
		if(!text.contains(word))
			return null;
		
		String[] words = text.split("[^a-zA-Z0-9#'][^a-zA-Z0-9#']*");
	
		for(int i=0;i<words.length;i++){
			if(words[i].equals(word) || words[i].contains(word)){
				if(i!=words.length-1 && words[i+1].length()>1 && !stopwords.is(words[i+1])){
					adjacentWords.add(words[i+1]);
				}
			}
		}
		
		return adjacentWords;
	}
	
	public void pruneLowConnectivityNodes(){
		Set<String> nodesToPrune = new HashSet<String>();
		
		boolean allChecked = false;
		
		while(!allChecked){
			for(Node node : graph.getNodes()){
				if((node.getInDegree()<2 && node.getOutDegree()<2)){
					boolean toPrune = true;
					
					for(String neigh : node.getOutNeighbors()){
						if(node.getOutNeighborsWeight(neigh) > 3){
							toPrune = false;
							break;
						}
					}
					
					if(!toPrune) continue;
					
					for(String inNeigh : node.getInNeighbors()){
						if(graph.getNode(inNeigh).getOutNeighborsWeight(node.getId()) > 3){
							toPrune = false;
							break;
						}
					}
					
					if(toPrune)
						nodesToPrune.add(node.getId());
				}
			}
			
			if(nodesToPrune.isEmpty())
				allChecked = true;
			
			for(String prunedNode : nodesToPrune){
				for(String outNeigh : graph.getNode(prunedNode).getOutNeighbors()){
					graph.getNode(outNeigh).removeFromInNeighbors(prunedNode);
					graph.getNode(outNeigh).setInDegree(graph.getNode(outNeigh).getInDegree() - 1);
				}
				
				for(String inNeigh : graph.getNode(prunedNode).getInNeighbors()){
					graph.getNode(inNeigh).removeFromOutNeighbors(prunedNode);
					graph.getNode(inNeigh).setOutDegree(graph.getNode(inNeigh).getOutDegree() - 1);
				}
				
				graph.removeFromGraph(prunedNode);
			}
			
			createGephiGraph();
			detectInAndOutDegrees();
			nodesToPrune.clear();
		}
		
		//graph.printGraph();
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
