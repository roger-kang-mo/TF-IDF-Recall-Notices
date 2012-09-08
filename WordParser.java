import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import jxl.*;
import jxl.read.biff.BiffException;


public class WordParser {

	private ArrayList<Doc> docs = new ArrayList<Doc>();
	private int summarySpecifier;


	public static void main(String[] args){
			new WordParser();
	}

	public WordParser(){

		int count = 3000;

		System.out.println(count + " documents scanned.\n");

		System.out.println("Defect Summary");
		docs = loadFile("NHTSA_DATA_5.xlsx", count);
		summarySpecifier = 0;
		printResults(runtfidf());

		docs.clear();
		System.out.println("\n");
		System.out.println("Consequence Summary");
		summarySpecifier++;
		docs = loadFile("NHTSA_DATA_5.xlsx", count);
		printResults(runtfidf());

		docs.clear();
		System.out.println("\n");
		System.out.println("Corrective Summary");
		summarySpecifier++;
		docs = loadFile("NHTSA_DATA_5.xlsx", count);
		printResults(runtfidf());

	}	

	private void printResults(WordCount[] topWords){
		for(int i = 0; i < topWords.length; i++){
			System.out.println("\t" +topWords[i].getWord() + "(" + topWords[i].getScore() + ")");

		}

		docs.clear();
	}


	private ArrayList<Doc> loadFile(String fName, int countTo){

		ArrayList<Doc> retVal = new ArrayList<Doc>();

		try {
			Workbook wb = Workbook.getWorkbook(new File(fName));

			Sheet sheet1 = wb.getSheet(0);

			for(int i = 1; i < countTo; i++){
				Doc tempDoc = new Doc(sheet1.getCell(summarySpecifier, i).getContents().trim());
				retVal.add(tempDoc);
				if(tempDoc.isEmpty()){
					retVal.remove(retVal.indexOf(tempDoc));
				}
			}

		} catch (BiffException e) {
			System.out.println("Error");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("ERROR!");
			e.printStackTrace();
		}

		return retVal;

	}

	private WordCount[] runtfidf(){
		WordCount[] topWords = new WordCount[3];
		for(int k = 0; k < 3; k++)
			topWords[k] = new WordCount("");
		int occurrence;
		double score, lowestTopScore = 0.0;
		for(Doc d:docs){
			WordCount[] temp = d.getWords();

			for(int i = 0; i < temp.length; i++){
				occurrence = getOccurrenceCount(temp[i].getWord());
				score = Math.log((double)docs.size()/(double)occurrence) * temp[i].getTermFrequency();
				temp[i].setScore(score);
				if(i < 3){
					if(i==1)
						lowestTopScore = temp[i].getScore();
					topWords[i] = temp[i];
					if(topWords[i].getScore() < lowestTopScore)
						lowestTopScore = topWords[i].getScore();
				}
				else{
					if(score > lowestTopScore && notAlreadyIn(topWords, temp[i].getWord())){
						for(int j = 0; j < 3; j++){
							if(topWords[j].getCount() == lowestTopScore){
								topWords[j] = temp[i];
								break;
							}
						}
						lowestTopScore = topWords[findLowest(topWords)].getScore();
					}
				}
			}
		}

		return topWords;
	}

	private boolean notAlreadyIn(WordCount[] wordList, String word){
		boolean retVal = true;

		for(int i = 0; i < wordList.length && retVal; i++){
			if(wordList[i].getWord().equalsIgnoreCase(word))
				retVal = false;

		}

		return retVal;
	}

	private int findLowest(WordCount[] inList){
		int lowest = inList[0].getCount(), index = 0;
		for(int i = 0; i < inList.length; i++)
			if(inList[i].getCount() < lowest){
				lowest = inList[i].getCount();
				index = i;
			}	
		return index;

	}

	private int getOccurrenceCount(String word){
		int retVal = 0;
		for(Doc d: docs){
			if(d.contains(word))
				retVal++;
		}
		return retVal;
	}

	public class Doc{
		private WordCount[] words = new WordCount[0];
		private int shift;

		public Doc(String inLine){
			inLine= inLine.replaceAll("[^A-Za-z0-9]", " ");
			String[] data = inLine.split("\\s+");

			shift = 1;

			words = getWordsAndCounts(data);
			
			Arrays.sort(words);
			words = chopUp(words);

		}

		private WordCount[] getWordsAndCounts(String[] wordsList){
			WordCount[] temp = new WordCount[wordsList.length];

			for(int k = 0; k < 10 && k < wordsList.length; k ++){
				if(wordsList[k].length() == 0){
					wordsList[k] = " ";
				}
			}
			String tempWord = "";
			int count = 0;
			String tWord = "";

			for(int i = 0; i < wordsList.length; i++){
				tWord = wordsList[i];
				if(!(tWord.length() <= 2) &&
						!tWord.equalsIgnoreCase("the") &&
						!tWord.equalsIgnoreCase("not") &&
						!tWord.equalsIgnoreCase("for") &&
						!tWord.equalsIgnoreCase("from") &&
						!tWord.equalsIgnoreCase("have") &&
						!tWord.equalsIgnoreCase("may") &&
						!tWord.equalsIgnoreCase("and")){
					if(!tempWord.equalsIgnoreCase(wordsList[i])){
						temp[count] = new WordCount(wordsList[i]);
						tempWord = wordsList[i];
						count++;
					}
					else{
						temp[count-1].setCount();
					}	
				}
			}	

			WordCount[] retVal= new WordCount[count];

			for(int i = 0; i < count; i++){
				retVal[i] = temp[i];
				retVal[i].calcTermFrequency(wordsList.length);
			}
			
			//Arrays.sort(retVal);

			return retVal;
		}

		private WordCount[] chopUp(WordCount[] inArray){
			WordCount[] retVal;
			int chop = inArray.length/3;
			retVal = new WordCount[chop];
			for(int i= 0; i < chop; i++){
				if((i+chop+shift >= 0)&&(i+chop+shift < inArray.length))
					retVal[i] = inArray[i+chop+shift];
			}

			return retVal;
		}

		public boolean contains(String term){
			boolean retVal = false;

			for(int i = 0; i < words.length && !retVal; i++){
				if(words[i].getWord().equalsIgnoreCase(term))
					retVal = true;
			}


			return retVal;
		}

		public boolean isEmpty(){
			boolean retVal = true;

			if(words != null)
				for(int i = 0; i < words.length; i++){
					if(words[i]!=null)
						if(words[i].getWord().length()>0){
							retVal = false;
						}
				}

			return retVal;
		}

		public WordCount[] getWords(){
			return words;
		}

	}

}
