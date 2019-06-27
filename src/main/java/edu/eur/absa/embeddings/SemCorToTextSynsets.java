package edu.eur.absa.embeddings;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

import edu.eur.absa.Framework;
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

/**
 * A class that transforms the Brown Corpus from SemCor into a text file with all the sentences.
 * 
 * @author Ewelina Dera
 *
 */
public class SemCorToTextSynsets 
{

	public static void main(String[] args) throws IOException 
	{
		//Note that this will use the file instead of the console out so you won't see much 
		Framework.fileInsteadOfConsole();
			
		ArrayList<String> brown = new ArrayList<String>();
		brown.add("brown1");
		brown.add("brown2");
		//brown.add("brownv");
		
		Framework.log("Creating the output files...");
		PrintWriter originalText = new PrintWriter(Framework.EXTERNALDATA_PATH + "originalTextSemCor.txt");    //File with all the sentences from Brown Corpus
		PrintWriter synsetText = new PrintWriter(Framework.EXTERNALDATA_PATH + "synsetTextSemCor.txt");    //File with all the sentences from Brown Corpus --> words are replaced by encoded synset IDs
			
		//Construct the URL to the Wordnet dictionary directory
		String path = Framework.EXTERNALDATA_PATH + "WordNet-3.0/dict";
		URL url = new URL("file", null, path);
		
		//Construct the dictionary object and open it
		IDictionary dict = new Dictionary(url);
		dict.open();
		
		for (int x = 0; x < brown.size(); x++)
		{
			String brownCurrent = brown.get(x);
		
			Framework.log("Getting all the sentences from " + brownCurrent + " corpus...");
			List<ISentence> allSentences = SemCor.getSemCorSentences(brownCurrent);
			
			//For each sentence
			for (int i = 0; i < allSentences.size(); i++)
			{
				ISentence currentSentence = allSentences.get(i);

				List<IWordform> wordsList = new ArrayList<IWordform>();
				wordsList = currentSentence.getWordList();

				//For each word
				for (int j = 0; j < wordsList.size(); j++)
				{
					int sentenceLength = wordsList.size();
					IWordform currentWord = wordsList.get(j);				
					String originalWord = currentWord.getText();    //Word to print in the originalText.txt
					Framework.log("Original word: " + originalWord);
					String wordToPrint = originalWord;    //Word to print in the synsetText.txt

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
							String synsetID = WordNet.getSynset(senseKey, senseNumber, idxWord, dict);
							wordToPrint = Synsets.encodeSemCor(synsetID);
							Framework.log("    Found the following synset: " + synsetID + " and encoded it to: " + wordToPrint);
						}
					}

					boolean lastWord = false;

					//Capitalizing the first word of a sentence
					if (j == 0)
					{
						StringUtils.capitalize(wordToPrint);
						StringUtils.capitalize(originalWord);
					}
					//Adding a dot after the last word of a sentence
					else if (j == sentenceLength - 1)
					{
						lastWord = true;
						wordToPrint = " " + wordToPrint + " .";
						originalWord = " " + originalWord + " .";

					}
					//Adding spaces between words
					else
					{
						wordToPrint = " " + wordToPrint;
						originalWord = " " + originalWord;
					}

					//Saving the word/synset in a file --> each sentence starts in a new line
					if (lastWord)
					{
						originalText.println(originalWord);
						synsetText.println(wordToPrint);
					}
					else
					{
						originalText.print(originalWord);
						synsetText.print(wordToPrint);
					}
				}
			}
		}
		
		originalText.close();
		synsetText.close();
		dict.close();
	}

}
