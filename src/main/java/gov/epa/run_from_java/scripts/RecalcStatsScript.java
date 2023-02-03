package gov.epa.run_from_java.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPointInSplitting;
import gov.epa.databases.dev_qsar.qsar_datasets.entity.Splitting;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointInSplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DataPointServiceImpl;
import gov.epa.databases.dev_qsar.qsar_datasets.service.SplittingServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelInConsensusModel;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelStatistic;
import gov.epa.databases.dev_qsar.qsar_models.entity.Prediction;
import gov.epa.databases.dev_qsar.qsar_models.entity.Statistic;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelStatisticServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionService;
import gov.epa.databases.dev_qsar.qsar_models.service.PredictionServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticService;
import gov.epa.databases.dev_qsar.qsar_models.service.StatisticServiceImpl;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

public class RecalcStatsScript {
	
	public static class SplitPredictions {
		List<ModelPrediction> trainingSetPredictions = new ArrayList<ModelPrediction>();
		List<ModelPrediction> testSetPredictions = new ArrayList<ModelPrediction>();
	}
	
	private static ModelService modelService=new ModelServiceImpl();
	private static DataPointInSplittingService dataPointInSplittingService=new DataPointInSplittingServiceImpl();
	private static DataPointService dataPointService=new DataPointServiceImpl();
	
	private static PredictionService predictionService=new PredictionServiceImpl();
	private static ModelStatisticService modelStatisticService=new ModelStatisticServiceImpl();
	private static StatisticService statisticService=new StatisticServiceImpl();
	private static SplittingServiceImpl splittingService=new SplittingServiceImpl();


	private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	
	private void writeModelExcelComparison(Long modelId, String folderPath) {
		Model model = modelService.findById(modelId);
		if (model==null) {
			return;
		}
		
		String datasetName = model.getDatasetName();
		String descriptorSetName = model.getDescriptorSetName();
		String splittingName = model.getSplittingName();
		String methodName = model.getMethod().getName();
		
		Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
		
		SplitPredictions splitPredictions = getSplitPredictions(model,splitting);
		Double meanExpTraining = ModelStatisticCalculator.calcMeanExpTraining(splitPredictions.trainingSetPredictions);
		
		Workbook workbook = new XSSFWorkbook();
		Sheet trainingSetSheet = workbook.createSheet("Training");
		Sheet testSetSheet = workbook.createSheet("Test");
		
		boolean isBinary = model.getMethod().getIsBinary();
		CellStyle headerStyle = createHeaderStyle(workbook);
		populateSheet(splitPredictions.trainingSetPredictions, meanExpTraining, DevQsarConstants.TAG_TRAINING, isBinary,
				trainingSetSheet, headerStyle);
		populateSheet(splitPredictions.testSetPredictions, meanExpTraining, DevQsarConstants.TAG_TEST, isBinary,
				testSetSheet, headerStyle);
		
		String subfolder = isBinary ? "binary/" : "continuous/";
		String fileName = subfolder + modelId + "_" + String.join("_", datasetName, descriptorSetName, splittingName, methodName) + ".xlsx";
		try {
			OutputStream fos = new FileOutputStream(folderPath + "/" + fileName);
			workbook.write(fos);
			workbook.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void populateSheet(List<ModelPrediction> predictions, Double meanExpTraining, String tag, boolean isBinary,
			Sheet sheet, CellStyle headerStyle) {
		int lastRow = populatePredictions(predictions, sheet);
		if (lastRow <= 1) {
			return;
		}
		
		Map<String, Double> stats = null;
		if (isBinary) {
			stats = ModelStatisticCalculator.calculateBinaryStatistics(predictions, DevQsarConstants.BINARY_CUTOFF, tag);
			populateJavaBinaryStats(stats, tag, sheet);
			populateExcelBinaryStats(tag, lastRow, sheet);
			makeBinaryStatsPretty(sheet, headerStyle);
		} else {
			stats = ModelStatisticCalculator.calculateContinuousStatistics(predictions, meanExpTraining, tag);
			populateJavaContinuousStats(stats, tag, sheet);
			populateExcelContinuousStats(tag, lastRow, sheet);
			makeContinuousStatsPretty(sheet, headerStyle);
		}
	}
	
	private int populatePredictions(List<ModelPrediction> predictions, Sheet sheet) {
		Row headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("ID");
		headerRow.createCell(1).setCellValue("EXP");
		headerRow.createCell(2).setCellValue("PRED");
		
		int lastRow = 0;
		for (int i = 0; i < predictions.size(); i++) {
			ModelPrediction pred = predictions.get(i);
			lastRow = i + 1;
			Row row = sheet.createRow(lastRow);
			row.createCell(0).setCellValue(pred.id);
			row.createCell(1).setCellValue(pred.exp);
			row.createCell(2).setCellValue(pred.pred);
		}
		
		return lastRow;
	}
	
	private void populateJavaContinuousStats(Map<String, Double> stats, String tag, Sheet sheet) {
		String coeffDet = tag.equals(DevQsarConstants.TAG_TEST) ? DevQsarConstants.Q2_TEST : DevQsarConstants.R2_TRAINING;
		String pearsonRsq = DevQsarConstants.PEARSON_RSQ + tag;
		Row headerRow = sheet.getRow(0);
		headerRow.createCell(4).setCellValue("JAVA_" + coeffDet.toUpperCase());
		headerRow.createCell(5).setCellValue("JAVA_" + pearsonRsq.toUpperCase());
		
		Row statsRow = sheet.getRow(1);
		statsRow.createCell(4).setCellValue(stats.get(coeffDet));
		statsRow.createCell(5).setCellValue(stats.get(pearsonRsq));
	}
	
	private void populateJavaBinaryStats(Map<String, Double> stats, String tag, Sheet sheet) {
		String sn = DevQsarConstants.SENSITIVITY + tag;
		String sp = DevQsarConstants.SPECIFICITY + tag;
		String ba = DevQsarConstants.BALANCED_ACCURACY + tag;
		
		Row headerRow = sheet.getRow(0);
		headerRow.createCell(4).setCellValue("JAVA_" + sn.toUpperCase());
		headerRow.createCell(5).setCellValue("JAVA_" + sp.toUpperCase());
		headerRow.createCell(6).setCellValue("JAVA_" + ba.toUpperCase());
		
		Row statsRow = sheet.getRow(1);
		statsRow.createCell(4).setCellValue(stats.get(sn));
		statsRow.createCell(5).setCellValue(stats.get(sp));
		statsRow.createCell(6).setCellValue(stats.get(ba));
	}
	
	private void populateExcelContinuousStats(String tag, int lastRow, Sheet sheet) {
		String pearsonRsq = DevQsarConstants.PEARSON_RSQ + tag;
		Row secondHeaderRow = sheet.getRow(3);
		secondHeaderRow.createCell(5).setCellValue("EXCEL_" + pearsonRsq.toUpperCase());
		
		Row secondStatsRow = sheet.getRow(4);
		int realLastRow = lastRow + 1;
		String expRange = "B2:B" + realLastRow;
		String predRange = "C2:C" + realLastRow;
		String pearsonRsqFormula = "RSQ(" + expRange + "," + predRange + ")";
		secondStatsRow.createCell(5).setCellFormula(pearsonRsqFormula);
	}
	
	private void populateExcelBinaryStats(String tag, int lastRow, Sheet sheet) {
		Row tableHeaderRow = sheet.getRow(6);
		tableHeaderRow.createCell(5).setCellValue("PRED_1");
		tableHeaderRow.createCell(6).setCellValue("PRED_0");
		Row tableFirstRow = sheet.getRow(7);
		tableFirstRow.createCell(4).setCellValue("EXP_1");
		Row tableSecondRow = sheet.getRow(8);
		tableSecondRow.createCell(4).setCellValue("EXP_0");
		
		int realLastRow = lastRow + 1;
		String expRange = "B2:B" + realLastRow;
		String predRange = "C2:C" + realLastRow;
		String tpFormula = "SUMPRODUCT((" + expRange + ">=0.5)*(" + predRange + ">=0.5))";
		String tnFormula = "SUMPRODUCT((" + expRange + "<0.5)*(" + predRange + "<0.5))";
		String fpFormula = "SUMPRODUCT((" + expRange + "<0.5)*(" + predRange + ">=0.5))";
		String fnFormula = "SUMPRODUCT((" + expRange + ">=0.5)*(" + predRange + "<0.5))";
		tableFirstRow.createCell(5).setCellFormula(tpFormula);
		tableFirstRow.createCell(6).setCellFormula(fnFormula);
		tableSecondRow.createCell(5).setCellFormula(fpFormula);
		tableSecondRow.createCell(6).setCellFormula(tnFormula);
		
		String sn = DevQsarConstants.SENSITIVITY + tag;
		String sp = DevQsarConstants.SPECIFICITY + tag;
		String ba = DevQsarConstants.BALANCED_ACCURACY + tag;
		
		Row secondHeaderRow = sheet.getRow(3);
		secondHeaderRow.createCell(4).setCellValue("EXCEL_" + sn.toUpperCase());
		secondHeaderRow.createCell(5).setCellValue("EXCEL_" + sp.toUpperCase());
		secondHeaderRow.createCell(6).setCellValue("EXCEL_" + ba.toUpperCase());
		
		Row secondStatsRow = sheet.getRow(4);
		String snFormula = "F8/(F8+G8)";
		String spFormula = "G9/(F9+G9)";
		String baFormula = "(E5+F5)/2";
		secondStatsRow.createCell(4).setCellFormula(snFormula);
		secondStatsRow.createCell(5).setCellFormula(spFormula);
		secondStatsRow.createCell(6).setCellFormula(baFormula);
	}
	
	private void makeContinuousStatsPretty(Sheet sheet, CellStyle headerStyle) {
		Row headerRow = sheet.getRow(0);
		Row secondHeaderRow = sheet.getRow(3);
		for (int i = 0; i <= 5; i++) {
			if (i!=3) {
				headerRow.getCell(i).setCellStyle(headerStyle);
				if (i==5) {
					secondHeaderRow.getCell(i).setCellStyle(headerStyle);
				}
			}
			sheet.autoSizeColumn(i);
		}
	}
	
	private void makeBinaryStatsPretty(Sheet sheet, CellStyle headerStyle) {
		Row headerRow = sheet.getRow(0);
		Row secondHeaderRow = sheet.getRow(3);
		Row tableHeaderRow = sheet.getRow(6);
		Row tableFirstRow = sheet.getRow(7);
		Row tableSecondRow = sheet.getRow(8);
		for (int i = 0; i <= 6; i++) {
			if (i!=3) {
				headerRow.getCell(i).setCellStyle(headerStyle);
				if (i>3) {
					secondHeaderRow.getCell(i).setCellStyle(headerStyle);
					if (i==4) {
						tableFirstRow.getCell(i).setCellStyle(headerStyle);
						tableSecondRow.getCell(i).setCellStyle(headerStyle);
					} else {
						tableHeaderRow.getCell(i).setCellStyle(headerStyle);
					}
				}
			}
			sheet.autoSizeColumn(i);
		}
	}
	
	private static CellStyle createHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		return style;
	}
	
	public static void writeExcelComparisons() {
		RecalcStatsScript script = new RecalcStatsScript();
		String folderPath = "data/recalc_stats/excel_with_binary";
		File binaryFolder = new File(folderPath + "/binary");
		File continuousFolder = new File(folderPath + "/continuous");
		binaryFolder.mkdirs();
		continuousFolder.mkdirs();
		
		for (Long l = 1L; l < 128L; l++) {
			script.writeModelExcelComparison(l, folderPath);
		}
	}
	
	private void addModelTrainingSetStatsToDatabase(Long modelId, String lanId) {
		Model model = modelService.findById(modelId);
		if (model==null) {
			return;
		}
		Splitting splitting=splittingService.findByName(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE);
		
		SplitPredictions splitPredictions = getSplitPredictions(model,splitting);
		
		Map<String, Double> modelTrainingStatisticValues = null;
		if (model.getMethod().getIsBinary()) {
			modelTrainingStatisticValues = 
					ModelStatisticCalculator.calculateBinaryStatistics(splitPredictions.trainingSetPredictions, 
							DevQsarConstants.BINARY_CUTOFF,
							DevQsarConstants.TAG_TRAINING);
		} else {
			modelTrainingStatisticValues = 
					ModelStatisticCalculator.calculateContinuousStatistics(splitPredictions.trainingSetPredictions, 
							ModelStatisticCalculator.calcMeanExpTraining(splitPredictions.trainingSetPredictions),
							DevQsarConstants.TAG_TRAINING);
		}
		
		for (String statisticName:modelTrainingStatisticValues.keySet()) {
			Statistic statistic = statisticService.findByName(statisticName);
			ModelStatistic modelStatistic = new ModelStatistic(statistic, model, modelTrainingStatisticValues.get(statisticName), lanId);
			try {
				modelStatisticService.create(modelStatistic);
			} catch (ConstraintViolationException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
	public static void addTrainingSetStatsToDatabase() {
		RecalcStatsScript script = new RecalcStatsScript();
		for (Long l = 7L; l < 128L; l++) {
			script.addModelTrainingSetStatsToDatabase(l, "gsincl01");
		}
	}
	
	public static void viewPredsForSplitSet(Long modelId) {
		Model model = modelService.findById(modelId);
		
		Splitting splitting=splittingService.findByName(model.getSplittingName());
		
		System.out.println(model.getDatasetName());
		System.out.println(splitting.getName());
		
		SplitPredictions splitPredictions = getSplitPredictions(model,splitting);
		
		System.out.println("training set predictions");
		for (ModelPrediction mp:splitPredictions.trainingSetPredictions) {
			System.out.println(String.join("\t", mp.id, String.valueOf(mp.exp), String.valueOf(mp.pred)));
		}
		
		System.out.println("\ntest set predictions");
		for (ModelPrediction mp:splitPredictions.testSetPredictions) {
			System.out.println(String.join("\t", mp.id, String.valueOf(mp.exp), String.valueOf(mp.pred)));
		}

	}
	
	/**
	 * 
	 * Calculates Q2_F3 see eqn 2 of Consonni et al, 2019 (https://onlinelibrary.wiley.com/doi/full/10.1002/minf.201800029)
	 * 
	 * @param modelId
	 * @return
	 */
	public double calculateCV_Stat(long modelId,String stat) {
		
		Model model=modelService.findById(modelId);
		

		double stat_Avg=0;
		for (int i=1;i<=5;i++) {
			
			Splitting splittingCV=splittingService.findByName("CV"+i);
			
			SplitPredictions sp=getSplitPredictions(model, splittingCV);
						
			double stat_i=0;
			
			if(stat.equals("Q2_CV")) {
				stat_i=ModelStatisticCalculator.calculateQ2(sp.trainingSetPredictions, sp.testSetPredictions);	
			} else if(stat.equals("R2_CV")) {				
				double YbarTrain=ModelStatisticCalculator.calcMeanExpTraining(sp.trainingSetPredictions);				
				Map<String, Double>mapStats=ModelStatisticCalculator.calculateContinuousStatistics(sp.testSetPredictions, YbarTrain, DevQsarConstants.TAG_TEST);				
				stat_i=mapStats.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
			}
						
			System.out.println("stat"+i+"="+stat_i);			
			stat_Avg+=stat_i;
		}		
		stat_Avg/=5.0;
		System.out.println("stat_Avg="+stat_Avg);
		
		return stat_Avg;
		
	}
	
	public static SplitPredictions getSplitPredictions(Model model, Splitting splitting) {
		
		SplitPredictions splitPredictions=new SplitPredictions();
		
		List<DataPoint> dataPoints =dataPointService.findByDatasetName(model.getDatasetName());
		
		Map<String, Double> expMap = dataPoints.stream()
				.collect(Collectors.toMap(dp -> dp.getCanonQsarSmiles(), dp -> dp.getQsarPropertyValue()));

		List<Prediction> predictions = predictionService.findByIds(model.getId(),splitting.getId());
		
		List<DataPointInSplitting> dataPointsInSplitting = 
				dataPointInSplittingService.findByDatasetNameAndSplittingName(model.getDatasetName(), splitting.getName());
		Map<String, Integer> splittingMap = dataPointsInSplitting.stream()
				.collect(Collectors.toMap(dpis -> dpis.getDataPoint().getCanonQsarSmiles(), dpis -> dpis.getSplitNum()));

		for(Prediction prediction:predictions) {
			
			String smiles=prediction.getCanonQsarSmiles();
			double exp=expMap.get(prediction.getCanonQsarSmiles());
			double pred=prediction.getQsarPredictedValue();
			int splitNum=splittingMap.get(prediction.getCanonQsarSmiles()); 			
			
			ModelPrediction mp=new ModelPrediction(smiles,exp,pred,splitNum);
			
			if (splitNum==DevQsarConstants.TRAIN_SPLIT_NUM) {
				splitPredictions.trainingSetPredictions.add(mp);	
			} else if (splitNum==DevQsarConstants.TEST_SPLIT_NUM) {
				splitPredictions.testSetPredictions.add(mp);
			}
		}
		
		if (splitPredictions.trainingSetPredictions.size()>0)		
			return splitPredictions;
		
		//For Cross Validation there wont be predictions for training set in the database so:
		
		for (DataPointInSplitting dpis:dataPointsInSplitting) {

			if(dpis.getSplitNum()!=DevQsarConstants.TRAIN_SPLIT_NUM) continue;//can have splitNum=2 for the PFAS ones...
			
			DataPoint dp=dpis.getDataPoint();
			
			String id=dp.getCanonQsarSmiles();
			double exp=expMap.get(dp.getCanonQsarSmiles());
			double pred=Double.NaN;//we dont have preds for training in CV 
				
			ModelPrediction mp=new ModelPrediction(id, exp, pred, DevQsarConstants.TRAIN_SPLIT_NUM);
			splitPredictions.trainingSetPredictions.add(mp);	
			
		}

		return splitPredictions;
	}
	
	
	private List<ModelPrediction> getModelPredictions(Map<String, Integer> splittingMap, Map<String, Double> expMap, Model model,Splitting splitting, int splitNum) {
		
		List<Prediction> predictions = predictionService.findByIds(model.getId(),splitting.getId());
		List<ModelPrediction>mps=new ArrayList<>();	
		
		for(Prediction prediction:predictions) {

			int splitNumPred=splittingMap.get(prediction.getCanonQsarSmiles()); 			
			if (splitNumPred!=splitNum) continue;
			
			double exp=expMap.get(prediction.getCanonQsarSmiles());
			double pred=prediction.getQsarPredictedValue();
			String smiles=prediction.getCanonQsarSmiles();
			
			ModelPrediction mp=new ModelPrediction(smiles,exp,pred,splitNum);			
			
//			System.out.println(mp.id+"\t"+mp.exp+"\t"+pred);
			
			mps.add(mp);
		}
		return mps;
	}
	

	public static void main(String[] args) {
//		writeExcelComparisons();
		
		RecalcStatsScript r=new RecalcStatsScript();
		
//		Model model=r.modelService.findById(1051L);
//		r.getSplitPredictions(model);
		
//				strModelID=1111
//				http://localhost:5004/models/knn/1111
//				storing R2_CV_Training=0.6700950135231265
//				storing Q2_CV_Training=0.6364247502716454
		
//		r.calculateCV_Stat(1111,"R2_CV");
//		r.calculateCV_Stat(1111,"Q2_CV");
	
//		r.viewPredsForSplitSet(1111L);
		
		
		
	}

}
