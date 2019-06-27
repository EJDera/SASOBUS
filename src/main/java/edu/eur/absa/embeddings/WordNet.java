package edu.eur.absa.embeddings;

import edu.eur.absa.Framework;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import edu.mit.jwi.*;
import edu.mit.jwi.item.*;
import edu.mit.jwi.data.parse.*;

import edu.mit.jsemcor.element.*;

/**
 * A class with different methods for WordNet
 * 
 * @author Ewelina Dera
 *
 */
public class WordNet 
{

	/**
	 * A method to get a WordNet synsetID based on sense key and sense number
	 * 
	 * @param senseKey sense key number of the given word (from SemCor)
	 * @param senseNumber sense number of the given word (from SemCor)
	 * @param idxWord index word with a base form of a given word (from SemCor)
	 * @param dict WordNet
	 * @return synsetID according to WordNet
	 * @throws IOException
	 */
	public static String getSynset(String senseKey, int senseNumber, IIndexWord idxWord, IDictionary dict) throws IOException
	{
		IWordID wordID = null;
		
		if (senseNumber >= 1)
		{
			wordID = idxWord.getWordIDs().get(senseNumber - 1);
		}
		else
		{
			wordID = idxWord.getWordIDs().get(senseNumber);
		}
		
		IWord word = dict.getWord(wordID);
		String synsetID = word.getSynset().getID().toString();

		return synsetID;
	}

}
