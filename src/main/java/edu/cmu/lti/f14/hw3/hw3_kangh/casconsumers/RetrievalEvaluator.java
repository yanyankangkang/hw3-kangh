package edu.cmu.lti.f14.hw3.hw3_kangh.casconsumers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;








import edu.cmu.lti.f14.hw3.hw3_kangh.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_kangh.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_kangh.utils.Utils;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	/** number of documents relevant to queries including queries **/ 
	public ArrayList<Integer> groupList;
	
	/** item to calculate MRR **/
	public ArrayList<Rank> mrrList;
	
	private static String outputPath = "src/main/resources/data/pd.txt";
//	private BufferedWriter fWriter = null;
	private Writer fWriter = null;
	private BufferedReader fReader = null;
	 // Scanner fReader = null;
	 private static int sQUERY = 99; 
	  private static int sIRREL = 0;
	  private static int sREL = 1;
	  private static int id, num;
	
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		groupList = new ArrayList<Integer>();
		
		mrrList = new ArrayList<Rank>();
		
		//File outputFile = new File((String) getConfigParameterValue("outputPath"));
	
		id = -1;
		num = 0;
		
	   File outputFile = new File(outputPath);
     System.out.println(outputFile);
      try {
         fWriter = new BufferedWriter(new FileWriter(outputFile));
         System.out.println("******");
       // URL outUrl = RetrievalEvaluator.class.getResource("persistingData.txt");
       // fWriter = 
       } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } 
	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence 
	 * 
	 *   using extra one grouplist to distinguish each query, record vectors and documents 
	 *   in a .txt file,  word and frequency in one sentence delimited by blank and each two by 
	 *   blank. and each sentence by "\n" 
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}
		
		FSIterator<Annotation> it = jcas.getAnnotationIndex(Document.type).iterator();
	
		if (it.hasNext()) {
			Document doc = (Document) it.next();

			//Make sure that your previous annotators have populated this in CAS
			/*******************************************************/ //test
			/*FSList fsTokenList = doc.getTokenList();
			ArrayList <Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
			System.out.println(doc.getText());
			for (Token word : tokenList){
			  System.out.println(word.getText() + " " + word.getFrequency());
			}
			System.out.println();
			System.out.println();
			*/
			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			if (id  ==  -1 ){
			  id = doc.getQueryID();
			}
			else{
			   if (id != doc.getQueryID()){
			      id = doc.getQueryID();
			      groupList.add(num);
			      num = 0;
			   }
			   else{
			     num++;
			   }
			}
			//Do something useful here
		
			try {
        fWriter.write(doc.getText() + "\n");
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(doc.getTokenList(), Token.class);
			for (Token word : tokenList){
			  try {
			//    System.out.println(word.getText());
          fWriter.write(word.getText() + " "  +word.getFrequency() + '\t');
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
			}
			try {
        fWriter.write('\n');
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
			 
		}

	}

	/**
	 * 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {
    
		super.collectionProcessComplete(arg0);
		fWriter.close();
		groupList.add(num);
		Iterator<Integer> qIdIter = qIdList.listIterator();
		Iterator<Integer> relIter = relList.listIterator();
		URL pdUrl = RetrievalEvaluator.class.getResource("/data/pd.txt"); 
	  if (pdUrl == null) {
      throw new IllegalArgumentException("Error opening data/documents.txt");
   }
     fReader = new BufferedReader(new InputStreamReader(pdUrl.openStream()));
		//File inputFile = new File((String) getConfigParameterValue("inputPath"));
		//File outputFile = new File((String) getConfigParameterValue("report.txt"));
   // fReader = new Scanner(inputFile);
     File outputFile = new File("src/main/resources/data/report.txt");
     fWriter = new BufferedWriter(new FileWriter(outputFile));
	//	HashMap<Document, Token> queryVector = new HashMap<Document, Token>();
	//	HashMap<Document, Token> docVector = new HashMap<Document, Token>();

		for (Integer N : groupList){
		  Rank r[] = new Rank[N];
		  int Id = qIdIter.next();
		  int rel = relIter.next();
		  assert rel == sQUERY;
		  fReader.readLine();
		  HashMap<String, Double> queryVector = decompose();
		  for(int i = 0; i < N; i++){ 
		    r[i] = new Rank();
		    r[i].setText(fReader.readLine());
		    HashMap<String, Double> docVector = decompose();  
		 // compute the cosine similarity measure
		    double similarity = computeCosineSimilarity(queryVector, docVector);
		    r[i].setCosine(similarity);
		    r[i].setQueryID(qIdIter.next());
		    r[i].setRelevanceValue(relIter.next());
		    System.out.println(r[i].getQueryID() + " " + r[i].getRelevanceValue() + " " + r[i].getText());
		  }
	    // compute the rank of retrieved sentences
		   Arrays.sort(r); 
		 //  System.out.println(r.length);
		   for (int i = 0; i < r.length; i++){
		     
		    // System.out.println(r[i].getRelevanceValue());
		     if (r[i].getRelevanceValue() == 1){
           String s = "cosine=" + r[i].getCosine() + "\t" + "rank=" + (i+1) + "\t" + 
		     "qid=" + Id + "\t" + "rel=" + r[i].getRelevanceValue() + "\t" + r[i].getText();
		       fWriter.write(s + "\n"); 
		       r[i].setQueryID(i + 1);
		       mrrList.add(r[i]);
		       break;
		     }
		   }  
		}
		// compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
		fWriter.write(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
		fWriter.close();
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	private double computeCosineSimilarity(Map<String, Double> queryVector,
			Map<String, Double> docVector) {
		double cosine_similarity=0.0;
		// compute cosine similarity between two sentences
		for (String word : queryVector.keySet()){
		  cosine_similarity += queryVector.get(word) * (docVector.get(word) == null? 0 : docVector.get(word));
		}
		return cosine_similarity;
	}

	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;
		// compute Mean Reciprocal Rank (MRR) of the text collection
		int length = mrrList.size();
		for (Rank r : mrrList){
		 metric_mrr += 1 / r.getQueryID() / length;
		}
		return metric_mrr;
	}
	/** return the word from .txt 
	 * @throws IOException **/
	private HashMap<String, Double> decompose() throws IOException{
	  HashMap<String, Double> vector = new HashMap<String, Double>();
    String sentence = fReader.readLine();
   // System.out.println("!"+ sentence);
    String tokens[] = sentence.split("\\t");
    for (String phrase : tokens){
         String info[] = phrase.split(" ");
         vector.put(info[0], Double.parseDouble(info[1]));
    }
    return vector;
	}

}
