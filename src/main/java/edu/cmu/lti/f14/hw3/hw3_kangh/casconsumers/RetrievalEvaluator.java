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

import edu.cmu.lti.f14.hw3.hw3_kangh.casconsumers.Rank;
import edu.cmu.lti.f14.hw3.hw3_kangh.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_kangh.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_kangh.utils.Utils;
/**
 * 
 * @author mac
 * @version 1.0 Build on Oct 22, 2014.
 * Store processed data in persisting file outside UIMA framework during pipeline
 * When process completed, reading all data from files to do a final calculation of MRR 
 */
public class RetrievalEvaluator extends CasConsumer_ImplBase {
  /**
   * count number of documents relavant to each query 
   */
  public ArrayList<Integer> groupList;
  /**
   * file name of storing in intermediate file
   */
  private static String outputPath = "src/main/resources/data/pd.txt";

  private Writer fWriter = null;

  private BufferedReader fReader = null;

  private static int sQUERY = 99;

  private static int sIRREL = 0;

  private static int sREL = 1;

  private static int id, num;

  public void initialize() throws ResourceInitializationException {


    groupList = new ArrayList<Integer>();

    id = -1;
    num = 0;

    File outputFile = new File(outputPath);
    System.out.println(outputFile);
    try {
      fWriter = new BufferedWriter(new FileWriter(outputFile));
    } catch (IOException e) {
      //Auto-gvenerated catch block
      e.printStackTrace();
    }
  }

  /**
   * 1. construct the global word dictionary 2. keep the word frequency for each sentence
   * 
   * using extra one grouplist to distinguish each query, record vectors and documents in a .txt
   * file, word and frequency in one sentence delimited by blank and each two by blank. and each
   * sentence by "\n"
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator<Annotation> it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();

      // Make sure that your previous annotators have populated this in CAS
      /*******************************************************/
      // test
      /*
       * FSList fsTokenList = doc.getTokenList(); ArrayList <Token> tokenList =
       * Utils.fromFSListToCollection(fsTokenList, Token.class); System.out.println(doc.getText());
       * for (Token word : tokenList){ System.out.println(word.getText() + " " +
       * word.getFrequency()); } System.out.println(); System.out.println();
       */
      // qIdList.add(doc.getQueryID());
      // relList.add(doc.getRelevanceValue());
      try {
        fWriter.write(doc.getQueryID() + "\n");
        fWriter.write(doc.getRelevanceValue() + "\n");
      } catch (IOException e2) {
        // TODO Auto-generated catch block
        e2.printStackTrace();
      }

      if (id == -1) {
        id = doc.getQueryID();
      } else {
        if (id != doc.getQueryID()) {
          id = doc.getQueryID();
          groupList.add(num);
          num = 0;
        } else {
          num++;
        }
      }
      // Do something useful here

      try {
        fWriter.write(doc.getText() + "\n");
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      ArrayList<Token> tokenList = Utils.fromFSListToCollection(doc.getTokenList(), Token.class);
      for (Token word : tokenList) {
        try {
          // System.out.println(word.getText());
          fWriter.write(word.getText() + " " + word.getFrequency() + '\t');
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
   * 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);
    fWriter.close();
    groupList.add(num);
    // Iterator<Integer> qIdIter = qIdList.listIterator();
    // Iterator<Integer> relIter = relList.listIterator();
    URL pdUrl = RetrievalEvaluator.class.getResource("/data/pd.txt");
    if (pdUrl == null) {
      throw new IllegalArgumentException("Error opening data/pd.txt");
    }
    fReader = new BufferedReader(new InputStreamReader(pdUrl.openStream()));
    File outputFile = new File("src/main/resources/data/report.txt");
    fWriter = new BufferedWriter(new FileWriter(outputFile));
    // HashMap<Document, Token> queryVector = new HashMap<Document, Token>();
    // HashMap<Document, Token> docVector = new HashMap<Document, Token>();
  //  File eaFile = new File("src/main/resources/data/errorAnalysis.txt");
  //  BufferedWriter fw = new BufferedWriter(new FileWriter(eaFile));
    /** item to calculate MRR **/
    ArrayList<Rank> mrrList = new ArrayList<Rank>();
    for (Integer N : groupList) {
      Rank r[] = new Rank[N];
      // int Id = qIdIter.next();
      // int rel = relIter.next();
      // System.out.println("*********");
      int id = Integer.parseInt(fReader.readLine().trim());
      int rel = Integer.parseInt(fReader.readLine().trim());
      assert rel == sQUERY;
      fReader.readLine(); // Read document
      String tokenList = fReader.readLine();
      HashMap<String, Integer> queryVector = Rank.decompose(tokenList);
      for (int i = 0; i < N; i++) {
        r[i] = new Rank();
        r[i].setQueryID(Integer.parseInt(fReader.readLine().trim()));
        r[i].setRelevanceValue(Integer.parseInt(fReader.readLine().trim()));
        r[i].setText(fReader.readLine());
        r[i].setTokenList(fReader.readLine());
      }
      Rank.naiveCosine(r, queryVector);
      // compute the rank of retrieved sentences
      Arrays.sort(r);
      // System.out.println(r.length);
    //  errorAnalysis(r, queryVector, fw);
      
      for (int i = 0; i < r.length; i++) {
        // System.out.println(r[i].getRelevanceValue());
        if (r[i].getRelevanceValue() == 1) {
          String s = "cosine=" + String.format("%.4f", r[i].getCosine()) + "\t" + "rank=" + (i + 1)
                  + "\t" + "qid=" + id + "\t" + "rel=" + r[i].getRelevanceValue() + "\t"
                  + r[i].getText();
          fWriter.write(s + "\n");
          r[i].setQueryID(id);
          r[i].setRank(i + 1);
          mrrList.add(r[i]);
          break;
        }
      }
    }
   // fw.close();
    // compute the metric:: mean reciprocal rank
    double metric_mrr = Rank.compute_Mrr(mrrList);
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
    fWriter.write("MRR=" + String.format("%.4f", metric_mrr));
    fWriter.close();
  }
  
  /*private void errorAnalysis(Rank r[], HashMap<String, Integer>query, BufferedWriter fw) throws IOException{
   
    fw.append("query" + r[0].getQueryID() + ":  ");
    for(String word : query.keySet()){
      if (query.containsKey(word)){
        fw.append(word + " " + query.get(word) + "|");
      }
    }
    fw.append("\n");
    for (int i = 0; i < r.length; i++){
      fw.write(i + ".  ");
     HashMap<String, Integer> doc = Rank.decompose(r[i].getTokenList());
     fw.append("rel=" + r[i].getRelevanceValue() + "  ");
        for(String word : doc.keySet()){
          if (query.containsKey(word)){
            fw.append(word + " " + doc.get(word) + "|");
          }
        }
        fw.append("\n");
    }
    fw.append("\n");
  }
   */
}
