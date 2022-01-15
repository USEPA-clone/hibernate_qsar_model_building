package gov.epa.endpoints.reports.predictions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorSetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.endpoints.reports.ReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportMetadata;

public class PredictionReportGenerator extends ReportGenerator {
	private DatasetService datasetService;
	private DescriptorSetService descriptorSetService;
	private DataPointService dataPointService;
	private DescriptorValuesService descriptorValuesService;
	private PredictionService predictionService;
	private DataPointInSplittingService dataPointInSplittingService;
	private ModelService modelService;
	
	private PredictionReport predictionReport;
	
	public PredictionReportGenerator() {
		super();
				
		this.predictionReport = new PredictionReport();
		
		datasetService = new DatasetServiceImpl();
		descriptorSetService = new DescriptorSetServiceImpl();
		dataPointService = new DataPointServiceImpl();
		descriptorValuesService = new DescriptorValuesServiceImpl();
		predictionService = new PredictionServiceImpl();
		dataPointInSplittingService = new DataPointInSplittingServiceImpl();
		modelService = new ModelServiceImpl();
	}
	
	private void initPredictionReport(String datasetName, String descriptorSetName) {
		Dataset dataset = datasetService.findByName(datasetName);
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		predictionReport.predictionReportMetadata = new PredictionReportMetadata(datasetName, 
				dataset.getProperty().getName(), 
				dataset.getUnit().getName(),
				descriptorSetName,
				descriptorSet.getHeadersTsv());
		
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<PredictionReportDataPoint> predictionReportData = dataPoints.stream()
				.map(dp -> new PredictionReportDataPoint(dp))
				.collect(Collectors.toList());
		predictionReport.predictionReportDataPoints = predictionReportData;
	}
	
	private void addModelDescriptorValues() {
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
			DescriptorValues dv = descriptorValuesService.findByCanonQsarSmilesAndDescriptorSetName(data.canonQsarSmiles, 
					predictionReport.predictionReportMetadata.descriptorSetName);
			if (dv!=null) {
				data.descriptorValues = dv.getValuesTsv();
			}
		}
	}
	
	private void addAllPredictions() {
		List<Model> models = modelService.findByDatasetName(predictionReport.predictionReportMetadata.datasetName);
		for (Model model:models) {
			if (!model.getDescriptorSetName().equals(predictionReport.predictionReportMetadata.descriptorSetName)) {
				continue;
			}
			
			List<DataPointInSplitting> dataPointsInSplitting = 
					dataPointInSplittingService.findByDatasetNameAndSplittingName(predictionReport.predictionReportMetadata.datasetName, 
					model.getSplittingName());
			Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
					.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));
			
			String modelMethodName = model.getMethod().getName();
			List<Prediction> modelPredictions = predictionService.findByModelId(model.getId());
			Map<String, Prediction> modelPredictionsMap = modelPredictions.stream()
					.collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
			for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
				Prediction pred = modelPredictionsMap.get(data.canonQsarSmiles);
				if (pred!=null) {
					data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, pred.getQsarPredictedValue(), 
							splittingMap.get(data.canonQsarSmiles)));
				} else {
					data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, null, splittingMap.get(data.canonQsarSmiles)));
				}
			}
		}
	}
	
	private void addModelPredictions(Long modelId) {
		Model model = modelService.findById(modelId);
		
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(predictionReport.predictionReportMetadata.datasetName, 
				model.getSplittingName());
		Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
				.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));
		
		String modelMethodName = model.getMethod().getName();
		List<Prediction> modelPredictions = predictionService.findByModelId(model.getId());
		Map<String, Prediction> modelPredictionsMap = modelPredictions.stream()
				.collect(Collectors.toMap(p -> p.getCanonQsarSmiles(), p -> p));
		for (PredictionReportDataPoint data:predictionReport.predictionReportDataPoints) {
			Prediction pred = modelPredictionsMap.get(data.canonQsarSmiles);
			if (pred!=null) {
				data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, pred.getQsarPredictedValue(), 
						splittingMap.get(data.canonQsarSmiles)));
			} else {
				data.qsarPredictedValues.add(new QsarPredictedValue(modelMethodName, null, splittingMap.get(data.canonQsarSmiles)));
			}
		}
	}
	
	public PredictionReport generateForAllPredictions(String datasetName, String descriptorSetName) {
		initPredictionReport(datasetName, descriptorSetName);
		addOriginalCompounds(predictionReport.predictionReportDataPoints);
		addModelDescriptorValues();
		addAllPredictions();
		return predictionReport;
	}
	
	public PredictionReport generateForModelPredictions(Long modelId) {
		Model model = modelService.findById(modelId);
		initPredictionReport(model.getDatasetName(), model.getDescriptorSetName());
		addOriginalCompounds(predictionReport.predictionReportDataPoints);
		addModelDescriptorValues();
		addModelPredictions(modelId);
		return predictionReport;
	}
	
	void createAllReports () {
		
//		String [] datasets= {"LC50 TEST","IGC50 TEST","LC50DM TEST",
//				"LD50 TEST","LLNA TEST","Mutagenicity TEST","LogBCF OPERA",
//				"LogHalfLife OPERA","LogKmHL OPERA","LogKOA OPERA","LogKOC OPERA","LogOH OPERA",
//				"Water solubility OPERA","Melting point OPERA","Vapor pressure OPERA","Octanol water partition coefficient OPERA"};

		
		String [] datasets= {"LC50DM TEST",
				"LD50 TEST","LLNA TEST","Mutagenicity TEST","LogBCF OPERA",
				"LogHalfLife OPERA","LogKmHL OPERA","LogKOA OPERA","LogKOC OPERA","LogOH OPERA",
				"Water solubility OPERA","Melting point OPERA"};

		
		String descriptorSetName = "T.E.S.T. 5.1";

		
		for (String datasetName:datasets) {
			
			String filePath = "reports/"+ datasetName + "_" + descriptorSetName + "_PredictionReport.json";
			
			File file = new File(filePath);
			if (file.getParentFile()!=null) {
				file.getParentFile().mkdirs();
			}
			
			long t1=System.currentTimeMillis();
			
			PredictionReport report=generateForAllPredictions(datasetName, descriptorSetName);
			
			long t2=System.currentTimeMillis();
			
			double time=(t2-t1)/1000.0;
			System.out.println("Time to generate report = "+time+" seconds");

			
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
				writer.write(gson.toJson(report));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public static void main(String[] args) {
		BasicConfigurator.configure();

		PredictionReportGenerator gen = new PredictionReportGenerator();
		gen.createAllReports();
		
		if (true) return;
		
		
//		String datasetName = "LC50 TEST";
		String datasetName = "IGC50 TEST";
//		String datasetName = "LC50 DM TEST";
//		String datasetName = "LD50 TEST";
//		String datasetName = "LLNA TEST";
//		String datasetName = "Mutagenicity TEST";
//		String datasetName = "LogBCF OPERA";
//		String datasetName = "LogHalfLife OPERA";
//		String datasetName = "LogKmHL OPERA";
//		String datasetName = "LogKOA OPERA";
//		String datasetName = "LogKOC OPERA";
//		String datasetName = "LogOH OPERA";
//		String datasetName = "Water solubility OPERA";
//		String datasetName = "Melting point OPERA";
//		String datasetName = "Vapor pressure OPERA";
//		String datasetName = "Octanol water partition coefficient OPERA";
		
		String descriptorSetName = "T.E.S.T. 5.1";
		
		String filePath = "reports/"+ datasetName + "_" + descriptorSetName + "_PredictionReport.json";
		
		File file = new File(filePath);
		if (file.getParentFile()!=null) {
			file.getParentFile().mkdirs();
		}
		
		long t1=System.currentTimeMillis();
		
		PredictionReport report=gen.generateForAllPredictions(datasetName, descriptorSetName);
		
		long t2=System.currentTimeMillis();
		
		double time=(t2-t1)/1000.0;
		System.out.println("Time to generate report = "+time+" seconds");

		
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(gson.toJson(report));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
