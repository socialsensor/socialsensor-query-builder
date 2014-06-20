package eu.socialsensor.sfc.builder.solrQueryBuilder.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import eu.socialsensor.framework.common.domain.JSONable;

/**
 * The data structure of a node in a graph
 * @author ailiakop
 * @email ailiakop@iti.gr
 */
public class Node implements JSONable{
	
	public Node(String id){
		this.id = id;
	}
	
	@Expose
    @SerializedName(value = "id")
	private String id;
	
	@Expose
    @SerializedName(value = "outNeighbors")
	private Map<String,Integer> outNeighbors = new HashMap<String,Integer>();
	
	@Expose
    @SerializedName(value = "inNeighbors")
	private Set<String> inNeighbors = new HashSet<String>();

	@Expose
    @SerializedName(value = "primary")
	private boolean primary = false;
	
	@Expose
    @SerializedName(value = "isEntity")
	private boolean isEntity = false;
	
	@Expose
    @SerializedName(value = "inDegree")
	private int inDegree;
	
	@Expose
    @SerializedName(value = "outDegree")
	private int outDegree;
	
	@Expose
    @SerializedName(value = "outWeightedDegree")
	private int outWeightedDegree;
	
	@Expose
    @SerializedName(value = "mutDegree")
	private int mutDegree;
	
	@Expose
    @SerializedName(value = "value")
	private double value;
	
	@Override
    public String toJSONString() {
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.toJson(this);
    }
	
	public String getId(){
		return id;
	}
	
	public void updateOutNeighbor(String nID, Integer weight){
		outNeighbors.put(nID, weight);
	}
	
	public Integer getOutNeighborsWeight(String nID){
		return outNeighbors.get(nID);
	}
	
	public Set<String> getOutNeighbors(){
		return outNeighbors.keySet();
	}
	
	public void removeFromOutNeighbors(String neighId){
		outNeighbors.remove(neighId);
	}
	
	public boolean isOutNeighbor(String neighID){
		if(outNeighbors.containsKey(neighID))
			return true;
		
		return false;
	}
	
	public void addInNeighbor(String nID){
		inNeighbors.add(nID);
	}
	

	public Set<String> getInNeighbors(){
		return inNeighbors;
	}
	
	public void removeFromInNeighbors(String neighId){
		inNeighbors.remove(neighId);
	}
	
	public boolean isInNeighbor(String neighID){
		if(inNeighbors.contains(neighID))
			return true;
		
		return false;
	}
	
	public void setPrimaryNode(boolean isPrimary){
		this.primary = true;
	}
	
	public boolean isPrimaryNode(){
		return primary;
	}
	
	public void setEntityNode(boolean isEntity){
		this.isEntity = isEntity;
	}
	
	public boolean isEntityNode(){
		return isEntity;
	}
	
	public void setInDegree(int inDegree){
		this.inDegree = inDegree;
	}
	
	public void setOutDegree(int outDegree){
		this.outDegree = outDegree;
	}
	
	public void setOutWeightedDegree(int outWeightedDegree){
		this.outWeightedDegree = outWeightedDegree;
	}
	
	public void setMutDegree(int mutDegree){
		this.mutDegree = mutDegree;
	}
	
	public int getInDegree(){
		return inDegree;
	}
	
	public int getOutDegree(){
		return outDegree;
	}
	
	public int getOutWeightedDegree(){
		return outWeightedDegree;
	}
	
	public int getMutDegree(){
		return mutDegree;
	}
	
	public double getValue(){
		return value;
	}
	
	public void setValue(double value){
		this.value = value;
	}
	
	/**
	 * Calculates the outgoing neighbor node with the maximum weight
	 * 
	 * @return the maximum weight of the node
	 */
	public int computeMaxOutNeighborsWeight(){
		int maxWeight = 0;
		for(String neighId : outNeighbors.keySet()){
			if(maxWeight < outNeighbors.get(neighId)){
				maxWeight = outNeighbors.get(neighId);
			}
		}
		
		return maxWeight;
	}
	
}
