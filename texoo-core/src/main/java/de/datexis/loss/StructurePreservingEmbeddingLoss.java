package de.datexis.loss;

import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.primitives.Pair;

public class StructurePreservingEmbeddingLoss implements ILossFunction {


  private int margin;

  @Override
  public double computeScore(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask, boolean average) {
    INDArray scoreArr = scoreArray(labels, preOutput, activationFn, mask);

    double score = scoreArr.sumNumber().doubleValue();

    if (average) {
      score /= scoreArr.size(0);
    }

    return score;
  }

  private void applyMask(INDArray mask, INDArray scoreArr) {
    if (mask != null) {
      scoreArr.muliColumnVector(mask);
    }
  }

  @Override
  public INDArray computeScoreArray(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask) {
    INDArray scoreArray = scoreArray(labels, preOutput, activationFn, mask);

    return scoreArray.sum(1);
  }

  public INDArray scoreArray(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask) {
    INDArray scoreArr = activationFn.getActivation(preOutput, true);
    INDArray x = scoreArr.get(NDArrayIndex.all(), NDArrayIndex.interval(0, 2));
    INDArray y = scoreArr.get(NDArrayIndex.all(),NDArrayIndex.interval(2,4));

    INDArray yContrastive = Nd4j.create(y.shape());
    yContrastive.putRow(0, y.getRow(2));
    yContrastive.putRow(1, y.getRow(0));
    yContrastive.putRow(2, y.getRow(0));    
    
    INDArray xContrastive = Nd4j.create(x.shape());
    xContrastive.putRow(0, x.getRow(2));
    xContrastive.putRow(1, x.getRow(0));
    xContrastive.putRow(2, x.getRow(0));

    INDArray xNeighbor = Nd4j.create(x.shape());
    xNeighbor.putRow(0, Nd4j.create(new double[]{0,1}));
    xNeighbor.putRow(1, x.getRow(2));
    xNeighbor.putRow(2, x.getRow(1));
    
    INDArray yNeighbor = Nd4j.create(y.shape());
    yNeighbor.putRow(0, Nd4j.create(new double[]{-1,0}));
    yNeighbor.putRow(1, y.getRow(2));
    yNeighbor.putRow(2, y.getRow(1));
    
    INDArray xNotANeighbor = Nd4j.create(x.shape());
    xNotANeighbor.putRow(0, x.getRow(2));
    xNotANeighbor.putRow(1, x.getRow(0));
    xNotANeighbor.putRow(2, x.getRow(0));

    INDArray yNotANeighbor = Nd4j.create(y.shape());
    yNotANeighbor.putRow(0, y.getRow(2));
    yNotANeighbor.putRow(1, y.getRow(0));
    yNotANeighbor.putRow(2, y.getRow(0));

    INDArray distancesXY = Transforms.allEuclideanDistances(x, y);
    INDArray distancesXYContrastive = Transforms.allEuclideanDistances(x, yContrastive);
    INDArray distancesXContrastiveY = Transforms.allEuclideanDistances(xContrastive, y);
    INDArray distancesXXNotANeighbor = Transforms.allEuclideanDistances(x, xNotANeighbor);
    INDArray distancesXXNeighbor = Transforms.allEuclideanDistances(x, xNeighbor);
    INDArray distancesYYNotANeighbor = Transforms.allEuclideanDistances(y, yNotANeighbor);
    INDArray distancesYYNeighbor = Transforms.allEuclideanDistances(y, yNeighbor);
    margin = 1;
    INDArray joinTerm1 = distancesXY.add(margin).sub(distancesXYContrastive);
    INDArray joinTerm2 = distancesXY.add(margin).sub(distancesXContrastiveY);
    INDArray structureX = distancesXXNeighbor.add(margin).sub(distancesXXNotANeighbor);
    INDArray structureY = distancesYYNeighbor.add(margin).sub(distancesYYNotANeighbor);
    joinTerm1 = Transforms.max(joinTerm1, 0);
    joinTerm2 = Transforms.max(joinTerm2, 0);
    structureX = Transforms.max(structureX, 0);
    structureY = Transforms.max(structureY, 0);


    //multiply with masks, always
    applyMask(mask, scoreArr);
    
    return scoreArr;
  }


  @Override
  public INDArray computeGradient(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask) {
    INDArray output = activationFn.getActivation(preOutput.dup(), true);

    INDArray dlDx = computeDlDx(labels, output);

    //Everything below remains the same
    output = activationFn.backprop(preOutput.dup(), dlDx).getFirst();
    //multiply with masks, always
    applyMask(mask, output);
    return null;
  }

  private INDArray computeDlDx(INDArray labels, INDArray output) {
    return null;
  }

  @Override
  public Pair<Double, INDArray> computeGradientAndScore(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask, boolean average) {
    return new Pair<>(
      computeScore(labels, preOutput, activationFn, mask, average),
      computeGradient(labels, preOutput, activationFn, mask));
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }
}
