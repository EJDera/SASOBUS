package edu.eur.absa.seminarhelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.eur.absa.model.Span;
import edu.eur.absa.model.Word;
import edu.eur.absa.model.exceptions.IllegalSpanException;
import edu.smu.tspell.wordnet.Synset;
import edu.eur.absa.Framework;
import edu.eur.absa.data.DatasetJSONReader;
import edu.eur.absa.model.Dataset;

/**
 * A program for getting the frequency of words per review and per document.
 * 
 * @author Karoliina Ranta
 * Adapted by Ewelina Dera
 * 
 * Code adapted from TermFrequencyCount by team5B
 *
 */
public class TermFrequencyYelp 
{
	public static void main(String[] args) throws IOException, ClassNotFoundException, JSONException, IllegalSpanException{
		getFrequency();
	}

	/**
	 * Loops over all reviews and words to compute the frequency per review and per document 
	 */
	public static void getFrequency() throws ClassNotFoundException, JSONException, IllegalSpanException, IOException {
		/* Read in the training data */
		Dataset yelp =  (new DatasetJSONReader()).read(new File(Framework.EXTERNALDATA_PATH+"yelp_academic_dataset_review_restaurant_auto5001.json"));	
		//Dataset yelp = (new DatasetJSONReader()).read(new File(Framework.DATA_PATH+"SemEval2016SB1Restaurants-Train.json"));

		/* Get all review spans of the data set */
		ArrayList<HashSet<Span>> reviewList = yelp.createSubSets("review", 1.0);
		TreeSet<Span> reviews = new TreeSet<Span>(reviewList.get(0));

		/* Create HashMaps for keeping track of the frequency per review and per document, respectively */
		HashMap<String, HashMap<String, Integer>> wordFrequencyReview = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> wordFrequencyDocument = new HashMap<String, Integer>();

		/* Loop over all reviews in the data set */
		for(Span review : reviews) {
			/* Get the ID number of the current review */
			String reviewID = review.getAnnotation("id", String.class);

			/* HashMap to keep track of the frequency in the current review */
			HashMap<String, Integer> wordFrequency = new HashMap<String, Integer>();

			Span scope = review.getTextualUnit();

			/* Loop over all words in the review */
			for(Word word : scope){
				
				//Code to get synset out of the word
				TreeSet<Span> possibleSpans = yelp.getSpans("sentence", word);
				Span sentence = getSentenceFromWord(word, word, possibleSpans, yelp);
				TreeSet<Word> sent = sentence.getWords();
				
				String[] words = new String[sent.size()];
				Iterator<Word> iter = sent.iterator();
				int x = 0;
				while (iter.hasNext())
				{
					Word temp = iter.next();
					words[x] = temp.getLemma();
					x++;
				}
				
				String pos = word.getPOS();
				if (pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") ) 
				{
					pos = "noun";
				} 
				else if (pos.equals("JJ") || pos.equals("JJR") || pos.equals("JJS")) 
				{
					pos = "adjective";
				} 
				else if (pos.equals("VB") || pos.equals("VBD") || pos.equals("VBG") || pos.equals("VBN") || pos.equals("VBN") || pos.equals("VBP")|| pos.equals("VBZ")) 
				{
					pos = "verb";
				}
				
				Synset synset = WordSenseDisambiguation.findSynset(words, word.getLemma(), pos);
				if (synset != null)
				{
					String ss = synset.toString();
					int bracket = ss.indexOf("[");
					String synsetFinal = ss.substring(0, bracket).toLowerCase();
					
					if (!wordFrequency.containsKey(synsetFinal))
					{
						wordFrequency.put(synsetFinal, 1);
					}
					else
					{
						int countS = wordFrequency.get(synsetFinal);
						wordFrequency.put(synsetFinal, countS+1);
					}
				}
				
				/* Added an if statement to see if the word is already in the HashMap */
				if(!wordFrequency.containsKey(word.getAnnotation("lemma", String.class))) { 
					wordFrequency.put(word.getAnnotation("lemma", String.class), 1);
				}
				else {
					int count = wordFrequency.get(word.getAnnotation("lemma", String.class)); 
					wordFrequency.put(word.getAnnotation("lemma", String.class), count+1); 
				}
				if (word.hasNextWord()) // 2-words
				{
					String multilemma = word.getAnnotation("lemma", String.class)+ " " + word.getNextWord().getAnnotation("lemma", String.class);
					if(!wordFrequency.containsKey(multilemma)) { 
						wordFrequency.put(multilemma, 1); 
					}
					else {
						int count = wordFrequency.get(multilemma); 
						wordFrequency.put(multilemma, count+1); 
					}
				
					if (word.getNextWord().hasNextWord()) //3-words
					{
						String multi_multilemma = multilemma + " "+ word.getNextWord().getNextWord().getAnnotation("lemma", String.class);
						if(!wordFrequency.containsKey(multi_multilemma)) { 
							wordFrequency.put(multi_multilemma, 1); 
						}
						else {
							int count = wordFrequency.get(multi_multilemma); 
							wordFrequency.put(multi_multilemma, count+1); 
						}
						if (word.getNextWord().getNextWord().hasNextWord()) {//4-words
							String multi_lemma = multi_multilemma + " " +word.getNextWord().getNextWord().getNextWord().getAnnotation("lemma", String.class);
							if(!wordFrequency.containsKey(multi_lemma)) { 
								wordFrequency.put(multi_lemma, 1); 
							}
							else {
								int count = wordFrequency.get(multi_lemma); 
								wordFrequency.put(multi_lemma, count+1); 
							}
						}
						
					}
				}
				
					
			}

			wordFrequencyReview.put(reviewID, wordFrequency);

			/* Loop over all present words in the current review */ 
			for(String key : wordFrequency.keySet()) {
				if(!wordFrequencyDocument.containsKey(key)) {
					wordFrequencyDocument.put(key, wordFrequency.get(key));
				}
				else {
					int count = wordFrequencyDocument.get(key);
					count += wordFrequency.get(key);
					wordFrequencyDocument.put(key, count);	
				}
			}
		}

		/* Write the frequencies to JSON files */
		JSONWordFreqWriter(wordFrequencyReview, new File(Framework.EXTERNALDATA_PATH+"wordFrequencyReviewYelpAuto5001_multiwords_Synsets.json"));
		JSONDocFreqWriter(wordFrequencyDocument, new File(Framework.EXTERNALDATA_PATH+"wordFrequencyDocumentYelpAuto5001_multiwords_Synsets.json"));
	}

	public static HashMap<String, HashMap<String, HashMap<String, Integer>>> getFrequency(String lemma) throws ClassNotFoundException, JSONException, IllegalSpanException, IOException {
		/* Read in the training data */
		Dataset yelp =  (new DatasetJSONReader()).read(new File(Framework.EXTERNALDATA_PATH+"yelp_academic_dataset_review_restaurant_auto5001.json"));	


		/* Get all review spans of the data set */
		ArrayList<HashSet<Span>> reviewList = yelp.createSubSets("review", 1.0);
		TreeSet<Span> reviews = new TreeSet<Span>(reviewList.get(0));

		/* Create HashMaps for keeping track of the frequency per review and per document, respectively */
		HashMap<String, HashMap<String, Integer>> wordFrequencyReview = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> wordFrequencyDocument = new HashMap<String, Integer>();
		wordFrequencyDocument.put(lemma, 0);

		/* Loop over all reviews in the data set */
		for(Span review : reviews) {
			/* Get the ID number of the current review */
			String reviewID = review.getAnnotation("id", String.class);

			/* HashMap to keep track of the frequency in the current review */
			HashMap<String, Integer> wordFrequency = new HashMap<String, Integer>();
			wordFrequency.put(lemma, 0);

			Span scope = review.getTextualUnit();
			String reviewText = scope.toString().toLowerCase();
			/* Loop over all words in the review */
			int index = reviewText.indexOf(lemma);
			while (index != -1) {
				int count = wordFrequency.get(lemma); 
				wordFrequency.put(lemma, count+1); 
			    reviewText = reviewText.substring(index + 1);
			    index = reviewText.indexOf(lemma);
			}
			wordFrequencyReview.put(reviewID, wordFrequency);

			/* Add wordFrequency to total frequency over all reviews*/ 

			int count = wordFrequencyDocument.get(lemma);
			count += wordFrequency.get(lemma);
			wordFrequencyDocument.put(lemma, count);	

		}

		/*cast wordFrequencyDocument to be in same form as wordFrequencyReview */
		HashMap<String, HashMap<String, Integer>> wordFrequencyDocumentCast = new HashMap<String, HashMap<String,Integer>>();
		wordFrequencyDocumentCast.put("wordFrequencyDocument", wordFrequencyDocument);
		/* return the frequencies */
		HashMap<String, HashMap<String, HashMap<String, Integer>>> wordFrequencies = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
		wordFrequencies.put("wordFrequencyDocumentCast", wordFrequencyDocumentCast);
		wordFrequencies.put("wordFrequencyReview", wordFrequencyReview);
		return wordFrequencies;
	}
	/**
	 * Writes the word frequencies of all reviews to a JSON file
	 * @param wordFrequencyReview HashMap with all words and frequencies per review
	 * @param file
	 * @throws IOException
	 */
	public static void JSONWordFreqWriter(HashMap<String, HashMap<String, Integer>> wordFrequencyReview, File file) throws IOException {
		JSONObject wordFrequencyReviewArray = new JSONObject();

		/* Loop over all reviews*/
		for (String revID : wordFrequencyReview.keySet()) {
			JSONObject words = new JSONObject();

			/* Loop over all present words in the current review */
			for (String word : wordFrequencyReview.get(revID).keySet()) {
				words.put(word, wordFrequencyReview.get(revID).get(word));
			}

			wordFrequencyReviewArray.put(revID, words);
		}

		Framework.debug("Writing "+file+" ....");
		BufferedWriter out;
		out = new BufferedWriter(new FileWriter(file));
		out.write(wordFrequencyReviewArray.toString());
		out.close();
	}

	/**
	 * Write the word frequencies of the document to a JSON file
	 * @param docFrequencyReview HashMap with all words and frequencies
	 * @param file
	 */
	public static void JSONDocFreqWriter(HashMap<String, Integer> docFrequencyReview, File file) throws IOException{
		JSONObject word = new JSONObject();

		/* Loop over all words */
		for(String key : docFrequencyReview.keySet()){
			word.put(key, docFrequencyReview.get(key));
		}

		Framework.debug("Writing "+file+" ....");
		BufferedWriter out;
		out = new BufferedWriter(new FileWriter(file));
		out.write(word.toString());
		out.close();
	}	
	
	/**
	 * A method needed for WSD of Yelp. This method returns a Span 'sentence' for a given Word
	 * 
	 * @param wordP previous word
	 * @param wordN next word
	 * @param possibleSpans a set of possible
	 * @param yelp dataset used
	 * @return Span 'sentence'
	 */
	public static Span getSentenceFromWord(Word wordP, Word wordN, TreeSet<Span> possibleSpans, Dataset yelp)
	{
		//If only one possible sentence
		if (possibleSpans.size() == 1 || (!wordP.hasPreviousWord() && !wordN.hasNextWord()))
		{
			return possibleSpans.first();
		}
		else
		{
			//Look at the list of sentences containing the given word. Then check which of those sentences also contains previous and next word
			Word previous = wordP;
			Word next = wordN;
			TreeSet<Span> spans = possibleSpans;
			if (wordN.hasNextWord() && wordP.hasPreviousWord())
			{
				TreeSet<Span> spansNext = new TreeSet<Span>();
				TreeSet<Span> spansPrevious = new TreeSet<Span>();
				previous = wordP.getPreviousWord();
				next = wordN.getNextWord();
				spansNext.addAll(yelp.getSpans("sentence", next));
				spansPrevious.addAll(yelp.getSpans("sentence", previous));
				spans.retainAll(spansPrevious);
				spans.retainAll(spansNext);
			}
			else if (wordN.hasNextWord() && !wordP.hasPreviousWord())
			{
				TreeSet<Span> spansNext = new TreeSet<Span>();
				next = wordN.getNextWord();
				spansNext.addAll(yelp.getSpans("sentence", next));
				spans.retainAll(spansNext);
				
			}
			else if (!wordN.hasNextWord() && wordP.hasPreviousWord())
			{
				TreeSet<Span> spansPrevious = new TreeSet<Span>();
				previous = wordP.getPreviousWord();
				spansPrevious.addAll(yelp.getSpans("sentence", previous));
				spans.retainAll(spansPrevious);
			}
				
			return getSentenceFromWord(previous, next, spans, yelp);    //Recursive call	
		}
	}
		

}
