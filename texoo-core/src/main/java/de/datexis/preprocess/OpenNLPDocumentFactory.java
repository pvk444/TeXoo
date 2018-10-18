package de.datexis.preprocess;

import de.datexis.model.Document;
import de.datexis.model.Sentence;
import de.datexis.model.Token;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.deeplearning4j.text.annotator.SentenceAnnotator;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.UimaTokenizerFactory;
import org.deeplearning4j.text.uima.UimaResource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class OpenNLPDocumentFactory implements IDocumentFactory {
  @Override
  public Document createFromText(String text) {
    return null;
  }

  @Override
  public Document createFromText(String text, Newlines newlines) {
    return null;
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

    } catch (ResourceInitializationException | CASException e) {
      e.printStackTrace();
    }
    return splittedSentences;
  }

  @NotNull
  private UimaResource prepareSentenceSplitter() throws ResourceInitializationException {
    AnalysisEngineDescription sentenceSplitterDescr = SentenceAnnotator.getDescription();
    UimaResource sentenceSplitterResource = new UimaResource(
      AnalysisEngineFactory.createEngine(AnalysisEngineFactory
                                           .createEngineDescription(sentenceSplitterDescr)));
    return sentenceSplitterResource;
  }

  private void tokenize() {
    try {
      TokenizerFactory tokenizerFactory = new UimaTokenizerFactory();
      Tokenizer tokenizer = tokenizerFactory.create("please tokenize this correctly.");


      //get the whole list of tokens
      List<String> tokens = tokenizer.getTokens();
      System.out.println(tokens);
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    }
  }

}
