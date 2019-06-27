package edu.eur.absa.embeddings;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.paukov.combinatorics3.Generator;

import edu.eur.absa.Framework;

/**
 * A class that returns a set of all the possible combinations (without repetitions) between elements in a given array
 * 
 * @author Ewelina Dera
 *
 */
public class SynsetPairs 
{

	public static void main(String[] args) throws Exception
	{
        File input = new File(Framework.EXTERNALDATA_PATH + "SynsetVocabulary.txt");    //Input array with all the elements (synsets)
        Scanner scan = new Scanner(input);

        PrintWriter outputFile = new PrintWriter(Framework.EXTERNALDATA_PATH + "SynsetEncodedPairs.txt");

        ArrayList<String> synsetList = new ArrayList<String>();

        int counter = 0;
        while (scan.hasNextLine()) 
        {
            String temp = scan.nextLine();
            synsetList.add(temp);
            counter++;
        }
        
        System.out.println("Counter " + counter);
		
        Generator.combination(synsetList)
        .simple(2)
        .stream()
        .forEach(outputFile::println);
        
        outputFile.close();
        scan.close();

	}

}
