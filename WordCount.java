
public class WordCount implements Comparable{
	private String thisWord = "";
	private int count;
	private double termFrequency = 0;
	private double score = -1;

	public WordCount(String pThisWord){
		thisWord = pThisWord;
		count = 1;
	}

	public void setScore(double val){
		score = val;
	}

	public double getScore(){
		return score;
	}

	public void calcTermFrequency(int total){
		termFrequency = (double)count/ (double)total;
	}

	public double getTermFrequency(){
		return termFrequency;
	}

	public String getWord(){
		return thisWord;
	}

	public int getCount() {
		return count;
	}

	public void setCount() {
		count++;
	}

	public int compareTo(Object inWord) {
		WordCount temp = (WordCount)inWord;
		int inCount = temp.getCount();
		return (this.getCount()-inCount);
	}
}
