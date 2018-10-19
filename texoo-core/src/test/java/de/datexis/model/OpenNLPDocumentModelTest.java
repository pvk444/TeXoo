package de.datexis.model;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import de.datexis.encoder.impl.PositionEncoder;
import de.datexis.encoder.impl.SurfaceEncoder;
import de.datexis.model.Annotation.Source;
import de.datexis.model.tag.BIO2Tag;
import de.datexis.model.tag.BIOESTag;
import de.datexis.preprocess.DocumentFactory;
import de.datexis.preprocess.OpenNLPDocumentFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sebastian Arnold <sarnold@beuth-hochschule.de>
 */
public class OpenNLPDocumentModelTest {

  final private String medText = "Aspirin has an antiplatelet effect (e.g. preventing heart attacts, strokes and blood clot formation) by stopping the binding together of platelets [1]. "
    + "Aspirin is also known as acetylsalicylic acid.";

  final private String deText = "Prof. Dr. Alexander Löser ist Professor an der Beuth Hochschule für Technik Berlin.";

  final private String tabText = "123\tabc\n\n\nxyz  789\t\n 456 ";
  final private String spcText = "123 abc\nxyz  789  456"; // tabs and trailing whitespace is removed

  final private String originalText = "In March 2009 mayor Sue Jones-Davies, who had played the role of Judith Iscariot in the film Monty Python's Life of Brian (1979), organised a charity screening of the film.";
  final private String tokenizedText = "In March 2009 mayor Sue Jones-Davies , who had played the role of Judith Iscariot in the film Monty Python 's Life of Brian ( 1979 ) , organised a charity screening of the film .";
  
  final private String punctText = "Thomas Martin Lowry (* 26. Oktober 1874 in Low Moor, Bradford, West Yorkshire, England ; † 2. November 1936) war ein englischer Chemiker. " +
    "Die Kraniche des Ibykus ist eine Ballade von Friedrich Schiller aus dem Jahr 1797, die im 6. Jh. v. Chr. spielt.";

  ObjectMapper mapper = new ObjectMapper();

  public OpenNLPDocumentModelTest() {
  }

  @Test
  public void testCreateDocument() {
    ArrayList<Token> tokens = new ArrayList<>();
    tokens.add(new Token("Zairean"));
    tokens.add(new Token("Prime"));
    tokens.add(new Token("Minister"));
    tokens.add(new Token("Kengo"));
    tokens.add(new Token("wa"));
    tokens.add(new Token("Dondo"));
    tokens.add(new Token("said"));
    tokens.add(new Token("at"));
    tokens.add(new Token("the"));
    tokens.add(new Token("end"));
    tokens.add(new Token("of"));
    tokens.add(new Token("a"));
    tokens.add(new Token("visit"));
    tokens.add(new Token("."));
    assertEquals(14, tokens.size());
    OpenNLPDocument doc = (OpenNLPDocument) DocumentFactory.fromTokens(tokens);
    assertEquals(1, doc.countSentences());
    assertEquals(14, doc.countTokens());
    assertEquals(0, doc.getSentence(0).getToken(0).getBegin());
    assertEquals(7, doc.getSentence(0).getToken(0).getLength());
    assertEquals(7, doc.getSentence(0).getToken(0).getEnd());
    assertEquals(8, doc.getSentence(0).getToken(1).getBegin());
    assertEquals(5, doc.getSentence(0).getToken(1).getLength());
    assertEquals(8 + 5, doc.getSentence(0).getToken(1).getEnd());
    assertEquals(0, doc.getBegin());
    assertEquals(65, doc.getLength());
    assertEquals(65, doc.getEnd());
    assertEquals(0, doc.getSentence(0).getBegin());
    assertEquals(65, doc.getSentence(0).getLength());
    assertEquals(65, doc.getSentence(0).getEnd());
    assertEquals(doc, doc.getSentence(0).getDocumentRef());
  }

  @Test
  public void testTokenization() {
    OpenNLPDocument doc = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(tabText, DocumentFactory.Newlines.KEEP_DOUBLE); // "123\tabc\n\n\nxyz  789\t\n 456 "
    assertEquals(3, doc.countSentences());
    assertEquals(6, doc.countTokens());
    assertEquals(0, doc.getBegin());
    assertEquals(spcText.length(), doc.getEnd());
    assertEquals(spcText.length(), doc.getLength());
    assertEquals("123", doc.getSentence(0).getToken(0).getText());
    assertEquals(0, doc.getSentence(0).getToken(0).getBegin());
    assertEquals(3, doc.getSentence(0).getToken(0).getEnd());
    assertEquals("abc", doc.getSentence(0).getToken(1).getText());
    assertEquals(4, doc.getSentence(0).getToken(1).getBegin());
    assertEquals(7, doc.getSentence(0).getToken(1).getEnd());
    assertEquals("\n", doc.getSentence(0).getToken(2).getText()); // \n\n is newline
    assertEquals(7, doc.getSentence(0).getToken(2).getBegin());
    assertEquals(8, doc.getSentence(0).getToken(2).getEnd()); // newline counts as space, ok?
    assertEquals("xyz", doc.getSentence(1).getToken(0).getText());
    assertEquals(8, doc.getSentence(1).getToken(0).getBegin()); // newline counts as space, ok?
    assertEquals(11, doc.getSentence(1).getToken(0).getEnd());
    assertEquals("789", doc.getSentence(1).getToken(1).getText());
    assertEquals(13, doc.getSentence(1).getToken(1).getBegin()); // spacing is preserved
    assertEquals(16, doc.getSentence(1).getToken(1).getEnd());
    assertEquals("456", doc.getSentence(2).getToken(0).getText()); // \n is sentence boundary, but not newline
    assertEquals(18, doc.getSentence(2).getToken(0).getBegin()); // spacing is preserved
    assertEquals(21, doc.getSentence(2).getToken(0).getEnd());
    assertEquals(spcText, doc.getText());
  }

  @Test
  public void testPreprocessor() {
    OpenNLPDocument doc = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(medText); // don't keep original text
    assertEquals(2, doc.countSentences());
    assertEquals(36, doc.countTokens());
    assertEquals(0, doc.getBegin());
    assertEquals(198, doc.getEnd());
    assertEquals(198, doc.getLength());
    assertEquals(medText, doc.getText());
    assertEquals("(", doc.getSentence(0).getToken(5).getText());
    assertEquals("e.g.", doc.getSentence(0).getToken(6).getText());
    assertEquals("1", doc.getSentence(0).getToken(25).getText());
    assertEquals(152, doc.getSentence(1).getToken(0).getBegin());
    assertEquals(7, doc.getSentence(1).getToken(0).getLength());
    assertEquals(159, doc.getSentence(1).getToken(0).getEnd());
    OpenNLPDocument doc2 = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(deText);
    assertEquals(1, doc2.countSentences());
    assertEquals(14, doc2.countTokens());
    assertEquals(0, doc2.getBegin());
    assertEquals(83, doc2.getEnd());
    assertEquals(83, doc2.getLength());
    assertEquals(deText, doc2.getText());
    assertEquals("Dr.", doc2.getSentence(0).getToken(1).getText());
    assertEquals("Löser", doc2.getSentence(0).getToken(3).getText());
    OpenNLPDocument doc3 = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(punctText);
    for (Sentence s : doc3.getSentences()) {
      System.out.println(s.getText());
    }
    // assertEquals(2, doc3.countSentences()); // TODO: CoreNLP PTBTokenizer fails on this sentence
  }

  @Test
  public void testChangeDocument() {
    OpenNLPDocument doc = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(medText); // don't keep original text
    OpenNLPDocument doc2 = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(deText);
    doc.addSentence(doc2.getSentence(0));
    assertEquals(3, doc.countSentences());
    assertEquals(36 + 14, doc.countTokens());
    assertEquals(0, doc.getBegin());
    assertEquals(198 + 83 + 1, doc.getEnd());
    assertEquals(198 + 83 + 1, doc.getLength());
    // FIXME: Token positions are not yet updated! Refactor to keep offsets etc.
    assertEquals(medText + " " + deText, doc.getText());
    assertEquals(doc, doc.getSentence(0).getDocumentRef());
    assertEquals(doc, doc.getSentence(2).getDocumentRef());
    Document doc3 = new Sample(doc2.getSentences(), false);
    assertEquals(doc, doc3.getSentence(0).getDocumentRef());
    Document doc4 = new Sample(doc.getSentences(), false);
    assertEquals(doc, doc4.getSentence(0).getDocumentRef());
  }

  @Test
  public void testSpanTags() {
    OpenNLPDocument doc = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(medText);
    Token t = doc.getSentence(1).getToken(0);
    Object tag = t.getTag(Source.GOLD, BIO2Tag.class);
    assertNotNull(tag);
    assertEquals(BIO2Tag.class, tag.getClass());
    assertEquals(BIO2Tag.O(), tag);
    assertEquals(BIO2Tag.Label.O, BIO2Tag.O().get());
    t.putTag(Source.GOLD, BIOESTag.B());
    t.putTag(Source.PRED, BIOESTag.I());
    t.putTag(Source.PRED, BIO2Tag.B());
    tag = t.getTag(Source.GOLD, BIO2Tag.class);
    assertNotNull(tag);
    assertEquals(BIO2Tag.class, tag.getClass());
    assertEquals(BIO2Tag.O(), tag);
    tag = t.getTag(Source.GOLD, BIOESTag.class);
    assertNotNull(tag);
    assertEquals(BIOESTag.class, tag.getClass());
    assertEquals(BIOESTag.B(), tag);
    tag = t.getTag(Source.PRED, BIOESTag.class);
    assertNotNull(tag);
    assertEquals(BIOESTag.class, tag.getClass());
    assertEquals(BIOESTag.I(), tag);
    tag = t.getTag(Source.PRED, BIO2Tag.class);
    assertNotNull(tag);
    assertEquals(BIO2Tag.class, tag.getClass());
    assertEquals(BIO2Tag.B(), tag);
    t.putTag(Source.PRED, BIO2Tag.I());
    tag = t.getTag(Source.PRED, BIO2Tag.class);
    assertNotNull(tag);
    assertEquals(BIO2Tag.class, tag.getClass());
    assertEquals(BIO2Tag.I(), tag);
  }

  @Test
  public void testSpanVectors() {
    OpenNLPDocument doc = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(medText);
    Sentence s = doc.getSentence(1);
    Token t = s.getToken(0);
    SurfaceEncoder encoder = new SurfaceEncoder();
    INDArray vec = encoder.encode(t);
    INDArray vec2 = Nd4j.create(new double[]{1., 0., .5});
    assertEquals(encoder.getVectorSize(), vec.length());
    INDArray test = t.getVector(SurfaceEncoder.class);
    assertNull(test);
    t.putVector(encoder.getClass(), vec);
    test = t.getVector(SurfaceEncoder.class);
    assertNotNull(test);
    assertEquals(encoder.getVectorSize(), test.length());
    assertEquals(vec, test);
    t.putVector(PositionEncoder.class, vec2);
    s.clearVectors();
    test = t.getVector(SurfaceEncoder.class);
    assertEquals(vec, test);
    test = t.getVector(PositionEncoder.class);
    assertEquals(vec2, test);
    t.clearVectors();
    test = t.getVector(SurfaceEncoder.class);
    assertNull(test);
  }

  public void testSpanVectorSet() {
    // TODO: implement using EncoderSet
  }

  public void testStringCleanup() {
    // TODO: implement keepOnlyLowercase...
  }

  public void testStopwordRemoval() {
    // TODO: implement
  }

  @Test
  public void testDocumentSerialization() {
    OpenNLPDocument doc = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(medText);
    doc.setId("testID01");
    List<Token> aspirin = new ArrayList<>();
    aspirin.add(doc.getSentence(0).getToken(0));
    List<Token> antiplatelet = new ArrayList<>();
    antiplatelet.add(doc.getSentence(0).getToken(3));
    antiplatelet.add(doc.getSentence(0).getToken(4));
    //doc.getSentence(0).getToken(0).setUid(4711l);
    // TODO: Annotations have moved to texoo-models
    //doc.addAnnotation(new MentionAnnotation(Source.USER, aspirin));
    //doc.addAnnotation(new MentionAnnotation(Source.USER, antiplatelet));
    try {
      String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(doc);
      System.out.println(doc.getClass());
      System.out.println(json);
      OpenNLPDocument test = mapper.readerFor(OpenNLPDocument.class).readValue(json);
      assertEquals(OpenNLPDocument.class, test.getClass());
      assertEquals("testID01", test.getId());
      assertFalse(test.isEmpty());
      assertEquals(2, test.countSentences());
      assertEquals(2, test.countSentences());
      assertEquals(0, test.getBegin());
      assertEquals(198, test.getEnd());
      assertEquals(198, test.getLength());
      assertEquals(medText, test.getText());
      assertEquals(36, test.countTokens());
      // Tokens are not serialized
      //assertEquals((Long)4711l, test.getSentence(0).getToken(0).getUid());
      //assertNull(test.getSentence(0).getToken(1).getUid());
      //assertEquals(2, test.countAnnotations());
    } catch (IOException ex) {
      fail(ex.toString());
    }
  }

  @Test
  public void testRangeQueries() {
    OpenNLPDocument doc = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(medText);
    System.out.println(doc.getToken(0).get().getText() + " " + doc.getToken(0).get().getBegin() + "-" + doc.getToken(0).get().getEnd());
    System.out.println(doc.getToken(2).get().getText() + " " + doc.getToken(2).get().getBegin() + "-" + doc.getToken(2).get().getEnd());
    System.out.println(doc.getToken(3).get().getText() + " " + doc.getToken(3).get().getBegin() + "-" + doc.getToken(3).get().getEnd());
    System.out.println(doc.getToken(4).get().getText() + " " + doc.getToken(4).get().getBegin() + "-" + doc.getToken(4).get().getEnd());
    List<Token> tokens = doc.streamTokensInRange(0, 7, true).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("Aspirin", tokens.get(0).getText());

    tokens = doc.streamTokensInRange(0, 7, false).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("Aspirin", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(15, 27, true).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(16, 27, true).collect(Collectors.toList());
    assertEquals(0, tokens.size());
    tokens = doc.streamTokensInRange(14, 27, true).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(15, 28, true).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(15, 29, true).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(15, 34, true).collect(Collectors.toList());
    assertEquals(2, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    assertEquals("effect", tokens.get(1).getText());
    tokens = doc.streamTokensInRange(15, 35, true).collect(Collectors.toList());
    assertEquals(2, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    assertEquals("effect", tokens.get(1).getText());
    tokens = doc.streamTokensInRange(12, 34, true).collect(Collectors.toList());
    assertEquals(3, tokens.size());
    assertEquals("antiplatelet", tokens.get(1).getText());
    assertEquals("effect", tokens.get(2).getText());

    tokens = doc.streamTokensInRange(15, 27, false).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(16, 27, false).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(14, 27, false).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(15, 28, false).collect(Collectors.toList());
    assertEquals(1, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    tokens = doc.streamTokensInRange(15, 29, false).collect(Collectors.toList());
    assertEquals(2, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    assertEquals("effect", tokens.get(1).getText());
    tokens = doc.streamTokensInRange(15, 34, false).collect(Collectors.toList());
    assertEquals(2, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    assertEquals("effect", tokens.get(1).getText());
    tokens = doc.streamTokensInRange(15, 35, false).collect(Collectors.toList());
    assertEquals(2, tokens.size());
    assertEquals("antiplatelet", tokens.get(0).getText());
    assertEquals("effect", tokens.get(1).getText());
    tokens = doc.streamTokensInRange(12, 34, false).collect(Collectors.toList());
    assertEquals(3, tokens.size());
    assertEquals("antiplatelet", tokens.get(1).getText());
    assertEquals("effect", tokens.get(2).getText());

    // "Aspirin has an antiplatelet effect (e.g. preventing heart attacts, strokes and blood clot formation) by stopping the binding together of platelets [1]. "
//          + "Aspirin is also known as acetylsalicylic acid."

  }

  @Test
  public void addSentenceShouldPutBeginOfFirstSentenceOnPositionZeroWhenSentencesIsEmpty() {
    final int expectedSentenceBegin = 0;
    final int expectedDocumentBegin = 0;

    OpenNLPDocument document = new OpenNLPDocument();
    Sentence firstSentence = new Sentence();
    firstSentence.setBegin(0);
    firstSentence.setEnd(1);

    document.addSentence(firstSentence);
    Sentence sentenceRetrievedFromDocument = document.getSentence(0);

    assertThat(document.getBegin(), is(equalTo(expectedDocumentBegin)));
    assertThat(firstSentence.getBegin(), is(equalTo(expectedSentenceBegin)));
    assertThat(sentenceRetrievedFromDocument.getBegin(), is(equalTo(expectedSentenceBegin)));
  }  
  
  @Test
  public void addSentenceShouldPutBeginOfFirstSentenceOnPositionZeroWhenSentencesIsEmptyAndAdjustOffsetsIsFalse() {
    final int expectedSentenceBegin = 0;
    final int expectedDocumentBegin = 0;

    OpenNLPDocument document = new OpenNLPDocument();
    Sentence firstSentence = new Sentence();
    firstSentence.setBegin(0);
    firstSentence.setEnd(1);

    document.addSentence(firstSentence, false);
    Sentence sentenceRetrievedFromDocument = document.getSentence(0);

    assertThat(document.getBegin(), is(equalTo(expectedDocumentBegin)));
    assertThat(firstSentence.getBegin(), is(equalTo(expectedSentenceBegin)));
    assertThat(sentenceRetrievedFromDocument.getBegin(), is(equalTo(expectedSentenceBegin)));
  }

  @Test
  public void testTokenizedText() {
    OpenNLPDocument docOriginal = (OpenNLPDocument) OpenNLPDocumentFactory.fromText(originalText);
    List<Token> tokens = OpenNLPDocumentFactory.createTokensFromText(originalText);
    OpenNLPDocument docTokens = (OpenNLPDocument) OpenNLPDocumentFactory.fromTokens(tokens);
    List<Token> tokenized = OpenNLPDocumentFactory.createTokensFromTokenizedText(tokenizedText);
    OpenNLPDocument docTokenized = (OpenNLPDocument) OpenNLPDocumentFactory.fromTokens(tokenized);
    
    assertEquals(docOriginal.getText(), docTokens.getText());
    assertEquals(docOriginal.getText(), docTokenized.getText());
    assertEquals(docOriginal.countTokens(), docTokens.countTokens());
    assertEquals(docOriginal.countTokens(), docTokenized.countTokens());
    assertEquals(docOriginal.countSentences(), docTokens.countSentences());
    assertEquals(docOriginal.countSentences(), docTokenized.countSentences());
    
  }
  
}
