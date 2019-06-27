# SASOBUS

The Semi-Automatic Sentiment Ontology Building Using Synsets (SASOBUS) framework is built upon [SOBA](https://github.com/lisazhuang/SOBA). 
Differently than other methods, this framework makes use of WordNet synsets in term extraction, concept formation and concept subsumption.

## Installation instructions

1. Download the DataSASOBUS folder: https://1drv.ms/u/s!Ao_FuxrvQWwpg986t2VdzsWc8XDGbg?e=Cq1GSM 
2. Put the contents of DataSASOBUS\SASOBUS_Data.zip into 2 folders:
    - SASOBUS\src\main\resources\externalData
    - SASOBUS\target\classes\externalData

## Directory explanation

- SASOBUS package edu.eur.absa.embeddings
  - ContrastiveCorpusToSynsets.java – a class which preprocesses (with NLP pipeline) the contrastive corpus (i.e., English prose) and also finds synsets. It returns a file, which has words replaced by synsets
  - SemCor.java – a class, which gets the SemCor Brown Corpus files and transforms them to txt file. It also provides some methods for the JSemCor library
  - SemCorToTextSynstes.java – a class which transforms the original SemCor text into a text file, where words are replaced with synsets
  - SimplifiedLeskOnSemCor.java – a class, which performs WSD on SemCor and returns the accuracy of the Simplified Lesk algorithm
  - SynsetPairs.java – a class that returns all combinations (without repetitions) between elements in an array. In this case, the array contains the synset vocabulary from the synset embeddings model
  - Synsets.java – a class, where the synset IDs (in both formats, i.e., in JSemCor and JAWS) can be encoded and decoded
  - WordNet.java – a class with methods for JWI library
  - YelpToTextSynsets.java – a class, where the Yelp restaurant reviews are preprocessed and the words are replaced with synsets

- SASOBUS package edu.eur.absa.ontologybuilder
  - GridSearchParameters.java -  a class which optimizes the parameters with grid search methods
  - GridSearchTermFrequency.java – a class which calculates all the DP and DC values. These values are later used by GridSearchParameters.java
  - MainOntoBuild.java – a class, which builds a semi-automatic ontology
  - OntologyBuilder.java – a class with methods to find terms, create hierarchy between concepts etc.

- SASOBUS package edu.eur.absa.seminarhelper
  - SeminarOntology.java – a class with methods for the ontology, e.g., adding classes or getting lex properties
  - TermFrequencyYelp.java – a class, which returns a file with frequencies of words/synsets in all the Yelp restaurant reviews
  - WordSenseDisambiguation.java – a class with the Simplified Lesk algorithm
