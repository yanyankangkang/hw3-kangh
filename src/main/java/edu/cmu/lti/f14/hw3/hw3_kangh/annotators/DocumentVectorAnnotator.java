package edu.cmu.lti.f14.hw3.hw3_kangh.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f14.hw3.hw3_kangh.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_kangh.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_kangh.utils.Utils;
/**
 * 
 * @author given by task1
 *  
 */
public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {
 
  /**
   *  read each document or query once.
   * @param jcas store each document 
   */
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}
		
	}
	/**
	 * first tokenize words from each document, then count term fequency of each document(sentence)
	 * @param jcas save all relevants information such as tf, word, tokenlist of one document 
	 * @param doc Type define in the typesystem
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {
    
		String docText = doc.getText();
		// construct a vector of tokens and update the tokenList in CAS
		String[] tokens = tokenize0(docText);
		HashMap <String, Integer> table = new HashMap <String, Integer> ();
		ArrayList<Token> tokenList = new ArrayList<Token>();
	//	Double norm = 0.0;
		for (String word : tokens){
		   if (table.containsKey(word)){
		     table.put(word, table.get(word) + 1);
		   }
		   else{
		     table.put(word, 1);
		   }
		}
	/*	for (String word : table.keySet()){
		  norm += table.get(word);
		}
		norm = Math.sqrt(norm);
	*/
		for (String word : table.keySet()){
		  Token tk = new Token(jcas);
		  tk.setFrequency(table.get(word));
		  tk.setText(word);
		  tokenList.add(tk);
		}
		  doc.setTokenList(Utils.fromCollectionToFSList(jcas, tokenList));	
	}
	

/** A basic white-space tokenizer, it deliberately does not split on punctuation!
  *
  * @param doc input text
  * @return    a list of tokens.
  */

 private String[] tokenize0(String doc) {
   
  return doc.split("\\s+");
 }

}
