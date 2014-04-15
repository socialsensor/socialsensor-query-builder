package eu.socialsensor.sfc.builder.solrQueryBuilder.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Graph {
	
	private Map<String,Node> nodes = new HashMap<String,Node>();
	
	public Graph(){
		
	}
	
	public void addNode(Node node){
		nodes.put(node.getId(), node);

	}
	
	public Collection<Node> getNodes(){
		return nodes.values();
	}
	
	public Node getNode(String id){
		return nodes.get(id);
	}
	
	public boolean exists(String id){
		if(nodes.containsKey(id))
			return true;
			
		return false;
	}
	
	public void removeFromGraph(String id){
		nodes.remove(id);
	}
	
	public void printGraph(){
		for(Node node : nodes.values()){
			System.out.println();
			System.out.print(node.getId()+":Out[");
			for(String neighId : node.getOutNeighbors()){
				System.out.print(neighId+",");
			}
			System.out.print("]");
			System.out.print(node.getId()+":In[");
			for(String neighId : node.getInNeighbors()){
				System.out.print(neighId+",");
			}
			System.out.print("]");
			System.out.println();
		}
		
	}

}
