package de.datexis.sector.encoder;

import de.datexis.common.WordHelpers;
import de.datexis.encoder.impl.BagOfWordsEncoder;
import de.datexis.model.Span;
import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for Bag-Of-Words Headings with OTHER class and minWordLength
 * @author Sebastian Arnold <sarnold@beuth-hochschule.de>
 */
public class HeadingEncoder extends BagOfWordsEncoder {

  protected final static Logger log = LoggerFactory.getLogger(HeadingEncoder.class);
  public static final String ID = "HL";
  
  public static String OTHER_CLASS = "OTHER";
  
  public HeadingEncoder() {
    super(ID);
  }
  
  public void trainModel(List<String> headlines, int minWordFrequency, int minWordLength, WordHelpers.Language language) {
    appendTrainLog("Training " + getName() + " model...");
    setModel(null);
    totalWords = 0;
    timer.start();
    setLanguage(language);
    for(String s : headlines) {
      for(String t : WordHelpers.splitSpaces(s)) {
        String w = preprocessor.preProcess(t);
        if(!w.isEmpty()) {
          totalWords++;
          if(!wordHelpers.isStopWord(w) && w.length() >= minWordLength) {
            if(!vocab.containsWord(w)) vocab.addWord(w);
            else vocab.incrementWordCounter(w);
          }
        }
      }
    }
    int total = vocab.numWords();
    vocab.truncateVocabulary(minWordFrequency);
    vocab.addWord(OTHER_CLASS);
    vocab.updateHuffmanCodes();
    timer.stop();
    appendTrainLog("trained " + vocab.numWords() + " words (" +  total + " total)", timer.getLong());
    setModelAvailable(true);
  }
  
  @Override
  public INDArray encode(Iterable<? extends Span> spans) {
    INDArray vec = super.encode(spans);
    return vec.sumNumber().doubleValue()> 0. ? vec : encode(new String[] { OTHER_CLASS });
  }
  
  @Override
  protected INDArray encode(String[] words) {
    INDArray vec = super.encode(words);
    return vec.sumNumber().doubleValue()> 0. ? vec : encode(new String[] { OTHER_CLASS });
  }
  
  @Override
  public INDArray encodeSubsampled(String phrase) {
    INDArray vec = super.encodeSubsampled(phrase);
    return vec.sumNumber().doubleValue()> 0. ? vec : encode(new String[] { OTHER_CLASS });
  }
  
}
