package edu.eur.absa.ontologybuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;

import edu.eur.absa.Framework;
import edu.eur.absa.model.exceptions.IllegalSpanException;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.impl.file.SynsetFactory;
import edu.smu.tspell.wordnet.impl.file.SynsetPointer;
import edu.smu.tspell.wordnet.impl.file.SynsetTypeConverter;

/**
 * A class that tests different values of parameters and evaluates them with acceptance ratio
 * 
 * @author Ewelina Dera
 *
 */
public class GridSearchParameters {
	
	private static HashMap<String,String> acceptReject = new HashMap<String, String>();

	public static void main(String[] args) throws ClassNotFoundException, JSONException, IllegalSpanException, IOException 
	{
		//Creating the output file
		PrintWriter resultsFile = new PrintWriter(Framework.EXTERNALDATA_PATH + "ParameterOptimisation.txt");
		resultsFile.println("alpha;beta;fraction;acceptedTerms;rejectedTerms;acceptanceRatio;acceptedVerbs;rejectedVerbs;ratioVerbs;acceptedNouns;rejectedNouns;ratioNouns;acceptedAdjectives;rejectedAdjectives;ratioAdjectives");
		
		GridSearchTermFrequency termFrequency = new GridSearchTermFrequency();
		
		termFrequency.loadContrast();
		
		try 
		{
			termFrequency.loadReviews();
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		boolean verbs=true; 
		boolean nouns=true; 
		boolean adj=true;
		
		Framework.log("Obtaining the results");

		//Getting the term frequencies
		ArrayList<HashMap<String, Double>> terms = termFrequency.findTerms(nouns, adj, verbs);
		
		HashMap<String, Double> verbTerms = terms.get(0);
		HashMap<String, Double> nounTerms = terms.get(1);
		HashMap<String, Double> adjectiveTerms = terms.get(2);
		HashMap<String, Double> DPs = terms.get(3);
		HashMap<String, Double> DCs = terms.get(4);
		HashMap<String, Double> tempMaxDP = terms.get(5);
		HashMap<String, Double> tempMaxDC = terms.get(6);
		
		double maxDP = tempMaxDP.get("maxDP");
		double maxDC = tempMaxDC.get("maxDC");
		
		//Possible values for alpha/beta --> grid search with step size 0.1
		ArrayList<Double> alpha = new ArrayList<Double>();
		alpha.add(0.0);
		alpha.add(0.1);
		alpha.add(0.2);
		alpha.add(0.3);
		alpha.add(0.4);
		alpha.add(0.5);
		alpha.add(0.6);
		alpha.add(0.7);
		alpha.add(0.8);
		alpha.add(0.9);
		alpha.add(1.0);
		
		//Possible values for fraction of suggested verbs/nouns/adjectives --> grid search with step size 0.01
		ArrayList<Double> fraction = new ArrayList<Double>();
		fraction.add(0.10);
		fraction.add(0.11);
		fraction.add(0.12);
		fraction.add(0.13);
		fraction.add(0.14);
		fraction.add(0.15);
		fraction.add(0.16);
		fraction.add(0.17);
		fraction.add(0.18);
		fraction.add(0.19);
		fraction.add(0.20);
		
		//For each value of alpha
		for (int i = alpha.size() - 1; i >= 0; i--)
		{
			double alphaTemp = alpha.get(i);
			double betaTemp = 1.0 - alphaTemp;    //Alpha + Beta = 1
			
			//For each value of fraction
			for (int j = alpha.size() - 1; j >= 0; j--)
			{
				double fractionTemp = fraction.get(j);
				
				Framework.log("Processing alpha " + alphaTemp + " and fraction " + fractionTemp);
				
				int totalAccept = 0;
				int totalReject = 0;
				int acceptVerbs = 0;
				int rejectVerbs = 0;
				int acceptNouns = 0;
				int rejectNouns = 0;
				int acceptAdjs = 0;
				int rejectAdjs = 0;
				
				Framework.log("    Processing verbs");
				ArrayList<Integer> resultsVerbs = acceptRejectTerms(verbTerms, alphaTemp, betaTemp, DPs, DCs, maxDP, maxDC, fractionTemp);
				acceptVerbs = resultsVerbs.get(0);
				rejectVerbs = resultsVerbs.get(1);
				
				Framework.log("    Processing nouns");
				ArrayList<Integer> resultsNouns = acceptRejectTerms(nounTerms, alphaTemp, betaTemp, DPs, DCs, maxDP, maxDC, fractionTemp);
				acceptNouns = resultsNouns.get(0);
				rejectNouns = resultsNouns.get(1);
				
				Framework.log("    Processing adjectives");
				ArrayList<Integer> resultsAdjs = acceptRejectTerms(adjectiveTerms, alphaTemp, betaTemp, DPs, DCs, maxDP, maxDC, fractionTemp);
				acceptAdjs = resultsAdjs.get(0);
				rejectAdjs = resultsAdjs.get(1);
				
				totalAccept = acceptVerbs + acceptNouns + acceptAdjs;
				totalReject = rejectVerbs + rejectNouns + rejectAdjs;
				
				double ratioTotal = (double) totalAccept / (totalAccept + totalReject);
				double ratioVerbs = (double) acceptVerbs / (acceptVerbs + rejectVerbs);
				double ratioNouns = (double) acceptNouns / (acceptNouns + rejectNouns);
				double ratioAdjs = (double) acceptAdjs / (acceptAdjs + rejectAdjs);
				
				Framework.log("    Printing output");
				String outputTemp = alphaTemp + ";" + betaTemp + ";" + fractionTemp + ";" + totalAccept + ";" + totalReject + ";" + ratioTotal + ";" + acceptVerbs + ";" + rejectVerbs + ";" + ratioVerbs + ";" + acceptNouns + ";" + rejectNouns + ";" + ratioNouns + ";" + acceptAdjs + ";" + rejectAdjs + ";" + ratioAdjs ;
				
				resultsFile.println(outputTemp);
			}
		}
		
		Framework.log("Finished successfully");
		resultsFile.close();
	}
	
	/**
	 * A method to calculate the relevant scores/thresholds with the given input parameters
	 * 
	 * @param input HashMap with the given terms
	 * @param alpha Currently investigated value of alpha
	 * @param beta Currently investigated value of beta
	 * @param DPs HashMap with DP values for terms
	 * @param DCs HashMap with DC values for terms
	 * @param maxDP
	 * @param maxDC
	 * @param fraction Currently investigated fraction value
	 * @return
	 */
	public static ArrayList<Integer> acceptRejectTerms(HashMap<String, Double> input, double alpha, double beta, HashMap<String, Double> DPs, HashMap<String, Double> DCs, double maxDP, double maxDC, double fraction)
	{
		int numAcceptTerms = 0;
		int numRejectTerms = 0;
		
		Framework.log("        Calculating scores");
		Double[] scores = new Double[input.size()];
		int i = 0;
		
		//Calculating score with the given alpha and beta
		for (String w : input.keySet()) 
		{
			double score = alpha * (DPs.get(w) / maxDP) + beta * (DCs.get(w) / maxDC);
			input.put(w, score);
			scores[i] = score;
			i++;
		}
		
		Framework.log("        Calculating threshold");
		Arrays.sort(scores);
		double ind = fraction * input.size();
		int index = (int) ind;			
		double scoreThreshold = scores[input.size() - 1 - index];	

		
		Framework.log("        Accepting/rejecting terms");
		System.out.println("Enter 'a' to accept the term");
		System.out.println("and 'r' to reject the term");
		Scanner in = new Scanner(System.in);
		
		//Processing all the terms
		for (String w : input.keySet()) {
			double score = input.get(w);
			if (score > scoreThreshold) 
			{
				String input1;
				//If the term was already accepted/rejected by the user
				if (acceptReject.containsKey(w))
				{
					input1 = acceptReject.get(w);
					
					if (input1.equals("a"))
					{
						Framework.log("Term was already accepted");
					}
					else
					{
						Framework.log("Term was already rejected");
					}
				}
				//First occurrence of a term
				else
				{
					if (w.contains("@"))
					{
						String[] temp = getLemmasFromSynset(w);
						List<String> temp2 = Arrays.asList(temp);
						System.out.println("Term: " + w + " - " + temp2);
					}
					else
					{
						System.out.println("Term: " + w);
					}
					
					input1 = in.next();
					
					//Instead of accepting/rejecting all the terms for every parameter combination, the output is saved here
					//Consequently, a parameter needs to be accepted/rejected only once
					acceptReject.put(w, input1);
				}

				if (input1.equals("a")) 
				{
					numAcceptTerms++;
				}  else 
				{
					numRejectTerms++;
				}
			}
		}
		
		ArrayList<Integer> output = new ArrayList<Integer>();
		output.add(numAcceptTerms);
		output.add(numRejectTerms);
		
		return output;
	}
	
	/**
	 * A method that returns a set of synonyms contained in a given synset
	 * 
	 * @param synset
	 * @return array with lemmas of the given synset
	 */
	public static String[] getLemmasFromSynset(String synset)
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
