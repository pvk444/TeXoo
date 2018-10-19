package de.datexis.preprocess;

import de.datexis.model.Document;
import de.datexis.model.OpenNLPDocument;
import de.datexis.model.Sentence;
import de.datexis.model.Token;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.xerces.impl.xpath.regex.RegularExpression;
import org.deeplearning4j.text.annotator.SentenceAnnotator;
import org.deeplearning4j.text.annotator.TokenizerAnnotator;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.deeplearning4j.text.uima.UimaResource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class OpenNLPDocumentFactory implements IDocumentFactory {

  protected static OpenNLPDocumentFactory instance;

  // TODO: fixme!
  static {
    try {
      instance = new OpenNLPDocumentFactory();
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    }
  }

  private final TokenizerFactory tokenizerFactory;


  /**
   * Creates a Document with Sentences and Tokens from a String.
   * Uses Stanford CoreNLP PTBTokenizerFactory and removes Tabs, Newlines and trailing whitespace.
   * Use fromText(text, true) to keep the original String in memory.
   *
   * @return TeXoo document model with tokenized text.
   */
  public static Document fromText(String text) {
    return OpenNLPDocumentFactory.instance.createFromText(text);
  }

  public static Document fromText(String text, Newlines newlines) {
    return OpenNLPDocumentFactory.instance.createFromText(text, newlines);
  }

  /**
   * Creates a Document from existing Tokens, processing Span positions and Sentence splitting.
   */
  public static Document fromTokens(List<Token> tokens) {
    return OpenNLPDocumentFactory.instance.createFromTokens(tokens);
  }

  /**
   * Create Tokens from raw text, without sentence splitting.
   */
  public static List<Token> createTokensFromText(String text) {
    return OpenNLPDocumentFactory.instance.tokenizeFast(text);
  }

  /**
   * Create Tokens from tokenized text, without sentence splitting.
   */
  public static List<Token> createTokensFromTokenizedText(String text) {
    return OpenNLPDocumentFactory.instance.createTokensFromTokenizedText(text, 0);
  }

  public static String getLanguage(String text) {
    return OpenNLPDocumentFactory.instance.detectLanguage(text);
  }

  public static Sentence createSentenceFromTokens(List<Token> sentence) {
    return OpenNLPDocumentFactory.instance.createSentenceFromTokens(sentence, "", 0);
  }

  public static OpenNLPDocumentFactory getInstance() {
    return instance;
  }

  public OpenNLPDocumentFactory() throws ResourceInitializationException {

    tokenizerFactory = new UimaTokenizerFactory();

  }

  @Override
  public Document createFromText(String text) {
    return null;
  }

  @Override
  public Document createFromText(String text, Newlines newlines) {
    Document document = new OpenNLPDocument();
    List<String> sentenceStrings = splitSentences(text);
    List<Sentence> sentences = sentenceStrings.stream().map(sentenceString -> {
      List<Token> tokens = tokenize(sentenceString);
      return new Sentence(tokens);
    }).collect(Collectors.toList());
    sentences.forEach(sentence -> document.addSentence(sentence, false));

    return document;
  }

  @Override
  public void addToDocumentFromText(String text, Document doc, Newlines newlines) {

  }

  @Override
  public List<Token> tokenizeFast(String text) {
    return null;
  }

  @Override
  public String detectLanguage(String text) {
    return null;
  }

  @Override
  public Document createFromTokens(List<Token> tokens) {
    return null;
  }

  @Override
  public List<Sentence> createSentencesFromTokens(List<Token> tokens) {
    return null;
  }

  @Override
  public Sentence createSentenceFromTokens(List<Token> sentence, String last, Integer cursor) {
    return null;
  }

  @Override
  public List<Token> createTokensFromText(String text, int offset) {
    return null;
  }

  @Override
  public List<Token> createTokensFromTokenizedText(String text, int offset) {
    return null;
  }

  @Override
  public void retokenize(Document doc) {

  }

  private List<String> splitSentences(String text) {
    List<String> splittedSentences = new ArrayList<>();
    try {
      UimaResource sentenceSplitter = prepareSentenceSplitter();
      CAS process = sentenceSplitter.process(text);
      Collection<org.cleartk.token.type.Sentence> uimaSentences = JCasUtil
        .select(process.getJCas(), org.cleartk.token.type.Sentence.class);
      splittedSentences = uimaSentences.stream()
                                       .map(Annotation::getCoveredText)
                                       .collect(Collectors.toList());

      splittedSentences = splitOnNewline(splittedSentences);

    } catch (ResourceInitializationException | CASException e) {
      e.printStackTrace();
    }
    return splittedSentences;
  }

  @NotNull
  private List<String> splitOnNewline(List<String> splittedSentences) {
    List<String> sentenceStringsWithNewlinesplits = new ArrayList<>(splittedSentences.size());
    for (String sentenceString : splittedSentences) {
      if (sentenceString.contains("\n")) {
        String[] split = sentenceString.split("\n");
        sentenceStringsWithNewlinesplits.addAll(Arrays.asList(split));
      }else {
        sentenceStringsWithNewlinesplits.add(sentenceString);
      }
    }
    return sentenceStringsWithNewlinesplits;
  }

  @NotNull
  private UimaResource prepareSentenceSplitter() throws ResourceInitializationException {
    AnalysisEngineDescription sentenceSplitterDescr = SentenceAnnotator.getDescription();
    AnalysisEngineDescription engineDescription = AnalysisEngineFactory
      .createEngineDescription(sentenceSplitterDescr);
    UimaResource sentenceSplitterResource = new UimaResource(
      AnalysisEngineFactory.createEngine(engineDescription));
    return sentenceSplitterResource;
  }

  // FIXME: this tokenizer discards new lines always this might become a problem
  private List<Token> tokenize(String text) {
    
    Tokenizer tokenizer = tokenizerFactory.create(text);
    List<String> tokens = tokenizer.getTokens();
    List<Token> texooTokens = new ArrayList<>();
    int offset = 0;
    for (String token : tokens) {
      offset = adjustOffsetForPunctuation(offset, token);
      Token texooToken = new Token(token, offset ,offset + token.length());
      texooTokens.add(texooToken);
      offset = texooToken.getEnd() + 1;
    }
    
    return texooTokens;
  }

  private int adjustOffsetForPunctuation(int lastEnd, String token) {
    if (token.matches("\\.|[!?]+")){
      lastEnd -= 1;
    } else if (token.matches("[)}\\]\"'>＂＇＞]")){
      lastEnd -= 1;
    }else if (token.matches("[({\\[<]")){
      lastEnd += 1;
    }
    return lastEnd;
  }

}
