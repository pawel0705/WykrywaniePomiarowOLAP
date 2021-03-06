package tpdia_project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tpdia_project.Models.StatisticValuesModel;
import tpdia_project.Models.ValueRatioModel;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

// class for extracting features data from columns
public class FeatureExtractionManager {

	// 1 - General Features ///////////////////////////////////////

	// DataType
	public int GetDataType(Instances instances, int columnNumber) {
		Attribute attribute = instances.attribute(columnNumber);

		if (!(attribute.isNumeric() || attribute.isNominal())) {
			return -1; // no numeric or nominal column
		}

		int instancesNumber = instances.numInstances();

		int intNumbers = 0;
		int realNumbers = 0;

		if (attribute.isNumeric()) {
			for (int row = 0; row < instancesNumber; row++) {
				double tmpVal = instances.attributeToDoubleArray(columnNumber)[row];

				tmpVal = Math.abs(tmpVal - Math.floor(tmpVal));

				if (Math.abs(tmpVal) < 2 * Double.MIN_VALUE) {
					intNumbers++;
				} else {
					realNumbers++;
				}
			}

			if (realNumbers > intNumbers) {
				return 0; // float column
			}

			return 1; // integer column
		}

		if (attribute.isNominal()) {
			int notNumber = 0;

			for (int row = 0; row < instancesNumber; row++) {
				Instance instance = instances.get(row);
				String tmpValString = instance.stringValue(columnNumber);
				double tmpVal = 0.0;

				try {
					tmpVal = Double.parseDouble(tmpValString);
					tmpVal = Math.abs(tmpVal - Math.floor(tmpVal));

					if (Math.abs(tmpVal) < 2 * Double.MIN_VALUE) {
						intNumbers++;
					} else {
						realNumbers++;
					}

				} catch (Exception ex) {
					notNumber++;
				}
			}

			if (notNumber > (instancesNumber * 0.8)) {
				return -1;
			}

			if (realNumbers > intNumbers) {
				return 0; // float column
			}

			return 1; // integer column
		}

		return -1;
	}

	// Positive, negative, zero value ratio
	public ValueRatioModel GetPositiveNegativeZeroValueRatio(Instances instances, int columnNumber) {
		Attribute attribute = instances.attribute(columnNumber);

		ValueRatioModel valueRatio = new ValueRatioModel();
		valueRatio.NegativeValueRatio = -1;
		valueRatio.PositiveValueRatio = -1;
		valueRatio.ZeroValueRatio = -1;

		if (!(attribute.isNumeric() || attribute.isNominal())) {
			return valueRatio;
		}

		int instancesNumber = instances.numInstances();
		int positiveValue = 0;
		int negativeValue = 0;
		int zeroValue = 0;

		// column with all numbers
		if (attribute.isNumeric()) {
			for (int row = 0; row < instancesNumber; row++) {
				double tmpVal = instances.attributeToDoubleArray(columnNumber)[row];

				if (Math.abs(tmpVal) < 2 * Double.MIN_VALUE) {
					zeroValue++;
				} else if (tmpVal > 0) {
					positiveValue++;
				} else {
					negativeValue++;
				}
			}

			valueRatio.NegativeValueRatio = negativeValue / (double) instancesNumber;
			valueRatio.PositiveValueRatio = positiveValue / (double) instancesNumber;
			valueRatio.ZeroValueRatio = zeroValue / (double) instancesNumber;

			if (Double.isNaN(valueRatio.NegativeValueRatio)) {
				valueRatio.NegativeValueRatio = 0;
			}

			if (Double.isNaN(valueRatio.PositiveValueRatio)) {
				valueRatio.PositiveValueRatio = 0;
			}

			if (Double.isNaN(valueRatio.ZeroValueRatio)) {
				valueRatio.ZeroValueRatio = 0;
			}

			return valueRatio;
		}

		int nullInstancesNumber = 0;

		// column that can have null, unknown, etc.
		if (attribute.isNominal()) {
			for (int row = 0; row < instancesNumber; row++) {
				Instance instance = instances.get(row);
				String tmpValString = instance.stringValue(columnNumber);
				double tmpVal = 0.0;

				try {
					tmpVal = Double.parseDouble(tmpValString);

					if (Math.abs(tmpVal) < 2 * Double.MIN_VALUE) {
						zeroValue++;
					} else if (tmpVal > 0) {
						positiveValue++;
					} else {
						negativeValue++;
					}

				} catch (Exception ex) {
					nullInstancesNumber++;
				}
			}

			int fixedInstancesNumber = instancesNumber - nullInstancesNumber;

			if (fixedInstancesNumber <= 0) {
				return valueRatio;
			}

			valueRatio.NegativeValueRatio = negativeValue / (double) fixedInstancesNumber;
			valueRatio.PositiveValueRatio = positiveValue / (double) fixedInstancesNumber;
			valueRatio.ZeroValueRatio = zeroValue / (double) fixedInstancesNumber;

			if (Double.isNaN(valueRatio.NegativeValueRatio)) {
				valueRatio.NegativeValueRatio = 0;
			}

			if (Double.isNaN(valueRatio.PositiveValueRatio)) {
				valueRatio.PositiveValueRatio = 0;
			}

			if (Double.isNaN(valueRatio.ZeroValueRatio)) {
				valueRatio.ZeroValueRatio = 0;
			}

			return valueRatio;
		}

		return valueRatio;
	}

	// Unique value ratio
	public double GetUniqueValueRatio(Instances instances, int columnNumber) {
		Attribute attribute = instances.attribute(columnNumber);

		if (!(attribute.isNumeric() || attribute.isNominal())) {
			return -1; // no numeric or nominal column
		}

		int instancesNumber = instances.numInstances();

		List<String> uniqueValues = new ArrayList<>();

		for (int row = 0; row < instancesNumber; row++) {
			Instance instance = instances.get(row);
			String tmpValString = "";

			if (attribute.isNominal()) {
				tmpValString = instance.stringValue(columnNumber);
			}

			if (attribute.isNumeric()) {
				double tmpVal = instances.attributeToDoubleArray(columnNumber)[row];
				tmpValString = String.valueOf(tmpVal);
			}

			if (!uniqueValues.contains(tmpValString)) {
				uniqueValues.add(tmpValString);
			}
		}

		double uniqueRatio = (double) uniqueValues.size() / (double) instancesNumber;

		if (Double.isNaN(uniqueRatio)) {
			uniqueRatio = 0;
		}

		return uniqueRatio;
	}

	// Same digital number
	public int GetSameDigitalNumber(Instances instances, int columnNumber) {

		int dataType = this.GetDataType(instances, columnNumber);

		if (dataType == -1) // must be integer
		{
			return -1;
		}

		if (dataType == 0) {
			return 0;
		}

		int instancesNumber = instances.numInstances();
		Attribute attribute = instances.attribute(columnNumber);

		List<Integer> uniqueLength = new ArrayList<>();

		for (int row = 0; row < instancesNumber; row++) {
			Instance instance = instances.get(row);

			String tmpValString = "";

			if (attribute.isNominal()) {
				tmpValString = instance.stringValue(columnNumber);
			}

			if (attribute.isNumeric()) {
				double tmpVal = instances.attributeToDoubleArray(columnNumber)[row];
				tmpValString = String.valueOf(tmpVal);
			}

			int dataLength = tmpValString.length();

			if (!uniqueLength.contains(dataLength)) {
				uniqueLength.add(dataLength);
			}

		}

		if (uniqueLength.size() == 1) {
			return 1;
		}

		return 0;
	}

	// 2 - Statistical Features ///////////////////////////////////////

	// Average/Minimum/Maximum/Median/Upper quartile/Lower quartile values
	public StatisticValuesModel GetAvgMinMaxMedianUpquarLowquar(Instances instances, int columnNumber) {
		Attribute attribute = instances.attribute(columnNumber);

		StatisticValuesModel statisticValues = new StatisticValuesModel();
		statisticValues.Average = -1;
		statisticValues.Minimum = -1;
		statisticValues.Maximum = -1;
		statisticValues.Median = -1;
		statisticValues.UpperQuartile = -1;
		statisticValues.LowerQuartile = -1;

		if (!(attribute.isNumeric() || attribute.isNominal())) {
			return statisticValues;
		}

		int instancesNumber = instances.numInstances();

		List<Double> readedValues = new ArrayList<Double>();

		// column with all numbers
		if (attribute.isNumeric()) {
			for (int row = 0; row < instancesNumber; row++) {
				double tmpVal = instances.attributeToDoubleArray(columnNumber)[row];

				readedValues.add(tmpVal);
			}
		}

		// column that can have null, unknown, etc.
		if (attribute.isNominal()) {
			for (int row = 0; row < instancesNumber; row++) {
				Instance instance = instances.get(row);
				String tmpValString = instance.stringValue(columnNumber);
				double tmpVal = 0.0;

				try {
					tmpVal = Double.parseDouble(tmpValString);
					readedValues.add(tmpVal);
				} catch (Exception ex) {
					// Do nothing
				}
			}
		}

		try {

			if (readedValues.size() > 0) {
				statisticValues.Average = readedValues.stream().mapToDouble(a -> a).average().getAsDouble();
				statisticValues.Minimum = readedValues.stream().mapToDouble(a -> a).min().getAsDouble();
				statisticValues.Maximum = readedValues.stream().mapToDouble(a -> a).max().getAsDouble();

				Collections.sort(readedValues);

				double middle = readedValues.size() / 2;
				if (readedValues.size() % 2 == 0) {
					middle = (readedValues.get(readedValues.size() / 2) + readedValues.get(readedValues.size() / 2 - 1))
							/ 2;
				} else {
					middle = readedValues.get(readedValues.size() / 2);
				}

				statisticValues.Median = middle;

				double quartilies[] = new double[3];

				for (int quartileType = 1; quartileType < 4; quartileType++) {
					float length = readedValues.size() - 1;
					double quartile;
					float newArraySize = (length * ((float) (quartileType) * 25 / 100)) - 1;

					if (newArraySize % 1 == 0) {
						quartile = readedValues.get((int) (newArraySize));
					} else {
						int newArraySize1 = (int) (newArraySize);
						quartile = (readedValues.get(newArraySize1) + readedValues.get(newArraySize1 + 1)) / 2;
					}
					quartilies[quartileType - 1] = quartile;
				}

				statisticValues.LowerQuartile = quartilies[0];
				statisticValues.UpperQuartile = quartilies[2];

				if (Double.isNaN(statisticValues.Average)) {
					statisticValues.Average = 0;
				}

				if (Double.isNaN(statisticValues.Minimum)) {
					statisticValues.Minimum = 0;
				}

				if (Double.isNaN(statisticValues.Maximum)) {
					statisticValues.Maximum = 0;
				}

				if (Double.isNaN(statisticValues.Median)) {
					statisticValues.Median = 0;
				}

				if (Double.isNaN(statisticValues.LowerQuartile)) {
					statisticValues.LowerQuartile = 0;
				}

				if (Double.isNaN(statisticValues.UpperQuartile)) {
					statisticValues.UpperQuartile = 0;
				}

			}
		} catch (Exception e) {
			return new StatisticValuesModel();
		}

		return statisticValues;
	}

	// Coefficient of variation
	public double GetCoefficientOfVariation(Instances instances, int columnNumber) {
		Attribute attribute = instances.attribute(columnNumber);

		if (!(attribute.isNumeric() || attribute.isNominal())) {
			return -1;
		}

		int instancesNumber = instances.numInstances();

		List<Double> readedValues = new ArrayList<Double>();

		// column with all numbers
		if (attribute.isNumeric()) {
			for (int row = 0; row < instancesNumber; row++) {
				double tmpVal = instances.attributeToDoubleArray(columnNumber)[row];

				readedValues.add(tmpVal);
			}
		}

		// column that can have null, unknown, etc.
		if (attribute.isNominal()) {
			for (int row = 0; row < instancesNumber; row++) {
				Instance instance = instances.get(row);
				String tmpValString = instance.stringValue(columnNumber);
				double tmpVal = 0.0;

				try {
					tmpVal = Double.parseDouble(tmpValString);
					readedValues.add(tmpVal);
				} catch (Exception ex) {
					// Do nothing
				}
			}
		}

		double average = 0.0;
		if (readedValues.size() > 0) {
			average = readedValues.stream().mapToDouble(a -> a).average().getAsDouble();
		}

		double stdev = -1;
		double sum = 0;

		if (readedValues.size() > 0) {
			for (int i = 0; i < readedValues.size(); i++) {
				sum = sum + (readedValues.get(i) - average) * (readedValues.get(i) - average);
			}
			double squaredDiffMean = (sum) / (readedValues.size());
			stdev = (Math.sqrt(squaredDiffMean));

			if (Math.abs(average) < 2 * Double.MIN_VALUE) {
				return stdev;
			}

			stdev = stdev / average;
		}

		if (Double.isNaN(stdev)) {
			stdev = 0;
		}

		return stdev;
	}

	// Range ratio
	public double GetRangeRatio(Instances instances, int columnNumber) {
		Attribute attribute = instances.attribute(columnNumber);

		if (!(attribute.isNumeric() || attribute.isNominal())) {
			return -1; // no numeric or nominal column
		}

		int instancesNumber = instances.numInstances();

		List<Double> uniqueValues = new ArrayList<>();

		for (int row = 0; row < instancesNumber; row++) {
			Instance instance = instances.get(row);
			double tmpVal = 0.0;

			if (attribute.isNominal()) {
				String tmpValString = instance.stringValue(columnNumber);

				try {
					tmpVal = Double.parseDouble(tmpValString);
				} catch (Exception ex) {
					continue;
				}
			}

			if (attribute.isNumeric()) {
				tmpVal = instances.attributeToDoubleArray(columnNumber)[row];
			}

			if (!uniqueValues.contains(tmpVal)) {
				uniqueValues.add(tmpVal);
			}
		}

		double rangeRatio = -1;

		if (uniqueValues.size() > 0) {
			double minimum = uniqueValues.stream().mapToDouble(a -> a).min().getAsDouble();
			double maximum = uniqueValues.stream().mapToDouble(a -> a).max().getAsDouble();

			rangeRatio = (maximum - minimum) / uniqueValues.size();
		}

		if (Double.isNaN(rangeRatio)) {
			rangeRatio = 0;
		}

		return rangeRatio;
	}

	// 3 - Inter-Column Features ///////////////////////////////////////

	// Location ratio
	public double GetLocationRatio(int maxColumns, int columnNumber) {
		double locationRatio = ((double) columnNumber) / ((double) maxColumns - 1.0);

		if (Double.isNaN(locationRatio)) {
			locationRatio = 0;
		}

		return locationRatio;
	}

	// Numerical column ratio
	public double GetNumericalColumnRatio(Instances instances) {
		int attributesNumber = instances.numAttributes();
		int columnsWithNumbers = 0;

		for (int i = 0; i < attributesNumber; i++) {
			if (this.GetDataType(instances, i) != -1) {
				columnsWithNumbers++;
			}
		}

		double numericalColumnRatio = (double) columnsWithNumbers / (double) attributesNumber;

		if (Double.isNaN(numericalColumnRatio)) {
			numericalColumnRatio = 0;
		}

		return numericalColumnRatio;
	}

	// Numerical neighbor
	public double GetNumericalNeighbor(Instances instances, int maxColumns, int columnNumber) {

		if (this.GetDataType(instances, columnNumber) == -1) {
			return 0;
		}

		if (columnNumber == 0 && this.GetDataType(instances, columnNumber + 1) != -1) {
			return 1;
		}

		if (columnNumber == (maxColumns - 1) && this.GetDataType(instances, columnNumber - 1) != -1) {
			return 1;
		}

		if (columnNumber != 0 && columnNumber != (maxColumns - 1) && this.GetDataType(instances, columnNumber + 1) != -1
				&& this.GetDataType(instances, columnNumber - 1) != -1) {
			return 1;
		}

		if (columnNumber != 0 && columnNumber != (maxColumns - 1) && this.GetDataType(instances, columnNumber + 1) != -1
				&& this.GetDataType(instances, columnNumber - 1) == -1) {
			return 0.5;
		}

		if (columnNumber != 0 && columnNumber != (maxColumns - 1) && this.GetDataType(instances, columnNumber + 1) == -1
				&& this.GetDataType(instances, columnNumber - 1) != -1) {
			return 0.5;
		}

		return 0;
	}
}
