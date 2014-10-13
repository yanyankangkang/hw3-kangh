package edu.cmu.lti.f14.hw3.hw3_kangh.casconsumers;


public class Rank implements Comparable<Rank>{
  private double cosine;
  private int relevance;
  private String text;
  private int id;
  public Rank(){
    
  }
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
  
  public String getText(){
    return text;
  }
  
  public void setCosine(double c){
    this.cosine = c;
  }
  
  public void setRelevanceValue(int r){
    this.relevance = r;
  }
  
  public void setText(String t){
    this.text = t;
  }
  
  public int compareTo(Rank other) {
    if (this.getCosine() == other.getCosine()){
      return other.getRelevanceValue() - this.getRelevanceValue();     
   }
   else{
     return (int) (other.getCosine() - this.getCosine());
   }
  }
  public void setQueryID(Integer Id) {
    this.id = Id;
  }
  
  public double getQueryID() {
    return id;
  }

}
