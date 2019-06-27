package edu.eur.absa.embeddings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import edu.eur.absa.Framework;
import edu.eur.absa.seminarhelper.WordSenseDisambiguation;
import edu.smu.tspell.wordnet.Synset;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ie.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.io.FileUtils;

import java.util.*;

/**
 * A class which transforms the contrastive corpus (for ontology learning)
 * Words are replaced with their respective (enocoded) synset IDs
 * In order to process a file change its name in readFile() method and change the output file name in main() method
 * 
 * @author Ewelina Dera
 *
 */
public class ContrastiveCorpusToSynsets 
{

	public static void main(String[] args) throws Exception 
	{

		// Note that this will use the file instead of the console out so you won't see much
		Framework.fileInsteadOfConsole();
		
		Framework.log("Reading the input txt file...");
		String text = readFile().toLowerCase();	
		
		Framework.log("NLP pipeline...");		
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");    //Splitting into sentences, POS-tagging and lemmatization
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        
		Framework.log("Creating the output file...");
		PrintWriter synsetText = new PrintWriter(Framework.EXTERNALDATA_PATH + "greatexpectationsSynsets.txt");    //File where the words from a given book are replaced by synsets
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);        
        //For each sentence
        for(CoreMap sentence: sentences) 
        {
        	Framework.log("Sentence is: " + sentence.toString());
        	String[] sentenceArray = stringToArray(sentence.toString());

        	int count = 0;
        	
        	//For each token (word)
        	for (CoreLabel token: sentence.get(TokensAnnotation.class)) 
        	{
        		count++;
        		int sentenceLength = sentence.get(TokensAnnotation.class).size();
        	    //This is the text of the token
        	    String word = token.get(TextAnnotation.class);
        	    Framework.log("  Current word is: " + word);
        	    //This is the lemma of the token
        	    String lemma = token.get(LemmaAnnotation.class);
        	    if (lemma.equals("-lrb-"))
        	    {
        	    	lemma = "(";
        	    }
        	    else if (lemma.equals("-rrb-"))
        	    {
        	    	lemma = ")";
        	    }
        	    else if (lemma.equals("-lsb-"))
        	    {
        	    	lemma = "[";
        	    }
        	    else if (lemma.equals("-rsb-"))
        	    {
        	    	lemma = "]";
        	    }
        	    else if (lemma.equals("-lcb-"))
        	    {
        	    	lemma = "{";
        	    }
        	    else if (lemma.equals("-rcb-"))
        	    {
        	    	lemma = "}";
        	    }
        	    
        	    Framework.log("    Lemma is: " + lemma);
        	    
        	    //This is the POS tag of the token
        	    String pos = token.get(PartOfSpeechAnnotation.class);
        	    Framework.log("    POS is: " + pos);
        	    
				if (pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") ) 
				{
					pos = "noun";
					Framework.log("    New POS is: " + pos);
				} 
				else if (pos.equals("JJ") || pos.equals("JJR") || pos.equals("JJS")) 
				{
					pos = "adjective";
					Framework.log("    New POS is: " + pos);
				} 
				else if (pos.equals("VB") || pos.equals("VBD") || pos.equals("VBG") || pos.equals("VBN") || pos.equals("VBN") || pos.equals("VBP")|| pos.equals("VBZ")) 
				{
					pos = "verb";
					Framework.log("    New POS is: " + pos);
				}
				
				String synsetToPrint = lemma;
				
				Synset synset = null;
				if (pos.equals("noun") || pos.equals("adjective") || pos.equals("verb"))
				{
					synset = WordSenseDisambiguation.findSynset(sentenceArray, lemma, pos);
				}
				
				if (synset != null)
				{
					String temp1 = synset.toString();
					int bracket = temp1.indexOf("[");
					synsetToPrint = temp1.substring(0, bracket);
				}
				
				Framework.log("    Synset is: " + synsetToPrint);
				
				//Last word
				if (sentenceLength == count)
				{
					synsetText.println(synsetToPrint + ".");
				}
				else
				{
					synsetText.print(synsetToPrint + " ");
				}
			}
		}
        
        synsetText.close();
	}	
	
	/**
	 * A method which reads a file into a string
	 * 
	 * @return string representation of a file
	 * @throws IOException
	 */
	public static String readFile() throws IOException 
	{
	    File file = new File(Framework.EXTERNALDATA_PATH + "greatexpectations.txt");
	    return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
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

}
