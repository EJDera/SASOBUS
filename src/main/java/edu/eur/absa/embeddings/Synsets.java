package edu.eur.absa.embeddings;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.eur.absa.Framework;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
//import edu.mit.jwi.item.Synset;
import edu.mit.jwi.item.SynsetID;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.AbstractSynset;
import edu.smu.tspell.wordnet.impl.file.ReferenceSynset;
import edu.smu.tspell.wordnet.impl.file.SynsetFactory;
import edu.smu.tspell.wordnet.impl.file.SynsetPointer;
import edu.smu.tspell.wordnet.impl.file.SynsetTypeConverter;
import edu.smu.tspell.wordnet.impl.file.synset.NounReferenceSynset;
import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;

/**
 * Synsets from TransformTextToSynsets.java are used in Word2Vec and Glove, which get rid of numbers.
 * That is why, here you can find methods to encode and decode synsetIDs to string of letters
 * 
 * @author Ewelina Dera
 *
 */
public class Synsets 
{

	public static void main(String[] args) throws IOException
	{	
		//Yelp
		File f = new File(Framework.EXTERNALDATA_PATH + "/WordNet-3.0/dict");
		System.setProperty("wordnet.database.dir", f.toString());
		WordNetDatabase wordDatabase = WordNetDatabase.getFileInstance();		
		
		String[] synsets = {"hcghccln", "lhfjgmn", "gckhflbn", "cmfkmma", "dkdmlcmv", "khhhljfn", "cffbfkhmn", "gkdkdcgn", "dbddmhfa"};
		int length = synsets.length;
		
		for (int i = 0; i < length; i++)
		{
			String temp = synsets[i];
			String decoded = decodeSynsetID(temp);
			Framework.log("Synset ID is " + decoded);
			
			String[] synonyms = synsetToWords(decoded, wordDatabase);
			ArrayList<String> output = new ArrayList<String>();
			for (int j = 0; j < synonyms.length; j++)
			{
				output.add(synonyms[j]);
			}
			Framework.log("Synonyms are " + output);
		}
		
		
//		//SemCor
//		String path = Framework.EXTERNALDATA_PATH + "WordNet-3.0/dict";
//		URL url = new URL("file", null, path);
//		
//		//Construct the dictionary object and open it
//		IDictionary dict = new Dictionary(url);
//		dict.open();
//		
//		String originalWord = "bbbdcdjhn";
//		List<String> inputWord = synsetToWordsSemCor(originalWord, dict);		
//		Framework.log("Input word is " + inputWord);
//		Framework.log("    " + getGloss(originalWord, dict));
//		
//		String[] synsetEmb = {"cfgjgldbn", "cfmhgdhfn", "bhcflglln", "bhdlmdmkn", "chcdcgbjn", "bdmhddkha", "bjdblkhcn", "bbgdjmdln", "bdbhjdjha", "bdkmlfkba"};
//
//		for (int i = 0; i < synsetEmb.length ; i++)
//		{
//			String temp = synsetEmb[i];
//			List<String> outputWord = synsetToWordsSemCor(temp, dict);
//			Framework.log("Nearest words " + outputWord);
//			Framework.log("    " + getGloss(temp, dict));
//		}		
//		dict.close();
	}
	
	/**
	 * Get a definition from an encoded synsetID
	 * 
	 * @param input encoded synsetID
	 * @param dict  dictionary (WordNet)
	 * @return definition of the synset
	 */
	public static String getGloss(String input, IDictionary dict)
	{
		String synsetID = decodeSemCor(input);			
		ISynsetID ISynsetID = SynsetID.parseSynsetID(synsetID);			
		ISynset synset = dict.getSynset(ISynsetID);
		List<IWord> IWords = synset.getWords();
		String gloss = synset.getGloss();
		return gloss;
	}
	
	/**
	 * Get a list of synonyms from an encoded synsetID
	 * 
	 * @param input encoded synsetID
	 * @param dict  dictionary (WordNet)
	 * @return list of synonyms
	 */
	public static List<String> synsetToWordsSemCor(String input, IDictionary dict)
	{
		String synsetID = decodeSemCor(input);
		Framework.log(synsetID);
		ISynsetID ISynsetID = SynsetID.parseSynsetID(synsetID);			
		ISynset Isynset = dict.getSynset(ISynsetID);
		List<IWord> IWords = Isynset.getWords();
		String gloss = Isynset.getGloss();		
		
		List<String> words = new ArrayList<String>();
		for (int j = 0; j < IWords.size(); j++)
		{
			String tempWord = IWords.get(j).getLemma();
			words.add(tempWord);
		}
		
		return words;
	}
	
	/**
	 * A simple method to encode a synsetID into a string of letters.
	 * Vowels are not used to avoid accidental creation of words.
	 * Therefore, 0 is replaced with b, 1 with c etc.
	 * 
	 * @param synsetID in SemCor format
	 * @return encoded synsetID
	 */
	public static String encodeSemCor(String synsetID)
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
		
		for (int i = 4; i < synsetID.length() - 2; i++)    //First 4 characters are "SID-" so skipping those
		{
			char tempChar = synsetID.charAt(i);
			int tempInt = Character.getNumericValue(tempChar);
			output = output + encoder.get(tempInt);
		}
		
		output = output + synsetID.charAt(synsetID.length() - 1);    //Adding last character		
		return output;
		
	}
	
	/**
	 * A simple method to decode a string of letters into a synsetID
	 * 
	 * @param input encoded synsetID
	 * @return decoded synsetID in SemCor format
	 */
	public static String decodeSemCor(String input)
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
		
		String output = "SID-";
		
		for (int i = 0; i < input.length() - 1; i++)
		{
			String tempChar = input.charAt(i) + "";
			int tempInt = decoder.get(tempChar);
			output = output + Integer.toString(tempInt);
		}
		
		char temp = input.charAt(input.length() - 1);
		output = output + "-" + Character.toUpperCase(temp);
		return output;
		
	}
	
	/**
	 * Get a list of synonyms from an encoded synsetID
	 * 
	 * @param input encoded synsetID
	 * @param dict  dictionary (WordNet)
	 * @return list of synonyms
	 */
	public static String[] synsetToWords(String input, WordNetDatabase wordDatabase)
	{
		String synsetID = decodeSynsetID(input);
		int at = input.indexOf("@");
		String type = input.substring(0, at);
		char typeCode = type.charAt(0);
		String id = input.substring(at + 1);
		int offset = Integer.parseInt(id);
		
		SynsetType synsetType = SynsetTypeConverter.getType(typeCode);		
		SynsetPointer synsetPointer = new SynsetPointer(synsetType, offset);
		
		SynsetFactory synsetFactory = SynsetFactory.getInstance();
		Synset synset = synsetFactory.getSynset(synsetPointer);
		System.out.println(synset.toString());
		String[] output = synset.getWordForms();
		
		return output;
	}
	
	/**
	 * A method enocode a synset ID
	 * 
	 * @param synsetID in WordNet (JAWS) format
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
	 * @param input encoded synset ID
	 * @return decoded synset ID in WordNet (JAWS) format
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
		//Framework.log("  POS is " + pos);
		
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
		
		//Framework.log("  Decoded synset ID is " + output);
		return output;
		
	}

}
