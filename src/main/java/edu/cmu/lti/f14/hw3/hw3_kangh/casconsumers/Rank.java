package edu.cmu.lti.f14.hw3.hw3_kangh.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.lti.f14.hw3.hw3_kangh.casconsumers.Rank;

/**
 * @author KangHuang
 * @version 1.0 Build on Oct 22, 2014.
 * encapsulate all functions and variables used in similarity score calculations
 */
public class Rank implements Comparable<Rank>{
  private double cosine;
  private int relevance;
  private String text;
  private int id;
  private int rank;
  private String tokenList;
  public Rank(){
    
  }
  /**
   * (deprecated)
   * @param c
   * @param r
   * @param t
   */
  public Rank(double c, int r, String t){
    this.cosine = c;
    this.relevance = r;
    this.text = t;  
  }
  
  public double getCosine(){
    return cosine;
  }
  
  public int getRelevanceValue(){
    return relevance;
  }
  /**
   * get original text of each document
   * @return original document text
   */
  public String getText(){
    return text;
  }
  
  public void setCosine(double c){
    this.cosine = c;
  }
  
  public void setRelevanceValue(int r){
    this.relevance = r;
  }
  /**
   *  store original text of each document
   * @param original text
   */
  public void setText(String t){
    this.text = t;
  }
  
  /**
   * implements interface of Comparable
   * the first key is cosineSimilarity, the second key is relValue
   */
  public int compareTo(Rank other) {
    if (this.getCosine() == other.getCosine()){
      if( other.getRelevanceValue() - this.getRelevanceValue() > 0){
        return 1;
      }
      else{
        return -1;
      }
   }
   else{
     if  (other.getCosine() - this.getCosine() > 0){
       return 1;
     }
     else{
       return -1;
     }
   }
  }
  public void setQueryID(Integer Id) {
    this.id = Id;
  }
  
  public double getQueryID() {
    return id;
  }
  
  
  public void setRank(Integer r) {
    this.rank = r;
  }
  
  public double getRank() {
    return rank;
  }
  
  /**
   * all tokens are stored in one string 
   * @param String 
   */
  public void setTokenList(String t) {
    this.tokenList = t;
  }
  /**
   * get all tokens in form of one string splitted by blank
   * @return
   */
  public String getTokenList() {
    return this.tokenList;
  }
  /**
   * calculate cosine similarity using term frequency as weight without any multipliers
   * @param r all documents 
   * @param queryVector build a query dictionary whose key word is words in query, and value is term frequency
   *
   */
  public static void naiveCosine(Rank r[], HashMap<String, Integer> queryVector){
    for (int i = 0; i < r.length; i++) {
      HashMap<String, Integer> docVector = decompose(r[i].getTokenList());
      // compute the cosine similarity measure
      double similarity = computeCosineSimilarity(queryVector, docVector);
      r[i].setCosine(similarity);
      // System.out.println(r[i].getQueryID() + " " + r[i].getRelevanceValue() + " " +
      // r[i].getText());
    }
  }
  /**
   * calculate basic normalized cosine similarity 
   * @return cosine_similarity
   */
  private static double computeCosineSimilarity(Map<String, Integer> queryVector,
      Map<String, Integer> docVector) {
    double cosine_similarity=0.0;
    // compute cosine similarity between two sentences
    double queryVectorNorm = norm(queryVector);
    double docVectorNorm = norm(docVector);
    for (String word : queryVector.keySet()){
      cosine_similarity += (queryVector.get(word) * (docVector.get(word) == null? 0 : docVector.get(word)) / (queryVectorNorm * docVectorNorm) );
    }
    return cosine_similarity;
  }
 /**
  * calculate normalized weight vector
  * @param vector unnormalized weight vector 
  * @return normalized vector in form of HashMap
  */
  private static double norm(Map<String, Integer> vector){
    double sum = 0;
    for (String word : vector.keySet()){
      int tf = vector.get(word);
      sum += tf * tf;
    }
    return Math.sqrt(sum);
  }
  
  /**
   * calculate MRR by averaging ranks of rel=1 all documents.
   * @return mrr
   */
  public static double compute_Mrr(ArrayList<Rank> mrrList) {
    double metric_mrr=0.0;
    // compute Mean Reciprocal Rank (MRR) of the text collection
    int length = mrrList.size();
    for (Rank r : mrrList){
     metric_mrr += 1 / r.getRank();
    }
    return metric_mrr / length;
  }
  /** transform  data structure of tokenList from String to HashMap
   *  
   *  @param String
   *  @return HashMap
   */
  public static HashMap<String, Integer> decompose(String sentence){
    HashMap<String, Integer> vector = new HashMap<String, Integer>();
  //  System.out.println("!"+ sentence);
    String tokens[] = sentence.split("\\t");
    for (String phrase : tokens){
         String info[] = phrase.split(" ");
         vector.put(info[0], Integer.parseInt(info[1]));
    }
    return vector;
  }
  

}
