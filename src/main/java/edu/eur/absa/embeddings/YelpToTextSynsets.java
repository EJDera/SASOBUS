package edu.eur.absa.embeddings;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.eur.absa.Framework;
import edu.eur.absa.data.DatasetJSONReader;
import edu.eur.absa.external.ReasoningOntology;
import edu.eur.absa.model.Dataset;
import edu.eur.absa.model.Span;
import edu.eur.absa.model.Word;
import edu.eur.absa.nlp.OntologyLookup;
import edu.eur.absa.seminarhelper.WordSenseDisambiguation;
import edu.smu.tspell.wordnet.Synset;

/**
 * A class which converts the Yelp dataset into 'ordinary' text and text where words are replaced with synset IDs
 * Each sentence starts in a new line
 * 
 * @author Ewelina Dera
 *
 */
public class YelpToTextSynsets 
{
	public static void main(String[] args) throws Exception
	{
		// Note that this will use the file instead of the console out so you won't see much
		Framework.fileInsteadOfConsole();
		
		Framework.log("Creating the output files...");
		PrintWriter originalText = new PrintWriter(Framework.EXTERNALDATA_PATH + "originalTextYelp.txt");    //File with all the sentences/reviews from Yelp
		PrintWriter synsetText = new PrintWriter(Framework.EXTERNALDATA_PATH + "synsetTextYelp.txt");    //File with all the sentences/reviews from Yelp
		PrintWriter synsetText1 = new PrintWriter(Framework.EXTERNALDATA_PATH + "synsetTextYelp1.txt"); 
		
		Framework.log("Reading the Yelp JSON file...");
		Dataset Yelp =  (new DatasetJSONReader()).read(new File(Framework.EXTERNALDATA_PATH+"yelp_academic_dataset_review_restaurant_auto5001.json"));
		
		TreeSet<Span> reviews = Yelp.getSpans("review");
		
		//For each review
		for (Span review : reviews)
		{
			//Framework.log("Review " + review.toString());
			TreeSet<Span> sentences = Yelp.getSpans(review, "sentence");
			
			//For each sentence
			for (Span sentence : sentences)
			{
				//Framework.log("Sentence " + sentence.toString());
				String currentSentence = sentence.toString();
				String[] sentenceArray = stringToArray(currentSentence);
				TreeSet<Word> words = sentence.getWords();
				Framework.log("Words " + words);
				
				//Just getting rid of the weird sentences e.g. consisting only of a dot
				if (words.size() == 1)
				{
					String temp = words.first().toString();
					if (temp.equals("!") || temp.equals(".") || temp.equals(",") || temp.equals("?") || temp.equals("-") || temp.equals(";") || temp.equals(":") || temp.equals(")") || temp.equals("(") || temp.equals("*"))
					{
						continue;
					}
				}
				
				int counter = 0;
				String previousWord = "";
				String currentWord = "";
				
				//For each word
				for (Word word : words)
				{					
					boolean condition = false;
					previousWord = currentWord;
					Framework.log("Word " + word.toString());
					counter++;
					String originalWord = word.toString();
					currentWord = originalWord;
					
					//Framework.log("Previous word is " + previousWord);
					//Framework.log("Current word is " + currentWord);
					
					//Just getting rid of "\n" which sometimes precedes sentences
					if (originalWord.equals("\\") || originalWord.equals("n"))
					{
						continue;
					}
					
					if (previousWord.equals("\\") && originalWord.substring(0,  1).equals("n"))
					{
						originalWord = originalWord.substring(1);
						condition = true;
					}
					
					if (originalWord.substring(0, 1).equals(" "))
					{
						originalWord = originalWord.substring(1);
						condition = true;
					}
					if (word.toString().contains("*"))
					{
						int tempCounter = 0;
						for (int m = 0; m < word.toString().length(); m++)
						{
							if (word.toString().substring(m, m + 1).equals("*"))
							{
								tempCounter++;
							}
						}
						
						if (tempCounter == word.toString().length())
						{
							continue;
						}
					}
					
					String synsetWord = originalWord;
					String synsetWord1 = originalWord;
					String pos = word.getAnnotation("pos");
					//Framework.log("  POS is " + pos);
					boolean conditionPOS = false;
					
					if (pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") ) 
					{
						pos = "noun";
						conditionPOS = true;
					} 
					else if (pos.equals("JJ") || pos.equals("JJR") || pos.equals("JJS")) 
					{
						pos = "adjective";
						conditionPOS = true;
					} 
					else if (pos.equals("VB") || pos.equals("VBD") || pos.equals("VBG") || pos.equals("VBN") || pos.equals("VBN") || pos.equals("VBP")|| pos.equals("VBZ")) 
					{
						pos = "verb";
						conditionPOS = true;
					}
					
					Framework.log("  POS is " + pos);					
					
					//Skipping synsets for punctuation marks
					if (originalWord.equals("!") || originalWord.equals(".") || originalWord.equals(",") || originalWord.equals("?") || originalWord.equals("-") || originalWord.equals(";") || originalWord.equals(":") || originalWord.equals("(") || originalWord.equals(")") || originalWord.equals("*"))
					{
					}
					else
					{
						int senseNumber = WordSenseDisambiguation.Sense(sentenceArray, originalWord, pos);
						Synset synset = null;
						if (conditionPOS)
						{
							synset = WordSenseDisambiguation.findSynset(sentenceArray, originalWord, pos);
						}
						
						if (condition)
						{
							synsetWord = originalWord.toLowerCase() + "#" + pos + "#" + encodeSenseNumber(senseNumber);
						}
						else
						{
							synsetWord = word.getLemma() + "#" + pos + "#" + encodeSenseNumber(senseNumber);
						}
						
						if (synset == null)
						{
							synsetWord1 = originalWord;
						}
						else
						{
							String ss = synset.toString();
							int bracket = ss.indexOf("[");
							int at = ss.indexOf("@");
							synsetWord1 = ss.substring(0, bracket).toLowerCase();
							Framework.log("  Synset ID is " + synsetWord1);
							synsetWord1 = encodeSynsetID(synsetWord1);
						}
						
						Framework.log("  Sense number is " + senseNumber);
						//Framework.log("  Synset ID is " + synsetWord1);
					}
					
					
					Framework.log("  Synset word is " + synsetWord);
					Framework.log("  Synset word1 is " + synsetWord1);
					Framework.log("  Original word is " + originalWord);
					
					
					//Capitalizing the first word of a sentence
					if (counter == 1)
					{
						StringUtils.capitalize(originalWord);
					}
					//Adding spaces between words
					else
					{
						synsetWord = " " + synsetWord;
						synsetWord1 = " " + synsetWord1;
						originalWord = " " + originalWord;
					}

					//Saving the word/synset in a file --> each sentence starts in a new line
					if (counter == words.size())
					{
						originalText.println(originalWord);
						synsetText.println(synsetWord);
						synsetText1.println(synsetWord1);
					}
					else
					{
						originalText.print(originalWord);
						synsetText.print(synsetWord);
						synsetText1.print(synsetWord1);
					}
				}
			}
		}
		
		originalText.close();
		synsetText.close();
		synsetText1.close();
	
	}
	
	/**
	 * A method enocode a synset ID
	 * 
	 * @param synsetID decoded synset ID
	 * @return encoded synset ID
	 */
	public static String encodeSynsetID(String synsetID)
	{
		HashMap<Integer, String> encoder = new HashMap<Integer, String>();
		encoder.put(0, "b");
		encoder.put(1, "c");
		encoder.put(2, "d");
		encoder.put(3, "f");
		encoder.put(4, "g");
		encoder.put(5, "h");
		encoder.put(6, "j");
		encoder.put(7, "k");
		encoder.put(8, "l");
		encoder.put(9, "m");
		
		String output = "";
		
		int at = synsetID.indexOf("@");
		
		for (int i = at + 1; i < synsetID.length(); i++)
		{
			char tempChar = synsetID.charAt(i);
			int tempInt = Character.getNumericValue(tempChar);
			output = output + encoder.get(tempInt);
		}
		
		output = output + synsetID.charAt(0);
		
		return output;
		
	}
	
	/**
	 * A method to decode a synset ID
	 * 
	 * @param encoded synset ID
	 * @return decoded synset ID
	 */
	public static String decodeSynsetID(String input)
	{
		HashMap<String, Integer> decoder = new HashMap<String, Integer>();
		decoder.put("b", 0);
		decoder.put("c", 1);
		decoder.put("d", 2);
		decoder.put("f", 3);
		decoder.put("g", 4);
		decoder.put("h", 5);
		decoder.put("j", 6);
		decoder.put("k", 7);
		decoder.put("l", 8);
		decoder.put("m", 9);
		
		String output = "";
		
		String pos = input.charAt(input.length() - 1) + "";
		
		if (pos.equals("n"))
		{
			output = "noun";
		}
		else if (pos.equals("v"))
		{
			output = "verb";
		}
		else if (pos.equals("a"))
		{
			output = "adjective";
		}
		
		output = output + "@";
		
		
		
		for (int i = 0; i < input.length() - 1; i++)
		{
			String tempChar = input.charAt(i) + "";
			int tempInt = decoder.get(tempChar);
			output = output + Integer.toString(tempInt);
		}
		return output;
		
	}
	
	/**
	 * A method which transforms a string into an array of words
	 * 
	 * @param sentence Input sentence to be split into words
	 * @return array of string, where each element is a word
	 */
	public static String[] stringToArray(String sentence)
	{
		String[] words = sentence.split("\\s");
		return words;
	}
	
	/**
	 * A method to count the number of digits in an integer
	 * 
	 * @param integer input number whose digits will be counted
	 * @return number of digits
	 */
	public static int numberOfDigits(int integer)
	{
		int number = integer;
		int count = 0;
		if (number == 0)
		{
			count = 1;
		}
		else
		{
			while (number > 0)
			{
				number = number / 10;
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * A method which encodes a sense number into a letter
	 * 
	 * @param senseNumber to be encoded
	 * @return string representing a sense number
	 */
	public static String encodeSenseNumber(int senseNumber)
	{
	
		HashMap<Integer, String> encoder = new HashMap<Integer, String>();
		encoder.put(0, "a");
		encoder.put(1, "b");
		encoder.put(2, "c");
		encoder.put(3, "d");
		encoder.put(4, "e");
		encoder.put(5, "f");
		encoder.put(6, "g");
		encoder.put(7, "h");
		encoder.put(8, "i");
		encoder.put(9, "j");
		
		String output = "";
		
		for (int i = 0; i < numberOfDigits(senseNumber); i++)
		{
			int temp = Integer.parseInt(Integer.toString(senseNumber).substring(i, i + 1));
			output = output + encoder.get(temp);
		}
		
		return output;
	}
	
	/**
	 * A method to decode a string back into a sense number
	 * 
	 * @param encodedSense string which represents the encoded sense number
	 * @return sense number
	 */
	public static int decodeSenseNumber(String encodedSense)
	{
		HashMap<String, Integer> decoder = new HashMap<String, Integer>();
		decoder.put("a", 0);
		decoder.put("b", 1);
		decoder.put("c", 2);
		decoder.put("d", 3);
		decoder.put("e", 4);
		decoder.put("f", 5);
		decoder.put("g", 6);
		decoder.put("h", 7);
		decoder.put("i", 8);
		decoder.put("j", 9);
		
		String output = "";
		
		for (int i = 0; i < encodedSense.length(); i++)
		{
			output = output + decoder.get(encodedSense.substring(i, i + 1));
		}
		
		int senseNumber = Integer.parseInt(output);
		return senseNumber;
		
	}
}
