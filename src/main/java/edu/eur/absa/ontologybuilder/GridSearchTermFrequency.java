package edu.eur.absa.ontologybuilder;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.json.JSONException;
import org.json.JSONObject;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.file.Morphology;
import edu.smu.tspell.wordnet.impl.file.SynsetFactory;
import edu.smu.tspell.wordnet.impl.file.SynsetPointer;
import edu.smu.tspell.wordnet.impl.file.SynsetTypeConverter;
import edu.eur.absa.Framework;
import edu.eur.absa.data.DatasetJSONReader;
import edu.eur.absa.model.Dataset;
import edu.eur.absa.model.Relation;
import edu.eur.absa.model.Span;
import edu.eur.absa.model.Word;
import edu.eur.absa.model.exceptions.IllegalSpanException;
import edu.eur.absa.seminarhelper.Synonyms;
import edu.eur.absa.seminarhelper.TermFrequencyYelp;
import edu.eur.absa.seminarhelper.SeminarOntology;
import edu.eur.absa.seminarhelper.WordSenseDisambiguation;
import edu.eur.absa.seminarhelper.Wu_Palmer;
import edu.eur.absa.seminarhelper.readJSON;
import edu.eur.absa.nlp.*;
import edu.smu.tspell.wordnet.WordNetDatabase.*;
import edu.cmu.lti.ws4j.*;

/**
 * A class that finds terms (synsets) and their frequencies so that they can be re-used for grid search
 * 
 * @author Ewelina Dera
 */
public class GridSearchTermFrequency 
{
	private Dataset reviewData;
	private JSONObject wordFrequencyReview;
	private JSONObject wordFrequencyDocument;
	private HashMap<String, HashMap<String, Integer>> contrastData;


	public GridSearchTermFrequency() 
	{
	}


	/**
	 * A method that loads the external reviews in order to be handled.
	 */
	public void loadReviews() throws Exception {

		Framework.log("Loading Yelp review data");

		/* Read in the restaurant review data. */
		reviewData =  (new DatasetJSONReader()).read(new File(Framework.EXTERNALDATA_PATH+"yelp_academic_dataset_review_restaurant_auto5001.json"));	

		/* Load the review and document frequencies of all appearing words. */
		try {
			this.wordFrequencyReview = readJSON.readJSONObjectFile(Framework.EXTERNALDATA_PATH + "wordFrequencyReviewYelpAuto5001_multiwords_Synsets.json");
		} catch (ClassNotFoundException | JSONException | IOException e) {
			e.printStackTrace();
		}
		try {
			this.wordFrequencyDocument = readJSON.readJSONObjectFile(Framework.EXTERNALDATA_PATH + "wordFrequencyDocumentYelpAuto5001_multiwords_Synsets.json");
		} catch (ClassNotFoundException | JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method that loads the external alternate domain documents.
	 */
	public void loadContrast() {

		Framework.log("Loading contrastive corpus");

		Framework.log("Loading Alice in Wonderland");

		/* Load Alice in Wonderland. */
		File alice = new File(Framework.EXTERNALDATA_PATH+"alice30Synsets.txt");

		/* Loop over all the words and keep track of their frequencies. */
		HashMap<String, Integer> aliceFreq = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(alice);
			sc.useDelimiter("[\\p{Punct}\\s&&[^@]]+");
			while (sc.hasNext()) {
				String word = sc.next().toLowerCase();

				/* Update the frequency of the word. */
				if (aliceFreq.containsKey(word)) {
					aliceFreq.put(word, aliceFreq.get(word) + 1);

				} else  {
					aliceFreq.put(word, 1);
				}
			}
			sc.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		contrastData = new HashMap<String, HashMap<String, Integer>>();
		this.contrastData.put("Alice", aliceFreq);

		Framework.log("Loading Pride and Prejudice");

		/* Load Pride and Prejudice. */
		File pride = new File(Framework.EXTERNALDATA_PATH+"prideandprejudiceSynsets.txt");

		/* Loop over all the words and keep track of their frequencies. */
		HashMap<String, Integer> prideFreq = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(pride);
			sc.useDelimiter("[\\p{Punct}\\s&&[^@]]+");
			while (sc.hasNext()) {
				String word = sc.next().toLowerCase();

				/* Update the frequency of the word. */
				if (prideFreq.containsKey(word)) {
					prideFreq.put(word, prideFreq.get(word) + 1);

				} else  {
					prideFreq.put(word, 1);
				}
			}
			sc.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		contrastData = new HashMap<String, HashMap<String, Integer>>();
		this.contrastData.put("Pride", prideFreq);

		Framework.log("Loading Sherlock Holmes");

		/* Load Sherlock Holmes. */
		File sherlock = new File(Framework.EXTERNALDATA_PATH+"sherlockholmesSynsets.txt");

		/* Loop over all the words and keep track of their frequencies. */
		HashMap<String, Integer> sherlockFreq = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(sherlock);
			sc.useDelimiter("[\\p{Punct}\\s&&[^@]]+");
			while (sc.hasNext()) {
				String word = sc.next().toLowerCase();

				/* Update the frequency of the word. */
				if (sherlockFreq.containsKey(word)) {
					sherlockFreq.put(word, sherlockFreq.get(word) + 1);

				} else  {
					sherlockFreq.put(word, 1);
				}
			}
			sc.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		contrastData = new HashMap<String, HashMap<String, Integer>>();
		this.contrastData.put("Sherlock", sherlockFreq);

		Framework.log("Loading Tom Sawyer");

		/* Load Tom Sawyer. */
		File tom = new File(Framework.EXTERNALDATA_PATH+"tomsawyerSynsets.txt");

		/* Loop over all the words and keep track of their frequencies. */
		HashMap<String, Integer> tomFreq = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(tom);
			sc.useDelimiter("[\\p{Punct}\\s&&[^@]]+");
			while (sc.hasNext()) {
				String word = sc.next().toLowerCase();

				/* Update the frequency of the word. */
				if (tomFreq.containsKey(word)) {
					tomFreq.put(word, tomFreq.get(word) + 1);

				} else  {
					tomFreq.put(word, 1);
				}
			}
			sc.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		contrastData = new HashMap<String, HashMap<String, Integer>>();
		this.contrastData.put("Tom", tomFreq);

		Framework.log("Loading Great Expectations");

		/* Load Great Expectations */
		File great = new File(Framework.EXTERNALDATA_PATH+"greatexpectationsSynsets.txt");

		/* Loop over all the words and keep track of their frequencies. */
		HashMap<String, Integer> greatFreq = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(great);
			sc.useDelimiter("[\\p{Punct}\\s&&[^@]]+");
			while (sc.hasNext()) {
				String word = sc.next().toLowerCase();

				/* Update the frequency of the word. */
				if (greatFreq.containsKey(word)) {
					greatFreq.put(word, greatFreq.get(word) + 1);

				} else  {
					greatFreq.put(word, 1);
				}
			}
			sc.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		contrastData = new HashMap<String, HashMap<String, Integer>>();
		this.contrastData.put("Great", greatFreq);

		Framework.log("Loading Sense and Sensibility");

		/* Load Sense and Sensibility. */
		File sense = new File(Framework.EXTERNALDATA_PATH+"senseandsensibilitySynsets.txt");

		/* Loop over all the words and keep track of their frequencies. */
		HashMap<String, Integer> senseFreq = new HashMap<String, Integer>();
		try {
			Scanner sc = new Scanner(sense);
			sc.useDelimiter("[\\p{Punct}\\s&&[^@]]+");
			while (sc.hasNext()) {
				String word = sc.next().toLowerCase();

				/* Update the frequency of the word. */
				if (senseFreq.containsKey(word)) {
					senseFreq.put(word, senseFreq.get(word) + 1);

				} else  {
					senseFreq.put(word, 1);
				}
			}
			sc.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		contrastData = new HashMap<String, HashMap<String, Integer>>();
		this.contrastData.put("Sense", senseFreq);
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

	/**
	 * A method that extracts term frequencies
	 */
	public ArrayList<HashMap<String, Double>> findTerms(boolean nn, boolean adj, boolean vrb) throws ClassNotFoundException, JSONException, IllegalSpanException, IOException {

		Framework.log("Finding terms");

		HashMap<String, Double> nouns = new HashMap<String, Double>(); //entities
		HashMap<String, Double> adjectives = new HashMap<String, Double>(); //properties
		HashMap<String, Double> verbs = new HashMap<String, Double>(); //actions			

		HashMap<String, Double> DPs = new HashMap<String, Double>(); 
		HashMap<String, Double> DCs = new HashMap<String, Double>(); 

		double maxDP = 0.0;
		double maxDC = 0.0;

		//Create a HashMap with all novels
		HashMap<String, File> contrastTexts = new HashMap<String, File>();
		File alice = new File(Framework.EXTERNALDATA_PATH+"alice30Synsets.txt");
		File pride = new File(Framework.EXTERNALDATA_PATH+"prideandprejudiceSynsets.txt");
		File sherlock = new File(Framework.EXTERNALDATA_PATH+"sherlockholmesSynsets.txt");
		File tom = new File(Framework.EXTERNALDATA_PATH+"tomsawyerSynsets.txt");
		File great = new File(Framework.EXTERNALDATA_PATH+"greatexpectationsSynsets.txt");
		File sensesensib = new File(Framework.EXTERNALDATA_PATH+"senseandsensibilitySynsets.txt");

		contrastTexts.put("Alice in Wonderland", alice);
		contrastTexts.put("Pride and Prejudice", pride);
		contrastTexts.put("Sherlock Holmes", sherlock);
		contrastTexts.put("Tom Sawyer", tom);
		contrastTexts.put("Great Expectations", great);
		contrastTexts.put("Sense and Sensibility", sensesensib);

		Framework.log("Processing all the reviews");

		int counter = 0;

		File f = new File(Framework.EXTERNALDATA_PATH + "/WordNet-3.0/dict");
		System.setProperty("wordnet.database.dir", f.toString());
		WordNetDatabase wordDatabase = WordNetDatabase.getFileInstance();

		/* Loop over all the reviews in the dataset. */
		for (Span review : reviewData.getSpans("review"))
		{		
			counter++;
			Framework.log("Processing review " + counter);

			/* Loop over all words in the review, but give longer phrases priority over shorter phrases. */
			Span scope = review.getTextualUnit();
			for(Word word : scope)
			{			
				//Framework.log("    Processing word " + word.getLemma());

				String lemma = word.getLemma();
				String pos = word.getAnnotation("pos");
				//Framework.log("POS is " + pos);

				TreeSet<Span> possibleSpans = reviewData.getSpans("sentence", word);
				Span sentence = getSentenceFromWord(word, word, possibleSpans, reviewData);

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

				boolean condition = false;
				if (pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") ) 
				{
					pos = "noun";
					condition = true;
					//Framework.log("    New POS is: " + pos);
				} 
				else if (pos.equals("JJ") || pos.equals("JJR") || pos.equals("JJS")) 
				{
					pos = "adjective";
					condition = true;
					//Framework.log("    New POS is: " + pos);
				} 
				else if (pos.equals("VB") || pos.equals("VBD") || pos.equals("VBG") || pos.equals("VBN") || pos.equals("VBN") || pos.equals("VBP")|| pos.equals("VBZ")) 
				{
					pos = "verb";
					condition = true;
					//Framework.log("    New POS is: " + pos);
				}
				String synsetLemma = lemma;				
				boolean usedSynset = false;

				if (condition = true)
				{
					String wordString = word.toString();
					Synset synset = WordSenseDisambiguation.findSynset(words, wordString, pos);

					if (synset != null)
					{
						String ss = synset.toString();
						int bracket = ss.indexOf("[");
						String synsetFinal = ss.substring(0, bracket).toLowerCase();
						synsetLemma = synsetFinal;
						//Framework.log("Synset is " + synsetLemma);
						usedSynset = true;
					}
				}

				if (vrb && (pos.equals("VB") || pos.equals("VBD") || pos.equals("VBG") || pos.equals("VBN") || pos.equals("VBN") || pos.equals("VBP") || pos.equals("VBZ")) && !usedSynset) {
					synsetLemma = verbConvertion(synsetLemma);
				}
				if (synsetLemma == null) {
					continue;
				}
				if (synsetLemma.length() < 2 ) {
					continue;
				}


				/* Calculate the domain pertinence. */
				double domainFreq = 0.0;

				if (wordFrequencyDocument.has(synsetLemma)) {
					domainFreq = (double) (int) wordFrequencyDocument.get(synsetLemma);
				}


				/* Find the maximum frequency of lemma in the contrasting corpus. */
				double contrastFreq = 0.0;

				for (String dom : contrastData.keySet()) {
					HashMap<String, Integer> frequencies = contrastData.get(dom);

					if (frequencies.containsKey(synsetLemma)) {
						double freq = (double) (int) frequencies.get(synsetLemma);
						/* Find the maximum frequency. */
						if (freq > contrastFreq) {
							contrastFreq = freq;
						}
					}
				}			

				double DP = domainFreq / contrastFreq;
				if (DP == Double.POSITIVE_INFINITY) 
				{
					DP = 0.0;
				}

				DPs.put(synsetLemma, DP);

				/* Find the maximum domain pertinence score. */
				if (DP > maxDP) 
				{
					maxDP = DP;
				}

				/* Calculate the domain consensus. */
				double DC = 0.0;

				double maxDomainFreq = 0.0;

				/* Find the maximum frequency of lemma across the reviews. */			
				for (String revId : wordFrequencyReview.keySet()) 
				{

					if (wordFrequencyReview.getJSONObject(revId).has(synsetLemma)) 
					{
						double revFreq = (double) (int) wordFrequencyReview.getJSONObject(revId).get(synsetLemma);
						if (revFreq > maxDomainFreq) {
							maxDomainFreq = revFreq;
						}
					}
				}


				/* Loop over all the reviews. */
				for (String revId2 : wordFrequencyReview.keySet()) {

					if (wordFrequencyReview.getJSONObject(revId2).has(synsetLemma)) {

						double frequency = (double) (int) wordFrequencyReview.getJSONObject(revId2).get(synsetLemma);
						double normFreq = frequency / maxDomainFreq;
						DC += -1 * (normFreq) * Math.log(normFreq);
					}

				}

				DCs.put(synsetLemma, DC);


				/* Find the maximum domain consensus score. */
				if (DC > maxDC) {
					maxDC = DC;
				}
				/* If DC & DP are bot noth 0.0 add it to nouns/adjectives. */
				if (!(DP == 0.0 && DC == 0.0)) {

					if (adj && pos.equals("adjective")) {
						adjectives.put(synsetLemma, 0.0);
					} 
					else if (nn && pos.equals("noun")) {
						nouns.put(synsetLemma, 0.0);				
					} 
					else if (vrb && pos.equals("verb")) {
						verbs.put(synsetLemma, 0.0);
					}
				}
			}
		}
		
		//Return all the relevant results in an array list
		ArrayList<HashMap<String, Double>> output = new ArrayList<HashMap<String, Double>>();
		output.add(verbs);
		output.add(nouns);
		output.add(adjectives);
		output.add(DPs);
		output.add(DCs);
		HashMap<String, Double> tempMaxDP = new HashMap<String, Double>();
		tempMaxDP.put("maxDP", maxDP);
		HashMap<String, Double> tempMaxDC = new HashMap<String, Double>();
		tempMaxDC.put("maxDC", maxDC);
		output.add(tempMaxDP);
		output.add(tempMaxDC);
			
		return output;
	}

	/**
	 * Converts a verb to its most simple form.
	 * @param verb to be converted
	 * @return simple form of the verb
	 */
	public String verbConvertion(String verb) {

		File f = new File(Framework.EXTERNALDATA_PATH + "/WordNet-3.0/dict");
		System.setProperty("wordnet.database.dir", f.toString());
		WordNetDatabase wordDatabase = WordNetDatabase.getFileInstance();

		Morphology id = Morphology.getInstance();

		String[] arr = id.getBaseFormCandidates(verb, SynsetType.VERB);
		if (arr.length>0) {
			return arr[0];
		}
		return verb;

	}

	/**
	 * A method that returns a set of synonyms contained in a given synset
	 * 
	 * @param synset
	 * @return array with lemmas of the given synset
	 */
	public String[] getLemmasFromSynset(String synset)
	{
		File f = new File(Framework.EXTERNALDATA_PATH + "/WordNet-3.0/dict");
		System.setProperty("wordnet.database.dir", f.toString());
		WordNetDatabase wordDatabase = WordNetDatabase.getFileInstance();

		int at = synset.indexOf("@");		
		String type = synset.substring(0, at).toLowerCase();
		char typeCode = type.charAt(0);
		String offset = synset.substring(at + 1);
		int targetOffset = Integer.parseInt(offset);

		SynsetType synsetType = SynsetTypeConverter.getType(typeCode);		
		SynsetPointer synsetPointer = new SynsetPointer(synsetType, targetOffset);		
		SynsetFactory synsetFactory = SynsetFactory.getInstance();
		Synset syns = synsetFactory.getSynset(synsetPointer);
		String[] lemmas = syns.getWordForms();

		return lemmas;
	}
}