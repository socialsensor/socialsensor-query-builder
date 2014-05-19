package eu.socialsensor.sfc.builder.solrQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.washington.cs.knowitall.morpha.MorphaStemmer;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Stopwords;

public class KeywordsExtractor {
	private List<Item> items;
	
	private Stopwords stopwords = new Stopwords();
	
	private Map<String,Integer> popularKeywords = new HashMap<String,Integer>();
	private Map<String,Integer> popularHashtags = new HashMap<String,Integer>();
	private Map<String,String>  wordsToReplace = new HashMap<String,String>();
	
	private Set<String> dictionary = new HashSet<String>();
	private Set<String> stemWords = new HashSet<String>();
	private Set<String> textContent = new HashSet<String>();
	
	private String[][] rankedKeywords1;
	private String[][] rankedHashtags1;
	
	private Map<String,Double> rankedKeywords = new HashMap<String,Double>();
	private Map<String,Double> rankedHashtags = new HashMap<String,Double>();
	
	private double keywordsDev;
	private double hashtagsDev;
	
	double keywordsAVG = 0.0;
	double hashtagsAVG = 0.0;
	
	public KeywordsExtractor(List<Item> items){
		this.items = items;
		
	/*	BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("./conf/american-english.txt"));
			String line = null;
			while ((line = reader.readLine()) != null) {
			    dictionary.add(line);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	public Map<String,String> getWordsToReplace(){
		return wordsToReplace;
	}
	
	public void processItemsText(){
		
		for(Item item : items){
			String title = item.getTitle();
			String description = item.getDescription();
			
			if(title != null && description != null)
				if(title.equals(description)){
					description = "";
				}
			
			
			if(title != null && !title.isEmpty()){
				title = title.toLowerCase();
				title = eraseWebLinks(title);
				title = eraseEmailAdresses(title);
				title = eraseReferences(title);
				title = eraseAccounts(title);
			
				if(!textContent.contains(title)){
					countWords(title);
					textContent.add(title);
					
				}

			}
			
			if(description != null && !description.isEmpty()){
				description = description.toLowerCase();
				description = eraseWebLinks(description);
				description = eraseEmailAdresses(description);
				description = eraseReferences(description);
				description = eraseAccounts(description);
				
				if(!textContent.contains(description)){
					countWords(description);
					textContent.add(description);
				
				}
			}
			
		}
	
		processPopularHashtags();
		processPopularKeywords();
		
		sortElements();
		
	}
	
	public Set<String> getTextContent(){
		return textContent;
	}
	
	public List<String> getTopKeywords(){
		List<String> topKeywords = new ArrayList<String>();
		
//		for(int i=0;i<rankedKeywords.length;i++){
//			if(Double.parseDouble(rankedKeywords[i][1])>keywordsAVG)
//				topKeywords.add(rankedKeywords[i][0]);
//		}
		for(Map.Entry<String, Double> entry : rankedKeywords.entrySet()){
			if(entry.getValue() > keywordsAVG)
				topKeywords.add(entry.getKey());
		}
		
		return topKeywords;
	}
	
	public Map<String,Double> getTopHashtags(){
		Map<String,Double> topHashtags = new HashMap<String,Double>();

//		for(int i=0;i<rankedHashtags.length;i++){
//			if(rankedHashtags[i][1] != null)
//				if(Double.parseDouble(rankedHashtags[i][1])>hashtagsAVG)
//					topHashtags.put(rankedHashtags[i][0],Double.parseDouble(rankedHashtags[i][1]));
//		}
		
		for(Map.Entry<String, Double> entry : rankedHashtags.entrySet()){
			if(entry.getValue() > hashtagsAVG)
				topHashtags.put(entry.getKey(), entry.getValue());
		}
		
		return topHashtags;
	}
	
	public Set<String> getRankedKeywords(){
	//	List<String> keywords = new ArrayList<String>();
//		
//		for(int i=0;i<rankedKeywords.length;i++){
//			keywords.add(rankedKeywords[i][0]);
//		}
		return rankedKeywords.keySet();
	}
	
	public Set<String> getRankedHashtags(){
//		List<String> hashtags = new ArrayList<String>();
//		
//		for(int i=0;i<rankedHashtags.length;i++){
//			hashtags.add(rankedHashtags[i][0]);
//		}
		return rankedKeywords.keySet();
	}
	
	private void countWords(String text){
		
		String[] parts = text.split("[^a-zA-Z0-9#'][^a-zA-Z0-9#']*");
		
		for(int i=0;i<parts.length;i++){
			if(!stopwords.is(parts[i]) && !parts[i].equals(" ") && !parts[i].isEmpty()){
				if(parts[i].contains("#")){
					String[] hashtags = parts[i].split("#");
					for(int j=0;j<hashtags.length;j++){
						if(!stopwords.is(hashtags[j]) && !hashtags[j].equals(" ") && !hashtags[j].isEmpty() && hashtags[j].length()>1){
							if(popularHashtags.containsKey(hashtags[j]))
								popularHashtags.put(hashtags[j], popularHashtags.get(hashtags[j])+1);
							else
								popularHashtags.put(hashtags[j], 1);
						}
					}
					
				}
				else{
					if(parts[i].length() > 2){

						if(popularKeywords.containsKey(parts[i]))
							popularKeywords.put(parts[i], popularKeywords.get(parts[i])+1);
						else
							popularKeywords.put(parts[i], 1);
					}
				}
			}
		}
		
	}
	
	private String eraseWebLinks(String text){
		
		List<String> links = new ArrayList<String>();
			 
		String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		while(m.find()) {
			String urlStr = m.group();
		
			links.add(urlStr);
		}
		
		for(String link : links){
			while(text.contains(link))
				text = text.replace(link, "");
		}
		
		String regex2 = "http";
		Pattern p2 = Pattern.compile(regex2);
		Matcher m2 = p2.matcher(text);
		while(m2.find()) {
			String urlStr = m2.group();
		
			links.add(urlStr);
		}
		
		for(String link : links){
			while(text.contains(link))
				text = text.replace(link, "");
		}
			
		return text;	
	}
	
	private String eraseEmailAdresses(String text){
		
		List<String> emails = new ArrayList<String>();
		
		Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.]*");
		
		Matcher m = pattern.matcher(text);
		while(m.find()) {
			String mailStr = m.group();
			
			emails.add(mailStr);
		}
		
		for(String email : emails){
			while(text.contains(email))
				text = text.replace(email, "");
		}
			
		return text;	
	}
	
	private String eraseReferences(String text){
		
		List<String> refs = new ArrayList<String>();
		
		Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+/[a-zA-Z0-9.]*");
		
		Matcher m = pattern.matcher(text);
		while(m.find()) {
			String refStr = m.group();
			
			refs.add(refStr);
		}
		
		for(String ref : refs){
			while(text.contains(ref))
				text = text.replace(ref, "");
		}
			
		return text;	
	}
	
	private String eraseAccounts(String text){
		List<String> accounts = new ArrayList<String>();
		
		Pattern pattern = Pattern.compile("@[a-zA-Z0-9.]*");
		
		Matcher m = pattern.matcher(text);
		while(m.find()) {
			String accStr = m.group();
			accounts.add(accStr);
		}
		
		for(String account : accounts){
			while(text.contains(account))
				text = text.replace(account, "");
		}
			
		return text;	
	}
	
	private void formStemWords(){
		Set<String> firstStemCollection = new HashSet<String>();
		
		//only from keywords
		for(String key : popularKeywords.keySet()){
			firstStemCollection.add(MorphaStemmer.stemToken(key));
		}
		
		for(String stemWord : firstStemCollection){
			stemWords.add(stemWord.replaceAll("[^a-zA-Z0-9#][^a-zA-Z0-9#]*",""));
		}
		Set<String> wordsToRemove = new HashSet<String>();
		//remove one-char strings and non-english words
		for(String stemWord : stemWords){
			if(stemWord.length() == 1)
				wordsToRemove.add(stemWord);
		}
		for(String rWord : wordsToRemove){
			stemWords.remove(rWord);
		}
	}
	
	private void sortElements(){
		Set<String> alreadyChecked = new HashSet<String>();
		
		int index = 0;
		int[] keywordsWeights = new int[popularKeywords.size()];
		for(Integer weight : popularKeywords.values()){
			keywordsWeights[index] = weight;
			index++;
		}
		
		index = 0;
		int[] hashtagsWeights = new int[popularHashtags.size()];
		for(int weight : popularHashtags.values()){
			hashtagsWeights[index] = weight;
			index++;
		}
		
		QuickSort quickSort = new QuickSort(); 
		
		quickSort.sort(keywordsWeights);
		keywordsWeights = quickSort.getResults();
		
		quickSort.sort(hashtagsWeights);
		hashtagsWeights = quickSort.getResults();
		
		//Compute averages - deviations
		if(keywordsWeights == null){
			keywordsAVG = 0;
			keywordsDev = 0;
		}
		else{
			keywordsAVG = Calculator.computeAverage(keywordsWeights);
			keywordsDev = Math.sqrt(Calculator.computeVariance(keywordsWeights,keywordsAVG));
			
			index = 0;
			for(int i=keywordsWeights.length-1;i>0;i--){
				for(Map.Entry<String, Integer> entry : popularKeywords.entrySet()){
					if(entry.getValue() == keywordsWeights[i] && !alreadyChecked.contains(entry.getKey())){
						
						rankedKeywords.put(entry.getKey(),entry.getValue().doubleValue());
						
//						rankedKeywords[index][0] = entry.getKey();
//						if(String.valueOf(keywordsWeights[i]) == null)
//							rankedKeywords[index][1] = "0.0";
//						else
//							rankedKeywords[index][1] = String.valueOf(keywordsWeights[i]);
//						alreadyChecked.add(entry.getKey());
//						index++;
					}
				}
			}
		}
			
		
		if(hashtagsWeights == null){
			hashtagsAVG = 0;
			hashtagsDev = 0;
			
		}	
		else{
			hashtagsAVG = Calculator.computeAverage(hashtagsWeights);
			hashtagsDev = Math.sqrt(Calculator.computeVariance(hashtagsWeights,hashtagsAVG));
			
			index = 0;
			alreadyChecked.clear();
			for(int i=hashtagsWeights.length-1;i>0;i--){
				for(Map.Entry<String, Integer> entry : popularHashtags.entrySet()){
					if(entry.getValue() == hashtagsWeights[i] && !alreadyChecked.contains(entry.getKey())){
						
						rankedHashtags.put(entry.getKey(), entry.getValue().doubleValue());
						
//						rankedHashtags[index][0] = entry.getKey();
//						rankedHashtags[index][1] = String.valueOf(hashtagsWeights[i]);
//						alreadyChecked.add(entry.getKey());
//						index++;
					}
				}
			}
		}
		
		
		
		
		
		
		
	}
	
	public void printKeywordsANDHashtags(){
		System.out.println("----Keywords----");
		for(Map.Entry<String, Integer> entry : popularKeywords.entrySet()){
			System.out.println(entry.getKey()+":"+entry.getValue());

		}
		
		System.out.println("----Hashtags----");
		for(Map.Entry<String, Integer> entry : popularHashtags.entrySet()){
			System.out.println(entry.getKey()+":"+entry.getValue());

		}
	}
	
	private void processPopularKeywords(){
		for(String pKey : popularKeywords.keySet()){
			for(String checkKey : popularKeywords.keySet()){
				if(!pKey.equals(checkKey) && checkKey.length() > pKey.length()){
					String stemmedWord = MorphaStemmer.stem(checkKey);
					if(pKey.equals(stemmedWord)){
						wordsToReplace.put(checkKey, pKey);
						popularKeywords.put(pKey, popularKeywords.get(pKey) + popularKeywords.get(checkKey));
					}
					else if(checkKey.contains(pKey) && Math.abs((checkKey.length() - pKey.length()))<=2){
						double score = popularKeywords.get(pKey);
						double otherScore = popularKeywords.get(checkKey);
						if(score > otherScore){
							wordsToReplace.put(checkKey, pKey);
							popularKeywords.put(pKey, popularKeywords.get(pKey) + popularKeywords.get(checkKey));
						}
						else{
							wordsToReplace.put(pKey, checkKey);
							popularKeywords.put(checkKey, popularKeywords.get(pKey) + popularKeywords.get(checkKey));
						}
						
					}
				}	
			}
		}
	
		for(String rWord : wordsToReplace.keySet()){
			popularKeywords.remove(rWord);
		}
	}
	
	private void processPopularHashtags(){
		for(String pHash : popularHashtags.keySet()){
			for(String pKey : popularKeywords.keySet()){
				if(pHash.contains(pKey)){
					popularHashtags.put(pHash, popularHashtags.get(pHash)+popularKeywords.get(pKey));
				}
			}
		}
	}
	
	public void printRankedKeywordsANDHashtags(){
		System.out.println("----Ranked Keywords----");
		
		for(Map.Entry<String, Double> entry : rankedKeywords.entrySet())
			System.out.println(entry.getKey()+" , "+entry.getValue());
		
		
		for(Map.Entry<String, Double> entry : rankedHashtags.entrySet())
			System.out.println(entry.getKey()+" , "+entry.getValue());
		
//		for(int i=0;i<rankedKeywords.length;i++){
//		
//			System.out.println(rankedKeywords[i][0]+","+rankedKeywords[i][1]);
//			
//		}
//		System.out.println("----Ranked Hashtags----");
//		for(int i=0;i<rankedHashtags.length;i++){
//			
//			System.out.println(rankedHashtags[i][0]+","+rankedHashtags[i][1]);
//			
//		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
