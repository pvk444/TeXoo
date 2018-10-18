package de.datexis.preprocess;

import de.datexis.model.Document;
import de.datexis.model.Sentence;
import de.datexis.model.Token;

import edu.stanford.nlp.ling.CoreLabel;

import java.util.List;

public interface IDocumentFactory {
  int AVERAGE_ENGLISH_SENTENCE_LENGTH = 14;

  static DocumentFactory getInstance() {
    return DocumentFactory.instance;
  }

  /**
   * Creates a Document with Sentences and Tokens from a String.
   * Uses Stanford CoreNLP PTBTokenizerFactory and removes Tabs, Newlines and trailing whitespace.
   * Use fromText(text, true) to keep the original String in memory.
   *
   * @return TeXoo document model with tokenized text.
   */
  static Document fromText(String text) {
    return DocumentFactory.instance.createFromText(text);
  }

  static Document fromText(String text, Newlines newlines) {
    return DocumentFactory.instance.createFromText(text, newlines);
  }

  /**
   * Creates a Document from existing Tokens, processing Span positions and Sentence splitting.
   */
  static Document fromTokens(List<Token> tokens) {
    return DocumentFactory.instance.createFromTokens(tokens);
  }

  /**
   * Create Tokens from raw text, without sentence splitting.
   */
  static List<Token> createTokensFromText(String text) {
    return DocumentFactory.instance.tokenizeFast(text);
  }

  /**
   * Create Tokens from tokenized text, without sentence splitting.
   */
  static List<Token> createTokensFromTokenizedText(String text) {
    return DocumentFactory.instance.createTokensFromTokenizedText(text, 0);
  }

  static String getLanguage(String text) {
    return DocumentFactory.instance.detectLanguage(text);
  }

  static Sentence createSentenceFromTokens(List<Token> sentence) {
    return DocumentFactory.instance.createSentenceFromTokens(sentence, "", 0);
  }

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
