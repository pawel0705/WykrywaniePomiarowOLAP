package tpdia_project;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Debug.Random;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class MLAlgorithms {

	public void KNN(Instances trainingDataSet, Instances testingDataSet) throws Exception {
		Classifier ibk = new IBk(1);
		ibk.buildClassifier(trainingDataSet);

		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(ibk, testingDataSet);

		System.out.println("** KNN Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(ibk);

		ClassificationPrecision("k-Nearest Neighbor", ibk, testingDataSet);

		eval.crossValidateModel(ibk, trainingDataSet, 10, new Random(1));
		System.out.println("10-fold cross validation: " + Double.toString(eval.pctCorrect()));
		System.out.println("-------------------------------------");
	}

	public void KStar(Instances trainingDataSet, Instances testingDataSet) throws Exception {

		NumericToNominal numericToNominal = new NumericToNominal();

		numericToNominal.setInputFormat(trainingDataSet);
		trainingDataSet = weka.filters.Filter.useFilter(trainingDataSet, numericToNominal);

		numericToNominal = new NumericToNominal();
		numericToNominal.setInputFormat(testingDataSet);
		testingDataSet = weka.filters.Filter.useFilter(testingDataSet, numericToNominal);

		Classifier kstar = new weka.classifiers.lazy.KStar();
		kstar.buildClassifier(trainingDataSet);

		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(kstar, testingDataSet);

		System.out.println("** K-Star Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(kstar);

		ClassificationPrecision("K-Star", kstar, testingDataSet);

		eval.crossValidateModel(kstar, trainingDataSet, 10, new Random(1));
		System.out.println("10-fold cross validation: " + Double.toString(eval.pctCorrect()));
		System.out.println("-------------------------------------");
	}

	public void RandomForest(Instances trainingDataSet, Instances testingDataSet) throws Exception {
		RandomForest forest = new RandomForest();

		forest.setNumFeatures(10);
		forest.setNumIterations(1);
		forest.buildClassifier(trainingDataSet);

		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(forest, testingDataSet);

		System.out.println("** Random Forest Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());
		System.out.print(" the expression for the input data as per alogorithm is ");
		System.out.println(forest);

		ClassificationPrecision("Random Forest", forest, testingDataSet);

		eval.crossValidateModel(forest, trainingDataSet, 10, new Random(1));
		System.out.println("10-fold cross validation: " + Double.toString(eval.pctCorrect()));
		System.out.println("-------------------------------------");
	}

	public void SVM(Instances trainingDataSet, Instances testingDataSet) throws Exception {
		SMO svm = new SMO();
		svm.setKernel(new RBFKernel());

		NumericToNominal numericToNominal = new NumericToNominal();

		numericToNominal.setInputFormat(trainingDataSet);
		testingDataSet = weka.filters.Filter.useFilter(trainingDataSet, numericToNominal);

		numericToNominal = new NumericToNominal();
		numericToNominal.setInputFormat(testingDataSet);
		trainingDataSet = weka.filters.Filter.useFilter(testingDataSet, numericToNominal);

		svm.buildClassifier(trainingDataSet);

		Evaluation eval = new Evaluation(trainingDataSet);
		eval.evaluateModel(svm, testingDataSet);

		System.out.println("** SVM Evaluation with Datasets **");
		System.out.println(eval.toSummaryString());

		ClassificationPrecision("SVM", svm, testingDataSet);

		eval.crossValidateModel(svm, trainingDataSet, 10, new Random(1));
		System.out.println("10-fold cross validation: " + Double.toString(eval.pctCorrect()));
		System.out.println("-------------------------------------");
	}

	private void ClassificationPrecision(String algoName, Classifier classifier, Instances testingDataSet)
			throws Exception {

		int sum = testingDataSet.numInstances();
		int right = 0;

		int Nmm = 0;
		int Nmn = 0;
		int Nnm = 0;
		int Nnn = 0; // not used in article

		for (int i = 0; i < sum; i++) {

			int real = (int) testingDataSet.instance(i).classValue();
			int predicted = (int) classifier.classifyInstance(testingDataSet.instance(i));

			if (real == predicted) {
				right++;
			}

			if ((real == 1) && (predicted == 1)) {
				Nmm++;
			} else if ((real == 1) && (predicted == 0)) {
				Nmn++;
			} else if ((real == 0) && (predicted == 0)) {
				Nnm++;
			} else {
				Nnn++;
			}
		}

		System.out.println(algoName + " classification precision: " + ((double) right / (double) sum));

		double R = 0;
		double P = 0;
		double F = 0;

		if ((Nmm + Nmn) != 0) {
			R = Nmm / (double) (Nmm + Nmn);
			R *= 100;
		}

		if ((Nmm + Nnm) != 0) {
			P = Nmm / (double) (Nmm + Nnm);
			P *= 100;
		}

		if ((P + R) != 0) {
			F = (2.0 * P * R) / (double) (P + R);
		}

		System.out.println("Recall: " + R + "%");
		System.out.println("Precision: " + P + "%");
		System.out.println("F-Measure: " + F + "%");
	}
}
