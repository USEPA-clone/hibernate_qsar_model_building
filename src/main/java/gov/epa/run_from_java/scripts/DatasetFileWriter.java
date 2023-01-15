package gov.epa.run_from_java.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointContributor;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.Compound;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.CompoundServiceImpl;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesService;
import gov.epa.databases.dev_qsar.qsar_descriptors.service.DescriptorValuesServiceImpl;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.models.ModelBuilder;
import gov.epa.endpoints.models.ModelData;

public class DatasetFileWriter {
	
	private DatasetService datasetService = new DatasetServiceImpl();
	private DataPointService dataPointService = new DataPointServiceImpl();
	private DescriptorValuesService descriptorValuesService = new DescriptorValuesServiceImpl();
	
	public String writeWithoutSplitting(String datasetName, String descriptorSetName, String outputFolderPath, boolean fetchDtxcids) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<DescriptorValues> descriptorValues = descriptorValuesService.findByDescriptorSetName(descriptorSetName);
		
		String outputFileName = datasetName + "_" + descriptorSetName + "_full.tsv";
		String outputFilePath = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName;
		File outputFile = new File(outputFilePath);
		outputFile.getParentFile().mkdirs();
		
//		try {
//			Gson gson=new Gson();
//			String json=gson.toJson(dataPoints);
//			String jsonFilePath= outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName.replace("tsv", ".json");
//			FileWriter fw=new FileWriter(jsonFilePath);
//			fw.write(json);
//			fw.flush();
//			fw.close();
//		} catch(Exception ex) {
//			ex.printStackTrace();
//		}
		
		String instances = ModelData.generateInstancesWithoutSplitting(dataPoints, descriptorValues, fetchDtxcids);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
			bw.write(instances);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instances;
	}
	
	public void writeWithSplitting(String descriptorSetName,String splittingName,String datasetName,String outputFolderPath) {
		ModelBuilder mb=new ModelBuilder("tmarti02");
				
		//Get training and test set instances as strings using TEST descriptors:
		ModelData md=mb.initModelData(datasetName, descriptorSetName,splittingName, false);

		File outputFolder = new File(outputFolderPath);
		outputFolder.mkdirs();

		String outputFileNameTraining = datasetName + "_" + descriptorSetName+"_"+splittingName + "_training.tsv";
		String outputFilePathTraining = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNameTraining;
		
		String outputFileNamePrediction = datasetName + "_" + descriptorSetName+"_"+splittingName + "_prediction.tsv";
		String outputFilePathPrediction = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNamePrediction;

		try {
			
			FileWriter fw=new FileWriter(outputFilePathTraining);
			fw.write(md.trainingSetInstances);
			fw.flush();
			fw.close();
			
			fw=new FileWriter(outputFilePathPrediction);
			fw.write(md.predictionSetInstances);
			fw.flush();
			fw.close();

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

		
	}
	
	public void writeWithSplitting2(String descriptorSetName,String splittingName,String datasetName,String outputFolderPath,boolean removeNullCols) {
		ModelBuilder mb=new ModelBuilder("tmarti02");
				
		//Get training and test set instances as strings using TEST descriptors:
		ModelData md=mb.initModelData(datasetName, descriptorSetName,splittingName, false);

		File outputFolder = new File(outputFolderPath);
		outputFolder.mkdirs();

		String outputFileNameTraining = datasetName + " " + descriptorSetName+" training.tsv";
		String outputFilePathTraining = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNameTraining;
		
		String outputFileNamePrediction = datasetName + " " + descriptorSetName+" prediction.tsv";
		String outputFilePathPrediction = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileNamePrediction;

		try {
			
			FileWriter fw=new FileWriter(outputFilePathTraining);
			
			if (removeNullCols) {
				md.trainingSetInstances=removeNulls(md.trainingSetInstances);
				md.predictionSetInstances=removeNulls(md.predictionSetInstances);
			}
			
			
			
			fw.write(md.trainingSetInstances);
			fw.flush();
			fw.close();
			
			fw=new FileWriter(outputFilePathPrediction);
			fw.write(md.predictionSetInstances);
			fw.flush();
			fw.close();

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

		
	}
	private String removeNulls(String strDataset) {
		//TODO
		return strDataset;
		
	}
	

	
	public String writeWithoutSplitting(Long datasetId, String descriptorSetName, String outputFolderPath, boolean fetchDtxcids) {
		Dataset dataset = datasetService.findById(datasetId);
		if (dataset==null) {
			return datasetId+" not found";
		}
		
		return writeWithoutSplitting(dataset.getName(), descriptorSetName, outputFolderPath, fetchDtxcids);
	}
	
	public void writeQSARSmilesCID(Long datasetId, String outputFolderPath) {
		Dataset dataset = datasetService.findById(datasetId);
		
		if (dataset==null) {
			return;
		}
		
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(dataset.getName());
		
		String outputFileName = dataset.getName()+ "_SmilesDTXCID.tsv";
		String outputFilePath = outputFolderPath + (outputFolderPath.endsWith("/") ? "" : "/") + outputFileName;
		File outputFile = new File(outputFilePath);
		outputFile.getParentFile().mkdirs();
		
		String instances = generateSmilesDTXCID(dataPoints);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {
			bw.write(instances);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public static String generateSmilesDTXCID(List<DataPoint> dataPoints) {
		Map<String, DataPoint> dataPointsMap = dataPoints.stream().collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp));
		
		CompoundService compoundService = new CompoundServiceImpl();
		
		String instanceHeader="QSARSmiles\tDTXCID\tPropertyValue\r\n";
		StringBuilder sbOverall = new StringBuilder(instanceHeader);
		
		for (String smiles:dataPointsMap.keySet()) {
			DataPoint dp = dataPointsMap.get(smiles);
			
			if (dp!=null && !dp.getOutlier()) {
				
				List<Compound> compounds = compoundService.findByCanonQsarSmiles(smiles);
				if (compounds!=null) {
					
					for (Compound compound:compounds) {
						String instance = smiles+"\t"+compound.getDtxcid()+"\t"+dp.getQsarPropertyValue()+"\r\n";
						sbOverall.append(instance);
					}
				}
				
			}
		}
		
		return sbOverall.toString();
	}

	void writeOPERAFiles() {
		
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
//		String descriptorSetName="WebTEST-default";
		String descriptorSetName="PaDEL-default";
//		String descriptorSetName="Padelpy webservice single";
//		
		
//		"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default"
		
		String descriptorSetName2="T.E.S.T. 5.1";
		
		String splittingName="OPERA";
		
		String[] OPERA_ENDPOINTS = { DevQsarConstants.LOG_KOA, DevQsarConstants.LOG_KM_HL,
				DevQsarConstants.HENRYS_LAW_CONSTANT, DevQsarConstants.LOG_BCF, DevQsarConstants.LOG_OH,
				DevQsarConstants.LOG_KOC, DevQsarConstants.VAPOR_PRESSURE, DevQsarConstants.WATER_SOLUBILITY,
				DevQsarConstants.BOILING_POINT, DevQsarConstants.MELTING_POINT, DevQsarConstants.LOG_KOW };

		String server="https://ccte-cced.epa.gov/";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		for (String endpoint:OPERA_ENDPOINTS) {			
			
			String datasetName=endpoint+" OPERA";

			String folder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\data\\datasets_benchmark\\"+datasetName+"\\";
			
			calc.calculateDescriptorsTodd(datasetName,  descriptorSetName, true,1);

			writeWithSplitting2(descriptorSetName, splittingName, datasetName, folder,true);
			
//			if(true)break;
			
//			String training_file_name = endpoint + " OPERA " + descriptorSetName + " training.tsv";
//			String training_file_name2 = endpoint + " OPERA " + descriptorSetName2 + " training.tsv";
//
//			try {
//				List<String> lines= Files.readAllLines(Paths.get(folder+training_file_name));
//				List<String> lines2= Files.readAllLines(Paths.get(folder+training_file_name2));
//				System.out.println(endpoint+"\t"+lines.size()+"\t"+lines2.size());
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			
		}
	}
	
	void writeTEST_Toxicity_Files() {
		
//		String descriptorSetName="WebTEST-default";
//		String descriptorSetName="ToxPrints-default";
//		String descriptorSetName="RDKit-default";
//		String descriptorSetName="Padelpy webservice single";
		String descriptorSetName="PaDEL-default";
//		
//		"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default"
		
		String splittingName="TEST";
		
		String[] TEST_ENDPOINTS = { DevQsarConstants.LC50DM, DevQsarConstants.LC50, DevQsarConstants.IGC50,
				DevQsarConstants.LD50, DevQsarConstants.MUTAGENICITY, DevQsarConstants.LLNA};

		
		String server="https://ccte-cced.epa.gov/";
		SciDataExpertsDescriptorValuesCalculator calc=new SciDataExpertsDescriptorValuesCalculator(server, "tmarti02");
		
		String mainFolder="C:\\Users\\TMARTI02\\OneDrive - Environmental Protection Agency (EPA)\\0 java\\QSAR_Model_Building\\data\\datasets_benchmark_TEST\\";
		
		for (String endpoint:TEST_ENDPOINTS) {			
			
			String datasetName=endpoint+" TEST";
			String folder=mainFolder+datasetName+"\\";
			calc.calculateDescriptorsTodd(datasetName,  descriptorSetName, true,1);
			writeWithSplitting2(descriptorSetName, splittingName, datasetName, folder,false);
			
		}
	}

	
	
	public static void main(String[] args) {
		DatasetFileWriter writer = new DatasetFileWriter();
//		writer.writeOPERAFiles();
		writer.writeTEST_Toxicity_Files();

//		writer.writeWithoutSplitting(38L, "T.E.S.T. 5.1", "data/dev_qsar/dataset_files/");
//		writer.writeWithoutSplitting(36L, "T.E.S.T. 5.1", "data/dev_qsar/dataset_files/");
//		writer.writeWithoutSplitting(42L, "T.E.S.T. 5.1", "data/dev_qsar/dataset_files/");
//		writer.writeWithoutSplitting(31L, "T.E.S.T. 5.1", "data/dev_qsar/dataset_files/", true);
//		writer.writeWithoutSplitting(34L, "T.E.S.T. 5.1", "data/dev_qsar/dataset_files/", true);
//		writer.writeQSARSmilesCID(31L,"data/dev_qsar/dataset_files/");
//		writer.writeQSARSmilesCID(34L,"data/dev_qsar/dataset_files/");

		
//		String outputFolderPath="data/dev_qsar/dataset_files/";
//		String descriptorSetName="T.E.S.T. 5.1";
//		
//		writer.writeWithoutSplitting(38L, descriptorSetName, outputFolderPath);
//		writer.writeWithoutSplitting(36L, descriptorSetName, outputFolderPath");
//		writer.writeWithoutSplitting(42L, descriptorSetName, outputFolderPath);
//		writer.writeWithoutSplitting(43L, descriptorSetName, outputFolderPath, true);
//		writer.writeWithoutSplitting(34L, descriptorSetName, outputFolderPath, true);
//		writer.writeWithoutSplitting(31L, descriptorSetName, outputFolderPath, true);
		
		//TODO need to compile list of sources used in compiling a dataset
//		writer.writeWithSplitting(descriptorSetName,"RND_REPRESENTATIVE","Standard Water solubility from exp_prop",outputFolderPath);
//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingPFASOnly,"Standard Water solubility from exp_prop",outputFolderPath);
//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingAll,"Standard Water solubility from exp_prop",outputFolderPath);
//		writer.writeWithSplitting(descriptorSetName,PFAS_SplittingGenerator.splittingAllButPFAS,"Standard Water solubility from exp_prop",outputFolderPath);


		
//		writer.writeWithSplitting("T.E.S.T. 5.1","TEST","LC50 TEST",outputFolderPath);
		
//		String dataset="LC50 TEST";		
//		String dataset="LD50 TEST";
//		String dataset="Mutagenicity TEST";
//		String splitting="TEST";
		
//		String dataset="LogBCF OPERA";
//		String splitting="OPERA";
//		
//		String[] sciDataExpertsDescriptorSetNames = {
//				"PaDEL-default", "RDKit-default", "WebTEST-default", "ToxPrints-default", "Mordred-default"
//		};
//
//		for (String descriptorSetName:sciDataExpertsDescriptorSetNames) {
//			System.out.println(descriptorSetName);
//			writer.writeWithSplitting(descriptorSetName,splitting,dataset,outputFolderPath);	
//		}
		

	}

}

