package eu.socialsensor.sfc.builder.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.socialsensor.framework.client.search.solr.SolrDyscoHandler;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.sfc.builder.ranking.TrendsRanker;

public class TrendsRankerEvaluator {
	
	long twoHourPeriod = 60000 * 60 * 2;
	long fifteenMinutePeriod = 60000 * 15;
	long thirtyMinutePeriod = 60000 * 30;
	long oneHourPeriod = 60000 * 60;
	
	Date date,gmtDate;
	String formattedDate;
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	SolrDyscoHandler solrDyscoHandler;
	
	TrendsRanker trendsRanker;
	
	List<Dysco> odyscos_ = new ArrayList<Dysco>();
	
	public TrendsRankerEvaluator(String dyscosSolrCollection, String newsSolrCollection) {
				
		this.solrDyscoHandler = SolrDyscoHandler.getInstance(dyscosSolrCollection);
		this.trendsRanker = new TrendsRanker(newsSolrCollection);
	}
	
	public void processData() {
		
		long lastTimeChecked = System.currentTimeMillis() - twoHourPeriod;
		while(true) {
			
			System.out.println("Going idle again");
			while(Math.abs(System.currentTimeMillis() - lastTimeChecked) < twoHourPeriod) {
				System.out.println("Sleep");
				try {
					Thread.sleep(5*60*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Going to check again");
			lastTimeChecked = System.currentTimeMillis();
			
			//15-minutes dyscos
			date = new Date(System.currentTimeMillis()-fifteenMinutePeriod);
			gmtDate = cvtToGmt(date);
			formattedDate = dateFormat.format(gmtDate);
			
			odyscos_ = solrDyscoHandler.findDyscosInTimeframe(formattedDate).getResults();
			
			//writeOriginalDataToFile(odyscos_, "fifteenMinutes");
			//writeRankedDataToFile(odyscos_, "fifteenMinutes");
			
			odyscos_.clear();
			
			//30-minutes dyscos
			date = new Date(System.currentTimeMillis()-thirtyMinutePeriod);
			gmtDate = cvtToGmt(date);
			formattedDate = dateFormat.format(gmtDate);
			
			odyscos_ = solrDyscoHandler.findDyscosInTimeframe(formattedDate).getResults();
			
			//writeOriginalDataToFile(odyscos_, "thirtyMinutes");
			//writeRankedDataToFile(odyscos_, "thirtyMinutes");
			
			odyscos_.clear();
			
			//1-hour dyscos
			date = new Date(System.currentTimeMillis()-oneHourPeriod);
			gmtDate = cvtToGmt(date);
			formattedDate = dateFormat.format(gmtDate);
			
			odyscos_ = solrDyscoHandler.findDyscosInTimeframe(formattedDate).getResults();
			
			//writeOriginalDataToFile(odyscos_,"oneHour");
			//writeRankedDataToFile(odyscos_,"oneHour");
			
			odyscos_.clear();
			
			//2-hour dyscos
			date = new Date(System.currentTimeMillis()-twoHourPeriod);
			gmtDate = cvtToGmt(date);
			formattedDate = dateFormat.format(gmtDate);
			
			odyscos_ = solrDyscoHandler.findDyscosInTimeframe(formattedDate).getResults();
			
			//writeOriginalDataToFile(odyscos_, "twoHours");
			//writeRankedDataToFile(odyscos_, "twoHours");
			
			odyscos_.clear();
			
		}
		
	}
	
	private void writeRankedDataToFile(List<Dysco> dyscos, String timeLog, String timeFrame) {
 
		int index = 1;
		sortByRankerScore(dyscos);
		
        //String timeLog = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(Calendar.getInstance().getTime());
        File file = new File("TrendsRanker Evaluation/Ranked/Ranked_"+timeLog+"_"+timeFrame);
        if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
  
        int count = 0;	
    	FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			
			BufferedWriter bw = new BufferedWriter(fw);

	        for(Dysco dysco : dyscos) {
	        	if(count>=20) {
	        		break;
	        	}
	        	
	        	if(dysco.getRankerScore() <= 0)
	        		continue;
	        	
	        	String line = (index++)+". "+dysco.getTitle()+"   ["+dysco.getId()+"]";
	        	//String bottomLine = "		Score: " + dysco.getNormalizedRankerScore();
	        	String bottomLine = "		Score: " + (dysco.getNormalizedRankerScore()/2. 
	        			+ dysco.getNormalizedDyscoScore()/2.);
	        	try {
	        		bw.write(line);
	                bw.write("\n");
	                bw.write(bottomLine);
		    		count++;	
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	        	bw.write("\n");	
	        }
			bw.close();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	
	public void writeOriginalDataToFile(List<Dysco> dyscos, String timeLog, String timeFrame) {
		int index = 1;
       
        //String timeLog = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(Calendar.getInstance().getTime());
        File file = new File("TrendsRanker Evaluation/Original/Original_" + timeLog+"_" + timeFrame);
        
        if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        int count = 0;
        FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			for(Dysco dysco : dyscos){
				if(count>=20){
					break;
				}
        		String line = (index++)+". "+dysco.getTitle()+"   ["+dysco.getId()+"]";
        		String bottomLine = "		Dysco Score: "+dysco.getScore();
                try {
                	bw.write(line);
                	bw.write("\n");
                	bw.write(bottomLine);
	    			count++;
    				
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
                bw.write("\n");
        	}
			bw.close();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private  Date cvtToGmt( Date date ) {
	    TimeZone tz = TimeZone.getDefault();
	    Date ret = new Date( date.getTime() - tz.getRawOffset() );

	    // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
	    if ( tz.inDaylightTime( ret )){
	        Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );

	        // check to make sure we have not crossed back into standard time
	        // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
	        if ( tz.inDaylightTime( dstDate )){
	            ret = dstDate;
	        }
	     }
	     return ret;
	}
	
	
	private void sortByRankerScore(List<Dysco> dyscos) {
		Collections.sort(dyscos, new Comparator<Dysco>() {
			@Override
			public int compare(Dysco d1, Dysco d2) {
				double score1 = (d1.getNormalizedDyscoScore() + d1.getNormalizedRankerScore())/2.;
				double score2 = (d2.getNormalizedDyscoScore() + d2.getNormalizedRankerScore())/2.;
				if(score1>score2)
					return -1;
				
				//if(d1.getNormalizedRankerScore()>d2.getNormalizedRankerScore())
				//	return -1;
				
				return 1;
			}
			
		});
	}
	
	public void run() {
		
		long baseTime = 1407251928757l;
		for(int i=0; i<5; i++) {

			Date end = new Date(baseTime);
			Date start = new Date(baseTime - oneHourPeriod);
			
			String started = dateFormat.format(cvtToGmt(start));
			String ended = dateFormat.format(cvtToGmt(end));
			
			odyscos_ = solrDyscoHandler.findDyscosInTimeframe(started, ended).getResults();
			
			trendsRanker.evaluateDyscosByContent(odyscos_, "3");
			
			//writeOriginalDataToFile(odyscos_, started, "thirtyMinutes");
			writeRankedDataToFile(odyscos_, started, "oneHour");
			
			odyscos_.clear();
			baseTime -= oneHourPeriod;
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String dyscosSolrCollection = "http://xxx.xxx.xxx.xxx/solr/dyscos";
		String newsSolrCollection = "http://xxx.xxx.xxx.xxx/solr/NewsFeed";
		
		TrendsRankerEvaluator evaluator = new TrendsRankerEvaluator(dyscosSolrCollection, newsSolrCollection);
		evaluator.run();
	}

}