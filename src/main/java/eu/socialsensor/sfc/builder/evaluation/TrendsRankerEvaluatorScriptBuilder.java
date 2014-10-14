package eu.socialsensor.sfc.builder.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TrendsRankerEvaluatorScriptBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String listId = "3";
		
		String max_dir = "/disk1_data/workspace/git/socialsensor-query-builder/TrendsRanker Evaluation/List "+listId+"/Max";
		String original_dir = "/disk1_data/workspace/git/socialsensor-query-builder/TrendsRanker Evaluation/List "+listId+"/Original";
		String current_dir = "/disk1_data/workspace/git/socialsensor-query-builder/TrendsRanker Evaluation/List "+listId+"/Current";
		String combined_dir = "/disk1_data/workspace/git/socialsensor-query-builder/TrendsRanker Evaluation/List "+listId+"/Combined";
		
        File _mdir = new File(max_dir);
        File[] max = _mdir.listFiles();
        
        File _cdir = new File(current_dir);
        File[] current = _cdir.listFiles();
        
        File _odir = new File(original_dir);
        File[] original = _odir.listFiles();
        
        File _cmbdir = new File(combined_dir);
        File[] combined = _cmbdir.listFiles();
        
        
        File a_rank_file = new File("./TrendsRanker Evaluation/List "+listId+"/A-Rankings.txt");
        File b_rank_file = new File("./TrendsRanker Evaluation/List "+listId+"/B-Rankings.txt");
        File c_rank_file = new File("./TrendsRanker Evaluation/List "+listId+"/C-Rankings.txt");
        File d_rank_file = new File("./TrendsRanker Evaluation/List "+listId+"/D-Rankings.txt");
        File score_file = new File("./TrendsRanker Evaluation/List "+listId+"/Scores.txt");
        
        FileWriter fw1 = null;
        FileWriter fw2 = null;
        FileWriter fw3 = null;
        FileWriter fw4 = null;
        
		FileWriter fw5 = null;
		
		try {
			fw1 = new FileWriter(a_rank_file.getAbsoluteFile());
			fw2 = new FileWriter(b_rank_file.getAbsoluteFile());
			fw3 = new FileWriter(c_rank_file.getAbsoluteFile());
			fw4 = new FileWriter(d_rank_file.getAbsoluteFile());
			
			fw5 = new FileWriter(score_file.getAbsoluteFile());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
		BufferedWriter bw1 = new BufferedWriter(fw1);
		BufferedWriter bw2 = new BufferedWriter(fw2);
		BufferedWriter bw3 = new BufferedWriter(fw3);
		BufferedWriter bw4 = new BufferedWriter(fw4);
		
		BufferedWriter bw5 = new BufferedWriter(fw5);

        if (!a_rank_file.exists()) {
			try {
				a_rank_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        if (!b_rank_file.exists()) {
			try {
				b_rank_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        if (!c_rank_file.exists()) {
			try {
				c_rank_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        if (!d_rank_file.exists()) {
			try {
				d_rank_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        if (!score_file.exists()) {
			try {
				score_file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
        int index_a = 1; 
        for (File f : max) {
            if(f.isFile()) {
                BufferedReader inputStream = null;
                
                String fileName = f.getName().replace("Ranked_", "");
               
                try {
                	bw1.write("["+(index_a)+"]"+fileName);
                	bw5.write("["+(index_a)+"]"+fileName);
                	index_a++;
                    inputStream = new BufferedReader(
                                    new FileReader(f));
                    String line;
                    bw1.write("\n");
                    bw1.write("\n");
                    
                    bw5.write("\n");
                    bw5.write("\n");
                    
                    int numberOfLines = 0;
                    while ((line = inputStream.readLine()) != null) {
                        bw1.write("\t"+line+"\n");
                        bw1.write("\n");
                        numberOfLines++;
                    }
                    bw1.write("\n");
                                        
                    bw5.write("Score:");
                    bw5.write("\n");
                    bw5.write("[A-Rankings]Not newsworthy dyscos: __/"+(numberOfLines/2));
                    bw5.write("\n");
                    bw5.write("[B-Rankings]Not newsworthy dyscos: __/"+(numberOfLines/2));
                    bw5.write("\n");
                    bw5.write("[C-Rankings]Not newsworthy dyscos: __/"+(numberOfLines/2));
                    bw5.write("\n");
                    bw5.write("\n");
                    
                } catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
                finally {
                    if (inputStream != null) {
                        try {
							inputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
                    }
                }
            }
        }
        
        int index_b = 1; 
        for (File f : original) {
        	if(f.isFile()) {
        		BufferedReader inputStream = null;
              
              
              String fileName = f.getName().replace("Original_", "");
              System.out.println("fileName: "+fileName);
              
              try {
              	bw2.write("["+(index_b)+"]"+fileName);
              
              	index_b++;
              	inputStream = new BufferedReader(new FileReader(f));
              	String line;
              	bw2.write("\n");
              	bw2.write("\n");
              
              	int numberOfLines = 0;
	            while ((line = inputStream.readLine()) != null) {
	            	if(numberOfLines>=40)
	            		break;
	            	bw2.write("\t"+line+"\n");
	            	bw2.write("\n");
	            	numberOfLines++;
	                  
	            }
	            bw2.write("\n");
                                  
             
              
            } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            finally {
             if (inputStream != null) {
                  try {
                	  inputStream.close();
				  } catch (IOException e) {
					  e.printStackTrace();
				  }
             }
            }
          }
		}
        
        int index_c = 1; 
        for (File f : current) {
        	if(f.isFile()) {
        		BufferedReader inputStream = null;
                
              String fileName = f.getName().replace("Ranked_", "");
              System.out.println("fileName: "+fileName);
              
              try {
              	bw3.write("["+(index_c)+"]"+fileName);
              
              	index_b++;
              	inputStream = new BufferedReader(new FileReader(f));
              	String line;
              	bw3.write("\n");
              	bw3.write("\n");
              
              	int numberOfLines = 0;
	            while ((line = inputStream.readLine()) != null) {
	            	if(numberOfLines>=40)
	            		break;
	            	bw3.write("\t"+line+"\n");
	            	bw3.write("\n");
	            	numberOfLines++;
	                  
	            }
	            bw3.write("\n");
                                  
             
              
            } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            finally {
             if (inputStream != null) {
                  try {
                	  inputStream.close();
				  } catch (IOException e) {
					  e.printStackTrace();
				  }
             }
            }
          }
		}
        
        int index_d = 1; 
        for (File f : combined) {
        	if(f.isFile()) {
        		BufferedReader inputStream = null;
                
              String fileName = f.getName().replace("Ranked_", "");
              System.out.println("fileName: "+fileName);
              
              try {
              	bw4.write("["+(index_d)+"]"+fileName);
              
              	index_b++;
              	inputStream = new BufferedReader(new FileReader(f));
              	String line;
              	bw4.write("\n");
              	bw4.write("\n");
              
              	int numberOfLines = 0;
	            while ((line = inputStream.readLine()) != null) {
	            	if(numberOfLines>=40)
	            		break;
	            	bw4.write("\t"+line+"\n");
	            	bw4.write("\n");
	            	numberOfLines++;
	                  
	            }
	            bw4.write("\n");
            } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            finally {
             if (inputStream != null) {
                  try {
                	  inputStream.close();
				  } catch (IOException e) {
					  e.printStackTrace();
				  }
             }
            }
          }
		}
        
        try {
			bw1.close();
			bw2.close();
			bw3.close();
			bw3.close();
			
		    bw5.close();
		        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}