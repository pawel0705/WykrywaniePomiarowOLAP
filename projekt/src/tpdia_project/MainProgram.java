package tpdia_project;

import java.beans.beancontext.BeanContextMembershipListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

import weka.core.Instance;
import weka.core.Instances;

public class MainProgram {
	static CSVManager csvManager = new CSVManager();

	public static void main(String[] args) {

		boolean exitProgram = false;
		String pressedKey = "";

		do {
			PrintHelp();

			Scanner in = new Scanner(System.in);
			pressedKey = in.nextLine();

			if (pressedKey.equals("a") || pressedKey.equals("A")) {
				PrepareFeaturesCSV();
			}

			if (pressedKey.equals("s") || pressedKey.equals("S")) {
				try {
					DetectFeatures();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (pressedKey.equals("q") || pressedKey.equals("Q")) {
				exitProgram = true;
			}

			pressedKey = "";
		} while (exitProgram == false);

		return;
	}

	private static void PrintHelp() {
		System.out.println("To calculate the features of measures columns, enter the [a] key.");
		System.out.println("To perform training and validation on machine learning algorithms, enter the [s] key.");
		System.out.println("To close the program, enter the [q] key.");
	}

	private static void DetectFeatures() throws Exception {
		Instances data = csvManager.GetDataSet("features.csv", ";");

		int seed = 100000;

		data = csvManager.DeleteAllCoumnsExceptNumeric(data);

		Random rand = new Random(seed);
		Instances randData = new Instances(data);
		randData.randomize(rand);

		int trainSize = (int) Math.round(randData.numInstances() * 0.7);
		int testSize = randData.numInstances() - trainSize;
		Instances train = new Instances(randData, 0, trainSize);
		Instances test = new Instances(randData, trainSize, testSize);

		train.setClassIndex(0); // searching label on position 0 after removing all except numeric columns
		test.setClassIndex(0);

		System.out.println("---- Train dataset information ----");
		System.out.println(train.toSummaryString());
		
		System.out.println("---- Test dataset information ----");
		System.out.println(test.toSummaryString());

		MLAlgorithms algorithmManager = new MLAlgorithms();
		algorithmManager.KStar(train, test);
		algorithmManager.KNN(train, test);
		algorithmManager.RandomForest(train, test);
		algorithmManager.SVM(train, test);
	}

	private static void PrepareFeaturesCSV() {
		Instances main = csvManager.GetDataSet("features.csv", ";");

		String prevFileName = "";

		for (Instance row : main) {
			String domainModelName = row.stringValue(0);
			String fileName = row.stringValue(1);

			if (prevFileName.equals(fileName)) {
				continue;
			}

			ProcessOneDataset(domainModelName, fileName, main);

			prevFileName = fileName;
		}

		SaveCSV(main, "features.csv", ";");
		System.out.println("---- Done creating features csv file ----");
	}

	private static void Preprocess(Instances dataset, String fileName)
	{
		for(int columnNr = 0; columnNr < dataset.numAttributes(); ++columnNr)
		{
			if(dataset.attribute(columnNr).isNominal())
			{
				boolean withUnit = true;
				String text = "";
				for(Instance row : dataset)
				{
					String cellValue = row.stringValue(columnNr);
					
					Pattern pattern = Pattern.compile("\\D+\\d+");
					Matcher matcher = pattern.matcher(cellValue);
					if (matcher.matches())
					{
						Pattern textPattern = Pattern.compile("\\D+");
						Matcher textMatcher = textPattern.matcher(cellValue);
						
						textMatcher.find();
						String cellText = textMatcher.group(0);
						if(text.isEmpty())
						{
							text = cellText;
						}
						else if (!text.equals(cellText))
						{
							withUnit = false;
							break;
						}
					}
					else
					{
						withUnit = false;
						break;
					}
				}
				if (withUnit)
				{
					for (Instance row : dataset)
					{
						String cellValue = row.stringValue(columnNr);
						
						Pattern valuePattern = Pattern.compile("\\d+");
						Matcher valueMatcher = valuePattern.matcher(cellValue);
						
						valueMatcher.find();
						String group = valueMatcher.group(0);
						
						dataset.renameAttributeValue(dataset.attribute(columnNr), cellValue, group);
						
						row.setValue(columnNr, group);
					}
				}
				
				withUnit = true;
				for(Instance row : dataset)
				{
					String cellValue = row.stringValue(columnNr);
					
					Pattern pattern = Pattern.compile("\\d+\\D+");
					Matcher matcher = pattern.matcher(cellValue);
					if (matcher.matches())
					{
						Pattern textPattern = Pattern.compile("\\D+");
						Matcher textMatcher = textPattern.matcher(cellValue);
						
						textMatcher.find();
						String cellText = textMatcher.group(0);
						if(text.isEmpty())
						{
							text = cellText;
						}
						else if (!text.equals(cellText))
						{
							withUnit = false;
							break;
						}
					}
					else
					{
						withUnit = false;
						break;
					}
				}
				if (withUnit)
				{
					for (Instance row : dataset)
					{
						
						String cellValue = row.stringValue(columnNr);
						Pattern valuePattern = Pattern.compile("\\d+");
						Matcher valueMatcher = valuePattern.matcher(cellValue);
						valueMatcher.find();
						String group = valueMatcher.group(0);
						
						dataset.renameAttributeValue(dataset.attribute(columnNr), cellValue, group);
						
						row.setValue(columnNr, group);
					}
				}
			}
		}
		
		SaveCSV(dataset, fileName, ",");
	}
	
	private static boolean ProcessOneDataset(String domainName, String fileName, Instances main) {
		boolean success = true;

		String fullDatasetPath = "datasets/" + domainName + "/" + fileName;

		Instances dataset;
		dataset = csvManager.GetDataSet(fullDatasetPath);

		if (dataset == null) {
			success = false;
			System.out.println("There was a problem reading the file: " + fullDatasetPath + ". Skipping..");
			return success;
		}

		System.out.println("----- File: " + fullDatasetPath + " -----");

		Preprocess(dataset, fullDatasetPath);
		
		int attributesNumber = dataset.numAttributes();
		Features[] features = new Features[attributesNumber];

		for (int columnNr = 0; columnNr < attributesNumber; columnNr++) {
			System.out.println("---- Column nr.: " + (columnNr + 1));

			String columnName = dataset.attribute(columnNr).name();
			features[columnNr] = new Features(dataset, columnName, columnNr);
		}

		SaveFeaturesToMainCSV(features, main, fileName);

		// Some data about CSV
		System.out.println();
		System.out.println(dataset.toSummaryString());

		return success;
	}

	private static void SaveFeaturesToMainCSV(Features[] features, Instances main, String fileName) {
		for (Instance row : main) {
			if (row.stringValue(1).equals(fileName)) {
				String columnName = row.stringValue(2);
				for (int i = 0; i < features.length; ++i) {
					if (columnName.equals(features[i].columnName)) {
						row.setValue(4, features[i].dataType);
						row.setValue(5, features[i].positiveNegativeZeroValueRatio.PositiveValueRatio);
						row.setValue(6, features[i].positiveNegativeZeroValueRatio.NegativeValueRatio);
						row.setValue(7, features[i].positiveNegativeZeroValueRatio.ZeroValueRatio);
						row.setValue(8, features[i].uniqueValueRatio);
						row.setValue(9, features[i].sameDigitalNumber);

						row.setValue(10, features[i].statisticValuesModel.Average);
						row.setValue(11, features[i].statisticValuesModel.Minimum);
						row.setValue(12, features[i].statisticValuesModel.Maximum);
						row.setValue(13, features[i].statisticValuesModel.Median);
						row.setValue(14, features[i].statisticValuesModel.UpperQuartile);
						row.setValue(15, features[i].statisticValuesModel.LowerQuartile);
						row.setValue(16, features[i].coefficientOfVariation);
						row.setValue(17, features[i].rangeRatio);

						row.setValue(18, features[i].locationRatio);
						row.setValue(19, features[i].numericalColumnRatioTmp);
						row.setValue(20, features[i].numericalNeighbor);

						break;
					}
				}
			}
		}
	}

	private static void SaveCSV(Instances dataSet, String fileName, String separator) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

			int coulmnsCount = dataSet.numAttributes();
			
			for (int i = 0; i < coulmnsCount; ++i) {
				writer.write(dataSet.attribute(i).name());
				if (i < dataSet.numAttributes() - 1) {
					writer.write(separator);
				} else {
					writer.write("\n");
				}
			}

			for (Instance row : dataSet) {
				for (int j = 0; j < coulmnsCount; ++j) 
				{
					if(dataSet.attribute(j).isNumeric())
					{
						writer.write(Double.toString(row.value(j)));
					}
					else
					{
						writer.write(row.stringValue(j));
					}
					
					if (j < dataSet.numAttributes() - 1) 
					{
						writer.write(separator);
					} 
					else 
					{
						writer.write("\n");
					}
				}
			}

			writer.close();
		} catch (IOException e) {

		}
	}
}
