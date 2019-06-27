package edu.eur.absa.ontologybuilder;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import edu.eur.absa.Framework;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase.*;
import edu.cmu.lti.ws4j.*;

/**
 * A method that builds an ontology semi-automatically.
 * 
 * @author Karoliina Ranta
 * Adapted by Suzanne Veltman
 * Adapted by Lisa Zhuang
 * Adapted by Ewelina Dera
 */
public class OntologyBuilder {

	/* The base ontology. */
	private SeminarOntology base;
	private HashMap<String, HashSet<String>> aspectCategories;
	private String domain;
	private Dataset reviewData;
	private Dataset reviewData1;
	private JSONObject wordFrequencyReview;
	private JSONObject wordFrequencyDocument;
	private HashMap<String, HashMap<String, Integer>> contrastData;
	private int numRejectTerms;   //only words
	private int numAcceptTerms;
	private int numRejectOverall;  //words + parent-relations
	private int numAcceptOverall;
	private int numRev;
	private HashSet<String> remove;
	private double threshold;
	private double invThreshold;
	private double[] fraction;
	private HashMap<String, HashSet<String>> relatedNouns;
	private boolean relations;
	private HashMap<String, HashSet<String>> nounsWithSynset;
	private HashSet<String> synonymsAccepted;

	/**
	 * A constructor for the OntologyBuilder class.
	 * @param baseOnt, the base ontology from which the final ontology is further constructed
	 * @param aspectCat, the aspect categories of the domain
	 * @param dom, the domain name
	 * @param thres, the threshold to use for the subsumption method
	 * @param frac, the top fraction of terms to suggest
	 */
	public OntologyBuilder(SeminarOntology baseOnt, HashMap<String, HashSet<String>> aspectCat, String dom, double thres, double[] frac, boolean r) {
		this(baseOnt, aspectCat, dom, thres, thres, frac, r );
	}

	/**
	 * A constructor for the OntologyBuilder class.
	 * @param baseOnt, the base ontology from which the final ontology is further constructed
	 * @param aspectCat, the aspect categories of the domain
	 * @param dom, the domain name
	 * @param thres, the threshold to use for the subsumption method
	 * @param invThres, the second threshold for the subsumption method
	 * @param frac, the top fraction of terms to suggest
	 */
	public OntologyBuilder(SeminarOntology baseOnt, HashMap<String, HashSet<String>> aspectCat, String dom, double thres, double invThres, double[] frac, boolean r) {

		/* Initialise the base ontology, aspect categories, and domain name. */
		base = baseOnt;
		aspectCategories = aspectCat;
		domain = dom;
		numRev = 5001;
		threshold = thres;
		invThreshold = invThres;
		numRejectTerms = 0;
		numAcceptTerms = 0;
		numRejectOverall = 0;
		numAcceptOverall = 0;
		fraction = frac;
		relations = r;

		remove = new HashSet<String>();
		remove.add("http://www.w3.org/2000/01/rdf-schema#Resource");
		remove.add("http://www.w3.org/2002/07/owl#Thing");
		remove.add(base.URI_Mention);
		remove.add(base.URI_Sentiment);
		remove.add(base.NS + "#" + domain.substring(0, 1).toUpperCase() + domain.substring(1).toLowerCase() + "Mention");

		HashMap<String, HashSet<String>> aspectTypes = groupAspects();

		synonymsAccepted = new HashSet<String>();
		HashSet<String> doneAspects = new HashSet<String>();

		Framework.log("Adding general positive and negative classes and suggesting synonyms");

		//Add synonyms in the the general positive and general negative aspect categories
		String negativeActionURI1 = base.addClassSynset("verb@1774136", true, new HashSet<String>(), base.URI_GenericNegativeAction);
		this.suggestSynonyms("hate", negativeActionURI1);
		String negativeActionURI2 = base.addClassSynset("verb@1776727", true, new HashSet<String>(), base.URI_GenericNegativeAction);
		this.suggestSynonyms("dislike", negativeActionURI2);
		String positiveActionURI1 = base.addClassSynset("verb@1775164", true, new HashSet<String>(), base.URI_GenericPositiveAction);
		this.suggestSynonyms("love", positiveActionURI1);
		String positiveActionURI2 = base.addClassSynset("verb@1820302", true, new HashSet<String>(), base.URI_GenericPositiveAction);
		this.suggestSynonyms("enjoy", positiveActionURI2);

		String negativeEntityURI1 = base.addClassSynset("noun@70965", true, new HashSet<String>(), base.URI_GenericNegativeEntity);
		this.suggestSynonyms("mistake", negativeEntityURI1);
		String negativeEntityURI2 = base.addClassSynset("noun@7317764", true, new HashSet<String>(), base.URI_GenericNegativeEntity);
		this.suggestSynonyms("failure", negativeEntityURI2);
		String positiveEntityURI1 = base.addClassSynset("noun@7531255", true, new HashSet<String>(), base.URI_GenericPositiveEntity);
		this.suggestSynonyms("satisfaction", positiveEntityURI1);
		String positiveEntityURI2 = base.addClassSynset("noun@5829782", true, new HashSet<String>(), base.URI_GenericPositiveEntity);
		this.suggestSynonyms("pleasure", positiveEntityURI2);

		String negativePropertyURI1 = base.addClassSynset("adjective@1125429", true, new HashSet<String>(), base.URI_GenericNegativeProperty);
		this.suggestSynonyms("bad", negativePropertyURI1);
		String negativePropertyURI2 = base.addClassSynset("adjective@1587077", true, new HashSet<String>(), base.URI_GenericNegativeProperty);
		this.suggestSynonyms("awful", negativePropertyURI2);
		String positivePropertyURI1 = base.addClassSynset("adjective@1123148", true, new HashSet<String>(), base.URI_GenericPositiveProperty);
		this.suggestSynonyms("good", positivePropertyURI1);
		String positivePropertyURI2 = base.addClassSynset("adjective@2343110", true, new HashSet<String>(), base.URI_GenericPositiveProperty);
		this.suggestSynonyms("excellent", positivePropertyURI2);

		/* Loop over the aspect category entities. */

		Framework.log("Adding aspect classes");

		HashMap<String, String> entitySynsets = new HashMap<String, String>();
		entitySynsets.put("ambience", "noun@14524849");
		entitySynsets.put("service", "noun@98625");
		entitySynsets.put("restaurant", "noun@4081281");
		entitySynsets.put("location", "noun@27167");
		entitySynsets.put("sustenance", "noun@7570720"); //Add drinks and food to sustenance

		for (String entity : aspectCat.keySet()) {
			HashSet<String> aspectSet = aspectCat.get(entity);

			/* Each entity should have its own AspectMention class. */
			HashSet<String> aspects = new HashSet<String>();
			String synset = entitySynsets.get(entity);
			for (String aspect : aspectSet) {

				/* Don't add miscellaneous to the ontology. */
				if (!aspect.equals("miscellaneous")) {
					aspects.add(entity.toUpperCase() + "#" + aspect.toUpperCase());
				}
			}
			String newClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "Mention", true, entity, aspects, base.URI_EntityMention);

			/* The domain entity doesn't get sentiment classes. */
			if (!entity.equals(domain)) {

				/* Create the SentimentMention classes (positive and negative) related to the entity. */
				String aspectPropertyClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "PropertyMention", true, entity.toLowerCase(), new HashSet<String>(), newClassURI, base.URI_PropertyMention);
				String aspectActionClassURI =  base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "ActionMention", true, entity.toLowerCase(), new HashSet<String>(), newClassURI, base.URI_ActionMention);
				String positivePropertyClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "PositiveProperty", false, entity.toLowerCase(), new HashSet<String>(), aspectPropertyClassURI, base.URI_Positive);
				String negativePropertyClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "NegativeProperty", false, entity.toLowerCase(), new HashSet<String>(), aspectPropertyClassURI,  base.URI_Negative);
				String positiveActionClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "PositiveAction", false, entity.toLowerCase(), new HashSet<String>(), aspectActionClassURI, base.URI_Positive);
				String negativeActionClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "NegativeAction", false, entity.toLowerCase(), new HashSet<String>(), aspectActionClassURI, base.URI_Negative);
				String positiveEntityClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "PositiveEntity", false, entity.toLowerCase(), new HashSet<String>(), newClassURI, base.URI_Positive);
				String negativeEntityClassURI = base.addClass(synset, entity.substring(0, 1).toUpperCase() + entity.substring(1).toLowerCase() + "NegativeEntity", false, entity.toLowerCase(), new HashSet<String>(), newClassURI, base.URI_Negative);
				this.suggestSynonyms(entity, newClassURI, aspectPropertyClassURI, aspectActionClassURI);
			} else {
				this.suggestSynonyms(entity, newClassURI);
			}

			/* Create AspectMention and SentimentMention subclasses for all aspects except for general and miscellaneous. */
			for (String aspectName : aspectTypes.keySet()) {
				if (!aspectName.equals("general") && !aspectName.equals("miscellaneous") && !doneAspects.contains(aspectName)) {
					doneAspects.add(aspectName);

					/* Create the AspectMention class. */
					HashSet<String> aspectsAsp = new HashSet<String>();
					for (String entityName : aspectTypes.get(aspectName)) {
						aspectsAsp.add(entityName.toUpperCase() + "#" + aspectName.toUpperCase());
					}
					String newClassURIAspect = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "Mention", true, aspectName, aspectsAsp, base.URI_EntityMention);

					/* Create the SentimentMention classes. */
					String aspectPropertyClassURI = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "PropertyMention", true, entity.toLowerCase(), new HashSet<String>(), newClassURIAspect, base.URI_PropertyMention);
					String aspectActionClassURI =  base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "ActionMention", true, entity.toLowerCase(), new HashSet<String>(), newClassURIAspect, base.URI_ActionMention);
					String positivePropertyURI = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "PositiveProperty", false, aspectName.toLowerCase(), new HashSet<String>(), aspectPropertyClassURI, base.URI_Positive);
					String negativePropertyURI = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "NegativeProperty", false, aspectName.toLowerCase(), new HashSet<String>(), aspectPropertyClassURI, base.URI_Negative);
					String positiveActionURI = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "PositiveAction", false, aspectName.toLowerCase(), new HashSet<String>(), aspectActionClassURI, base.URI_Positive);
					String negativeActionURI = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "NegativeAction", false, aspectName.toLowerCase(), new HashSet<String>(), aspectActionClassURI, base.URI_Negative);
					String positiveEntityURI = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "PositiveEntity", false, aspectName.toLowerCase(), new HashSet<String>(), newClassURIAspect, base.URI_Positive);
					String negativeEntityURI = base.addClass(aspectName.substring(0, 1).toUpperCase() + aspectName.substring(1).toLowerCase() + "NegativeEntity", false, aspectName.toLowerCase(), new HashSet<String>(), newClassURIAspect, base.URI_Negative);					
					this.suggestSynonyms(aspectName, newClassURIAspect, aspectPropertyClassURI, aspectActionClassURI);

					if (aspectName.contains("&")) {
						HashSet<String> lexs = new HashSet<String>();
						String[] parts = aspectName.split("&");
						lexs.add(parts[0]);
						lexs.add(parts[1]);
						base.addLexicalizations(newClassURIAspect, lexs);
						base.addLexicalizations(aspectPropertyClassURI, lexs);
						base.addLexicalizations(aspectActionClassURI, lexs);
						this.suggestSynonyms(parts[0], newClassURIAspect, aspectPropertyClassURI, aspectActionClassURI);
						this.suggestSynonyms(parts[1], newClassURIAspect, aspectPropertyClassURI, aspectActionClassURI);
					}
					if (aspectName.contains("_")) { 
						HashSet<String> lexs = new HashSet<String>();
						String[] parts = aspectName.split("_");
						lexs.add(parts[0]);
						lexs.add(parts[1]);
						base.addLexicalizations(newClassURIAspect, lexs);
						base.addLexicalizations(aspectPropertyClassURI, lexs);
						base.addLexicalizations(aspectActionClassURI, lexs);
						this.suggestSynonyms(parts[0], newClassURIAspect, aspectPropertyClassURI, aspectActionClassURI);
						this.suggestSynonyms(parts[1], newClassURIAspect, aspectPropertyClassURI, aspectActionClassURI);
					}
				}
			}			
		}

		//Add Food and Drinks Mention to Sustenance class	
		Framework.log("Adding food and drinks classes");

		String FoodMentionClassURI = base.addClass("noun@7555863", "FoodMention",true, "food", aspectCat.get("sustenance"), base.NS + "#SustenanceMention");
		String FoodMentionActionClassURI = base.addClass("noun@7555863", "FoodActionMention",true, "food", aspectCat.get("sustenance"), base.NS + "#SustenanceActionMention");
		String FoodMentionPropertyClassURI = base.addClass("noun@7555863",  "FoodPropertyMention", true, "food", aspectCat.get("sustenance"), base.NS + "#SustenancePropertyMention");
		this.suggestSynonyms("food", FoodMentionClassURI, FoodMentionActionClassURI, FoodMentionPropertyClassURI);
		String DrinksMentionClassURI = base.addClass("noun@7881800", "DrinksMention", true, "drinks", aspectCat.get("sustenance"), base.NS + "#SustenanceMention");
		String DrinksMentionActionClassURI = base.addClass("noun@7881800", "DrinksActionMention", true, "drinks", aspectCat.get("sustenance"), base.NS + "#SustenanceActionMention");
		String DrinksMentionPropertyClassURI = base.addClass("noun@7881800", "DrinksPropertyMention", true, "drinks", aspectCat.get("sustenance"), base.NS + "#SustenancePropertyMention");
		this.suggestSynonyms("drinks", DrinksMentionClassURI, DrinksMentionActionClassURI, DrinksMentionPropertyClassURI);

		HashSet<String> food = new HashSet<String>();
		food.add("food#prices");
		food.add("food#quality");
		food.add("food#style&options");

		String FoodPositivePropertyURI = base.addClass("Food" + "PositiveProperty", false, "", new HashSet<String>(), FoodMentionPropertyClassURI, base.URI_Positive);
		String FoodNegativePropertyURI = base.addClass("Food" + "NegativeProperty", false, "", new HashSet<String>(), FoodMentionPropertyClassURI, base.URI_Negative);
		String FoodPositiveActionURI = base.addClass("Food" + "PositiveAction", false, "", new HashSet<String>(), FoodMentionActionClassURI, base.URI_Positive);
		String FoodNegativeActionURI = base.addClass("Food" + "NegativeAction", false, "", new HashSet<String>(), FoodMentionActionClassURI, base.URI_Negative);
		String FoodPositiveEntityURI = base.addClass("Food" + "PositiveEntity", false, "", food, FoodMentionPropertyClassURI, base.URI_Positive);
		String FoodNegativeEntityURI = base.addClass("Food" + "NegativeEntity", false, "", food, FoodMentionPropertyClassURI, base.URI_Negative);	

		HashSet<String> drinks = new HashSet<String>();
		drinks.add("drinks#prices");
		drinks.add("drinks#quality");
		drinks.add("drinks#style&options");

		String DrinksPositivePropertyURI = base.addClass("Drinks" + "PositiveProperty", false, "", new HashSet<String>(), DrinksMentionPropertyClassURI, base.URI_Positive);
		String DrinksNegativePropertyURI = base.addClass("Drinks" + "NegativeProperty", false, "", new HashSet<String>(), DrinksMentionPropertyClassURI, base.URI_Negative);
		String DrinksPositiveActionURI = base.addClass("Drinks" + "PositiveAction", false, "", new HashSet<String>(), DrinksMentionActionClassURI, base.URI_Positive);
		String DrinksNegativeActionURI = base.addClass("Drinks" + "NegativeAction", false, "", new HashSet<String>(), DrinksMentionActionClassURI, base.URI_Negative);
		String DrinksPositiveEntityURI = base.addClass("Drinks" + "PositiveEntity", false, "", drinks, DrinksMentionPropertyClassURI, base.URI_Positive);
		String DrinksNegativeEntityURI = base.addClass("Drinks" + "NegativeEntity", false, "", drinks, DrinksMentionPropertyClassURI, base.URI_Negative);	

		Framework.log("Adding experience, person and time classes");

		//Add a few extra EntityMention classes
		
		//ExperienceMention
		HashSet<String> experienceAspects = new HashSet<String>();
		experienceAspects.add("RESTAURANT#MISCELLANEOUS");
		String ExperienceMentionClassURI = base.addClass("noun@7285403", "Experience" + "Mention", true, "experience", experienceAspects, base.URI_EntityMention);
		String ExperienceMentionActionClassURI = base.addClass("noun@7285403", "Experience" + "ActionMention", true, "experience", experienceAspects, base.URI_ActionMention);
		String ExperienceMentionPropertyClassURI = base.addClass("noun@7285403", "Experience" + "PropertyMention", true, "experience", experienceAspects, base.URI_PropertyMention);
		this.suggestSynonyms("experience", ExperienceMentionClassURI, ExperienceMentionActionClassURI, ExperienceMentionPropertyClassURI);

		//PersonMention
		HashSet<String> personAspects = new HashSet<String>();
		String PersonMentionClassURI = base.addClass("noun@7846", "Person" + "Mention", true, "person", personAspects, base.URI_EntityMention);
		String PersonMentionActionClassURI = base.addClass("noun@7846", "Person" + "ActionMention", true, "person", personAspects, base.URI_ActionMention);
		String PersonMentionPropertyClassURI = base.addClass("noun@7846", "Person" + "PropertyMention", true, "person", personAspects, base.URI_PropertyMention);
		this.suggestSynonyms("person", PersonMentionClassURI, PersonMentionActionClassURI, PersonMentionPropertyClassURI);

		//TimeMention
		HashSet<String> timeAspects = new HashSet<String>();
		String TimeMentionClassURI = base.addClass("noun@15270431", "Time" + "Mention", true, "time", timeAspects, base.URI_EntityMention);
		String TimeMentionActionClassURI = base.addClass("noun@15270431", "Time" + "ActionMention", true, "time", timeAspects, base.URI_ActionMention);
		String TimeMentionPropertyClassURI = base.addClass("noun@15270431", "Time" + "PropertyMention", true, "time", timeAspects, base.URI_PropertyMention);
		this.suggestSynonyms("time", TimeMentionClassURI, TimeMentionActionClassURI, TimeMentionPropertyClassURI);
	}

	/**
	 * A method that suggests the synonyms of a word and adds it as a lexicalization to the concepts.
	 * @param classURI, the concepts to which to add the lexicalizations
	 * @param word, the word of which to find synonyms
	 */
	public void suggestSynonyms(String word, String... classURI) {
		HashSet<String> accepted = new HashSet<String>();

		Synonyms syn = new Synonyms(word);
		System.out.println("Enter 'a' to accept and 'r' to reject the synonym: ");
		Scanner input = new Scanner(System.in);
		int i = 0;
		for (String synonym : syn.synonyms()) {
			i++;
			if (i > 20) {
				break;
			}
			if (synonym.equals(word)) {
				continue;
			}

			System.out.println("Synonym of word: " + word + " --> " + synonym);
			if (input.next().equals("a")) {
				numAcceptOverall++;
				accepted.add(synonym);
				synonymsAccepted.add(synonym);
			} else {
				numRejectOverall++;
			}
		}
		for (String URI : classURI) {
			base.addLexicalizations(URI, accepted);
		}		
	}

	/**
	 * Creates an object that stores all the aspect types and for each aspect which entities have this aspect.
	 * @return The HashMap containing the aspects and corresponding entities.
	 */
	public HashMap<String, HashSet<String>> groupAspects() {
		HashMap<String, HashSet<String>> aspectTypes = new HashMap<String, HashSet<String>>();

		/* Loop over the entities. */
		for (String entity : aspectCategories.keySet()) {

			/* Loop over the aspects of the entity. */
			for (String aspect : aspectCategories.get(entity)) {
				HashSet<String> entities;

				/* Check if the set already contains the aspect. */
				if (aspectTypes.containsKey(aspect)) {
					entities = aspectTypes.get(aspect);
				} else {
					entities = new HashSet<String>();
				}
				entities.add(entity);
				aspectTypes.put(aspect, entities);
			}
		}
		return aspectTypes;
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
	 * A method for finding important terms. 
	 * @param nn, true if searching for nouns
	 * @param adj, true if searching for adjectives
	 * @param alpha,
	 * @param beta, 
	 * @throws IOException 
	 * @throws IllegalSpanException 
	 * @throws JSONException 
	 * @throws ClassNotFoundException 
	 */
	public void findTerms(boolean nn, boolean adj, boolean vrb, double alpha, double beta) throws ClassNotFoundException, JSONException, IllegalSpanException, IOException {

		Framework.log("Finding terms");

		HashMap<String, Double> nouns = new HashMap<String, Double>(); //entities
		HashMap<String, Double> adjectives = new HashMap<String, Double>(); //properties
		HashMap<String, Double> verbs = new HashMap<String, Double>(); //actions

		double fractionNouns = fraction[0];
		double fractionAdj = fraction[1]; 
		double fractionVerb = fraction[2]; 	
		
		HashMap<String, String> nounContext = new HashMap<String, String>(); 
		HashMap<String, String> verbContext = new HashMap<String, String>();
		HashMap<String, String> adjContext = new HashMap<String, String>();

		HashMap<String, Double> aspectNouns = new HashMap<String, Double>();
		HashMap<String, Double> sentimentNouns = new HashMap<String, Double>();
		HashMap<String, Double> aspectAdjectives = new HashMap<String, Double>();
		HashMap<String, Double> sentimentAdjectives = new HashMap<String, Double>();
		HashMap<String, Double> aspectVerbs = new HashMap<String, Double>();
		HashMap<String, Double> sentimentVerbs = new HashMap<String, Double>();

		HashMap<String, Double> DPs = new HashMap<String, Double>(); 
		HashMap<String, Double> DCs = new HashMap<String, Double>(); 

		double maxDP = 0.0;
		double maxDC = 0.0;

		int max_length = 3; //Only consider phrases up until a length of 4 words (max_length = 3)

		//Create a HashMap with all novels
		HashMap<String, File> contrastTexts = new HashMap<String, File>();
		File alice = new File(Framework.EXTERNALDATA_PATH+"alice30Synsets.txt");
		File pride = new File(Framework.EXTERNALDATA_PATH+"prideandprejudiceSynsets.txt");
		File sherlock = new File(Framework.EXTERNALDATA_PATH+"sherlockholmesSynsets.txt");
		File tom = new File(Framework.EXTERNALDATA_PATH+"tomsawyerSynsets.txt");
		File great = new File(Framework.EXTERNALDATA_PATH+"greatexpectationsSynsets.txt");
		File sensesensib = new File(Framework.EXTERNALDATA_PATH+"senseandsensibilitySynsets.txt");

		HashMap<String, HashMap<String, HashMap<String, Integer>>> wordFrequencies = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();
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
		for (Span review : reviewData.getSpans("review")){

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
				if (DP == Double.POSITIVE_INFINITY) {
					DP = 0.0;
				}

				DPs.put(synsetLemma, DP);

				/* Find the maximum domain pertinence score. */
				if (DP > maxDP) {
					maxDP = DP;
				}

				/* Calculate the domain consensus. */
				double DC = 0.0;


				double maxDomainFreq = 0.0;

				/* Find the maximum frequency of lemma across the reviews. */			
				for (String revId : wordFrequencyReview.keySet()) {

					if (wordFrequencyReview.getJSONObject(revId).has(synsetLemma)) {
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
				/* If DC DP are not both 0.0 add it to nouns/adjectives. */
				if (!(DP == 0.0 && DC == 0.0)) {


					if (adj && pos.equals("adjective")) {
						adjectives.put(synsetLemma, 0.0);
						int i = 0;
						String text = lemma;
						if (relations) {
							for (Relation rel : word.getRelations().getAllRelationsToParents()) {
								if (rel.getAnnotation("relationShortName").equals("nsubj") || rel.getAnnotation("relationShortName").equals("amod")) {
									Word nword = (Word) rel.getParent();
									String relNoun = nword.getAnnotation("lemma", String.class);
									HashSet<String> related;
									if (relatedNouns.containsKey(lemma)) {
										related = relatedNouns.get(lemma);
									} else {
										related = new HashSet<String>();
									}
									related.add(relNoun);
									relatedNouns.put(lemma, related);
								}
							}
						}

						//						for (Word context : review.getWords()) {
						//							String pos1 = context.getAnnotation("pos");
						//							if(pos1.equals("NN") || pos1.equals("NNS") || pos1.equals("NNP") || pos1.equals("VB")||pos1.equals("VBD")||pos1.equals("VBG")||pos1.equals("VBN")||pos1.equals("VBN")||pos1.equals("VBP")|pos1.equals("VBZ")||pos1.equals("JJ")) {
						//							text = text + " " + context.getAnnotation("lemma", String.class);
						//							}
						//						}


						if(adjContext.containsKey(lemma)) {
							adjContext.put(lemma, adjContext.get(lemma) + " " + text);
						}
						else {
							adjContext.put(lemma, text);
						}

					} 
					else if (nn && pos.equals("noun")) {
						nouns.put(synsetLemma, 0.0);
						int i = 0;
						String text = lemma;				

						//						for (Word context : review.getWords()) {
						//							String pos1 = context.getAnnotation("pos");
						//							if(pos1.equals("NN") || pos1.equals("NNS") || pos1.equals("NNP") || pos1.equals("VB")||pos1.equals("VBD")||pos1.equals("VBG")||pos1.equals("VBN")||pos1.equals("VBN")||pos1.equals("VBP")||pos1.equals("JJ")) {
						//							text = text + " " + context.getAnnotation("lemma", String.class);
						//							}
						//						}

						if(nounContext.containsKey(lemma)) {
							nounContext.put(lemma, nounContext.get(lemma)+" "+text);
						}
						else {
							nounContext.put(lemma, text);
						}


					} 
					else if (vrb && pos.equals("verb")) {
						verbs.put(synsetLemma, 0.0);
						int i = 0;
						String text = lemma;

						//						for (Word context : review.getWords()) {
						//							String pos1 = context.getAnnotation("pos");
						//							if(pos1.equals("NN") || pos1.equals("NNS") || pos1.equals("NNP") || pos1.equals("VB")||pos1.equals("VBD")||pos1.equals("VBG")||pos1.equals("VBN")||pos1.equals("VBN")||pos1.equals("VBP")||pos1.equals("JJ")) {
						//							text = text + " " + context.getAnnotation("lemma", String.class);
						//							}
						//						}

						if(verbContext.containsKey(lemma)) {
							verbContext.put(lemma, verbContext.get(lemma)+" "+text);
						}
						else {
							verbContext.put(lemma, text);
						}

					}
				}
			}
		}

		Framework.log("Processing verbs");

		if (vrb) {
			verbs.keySet().removeAll(synonymsAccepted);
			/* Calculate the scores. */
			Double[] scores = new Double[verbs.size()];
			int i = 0;
			for (String w : verbs.keySet()) {
				double score = alpha * (DPs.get(w) / maxDP) + beta * (DCs.get(w) / maxDC);
				verbs.put(w, score);
				scores[i] = score;
				i++;
			}
			/* Find the threshold value to get only the top n% of the terms. */
			Arrays.sort(scores);
			double ind = fractionVerb * verbs.size();
			int index = (int) ind;			
			double scoreThreshold = scores[verbs.size() - 1 - index];

			System.out.println("Enter 's' to indicate sentimental verb, 'a' to indicate aspect verb, and 'r' to reject verb: ");
			Scanner in = new Scanner(System.in);
			for (String w : verbs.keySet()) {
				double score = verbs.get(w);
				if (score > scoreThreshold) {
					if (w.contains("@"))
					{
						String[] temp = getLemmasFromSynset(w);
						List<String> temp2 = Arrays.asList(temp);
						System.out.println("Verb: " + w + " - " + temp2);
					}
					else
					{
						System.out.println("Verb: " + w);
					}

					String input = in.next();
					if (input.equals("a")) { //aspect verb
						aspectVerbs.put(w, score);
						numAcceptTerms++;
						numAcceptOverall++;
					} else if (input.equals("s")) { //sentiment verb
						sentimentVerbs.put(w, score);
						numAcceptTerms++;
						numAcceptOverall++;

					} else {
						numRejectTerms++;
						numRejectOverall++;

					}
				}
			}
		}

		Framework.log("SubsumptionAspect method for verbs");

		this.subsumptionAspect(aspectVerbs, "verb", verbContext);

		Framework.log("SubsumptionSentiment method for verbs");

		this.subsumptionSentiment(sentimentVerbs, "verb", verbContext);

		Framework.log("Processing nouns");

		if (nn)
		{
			/* Calculate the scores. */
			nouns.keySet().removeAll(sentimentVerbs.keySet());
			nouns.keySet().removeAll(aspectVerbs.keySet());
			nouns.keySet().removeAll(synonymsAccepted);
			Double[] scores = new Double[nouns.size()];
			int i = 0;

			for (String w : nouns.keySet())
			{
				double score = alpha * (DPs.get(w) / maxDP) + beta * (DCs.get(w) / maxDC);
				nouns.put(w, score);
				scores[i] = score;
				i++;
			}

			//Find the threshold value to get only the top %n of the terms
			Arrays.sort(scores);
			double ind = fractionAdj * nouns.size();
			int index = (int) ind;
			double scoreThreshold = scores[nouns.size() - 1 - index];

			System.out.println("Enter 'a' to accept the noun as aspect based, enter 's' to accept the noun as sentiment based");
			System.out.println("and 'r' to reject the noun: ");
			Scanner in = new Scanner(System.in);
			for (String w : nouns.keySet()) {
				double score = nouns.get(w);
				if (score > scoreThreshold) {

					if (w.contains("@"))
					{
						String[] temp = getLemmasFromSynset(w);
						List<String> temp2 = Arrays.asList(temp);
						System.out.println("Noun: " + w + " - " + temp2);
					}
					else
					{
						System.out.println("Noun: " + w);
					}

					String input = in.next();
					if (input.equals("a")) {
						aspectNouns.put(w, score);
						numAcceptTerms++;
						numAcceptOverall++;
						nouns.put(w, 0.0);
					} else if (input.equals("s")) {
						sentimentNouns.put(w, score);
						numAcceptTerms++;
						numAcceptOverall++;
						nouns.put(w, 0.0);
					} else {
						numRejectTerms++;
						numRejectOverall++; //reject all other comparable words.
						nouns.put(w, 0.0);
					}
				}
			}

		}

		Framework.log("SubsumptionAspect method for nouns");

		this.subsumptionAspect(aspectNouns, "noun", nounContext);

		Framework.log("SubsumptionSentiment method for nouns");

		this.subsumptionSentiment(sentimentNouns, "noun", nounContext);


		Framework.log("Saving temporary ontology");

		this.save("EwelinaOntologyTussenProductFINAL.owl");
		

		Framework.log("Processing adjectives");

		if (adj) {
			adjectives.keySet().removeAll(sentimentNouns.keySet());
			adjectives.keySet().removeAll(aspectNouns.keySet());
			adjectives.keySet().removeAll(sentimentVerbs.keySet());
			adjectives.keySet().removeAll(aspectVerbs.keySet());
			adjectives.keySet().removeAll(synonymsAccepted);
			/* Calculate the scores. */
			Double[] scores = new Double[adjectives.size()];
			int i = 0;
			for (String w : adjectives.keySet()) {
				double score = alpha * (DPs.get(w) / maxDP) + beta * (DCs.get(w) / maxDC);
				adjectives.put(w, score);
				scores[i] = score;
				i++;
			}
			/* Find the threshold value to get only the top n% of the terms. */
			Arrays.sort(scores);
			double ind = fractionAdj * adjectives.size();
			int index = (int) ind;			
			double scoreThreshold = scores[adjectives.size() - 1 - index];	

			System.out.println("Enter 'a' to accept the adjective as aspect based, enter 's' to accept the adjective as sentiment based");
			System.out.println("and 'r' to reject the adjective: ");
			Scanner in = new Scanner(System.in);
			for (String w : adjectives.keySet()) {
				double score = adjectives.get(w);
				if (score > scoreThreshold) {

					if (w.contains("@"))
					{
						String[] temp = getLemmasFromSynset(w);
						List<String> temp2 = Arrays.asList(temp);
						System.out.println("Adjective: " + w + " - " + temp2);
					}
					else
					{
						System.out.println("Adjective: " + w);
					}

					String input = in.next();
					if (input.equals("a")) {
						aspectAdjectives.put(w, score);
						numAcceptTerms++;
						numAcceptOverall++;
						adjectives.put(w, 0.0);
					} else if (input.equals("s")) {
						sentimentAdjectives.put(w, score);
						numAcceptTerms++;
						numAcceptOverall++;
						adjectives.put(w, 0.0);
					} else {
						numRejectTerms++;
						numRejectOverall++; //reject all other comparable words.
						adjectives.put(w, 0.0);
					}
				}
			}
		}
		
		Framework.log("SubsumptionAspect method for adjectives");

		this.subsumptionAspect(aspectAdjectives, "adjective", adjContext);

		Framework.log("SubsumptionSentiment method for adjectives");

		this.subsumptionSentiment(sentimentAdjectives, "adjective", adjContext);

	}


	/**
	 * A method that uses the subsumption method to determine the Aspect superclasses of the accepted terms
	 * and adds them to the ontology.
	 * @param words, the accepted terms to be added to the ontology.
	 */
	public void subsumptionAspect(HashMap<String, Double> words, String pos, HashMap<String, String> context) {
		HashSet<String> set;

		if (pos.equals("noun")) {
			set = base.getSubclasses(base.URI_EntityMention); 
			set.remove(base.URI_EntityMention);
			HashSet<String> temp = base.getSubclasses(base.URI_ActionMention);
			temp.addAll(base.getSubclasses(base.URI_PropertyMention));
			temp.addAll(base.getSubclasses(base.URI_Sentiment));
			set.removeAll(temp);
		}
		else if (pos.equals("verb")) {
			set = base.getSubclasses(base.URI_ActionMention); 
			set.remove(base.URI_ActionMention);
			HashSet<String> temp = base.getSubclasses(base.URI_Sentiment);
			set.removeAll(temp);
		}
		else {
			set = base.getSubclasses(base.URI_PropertyMention); 
			set.remove(base.URI_PropertyMention);
			HashSet<String> temp = base.getSubclasses(base.URI_Sentiment);
			set.removeAll(temp);
		}

		/* Loop over all the accepted words. */
		for (String word : words.keySet()) {

			double numWord = 0.0;

			/* Only increment the parent during the first loop over the reviews. */
			boolean checkWord = true;

			HashMap<String, Double> parents = new HashMap<String, Double>();
			TreeMap<Double, String> scoreParents = new TreeMap<Double, String>();

			for (String parentURI : set) {
				HashSet<String> parent = base.getLexicalizations(parentURI);

				double score = 0.0;

				double numParent = 0.0;
				double numParentCond = 0.0;

				/* Loop over all the reviews to see if they appear with the word. */
				for (Span review : reviewData.getSpans("review")){
					String revId = review.getAnnotation("id", String.class);
					JSONObject obj = wordFrequencyReview.getJSONObject(revId);

					/* Update the counters. */
					for (String par : parent) {
						if (obj.has(par)) {
							numParent++;
							break;
						} 
					}
					if (obj.has(word)) {
						if (checkWord) {
							numWord++;
						}
						for (String par : parent) {
							if (obj.has(par)) {
								numParentCond++;
								break;
							} 
						}
					}
				}
				checkWord = false;

				double wordProb = ( numParentCond / numRev ) / ( numWord / numRev );
				score += wordProb;

				/* Check if a possible parent. */
				double wordProbInv = ( numParentCond / numRev ) / ( numParent / numRev );
				if (wordProb >= threshold && wordProbInv < invThreshold ) {
					while (scoreParents.containsKey(-score)) {
						score += 0.000000000000001;
					}
					scoreParents.put(-score, parentURI);
					parents.put(parentURI, score);
				}
			}

			/* Suggest the parents, highest score first, till one or none is accepted. */
			if (!parents.isEmpty()) 
			{
				if (word.contains("@"))
				{
					String[] temp = getLemmasFromSynset(word);
					List<String> temp2 = Arrays.asList(temp);
					System.out.println("Enter 'i'if there are no relations (anymore) for " + word + " - " + temp2 + ". Enter 'a' to accept and 'r' to reject the relation: ");
				}
				else
				{
					System.out.println("Enter 'i'if there are no relations (anymore) for "+word+ ". Enter 'a' to accept and 'r' to reject the relation: ");
				}
			}
			Scanner in = new Scanner(System.in);

			/* Loop over all the possible parents. */
			boolean accept = false;
			for (double parentScore : scoreParents.keySet()) {
				if (!accept) { //not accepted as either negative of positive generic.
					String finalParentURI = scoreParents.get(parentScore);	
					parentScore = -parentScore;	

					int hashtag = finalParentURI.indexOf("#");
					String newFinalParentURI = finalParentURI.substring(hashtag + 1);

					if (word.contains("@"))
					{
						String[] temp = getLemmasFromSynset(word);
						List<String> temp2 = Arrays.asList(temp);
						System.out.println("Type 'a' to accept and 'r' to reject relation: " + word + " - " + temp2 + " --> " + newFinalParentURI + " : " + parentScore + " type 'i'if no more parent classes");
					}
					else
					{
						System.out.println("Type 'a' to accept and 'r' to reject relation: " + word + " --> " + newFinalParentURI + " : " + parentScore + " type 'i'if no more parent classes");
					}

					String input= in.next();
					if (input.equals("a")) {
						numAcceptOverall++;
							String newConcept = base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), finalParentURI);
					} else if (input.equals("i")) {
						numRejectOverall++;
						break;
					} else { numRejectOverall++;}
				}
			}
		}
	}

	/**
	 * A method that uses the subsumption method to determine the Sentiment based superclasses of the accepted terms
	 * and adds them to the ontology.
	 * @param words, the accepted terms to be added to the ontology.
	 */
	public void subsumptionSentiment(HashMap<String, Double> words, String pos, HashMap<String, String> context) {
		HashSet<String> set;
		double numerator = 0.0;
		double denominator = 0.0;
		if (pos.equals("noun")) {
			set = base.getSubclasses(base.URI_EntityMention); 
			set.remove(base.URI_EntityMention);
			HashSet<String> temp = base.getSubclasses(base.URI_ActionMention);
			temp.addAll(base.getSubclasses(base.URI_PropertyMention));
			temp.addAll(base.getSubclasses(base.URI_Sentiment));
			set.removeAll(temp);
		}
		else if (pos.equals("verb")) {
			set = base.getSubclasses(base.URI_ActionMention); 
			set.remove(base.URI_ActionMention);
			HashSet<String> temp = base.getSubclasses(base.URI_Sentiment);
			set.removeAll(temp);
		}
		else {
			set = base.getSubclasses(base.URI_PropertyMention); 
			set.remove(base.URI_PropertyMention);
			HashSet<String> temp = base.getSubclasses(base.URI_Sentiment);
			set.removeAll(temp);
		}

		/* Loop over all the nouns. */
		for (String word : words.keySet()) {

			double numWord = 0.0;

			/* Only increment the parent during the first loop over the reviews. */
			boolean checkWord = true;

			HashMap<String, Double> parents = new HashMap<String, Double>();
			TreeMap<Double, String> scoreParents = new TreeMap<Double, String>();


			for (String parentURI : set) {
				HashSet<String> parent = base.getLexicalizations(parentURI);

				double score = 0.0;

				double numParent = 0.0;
				double numParentCond = 0.0;

				/* Loop over all the reviews to see if they appear with the word. */
				for (Span review : reviewData.getSpans("review")){
					String revId = review.getAnnotation("id", String.class);
					JSONObject obj = wordFrequencyReview.getJSONObject(revId);

					double stars;
					try {
						stars = Double.parseDouble(review.getAnnotation("stars"));						}
					catch (NumberFormatException e) {
						stars = 0.0;
					}

					/* Update the counters. */
					double subNumerator = 0.0;
					for (String w: words.keySet()) {
						if (obj.has(w)) {
							subNumerator += (double) (int) obj.get(w); //add all frequencies of word w from each review together
						}
					}
					double frequency = 0.0;
					if (obj.has(word)) {
						frequency = (double) (int) obj.get(word);
					}
					if (subNumerator != 0.0) {
						denominator += (stars/5.0)*(frequency/subNumerator); //get weighted average stars-score for that word
					}

					/* Update the numerator with the score. */
					numerator += (stars / 5.0);

					/* Update the counters. */
					for (String par : parent) {
						if (obj.has(par)) {
							numParent++;
							break;
						} 
					}
					if (obj.has(word)) {
						if (checkWord) {
							numWord++;
						}
						for (String par : parent) {
							if (obj.has(par)) {
								numParentCond++;
								break;
							} 
						}
					}
				}
				checkWord = false;

				double wordProb = ( numParentCond / numRev ) / ( numWord / numRev );
				score += wordProb;

				/* Check if a possible parent. */
				double wordProbInv = ( numParentCond / numRev ) / ( numParent / numRev );
				if (wordProb >= threshold && wordProbInv < invThreshold ) {

					while (scoreParents.containsKey(-score)) {
						score += 0.000000000000001;
					}
					scoreParents.put(-score, parentURI);
					parents.put(parentURI, score);
				}
			}
			double sentimentScore = denominator/numerator;

			/* Suggest the parents, highest score first, till one or none is accepted. */
			if (!parents.isEmpty()) {
				System.out.println("Enter 'a' to accept and 'r' to reject the relation: ");
			}
			Scanner in = new Scanner(System.in);

			/* Loop over all the possible parents, start with Generic */
			boolean accept = false;
			if  (sentimentScore > 0.025) {

				if (word.contains("@"))
				{
					String[] temp = getLemmasFromSynset(word);
					List<String> temp2 = Arrays.asList(temp);
					System.out.println(word + " - " + temp2 + " has sentimentScore: " + sentimentScore);
				}
				else
				{
					System.out.println(word + " has sentimentScore: " + sentimentScore);
				}

				String parentClass = "";
				if (pos.equals("noun")) {
					parentClass = base.URI_GenericPositiveEntity;
				}
				else if (pos.equals("verb")) {
					parentClass = base.URI_GenericPositiveAction;
				}
				else {
					parentClass = base.URI_GenericPositiveProperty;
				}

				int hashtag = parentClass.indexOf("#");
				String newParentClass = parentClass.substring(hashtag + 1);				

				if (word.contains("@"))
				{
					String[] temp = getLemmasFromSynset(word);
					List<String> temp2 = Arrays.asList(temp);
					System.out.println("relation: " + word + " - " + temp2 + " --> " + newParentClass);
				}
				else
				{
					System.out.println("relation: " + word + " --> " + newParentClass);
				}

				if (in.next().equals("a")) {
					numAcceptOverall++;
					accept = true;
					base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClass);
				} else {
					numRejectOverall++;
					/* Suggest the opposite sentiment. */
					parentClass = "";
					if (pos.equals("noun")) {
						parentClass = base.URI_GenericNegativeEntity;
					}
					else if (pos.equals("verb")) {
						parentClass = base.URI_GenericNegativeAction;
					}
					else {
						parentClass = base.URI_GenericNegativeProperty;
					}

					int hashtag1 = parentClass.indexOf("#");
					String newParentClass1 = parentClass.substring(hashtag1 + 1);

					if (word.contains("@"))
					{
						String[] temp = getLemmasFromSynset(word);
						List<String> temp2 = Arrays.asList(temp);
						System.out.println("relation: " + word + " - " + temp2 + " --> " + newParentClass1);
					}
					else
					{
						System.out.println("relation: " + word + " --> " + newParentClass1);
					}

					if (in.next().equals("a")) {
						numAcceptOverall++;
						accept = true;
						base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClass);				
					} else {
						numRejectOverall++;
					}
				}
			} else { //sentimentScore indicates negative sentiment

				if (word.contains("@"))
				{
					String[] temp = getLemmasFromSynset(word);
					List<String> temp2 = Arrays.asList(temp);
					System.out.println(word + " - " + temp2 + " : " + sentimentScore);
				}
				else
				{
					System.out.println(word + " : " + sentimentScore);
				}

				String parentClass = "";
				if (pos.equals("noun")) {
					parentClass = base.URI_GenericNegativeEntity;
				}
				else if (pos.equals("verb")) {
					parentClass = base.URI_GenericNegativeAction;
				}
				else {
					parentClass = base.URI_GenericNegativeProperty;
				}

				if (word.contains("@"))
				{
					String[] temp = getLemmasFromSynset(word);
					List<String> temp2 = Arrays.asList(temp);
					System.out.println(word + " - " + temp2 + " has sentimentScore: " + sentimentScore);
				}
				else
				{
					System.out.println(word + "has sentimentScore: " + sentimentScore);
				}

				if (in.next().equals("a")) {
					numAcceptOverall++;
					accept = true;
					base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClass);									
				} else {
					numRejectOverall++;
					/* Suggest the opposite sentiment. */
					parentClass = "";
					if (pos.equals("noun")) {
						parentClass = base.URI_GenericPositiveEntity;
					}
					else if (pos.equals("verb")) {
						parentClass = base.URI_GenericPositiveAction;
					}
					else {
						parentClass = base.URI_GenericPositiveProperty;
					}

					int hashtag2 = parentClass.indexOf("#");
					String newParentClass2 = parentClass.substring(hashtag2 + 1);

					if (word.contains("@"))
					{
						String[] temp = getLemmasFromSynset(word);
						List<String> temp2 = Arrays.asList(temp);
						System.out.println("relation: " + word + " - " + temp2 + " --> " + newParentClass2);
					}
					else
					{
						System.out.println("relation: " + word + " --> " + newParentClass2);
					}

					if (in.next().equals("a")) {
						numAcceptOverall++;
						accept = true;
						base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClass);				
					} else {
						numRejectOverall++;
					}
				}
			}

			HashSet<String> containsURI = new HashSet<String>();
			HashMap<String, String> savedParents = new HashMap<String, String>();
			for (double parentScore : scoreParents.keySet()) {
				if (!accept) { //not accepted as generic
					String finalParentURI = scoreParents.get(parentScore);		
					parentScore = -parentScore;
					String finalParentURIshort = "";

					//Framework.log("Final parent URI " + finalParentURI);

					if (finalParentURI.contains("Mention"))
					{
						finalParentURIshort = finalParentURI.substring(0, finalParentURI.length()-7);

						//Framework.log("Final parent URI short " + finalParentURIshort);

						if (finalParentURIshort.contains("Property"))
						{
							finalParentURIshort = finalParentURIshort.substring(0, finalParentURIshort.length() - 8);
						}
						else if (finalParentURIshort.contains("Action"))
						{
							finalParentURIshort = finalParentURIshort.substring(0, finalParentURIshort.length() - 6);
						}
					}
					else
					{
						finalParentURIshort = finalParentURI;
					}

					//Framework.log("Final parent URI short " + finalParentURIshort);

					int number = 0;
					if (domain.equals("restaurant")) {
						number = 79;
					}
					else {
						number = 76;
					}
					System.out.println("Enter 'a' to accept and 'r' to reject the relation: ");
					String genericTest = finalParentURI.substring(0,number) + "Generic";
					if (!containsURI.contains(finalParentURIshort) && !finalParentURIshort.equals(genericTest)) {
						String sentiment = "";
						if (sentimentScore > 0.025)
						{
							sentiment = "Positive";

							int hashtag = finalParentURIshort.indexOf("#");
							String newFinalParentURIshort = finalParentURIshort.substring(hashtag + 1);

							if (word.contains("@"))
							{
								String[] temp = getLemmasFromSynset(word);
								List<String> temp2 = Arrays.asList(temp);
								System.out.println(word + " - " + temp2 + ": "+ newFinalParentURIshort + sentiment );
							}
							else
							{
								System.out.println(word + ": "+ newFinalParentURIshort + sentiment );
							}

							String inputt = in.next();
							if (inputt.equals("a")) {
								numAcceptOverall++;
								containsURI.add(finalParentURIshort);
								savedParents.put(finalParentURIshort, "p");
							} else if (inputt.equals("i")){
								numRejectOverall++;
								break;
							} else {
								numRejectOverall++;

								/* suggest opposite sentiment*/
								sentiment = "Negative";

								int hashtag3 = finalParentURIshort.indexOf("#");
								String newFinalParentURIshort3 = finalParentURIshort.substring(hashtag3 + 1);


								if (word.contains("@"))
								{
									String[] temp = getLemmasFromSynset(word);
									List<String> temp2 = Arrays.asList(temp);
									System.out.println(word + " - " + temp2 + ": "+ newFinalParentURIshort3 + sentiment );
								}
								else
								{
									System.out.println(word + ": "+ newFinalParentURIshort3 + sentiment );
								}

								inputt = in.next();
								if (inputt.equals("a")) {
									numAcceptOverall++;
									containsURI.add(finalParentURIshort);
									savedParents.put(finalParentURIshort, "n");
								} else {
									numRejectOverall++; //whole aspect category is rejected
								}
							}
						} else { //if negative sentiment is indicated by sentimentScore
							sentiment = "Negative";

							int hashtag = finalParentURIshort.indexOf("#");
							String newFinalParentURIshort = finalParentURIshort.substring(hashtag + 1);


							if (word.contains("@"))
							{
								String[] temp = getLemmasFromSynset(word);
								List<String> temp2 = Arrays.asList(temp);
								System.out.println(word + " - " + temp2 + ": "+ newFinalParentURIshort + sentiment );
							}
							else
							{
								System.out.println(word + ": "+ newFinalParentURIshort + sentiment );
							}

							String inputt = in.next();
							if (inputt.equals("a")) {
								numAcceptOverall++;
								containsURI.add(finalParentURIshort);
								savedParents.put(finalParentURIshort, "n");
							} else {
								numRejectOverall++;

								/* suggest opposite sentiment*/
								sentiment = "Positive";

								int hashtag4 = finalParentURIshort.indexOf("#");
								String newFinalParentURIshort4 = finalParentURIshort.substring(hashtag4 + 1);


								if (word.contains("@"))
								{
									String[] temp = getLemmasFromSynset(word);
									List<String> temp2 = Arrays.asList(temp);
									System.out.println(word + " - " + temp2 + ": "+ newFinalParentURIshort4 + sentiment );
								}
								else
								{
									System.out.println(word + ": "+ newFinalParentURIshort4 + sentiment );
								}

								inputt = in.next();
								if (inputt.equals("a")) {
									numAcceptOverall++;
									containsURI.add(finalParentURIshort);
									savedParents.put(finalParentURIshort, "p");
								} else {
									numRejectOverall++; //whole aspect category is rejected
								}
							}
						}
					}
				}
			}

			//type 2
			if (savedParents.size() == 1) {
				for(String P : savedParents.keySet()) { //get shortURIs
					String answer = savedParents.get(P);

					if (P.contains("Property"))
					{
						P = P.substring(0, P.length() - 8);
					}
					else if (P.contains("Action"))
					{
						P = P.substring(0, P.length() - 6);
					}

					if (answer.equals("p")) {
						numAcceptOverall++;
						String parentClassName = "";

						if (pos.equals("verb"))
						{
							parentClassName = P + "PositiveAction";
						}
						else if (pos.equals("noun"))
						{
							parentClassName = P + "PositiveEntity";
						}
						else
						{
							parentClassName = P + "PositiveProperty";
						}

						String newConcept = base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClassName);
						//this.suggestSynonyms(word, newConcept);
					} else if (answer.equals("n")) {
						numAcceptOverall++;
						String parentClassName = "";

						if (pos.equals("verb"))
						{
							parentClassName = P + "NegativeAction";
						}
						else if (pos.equals("noun"))
						{
							parentClassName = P + "NegativeEntity";
						}
						else
						{
							parentClassName = P + "NegativeProperty";
						}

						String newConcept = base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClassName);
					}
				}

			}
			//type 3
			else if(savedParents.size() > 1) {
				for(String P : savedParents.keySet()) {					
					String answer = savedParents.get(P);

					if (P.contains("Property"))
					{
						P = P.substring(0, P.length() - 8);
					}
					else if (P.contains("Action"))
					{
						P = P.substring(0, P.length() - 6);
					}

					if (answer.equals("p")) {
						numAcceptOverall++;
						String parentClassName = "";
						if (pos.equals("verb"))
						{
							parentClassName = P+"PositiveAction";
						}
						else if (pos.equals("noun"))
						{
							parentClassName = P+"PositiveEntity";
						}
						else
						{
							parentClassName = P + "PositiveProperty";
						}

						String newConcept = base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClassName);
						//this.suggestSynonyms(word, newConcept);
					} else if (answer.equals("n")) {
						numAcceptOverall++;
						String parentClassName = "";
						if (pos.equals("verb"))
						{
							parentClassName = P+"NegativeAction";
						}
						else if (pos.equals("noun"))
						{
							parentClassName = P+"NegativeEntity";
						}
						else
						{
							parentClassName = P + "NegativeProperty";
						}

						String newConcept = base.addClass(pos, context, word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase(), true, word, new HashSet<String>(), parentClassName);
					}
				}

				for(String P : savedParents.keySet()) { 
					String answer = savedParents.get(P);

					String newWord = word;
					if (word.contains("@"))
					{
						String[] lemmas = getLemmasFromSynset(word);
						newWord = lemmas[0];
					}

					if (answer.equals("p")) {
						numAcceptOverall++;

						base.addClass2(base.NS + "#" + (newWord.substring(0, 1).toUpperCase() + newWord.substring(1).toLowerCase()).replaceAll(" ", "") ,  P+"Mention" , base.URI_Positive);

					} else if (answer.equals("n")) {
						numAcceptOverall++;

						base.addClass2(base.NS + "#" + (newWord.substring(0, 1).toUpperCase() + newWord.substring(1).toLowerCase()).replaceAll(" ", "") ,  P+"Mention" , base.URI_Negative);		

					}
				}
			}
		}
	}

	/**
	 * A method that returns the number of accepted and rejected terms.
	 * @return an array with first the number of accepted and second number of rejected terms
	 */
	public int[] getStats() {
		int[] stats = new int[3];
		stats[0] = numAcceptTerms;
		stats[1] = numRejectTerms;
		return stats;
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
	 * Save the built ontology.
	 * @param file, the name of the file to which to save the ontology
	 */
	public void save(String file) {
		base.save(file);
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