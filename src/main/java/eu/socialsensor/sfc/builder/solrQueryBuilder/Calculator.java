package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.Collection;
import java.util.Set;

/**
 * Class that contains methods for performing basic calculations such as
 * computing the average/variance of a set of number
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class Calculator {
	
	public static double computeAverage(int[] elements){
		double average = 0.0;
		
		if(elements.length == 0)
			return average;
		
		for(int i=0;i<elements.length;i++){
			average += elements[i];
		}
		
		return average/=elements.length;
	}
	
	public static double computeVariance(int[] elements,double average){
		double variance = 0.0;
		
		if(elements.length == 0)
			return average;
		
		for(int i=0;i<elements.length;i++){
			variance += Math.pow(elements[i] - average, 2);
		}
		
		return variance /= elements.length;
	}
	
	public static double computeAverage(Set<Integer> elements){
		double average = 0.0;
		
		if(elements.size() == 0)
			return average;
		
		for(Integer elem : elements){
			average += elem;
		}
		
		return average/= elements.size();
	}
	
	public static double computeVariance(Set<Integer> elements,double average){
		double variance = 0.0;
		
		if(elements.size() == 0)
			return average;
		
		for(Integer elem : elements){
			variance += Math.pow(elem - average, 2);
		}
		
		return variance /= elements.size();
	}
	
	public static double computeAverageDouble(Set<Double> elements){
		double average = 0.0;
		
		if(elements.size() == 0)
			return average;
		
		for(Double elem : elements){
			average += elem;
		}
		
		return average/= elements.size();
	}
	
	public static double computeVarianceDouble(Set<Double> elements,double average){
		double variance = 0.0;
		
		if(elements.size() == 0)
			return average;
		
		for(Double elem : elements){
			variance += Math.pow(elem - average, 2);
		}
		
		return variance /= elements.size();
	}
	
	public static float computeAverageFloat(Collection<Float> elements){
		float average = 0F;
		
		if(elements.size() == 0)
			return average;
		
		for(Float elem : elements) {
			average += elem;
		}
		
		return average = average / elements.size();
	}

}
