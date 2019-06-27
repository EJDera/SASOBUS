package edu.eur.absa.embeddings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.eur.absa.Framework;
import edu.mit.jsemcor.main.*;
import edu.mit.jsemcor.element.*;

/**
 * A class with different methods for SemCor
 * 
 * @author Ewelina Dera
 *
 */
public class SemCor 
{
	/**
	 * A method to get all the sentences from a given brown corpus
	 * 
	 * @param brown Brown corpus
	 * @return list of all the sentences for the given Brown corpus
	 * @throws IOException
	 */
	public static List<ISentence> getSemCorSentences(String brown) throws IOException
	{
		List<ISentence> sentencesList = new ArrayList<ISentence>();
		
		//Construct the URL to the Semcor directory
		String path = Framework.EXTERNALDATA_PATH + "semcor3.0/";
		URL url = new URL("file", null, path);
		
		//Construct the semcor object and open it
		IConcordanceSet semcor = new Semcor(url);
		semcor.open();
		
		IConcordance concord = semcor.get(brown);
		for (int i = 0; i < getAllContexts(brown).size(); i ++)
		{
			String currentContext = getAllContexts(brown).get(i);
			IContext context = concord.getContext(currentContext);
			sentencesList.addAll(context.getSentences());
		}
		semcor.close();
		
		return sentencesList;	
	}
	
	/**
	 * A method to get a text representation of a given word
	 * 
	 * @param word
	 * @return lexicalization of the input word
	 */
	public static String getText(IWordform word)
	{
		String text = word.getText();
		return text;
	}
	
	/**
	 * A method to get a sense keys from a given word
	 * 
	 * @param word
	 * @return list of sense keys of the input word
	 */
	public static List<String> getSenseKeys(IWordform word)
	{
		List<String> senseKeys = word.getSemanticTag().getSenseKeys();
		return senseKeys;
	}
	
	/**
	 * A method to get a sense numbers of a given word
	 * 
	 * @param word
	 * @return list of sense numbers of the input word
	 */
	public static List<Integer> getSenseNumber(IWordform word)
	{
		List<Integer> senseNumber = word.getSemanticTag().getSenseNumber();
		return senseNumber;
	}
	
	/**
	 * A method to get all of the contexts of a given concordance
	 * 
	 * @param concordance the concordance for which all the contexts will be given
	 * return list of all contexts
	 */
	public static List<String> getAllContexts(String concordance)
	{
		List<String> listOfContexts = new ArrayList<String>();
		
		if (concordance == "brown1")
		{
			listOfContexts.add("br-a01");
			listOfContexts.add("br-a02");
			listOfContexts.add("br-a11");
			listOfContexts.add("br-a12");
			listOfContexts.add("br-a13");
			listOfContexts.add("br-a14");
			listOfContexts.add("br-a15");
			listOfContexts.add("br-b13");
			listOfContexts.add("br-b20");
			listOfContexts.add("br-c01");
			listOfContexts.add("br-c02");
			listOfContexts.add("br-c04");
			listOfContexts.add("br-d01");
			listOfContexts.add("br-d02");
			listOfContexts.add("br-d03");
			listOfContexts.add("br-d04");
			listOfContexts.add("br-e01");
			listOfContexts.add("br-e02");
			listOfContexts.add("br-e04");
			listOfContexts.add("br-e21");
			listOfContexts.add("br-e24");
			listOfContexts.add("br-e29");
			listOfContexts.add("br-f03");
			listOfContexts.add("br-f10");
			listOfContexts.add("br-f19");
			listOfContexts.add("br-f43");
			listOfContexts.add("br-g01");
			listOfContexts.add("br-g11");
			listOfContexts.add("br-g15");
			listOfContexts.add("br-h01");
			listOfContexts.add("br-j01");
			listOfContexts.add("br-j02");
			listOfContexts.add("br-j03");
			listOfContexts.add("br-j04");
			listOfContexts.add("br-j05");
			listOfContexts.add("br-j06");
			listOfContexts.add("br-j07");
			listOfContexts.add("br-j08");
			listOfContexts.add("br-j09");
			listOfContexts.add("br-j10");
			listOfContexts.add("br-j11");
			listOfContexts.add("br-j12");
			listOfContexts.add("br-j13");
			listOfContexts.add("br-j14");
			listOfContexts.add("br-j15");
			listOfContexts.add("br-j16");
			listOfContexts.add("br-j17");
			listOfContexts.add("br-j18");
			listOfContexts.add("br-j19");
			listOfContexts.add("br-j20");
			listOfContexts.add("br-j22");
			listOfContexts.add("br-j23");
			listOfContexts.add("br-j37");
			listOfContexts.add("br-j52");
			listOfContexts.add("br-j53");
			listOfContexts.add("br-j54");
			listOfContexts.add("br-j55");
			listOfContexts.add("br-j56");
			listOfContexts.add("br-j57");
			listOfContexts.add("br-j58");
			listOfContexts.add("br-j59");
			listOfContexts.add("br-j60");
			listOfContexts.add("br-j70");
			listOfContexts.add("br-k01");
			listOfContexts.add("br-k02");
			listOfContexts.add("br-k03");
			listOfContexts.add("br-k04");
			listOfContexts.add("br-k05");
			listOfContexts.add("br-k06");
			listOfContexts.add("br-k07");
			listOfContexts.add("br-k08");
			listOfContexts.add("br-k09");
			listOfContexts.add("br-k10");
			listOfContexts.add("br-k11");
			listOfContexts.add("br-k12");
			listOfContexts.add("br-k13");
			listOfContexts.add("br-k14");
			listOfContexts.add("br-k15");
			listOfContexts.add("br-k16");
			listOfContexts.add("br-k17");
			listOfContexts.add("br-k18");
			listOfContexts.add("br-k19");
			listOfContexts.add("br-k20");
			listOfContexts.add("br-k21");
			listOfContexts.add("br-k22");
			listOfContexts.add("br-k23");
			listOfContexts.add("br-k24");
			listOfContexts.add("br-k25");
			listOfContexts.add("br-k26");
			listOfContexts.add("br-k27");
			listOfContexts.add("br-k28");
			listOfContexts.add("br-k29");
			listOfContexts.add("br-l11");
			listOfContexts.add("br-l12");
			listOfContexts.add("br-m01");
			listOfContexts.add("br-m02");
			listOfContexts.add("br-n05");
			listOfContexts.add("br-p01");
			listOfContexts.add("br-r05");
			listOfContexts.add("br-r06");
			listOfContexts.add("br-r07");
			listOfContexts.add("br-r08");
			listOfContexts.add("br-r09");

		}
		else if (concordance == "brown2")
		{
			listOfContexts.add("br-e22");
			listOfContexts.add("br-e23");
			listOfContexts.add("br-e25");
			listOfContexts.add("br-e26");
			listOfContexts.add("br-e27");
			listOfContexts.add("br-e28");
			listOfContexts.add("br-e30");
			listOfContexts.add("br-e31");
			listOfContexts.add("br-f08");
			listOfContexts.add("br-f13");
			listOfContexts.add("br-f14");
			listOfContexts.add("br-f15");
			listOfContexts.add("br-f16");
			listOfContexts.add("br-f17");
			listOfContexts.add("br-f18");
			listOfContexts.add("br-f20");
			listOfContexts.add("br-f21");
			listOfContexts.add("br-f22");
			listOfContexts.add("br-f23");
			listOfContexts.add("br-f24");
			listOfContexts.add("br-f25");
			listOfContexts.add("br-f33");
			listOfContexts.add("br-f44");
			listOfContexts.add("br-g12");
			listOfContexts.add("br-g14");
			listOfContexts.add("br-g16");
			listOfContexts.add("br-g17");
			listOfContexts.add("br-g18");
			listOfContexts.add("br-g19");
			listOfContexts.add("br-g20");
			listOfContexts.add("br-g21");
			listOfContexts.add("br-g22");
			listOfContexts.add("br-g23");
			listOfContexts.add("br-g28");
			listOfContexts.add("br-g31");
			listOfContexts.add("br-g39");
			listOfContexts.add("br-g43");
			listOfContexts.add("br-g44");
			listOfContexts.add("br-h09");
			listOfContexts.add("br-h11");
			listOfContexts.add("br-h12");
			listOfContexts.add("br-h13");
			listOfContexts.add("br-h14");
			listOfContexts.add("br-h15");
			listOfContexts.add("br-h16");
			listOfContexts.add("br-h17");
			listOfContexts.add("br-h18");
			listOfContexts.add("br-h21");
			listOfContexts.add("br-h24");
			listOfContexts.add("br-j29");
			listOfContexts.add("br-j30");
			listOfContexts.add("br-j31");
			listOfContexts.add("br-j32");
			listOfContexts.add("br-j33");
			listOfContexts.add("br-j34");
			listOfContexts.add("br-j35");
			listOfContexts.add("br-j38");
			listOfContexts.add("br-j41");
			listOfContexts.add("br-j42");
			listOfContexts.add("br-l08");
			listOfContexts.add("br-l09");
			listOfContexts.add("br-l10");
			listOfContexts.add("br-l13");
			listOfContexts.add("br-l14");
			listOfContexts.add("br-l15");
			listOfContexts.add("br-l16");
			listOfContexts.add("br-l17");
			listOfContexts.add("br-l18");
			listOfContexts.add("br-n09");
			listOfContexts.add("br-n10");
			listOfContexts.add("br-n11");
			listOfContexts.add("br-n12");
			listOfContexts.add("br-n14");
			listOfContexts.add("br-n15");
			listOfContexts.add("br-n16");
			listOfContexts.add("br-n17");
			listOfContexts.add("br-n20");
			listOfContexts.add("br-p07");
			listOfContexts.add("br-p09");
			listOfContexts.add("br-p10");
			listOfContexts.add("br-p12");
			listOfContexts.add("br-p24");
			listOfContexts.add("br-r04");
			
		}
		else if (concordance == "brownv")
		{
			listOfContexts.add("br-a03");
			listOfContexts.add("br-a04");
			listOfContexts.add("br-a05");
			listOfContexts.add("br-a06");
			listOfContexts.add("br-a07");
			listOfContexts.add("br-a08");
			listOfContexts.add("br-a09");
			listOfContexts.add("br-a10");
			listOfContexts.add("br-a16");
			listOfContexts.add("br-a17");
			listOfContexts.add("br-a18");
			listOfContexts.add("br-a19");
			listOfContexts.add("br-a20");
			listOfContexts.add("br-a21");
			listOfContexts.add("br-a22");
			listOfContexts.add("br-a23");
			listOfContexts.add("br-a24");
			listOfContexts.add("br-a25");
			listOfContexts.add("br-a26");
			listOfContexts.add("br-a27");
			listOfContexts.add("br-a28");
			listOfContexts.add("br-a29");
			listOfContexts.add("br-a30");
			listOfContexts.add("br-a31");
			listOfContexts.add("br-a32");
			listOfContexts.add("br-a33");
			listOfContexts.add("br-a34");
			listOfContexts.add("br-a35");
			listOfContexts.add("br-a36");
			listOfContexts.add("br-a37");
			listOfContexts.add("br-a38");
			listOfContexts.add("br-a39");
			listOfContexts.add("br-a40");
			listOfContexts.add("br-a41");
			listOfContexts.add("br-a42");
			listOfContexts.add("br-a43");
			listOfContexts.add("br-a44");
			listOfContexts.add("br-b01");
			listOfContexts.add("br-b02");
			listOfContexts.add("br-b03");
			listOfContexts.add("br-b04");
			listOfContexts.add("br-b05");
			listOfContexts.add("br-b06");
			listOfContexts.add("br-b07");
			listOfContexts.add("br-b08");
			listOfContexts.add("br-b09");
			listOfContexts.add("br-b10");
			listOfContexts.add("br-b11");
			listOfContexts.add("br-b12");
			listOfContexts.add("br-b14");
			listOfContexts.add("br-b15");
			listOfContexts.add("br-b16");
			listOfContexts.add("br-b17");
			listOfContexts.add("br-b18");
			listOfContexts.add("br-b19");
			listOfContexts.add("br-b21");
			listOfContexts.add("br-b22");
			listOfContexts.add("br-b23");
			listOfContexts.add("br-b24");
			listOfContexts.add("br-b25");
			listOfContexts.add("br-b26");
			listOfContexts.add("br-b27");
			listOfContexts.add("br-c03");
			listOfContexts.add("br-c05");
			listOfContexts.add("br-c06");
			listOfContexts.add("br-c07");
			listOfContexts.add("br-c08");
			listOfContexts.add("br-c09");
			listOfContexts.add("br-c10");
			listOfContexts.add("br-c11");
			listOfContexts.add("br-c12");
			listOfContexts.add("br-c13");
			listOfContexts.add("br-c14");
			listOfContexts.add("br-c15");
			listOfContexts.add("br-c16");
			listOfContexts.add("br-c17");
			listOfContexts.add("br-d05");
			listOfContexts.add("br-d06");
			listOfContexts.add("br-d07");
			listOfContexts.add("br-d08");
			listOfContexts.add("br-d09");
			listOfContexts.add("br-d10");
			listOfContexts.add("br-d11");
			listOfContexts.add("br-d12");
			listOfContexts.add("br-d13");
			listOfContexts.add("br-d14");
			listOfContexts.add("br-d15");
			listOfContexts.add("br-d16");
			listOfContexts.add("br-d17");
			listOfContexts.add("br-e03");
			listOfContexts.add("br-e05");
			listOfContexts.add("br-e06");
			listOfContexts.add("br-e07");
			listOfContexts.add("br-e08");
			listOfContexts.add("br-e09");
			listOfContexts.add("br-e10");
			listOfContexts.add("br-e11");
			listOfContexts.add("br-e12");
			listOfContexts.add("br-e13");
			listOfContexts.add("br-e14");
			listOfContexts.add("br-e15");
			listOfContexts.add("br-e16");
			listOfContexts.add("br-e17");
			listOfContexts.add("br-e18");
			listOfContexts.add("br-e19");
			listOfContexts.add("br-e20");
			listOfContexts.add("br-f01");
			listOfContexts.add("br-f02");
			listOfContexts.add("br-f04");
			listOfContexts.add("br-f05");
			listOfContexts.add("br-f06");
			listOfContexts.add("br-f07");
			listOfContexts.add("br-f09");
			listOfContexts.add("br-f11");
			listOfContexts.add("br-f12");
			listOfContexts.add("br-g02");
			listOfContexts.add("br-g03");
			listOfContexts.add("br-g04");
			listOfContexts.add("br-g05");
			listOfContexts.add("br-g06");
			listOfContexts.add("br-g07");
			listOfContexts.add("br-g08");
			listOfContexts.add("br-g09");
			listOfContexts.add("br-g10");
			listOfContexts.add("br-g13");
			listOfContexts.add("br-h02");
			listOfContexts.add("br-h03");
			listOfContexts.add("br-h04");
			listOfContexts.add("br-h05");
			listOfContexts.add("br-h06");
			listOfContexts.add("br-h07");
			listOfContexts.add("br-h08");
			listOfContexts.add("br-h10");
			listOfContexts.add("br-j21");
			listOfContexts.add("br-j24");
			listOfContexts.add("br-j25");
			listOfContexts.add("br-j26");
			listOfContexts.add("br-j27");
			listOfContexts.add("br-j28");
			listOfContexts.add("br-l01");
			listOfContexts.add("br-l02");
			listOfContexts.add("br-l03");
			listOfContexts.add("br-l04");
			listOfContexts.add("br-l05");
			listOfContexts.add("br-l06");
			listOfContexts.add("br-l07");
			listOfContexts.add("br-m03");
			listOfContexts.add("br-m04");
			listOfContexts.add("br-m05");
			listOfContexts.add("br-m06");
			listOfContexts.add("br-n01");
			listOfContexts.add("br-n02");
			listOfContexts.add("br-n03");
			listOfContexts.add("br-n04");
			listOfContexts.add("br-n06");
			listOfContexts.add("br-n07");
			listOfContexts.add("br-n08");
			listOfContexts.add("br-p02");
			listOfContexts.add("br-p03");
			listOfContexts.add("br-p04");
			listOfContexts.add("br-p05");
			listOfContexts.add("br-p06");
			listOfContexts.add("br-p08");
			listOfContexts.add("br-r01");
			listOfContexts.add("br-r02");
			listOfContexts.add("br-r03");
		}
		
		return listOfContexts;
	}

}
