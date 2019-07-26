package edu.eur.absa.ontologybuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONException;

import edu.eur.absa.Framework;
import edu.eur.absa.model.exceptions.IllegalSpanException;
import edu.eur.absa.seminarhelper.SeminarOntology;

/**
 * The main for the semi-automatic ontology builder.
 * 
 * @author Karoliina Ranta
 * Adapted by Suzanne Veltman
 * Adapted by Lisa Zhuang
 * Adapted by Ewelina Dera
 */
public class MainOntoBuild {
	public static void main(String[] args) throws ClassNotFoundException, JSONException, IllegalSpanException, IOException {

		/* RESTAURANT DOMAIN */

		/* Start with the skeletal ontology. */
		SeminarOntology base = new SeminarOntology(Framework.EXTERNALDATA_PATH + "RestaurantOntologyThesisBase2018.owl");
		HashMap<String, HashSet<String>> aspectCategories = new HashMap<String, HashSet<String>>();

		HashSet<String> restaurant = new HashSet<String>();
		restaurant.add("general");
		restaurant.add("prices");
		restaurant.add("miscellaneous");
		aspectCategories.put("restaurant", restaurant);

		HashSet<String> ambience = new HashSet<String>();  
		ambience.add("general");
		aspectCategories.put("ambience", ambience);

		HashSet<String> service = new HashSet<String>();
		service.add("general");
		aspectCategories.put("service", service);

		HashSet<String> location = new HashSet<String>();
		location.add("general");
		aspectCategories.put("location", location);

		HashSet<String> sustenance = new HashSet<String>();
		sustenance.add("prices");
		sustenance.add("quality");
		sustenance.add("style&options");
		aspectCategories.put("sustenance", sustenance);

		/* Set the domain. */
		String domain = "restaurant";

		// set the fractions
		double[] fraction = new double[9];	
		fraction[0]=0.11;	//fraction nouns
		fraction[1]=0.50;   //alpha for nouns
		fraction[2]=0.50;   //beta for nouns
		fraction[3]=0.14; 	//fraction adjectives
		fraction[4]=0.10;   //alpha for adjectives
		fraction[5]=0.90;   //beta for adjectives
		fraction[6]=0.10;   //fraction verbs
		fraction[7]=1.00;   //alpha for verbs
		fraction[8]=0.00;   //beta for verbs

		//set the threshold 
		double threshold = 0.20;

		/* Initialise the semi-automatic ontology builder. */
		OntologyBuilder build = new OntologyBuilder(base, aspectCategories, domain, threshold, 1.0, fraction, true);
		
		build.save("EwelinaTestSkeletalOntologyFINAL.owl");

		/* Load the contrasting text. */
		build.loadContrast();

		/* Load the reviews to
		 *  be used to build the ontology. */
		try {
			build.loadReviews();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean verbs=true; boolean nouns=true; boolean adj=true;

		/* Find important terms and add them to the ontology. */
		build.findTerms(nouns, adj, verbs);

		/* Get the stats. */
		int[] stats = build.getStats();
		System.out.println("Number accepted: " + stats[0]);
		System.out.println("Number rejected: " + stats[1]);
		System.out.println("Ratio accepted: " + (double) stats[0] / ( (double) stats[0] + (double) stats[1] ));

		/* Save the built ontology. */
		build.save("EwelinaOntologyRestaurantAutomaticFINAL.owl");
	}
}
