package de.datexis.preprocess;

import de.datexis.model.Document;
import de.datexis.model.Sentence;
import de.datexis.model.Token;

import edu.stanford.nlp.ling.CoreLabel;

import java.util.List;

public interface IDocumentFactory {
  int AVERAGE_ENGLISH_SENTENCE_LENGTH = 14;

  Document createFromText(String text);

  Document createFromText(String text, Newlines newlines);

  void addToDocumentFromText(String text, Document doc, Newlines newlines);

  List<Token> tokenizeFast(String text);

  String detectLanguage(String text);

  Document createFromTokens(List<Token> tokens);

  List<Sentence> createSentencesFromTokens(List<Token> tokens);

  Sentence createSentenceFromTokens(List<Token> sentence, String last, Integer cursor);

  List<Token> createTokensFromText(String text, int offset);

  List<Token> createTokensFromTokenizedText(String text, int offset);

  void retokenize(Document doc);
  
  enum Newlines {KEEP, KEEP_DOUBLE, DISCARD}
}
