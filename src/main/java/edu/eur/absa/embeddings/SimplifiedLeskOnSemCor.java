package edu.eur.absa.embeddings;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.eur.absa.Framework;
import edu.eur.absa.seminarhelper.WordSenseDisambiguation;
import edu.mit.jsemcor.element.ISentence;
import edu.mit.jsemcor.element.IWordform;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.data.parse.SenseKeyParser;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.IStemmer;
import edu.mit.jwi.morph.SimpleStemmer;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.eur.absa.Framework;

/**
 * A class which compares SemCor sense and the sense found by Simplified Lesk
 * Consequently, Simplified Lesk's accuracy is calculated
 * 
 * @author Ewelina Dera
 *
 */
public class SimplifiedLeskOnSemCor 
{
	
	
	public static void main(String[] args) throws IOException 
	{
		
		// Note that this will use the file instead of the console out so you won't see much
		Framework.fileInsteadOfConsole();
		
		int correct = 0;
		int incorrect = 0;
		int total = 0;

		ArrayList<String> brown = new ArrayList<String>();
		brown.add("brown1");
		brown.add("brown2");
		//brown.add("brownv");    //Skipping this as only verbs are annotated with senses
		
		//Construct the URL to the Wordnet dictionary directory
		String path = Framework.EXTERNALDATA_PATH + "WordNet-3.0/dict";
		URL url = new URL("file", null, path);
		
		//Construct the dictionary object and open it
		IDictionary dict = new Dictionary(url);
		dict.open();

		Framework.log("Creating the output files...");
		//PrintWriter originalText = new PrintWriter(Framework.EXTERNALDATA_PATH + "originalText.txt");    //File with all the sentences from Brown Corpus
		PrintWriter synsetText = new PrintWriter(Framework.EXTERNALDATA_PATH + "synsetTextSemCorSimplifiedLesk.txt");    //File with all the sentences from Brown Corpus --> words are replaced by synset IDs as identified by Simplified Lesk

		for (int x = 0; x < brown.size(); x++) 
		{
			String brownCurrent = brown.get(x);

			Framework.log("Getting all the sentences from " + brownCurrent + " corpus...");
			List<ISentence> allSentences = SemCor.getSemCorSentences(brownCurrent);

			// For each sentence
			for (int i = 0; i < allSentences.size(); i++) {
				ISentence currentSentence = allSentences.get(i);
				List<IWordform> wordsList = new ArrayList<IWordform>();
				wordsList = currentSentence.getWordList();
				String[] sentence = new String[wordsList.size()];
				
				for (int y = 0; y < wordsList.size(); y++)
				{
					String temp = wordsList.get(y).getText();
					sentence[y] = temp;
				}

				int counter = 0;
				
				// For each word
				for (int j = 0; j < wordsList.size(); j++) 
				{
					counter++;
					int sentenceLength = wordsList.size();
					IWordform currentWord = wordsList.get(j);
					String originalWord = currentWord.getText();
					Framework.log("Original word: " + originalWord);
					String wordToPrint = originalWord; // Word to print in the synsetTextSemCorSimplifiedLesk.txt
					
					String synsetID = null;
					Synset synset = null;
					if (currentWord.getSemanticTag() != null)
					{
						//Getting the synset ID
						String senseKey = currentWord.getSemanticTag().getSenseKeys().get(0);
						ISenseKey iSenseKey = SenseKeyParser.getInstance().parseLine(senseKey);
						POS pos = iSenseKey.getPOS();
						//Stemmer to get base forms of words --> e.g. "dogs" is transformed to "dog"
						List<String> foundStems = new ArrayList<String>();
						IStemmer stemmer = new SimpleStemmer();
						foundStems = stemmer.findStems(originalWord, pos);
						Framework.log("Found stems " + foundStems);
						String posString = StringUtils.capitalize(pos.toString());

						String currWord = "";
						if (foundStems.size() > 0)
						{
							//foundStems list is in the preferred order so choosing the first (most preferred) element here
							currWord = foundStems.get(0);
							Framework.log("Used stem " + currWord);
						}
						else
						{
							currWord = originalWord;
						}
						
						IIndexWord idxWord = dict.getIndexWord(iSenseKey.getLemma(), pos);
						
						if (idxWord != null)
						{
							int senseNumber = currentWord.getSemanticTag().getSenseNumber().get(0);
							Framework.log("    Processed word: " + originalWord + " has sense key: " + senseKey + " and sense number: " + senseNumber);
							synsetID = WordNet.getSynset(senseKey, senseNumber, idxWord, dict);
							Framework.log("    SemCor synset is: " + synsetID);
						}
						else
						{
						}
						
						//Getting the synset from Simplified Lesk
						synset = WordSenseDisambiguation.findSynset(sentence, currWord, posString.toLowerCase());
						Framework.log("    Simplified Lesk synset is: " + synset);
						if (synset != null)
						{
							int bracket = synset.toString().indexOf("]");
							wordToPrint = synset.toString().substring(0, bracket + 1);
						}
					}
					
					//Comparing SemCor and Simplified Lesk synsets
					if (synset != null && synsetID != null)
					{
						if (checkAccuracy(synsetID, synset.toString()))
						{
							correct++;
							//Framework.log("Neither null + true");
						}
						else
						{
							incorrect++;
							//Framework.log("Neither null + false");
						}
					}
					else if (synset == null && synsetID == null)
					{
						correct++;
						//Framework.log("Both null");
					}
					else
					{
						incorrect++;
						//Framework.log("One null");
					}
					total++;
				
					
					//Capitalizing the first word of a sentence
					if (counter == 1)
					{
						StringUtils.capitalize(wordToPrint);
						//StringUtils.capitalize(originalWord);
					}
					//Adding a dot after the last word of a sentence
					else if (counter == wordsList.size())
					{
						wordToPrint = " " + wordToPrint + " .";
						//originalWord = " " + originalWord + " .";

					}
					//Adding spaces between words
					else
					{
						wordToPrint = " " + wordToPrint;
						//originalWord = " " + originalWord;
					}

					//Saving the word/synset in a file --> each sentence starts in a new line
					if (counter == wordsList.size())
					{
						//originalText.println(originalWord);
						synsetText.println(wordToPrint);
					}
					else
					{
						//originalText.print(originalWord);
						synsetText.print(wordToPrint);
					}
				}
			}
		}
		
		Framework.log("Correct " + correct);
		Framework.log("Incorrect " + incorrect);
		Framework.log("Total " + total);
		
		synsetText.close();
		dict.close();
	}
	
	/**
	 * A method which checks whether the SemCor synset is the same as the synset identified by Simplified Lesk
	 * 
	 * @param SemCor synset from SemCor
	 * @param SimplifiedLesk synset from Simplified Lesk algorithm
	 * @return true if the two synsets are the same
	 */
	public static boolean checkAccuracy(String SemCor, String SimplifiedLesk)
	{
		boolean check = false;
		
		//Code to put SemCor synset and Simplified Lesk synset in the same format
		String SemCorPOS = SemCor.substring(SemCor.length() - 1);
		if (SemCorPOS.equals("N"))
		{
			SemCorPOS = "Noun";
		}
		else if (SemCorPOS.equals("V"))
		{
			SemCorPOS = "Verb";
		}
		else if (SemCorPOS.equals("A"))
		{
			SemCorPOS = "Adjective";
		}
		
		int at = SimplifiedLesk.indexOf("@");
		int bracket = SimplifiedLesk.indexOf("[");
		String SimplifiedLeskPOS = SimplifiedLesk.substring(0, at);
		
		//Synset ID is between 'SID-' and '-POS'
		String temp = SemCor.substring(4, SemCor.length() - 2);
		int SemCorID = Integer.parseInt(temp);
		
		//Synset ID is between '@' and '['
		String temp1 = SimplifiedLesk.substring(at + 1, bracket);
		int SimplifiedLeskID = Integer.parseInt(temp1);
		
		if (SemCorPOS.equals(SimplifiedLeskPOS) && SemCorID == SimplifiedLeskID)
		{
			check = true;
			//Framework.log("    True");
		}
		
		return check;
	}
	
}
