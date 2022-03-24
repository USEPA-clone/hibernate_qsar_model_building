package gov.epa.endpoints.reports.predictions.ExcelReports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ScatterChartSeries;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.charts.XSSFChartLegend;
import org.apache.poi.xssf.usermodel.charts.XSSFScatterChartData;
import org.apache.poi.xssf.usermodel.charts.XSSFValueAxis;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTitle;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.STMarkerStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.models.ModelPrediction;
import gov.epa.endpoints.models.ModelStatisticCalculator;
import gov.epa.endpoints.reports.OriginalCompound;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.PredictionReportGenerator;
import gov.epa.endpoints.reports.predictions.PredictionReport.PredictionReportDataPoint;

public class ExcelPredictionReportGenerator {


//	String outpath;
//	String inpath = "data\\reports";
	public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//	private PredictionReport report;

//	public Workbook wb = new XSSFWorkbook();
//	private Boolean isBinary;
//	String reportName;
//	public FileOutputStream out;



	private static class Stats {
		Double R2;
		Double Q2;
		Double RMSE;
		Double MAE;
		Double BA;
		Double SN;
		Double SP;
		Double Coverage;
		String splitting;
		String type;
		private ArrayList<String> continuousStats = new ArrayList<String>(Arrays.asList("R2", "Q2", "RMSE", "MAE", "Coverage"));
		private ArrayList<String> binaryStats = new ArrayList<String>(Arrays.asList("BA", "SN", "SP", "Coverage"));


		// switch statement limited to newer version of java on strings for some reason
		private void alignContinuous(String statisticName, Double statisticValue) {
			if (this.splitting.equals("Test")) {
			if (statisticName.equals("PearsonRSQ_Test")) {
				this.R2 = statisticValue;
			} else if (statisticName.equals("Q2_Test")) {
				this.Q2 = statisticValue;
			} else if (statisticName.equals("RMSE_Test")) {
				this.RMSE = statisticValue;
			} else if (statisticName.equals("MAE_Test")) {
				this.MAE = statisticValue;
			} else if (statisticName.equals("Coverage_Test")) {
				this.Coverage = statisticValue;
			}
			} else if (this.splitting.equals("Training")) {
				if (statisticName.equals("PearsonRSQ_Training")) {
					this.R2 = statisticValue;
				} else if (statisticName.equals("RMSE_Training")) {
					this.RMSE = statisticValue;
				} else if (statisticName.equals("MAE_Training")) {
					this.MAE = statisticValue;
				} else if (statisticName.equals("Coverage_Training")) {
					this.Coverage = statisticValue;
				}

			}

		}

		// switch statement limited to newer version of java on strings for some reason
		private void alignBinary(String statisticName, Double statisticValue) {
			if (this.splitting.equals("Test")) {
			if (statisticName.equals("BA_Test")) {
				this.BA = statisticValue;
			} else if (statisticName.equals("SN_Test")) {
				this.SN = statisticValue;
			} else if (statisticName.equals("SP_Test")) {
				this.SP = statisticValue;
			} else if (statisticName.equals("Coverage_Test")) {
				this.Coverage = statisticValue;
			}
			} else if (this.splitting.equals("Training")) {
				if (statisticName.equals("BA_Training")) {
					this.BA = statisticValue;
				} else if (statisticName.equals("SN_Training")) {
					this.SN = statisticValue;
				} else if (statisticName.equals("SP_Training")) {
					this.SP = statisticValue;
				} else if (statisticName.equals("Coverage_Training")) {
					this.Coverage = statisticValue;
				}

			}

		}
		
		private ArrayList<Object> provideStats() {
			if (this.type.equals("Continuous")) {
				return new ArrayList<Object>(Arrays.asList(this.splitting, this.R2, this.Q2, this.RMSE, this.MAE, this.Coverage));
			}
			else {
				return new ArrayList<Object>(Arrays.asList(this.splitting, this.BA, this.SN, this.SP, this.Coverage));
			}
		}



	}









	private static void printHT(Hashtable hashtable) {
		Enumeration<String> keys = hashtable.keys();
		while(keys.hasMoreElements()){
			String key = keys.nextElement();
			System.out.println("key:" + key + " value:" + hashtable.get(key));
		}
	}

	private static void printmap(Map map) {
		Iterator<Integer> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next());
		}

	}

	public static void main(String [] args) {
		ExcelPredictionReportGenerator e=new ExcelPredictionReportGenerator();
		
		PredictionReportGenerator gen = new PredictionReportGenerator();

		String endpoint=DevQsarConstants.DEV_TOX;
		String datasetName = endpoint+" TEST";
		String descriptorSetName="T.E.S.T. 5.1";

		String splittingName="TEST";
		String modelSetName="Sample models";

		PredictionReport predictionReport = null;

		
		predictionReport=gen.generateForModelSetPredictions(datasetName, splittingName,modelSetName);

//		File jsonFile = new File("data/reports/Henry's law constant OPERA_PredictionReport.json");
		
		try {
//		predictionReport = gson.fromJson(new FileReader(jsonFile), PredictionReport.class);

		} catch (Exception ex) {
			
		}
//		String json=gson.toJson(predictionReport);
		
				
			
		File folder=new File("data/reports");
		folder.mkdirs();		
//		e.generate( predictionReport,folder.getAbsolutePath()+File.separator+"report.xlsx");
		e.generate(predictionReport, folder.getAbsolutePath()+File.separator+datasetName + "_report.xlsx");
	}

	public void generate(PredictionReport report, String filepathOut) {
		XSSFWorkbook wb=new XSSFWorkbook();		
		boolean isBinary = report.predictionReportMetadata.datasetUnit.equalsIgnoreCase("binary") ? true : false;
		generateCoverSheet2(report,wb, isBinary);
		
		generateSummarySheet2(report, wb, isBinary);
		
		generateSplitSheet2(report, wb, isBinary);
		
		for (int i = 0; i < report.predictionReportDataPoints.get(0).qsarPredictedValues.size(); i++) {
			generatePredictionSheet2(report.predictionReportDataPoints, i, wb, isBinary);
		}
		
		columnResizing(wb);
		
		try {
			FileOutputStream out = new FileOutputStream(filepathOut);
			wb.write(out);
			wb.close();			
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Spreadsheet written successfully" );

		

	}
	


	public void generate3(PredictionReport report,String filepathOut) {

		XSSFWorkbook wb=new XSSFWorkbook();		
		boolean isBinary = report.predictionReportMetadata.datasetUnit.equalsIgnoreCase("binary") ? true : false;
		
		generateCoverSheet(report,wb, isBinary);

		
//		System.out.println("number of data points="+report.predictionReportDataPoints.size());

		generateCoverSheet(report,wb, isBinary);
		generateSplitSheet(report,wb, isBinary);

		ArrayList<modelHashTables> manyModelHashTables = new ArrayList<modelHashTables>();
		// purely for resizing later on
		ArrayList<String> modelNames = new ArrayList<String>();
		for (int i = 0; i < report.predictionReportDataPoints.get(0).qsarPredictedValues.size(); i++) {

			// this is like the second return type for generatePredictionSheet, added to w/ side effects
			modelHashTables modelHashTables = new modelHashTables();
			Map < String, Object[] > map = generatePredictionSheet(report.predictionReportDataPoints,i,modelHashTables);

			populateSheet(map, modelHashTables.modelName, false,wb, isBinary);
			manyModelHashTables.add(modelHashTables);

			modelNames.add(modelHashTables.modelName);
		}
		List<ModelPrediction> modelPredictionsTest = new ArrayList<ModelPrediction>();
		List<ModelPrediction> modelPredictionsTraining = new ArrayList<ModelPrediction>();
		modelHashTables consensusHashTables = new modelHashTables(); //

		Map < String, Object[] > consensusMap = generateConsensusSheet(manyModelHashTables, consensusHashTables, modelPredictionsTest, modelPredictionsTraining);


		populateSheet(consensusMap, "Consensus", false,wb, isBinary);
		
		if (isBinary) {
			Map<String, Double> StatisticsMap = ModelStatisticCalculator.calculateBinaryStatistics(modelPredictionsTest, 0.5, DevQsarConstants.TAG_TEST);
			// pulling the same thing twice to avoid errors			
			// generateSummarySheet(report, StatisticsMap, StatisticsMap, wb, isBinary);
			
		} else {
			//TODO This is wrong- needs to only look at experimental values for training compounds:
			double avgExpTraining=findExperimentalAverage(modelPredictionsTraining);
			System.out.println("avgExpTraining="+avgExpTraining);//doesnt match spreadsheet
			
			//TODO- This is wrong- needs to just use chemicals appearing in the test set:
			Map<String, Double> StatisticsMapTest = ModelStatisticCalculator.calculateContinuousStatistics(modelPredictionsTest, avgExpTraining, DevQsarConstants.TAG_TEST);
			
			
			for (int i =0 ; i < modelPredictionsTraining.size(); i++) {
				System.out.println(modelPredictionsTraining.get(i).exp);
			}
//			TODO This is wrong- needs to just use chemicals appearing in the training set
			Map<String, Double> StatisticsMapTrain = ModelStatisticCalculator.calculateContinuousStatistics(modelPredictionsTraining, avgExpTraining, DevQsarConstants.TAG_TRAINING);
			// generateSummarySheet(report, StatisticsMapTest, StatisticsMapTrain, wb, isBinary);
		}
		
		wb.setSheetOrder("Summary", 1);

		String[] trainTest = {"Training", "Test"};
		for (String s:trainTest) {
			XSSFSheet sheet = (XSSFSheet) wb.getSheet(s);
			sheet.setColumnWidth(2, 50 * 256);
			sheet.setColumnWidth(3, 50 * 256);
			sheet.setColumnWidth(5, 50 * 256);
		}

		modelNames.add("Consensus");
		for (String s:modelNames) {
			XSSFSheet sheet = (XSSFSheet) wb.getSheet(s);
			sheet.setColumnWidth(0, 50 * 256);
		}

		try {
		
			FileOutputStream out = new FileOutputStream(filepathOut);
			wb.write(out);
			wb.close();			
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Spreadsheet written successfully" );
	}



	public void generate(String inputFilePath,String outputFilePath) {
		
		PredictionReport report = null;

		File jsonFile = new File(inputFilePath);

		try {
			report = gson.fromJson(new FileReader(jsonFile), PredictionReport.class);

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		generate(report,outputFilePath);
	}




	public void generateSplitSheet(PredictionReport predictionReport, Workbook wb,boolean isBinary) {
		Map < String, Object[] > trainMap = new TreeMap < String, Object[] >();
		trainMap.put( "AAA", new Object[] { "DTXCID","CASRN", "Preferred Name", "Smiles", "MolecularWeight", "Canonical QSAR Ready Smiles", "Experimental Value" });

		Map < String, Object[] > testMap = new TreeMap < String, Object[] >();
		testMap.put( "AAA", new Object[] { "DTXCID","CASRN", "Preferred Name", "Smiles", "MolecularWeight", "Canonical QSAR Ready Smiles", "Experimental Value" });

		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {

			OriginalCompound oc = null;
			// need to sort for consistency
			try {
				oc = predictionReport.predictionReportDataPoints.get(i).originalCompounds.get(0);
			} catch (IndexOutOfBoundsException ex) {
				// ex.printStackTrace();
				continue;
			}
			String canonQsarSmiles = predictionReport.predictionReportDataPoints.get(i).canonQsarSmiles;
			boolean train = predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == 0 ? true : false;
			if (oc != null) {
				Object[] row = new Object[] { oc.dtxcid, oc.casrn, oc.preferredName, oc.smiles, oc.molWeight, canonQsarSmiles, predictionReport.predictionReportDataPoints.get(i).experimentalPropertyValue 
				};

				if (train == true) {
					trainMap.put("BBB" + String.valueOf(i), row);
				} else {
					testMap.put("BBB" + String.valueOf(i), row);
				}
			}
		}

		populateSheet(trainMap, "Training", true, wb, isBinary);
		populateSheet(testMap, "Test", true, wb, isBinary);


	}

	public void generateCoverSheet(PredictionReport predictionReport, Workbook wb, boolean isBinary) {
		Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();

		spreadsheetMap.put("A", prepareCoverSheetRow("Property Name", predictionReport.predictionReportMetadata.datasetProperty));
		spreadsheetMap.put("B", prepareCoverSheetRow("Property Description", predictionReport.predictionReportMetadata.datasetPropertyDescription));
		spreadsheetMap.put("C", prepareCoverSheetRow("Dataset Name", predictionReport.predictionReportMetadata.datasetName));
		spreadsheetMap.put("D", prepareCoverSheetRow("Dataset Description", predictionReport.predictionReportMetadata.datasetDescription));
		spreadsheetMap.put("E", prepareCoverSheetRow("Property Units", predictionReport.predictionReportMetadata.datasetUnit));

		int nTrain = 0;
		int nPredict = 0;
		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
			if (!(predictionReport.predictionReportDataPoints.get(i) == null)) {
				if (predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == 0) {
					nTrain++;
				} else {
					nPredict++;
				}
			}
		}

		spreadsheetMap.put("G", prepareCoverSheetRow("nTraining", String.valueOf(nTrain)));
		spreadsheetMap.put("H", prepareCoverSheetRow("nTEST", String.valueOf(nPredict)));

		populateSheet(spreadsheetMap, "Cover sheet", true, wb, isBinary);

	}
	
	public void generateCoverSheet2(PredictionReport predictionReport, Workbook wb, boolean isBinary) {
		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();

		spreadsheetMap.put(0, prepareCoverSheetRow("Property Name", predictionReport.predictionReportMetadata.datasetProperty));
		spreadsheetMap.put(1, prepareCoverSheetRow("Property Description", predictionReport.predictionReportMetadata.datasetPropertyDescription));
		spreadsheetMap.put(2, prepareCoverSheetRow("Dataset Name", predictionReport.predictionReportMetadata.datasetName));
		spreadsheetMap.put(3, prepareCoverSheetRow("Dataset Description", predictionReport.predictionReportMetadata.datasetDescription));
		spreadsheetMap.put(4, prepareCoverSheetRow("Property Units", predictionReport.predictionReportMetadata.datasetUnit));
	//	spreadsheetMap.put(5, prepareCoverSheetRow("Descriptor Set Name", predictionReport.predictionReportMetadata.descriptorSetName));

		int nTrain = 0;
		int nPredict = 0;
		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {
			if (!(predictionReport.predictionReportDataPoints.get(i) == null)) {
				if (predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == 0) {
					nTrain++;
				} else {
					nPredict++;
				}
			}
		}

		spreadsheetMap.put(6, prepareCoverSheetRow("nTraining", String.valueOf(nTrain)));
		spreadsheetMap.put(7, prepareCoverSheetRow("nTEST", String.valueOf(nPredict)));

		populateSheet2(spreadsheetMap, wb, isBinary, "Cover sheet");

	}

	

	private static Object[] prepareCoverSheetRow(String name, String value) {
		List<String> rowArrayList = new ArrayList<String>(Arrays.asList(name, value));
		return rowArrayList.toArray(new Object[rowArrayList.size()]);
	}
	
	public void generateSplitSheet2(PredictionReport predictionReport, Workbook wb,boolean isBinary) {
		Map < Integer, Object[] > trainMap = new TreeMap < Integer, Object[] >();
		trainMap.put( 0, new Object[] { "DTXCID","CASRN", "Preferred Name", "Smiles", "MolecularWeight", "Canonical QSAR Ready Smiles", "Experimental Value" });

		Map < Integer, Object[] > testMap = new TreeMap < Integer, Object[] >();
		testMap.put( 0, new Object[] { "DTXCID","CASRN", "Preferred Name", "Smiles", "MolecularWeight", "Canonical QSAR Ready Smiles", "Experimental Value" });

		for (int i = 0; i < predictionReport.predictionReportDataPoints.size(); i++) {

			OriginalCompound oc = null;
			// need to sort for consistency
			try {
				oc = predictionReport.predictionReportDataPoints.get(i).originalCompounds.get(0);
			} catch (IndexOutOfBoundsException ex) {
				// ex.printStackTrace();
				continue;
			}
			String canonQsarSmiles = predictionReport.predictionReportDataPoints.get(i).canonQsarSmiles;
			boolean train = predictionReport.predictionReportDataPoints.get(i).qsarPredictedValues.get(0).splitNum == 0 ? true : false;
			if (oc != null) {
				Object[] row = new Object[] { oc.dtxcid, oc.casrn, oc.preferredName, oc.smiles, oc.molWeight, canonQsarSmiles, predictionReport.predictionReportDataPoints.get(i).experimentalPropertyValue 
				};

				if (train == true) {
					trainMap.put(2 * i + 1, row);
				} else {
					testMap.put(2 * i + 2, row);
				}
			}
		}

		populateSheet2(trainMap,  wb, isBinary, "Training");
		populateSheet2(testMap, wb, isBinary, "Test");


	}

	
	
	private void generateSummarySheet2(PredictionReport report, XSSFWorkbook wb,
			boolean isBinary) {
		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();
		ArrayList<String> headerStats = new ArrayList<String>(Arrays.asList("Dataset Name", "Descriptor Software", "Method Name", "Split"));
		
		if (isBinary) headerStats.addAll(new Stats().binaryStats);
		else headerStats.addAll(new Stats().continuousStats);

		String[] headerStatsArray = new String[headerStats.size()];
		headerStatsArray = headerStats.toArray(headerStatsArray);
		Object[] headerStatsObject = headerStatsArray;
		spreadsheetMap.put(0, headerStatsObject);	        
		
		
		for (int i = 0; i < report.predictionReportModelMetadata.size(); i++) {
			
			Stats testStats = new Stats();
			testStats.splitting = "Test";
			Stats trainStats = new Stats();
			trainStats.splitting = "Training";
			
			
			ArrayList<Object> modelSplitInfoTrain = new ArrayList<Object>();
			modelSplitInfoTrain.add(report.predictionReportMetadata.datasetName);
			modelSplitInfoTrain.add("STANDINDESCRIPTORS");
			modelSplitInfoTrain.add(report.predictionReportModelMetadata.get(i).qsarMethodName);
			
			ArrayList<Object> modelSplitInfoTest = (ArrayList<Object>) modelSplitInfoTrain.clone();
			
			if (isBinary) {
				trainStats.type = "Binary";
				testStats.type = "Binary";
			for (int j = 0; j < report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
				trainStats.alignBinary(report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);
				testStats.alignBinary(report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);
			}
			} else {
				trainStats.type = "Continuous";
				testStats.type = "Continuous";
				for (int j = 0; j < report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
					trainStats.alignContinuous(report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);
					testStats.alignContinuous(report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, report.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);
				}
			}
			
			modelSplitInfoTrain.addAll(trainStats.provideStats());
			modelSplitInfoTest.addAll(testStats.provideStats());
			
			Object[] trainRow = modelSplitInfoTrain.toArray();
			Object[] testRow = modelSplitInfoTest.toArray();

			spreadsheetMap.put(2*i+1, trainRow);
			spreadsheetMap.put(2*i + 2, testRow);
			
			}
				
		populateSheet2(spreadsheetMap, wb, isBinary, "Summary sheet");
		
	}

/*
	public void generateSummarySheet(PredictionReport predictionReport, Map<String, Double> consensusStatisticsMap, Map<String, Double> consensusStatisticsMapTrain,Workbook wb, boolean isBinary) {
		Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();


		// String[] headers = new String[] { "modelid","datasetname", "descriptorsoftware", "splittingname", "methodname"};
		ArrayList<String> headerStats = new ArrayList<String>(Arrays.asList("Dataset Name", "Descriptor Software", "Method Name", "Split"));
		

		if (isBinary) headerStats.addAll(new BinaryStats().binaryStats);
		else headerStats.addAll(new ContinuousStats().continuousStats);
		

		String[] headerStatsArray = new String[headerStats.size()];
		headerStatsArray = headerStats.toArray(headerStatsArray);
		Object[] headerStatsObject = headerStatsArray;
		spreadsheetMap.put("AAA", headerStatsObject);	        


		for (int i = 0; i < predictionReport.predictionReportModelMetadata.size(); i++) {
			ArrayList<Object> rowArrayList = new ArrayList<Object>();
			rowArrayList.add(predictionReport.predictionReportMetadata.datasetName);
			rowArrayList.add(predictionReport.predictionReportModelMetadata.get(i).qsarMethodName);

			// "R2", "Q2", "RMSE", "MAE", "Coverage"

			if (isBinary) {
				rowArrayList.add(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics);
				BinaryStats bs = new BinaryStats();
				bs.splitting = "Test";

				for (int j = 0; j < predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
					bs.align_Test(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);	
				}

				Object[] row = new Object[] { predictionReport.predictionReportMetadata.datasetName,
						predictionReport.predictionReportModelMetadata.get(i).qsarMethodName,
						bs.splitting,
						bs.BA,bs.SN,bs.SP,bs.Coverage};
				
				BinaryStats bs_train = new BinaryStats();
				bs_train.splitting = "Training";

				for (int j = 0; j < predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
					bs_train.align_Test(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);	
				}

				
				
				Object[] row_train = new Object[] { predictionReport.predictionReportMetadata.datasetName,
						predictionReport.predictionReportMetadata.descriptorSetName,
						predictionReport.predictionReportModelMetadata.get(i).qsarMethodName,
						bs_train.splitting,
						bs_train.BA,bs_train.SN,bs_train.SP,bs_train.Coverage
				};

				

				spreadsheetMap.put("BBB" + String.valueOf(i), row);
				spreadsheetMap.put("BBB" + String.valueOf(i) + "Z", row_train);



			} else {
				rowArrayList.add(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics);

				ContinuousStats cs_test = new ContinuousStats();
				cs_test.splitting = "Test";
				for (int j = 0; j < predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
					cs_test.alignTest(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);	
				}

				ContinuousStats cs_train = new ContinuousStats();
				cs_train.splitting = "Training";
				for (int j = 0; j < predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.size(); j++) {
					cs_train.alignTrain(predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticName, predictionReport.predictionReportModelMetadata.get(i).predictionReportModelStatistics.get(j).statisticValue);	
				}        		


				Object[] row_test = new Object[] { predictionReport.predictionReportMetadata.datasetName,
						predictionReport.predictionReportModelMetadata.get(i).qsarMethodName,
						cs_test.splitting,
						cs_test.R2,cs_test.Q2,cs_test.RMSE,cs_test.MAE,cs_test.Coverage
				};

				Object[] row_train = new Object[] { predictionReport.predictionReportMetadata.datasetName,
						predictionReport.predictionReportModelMetadata.get(i).qsarMethodName,
						cs_train.splitting,
						cs_train.R2,cs_train.Q2,cs_train.RMSE,cs_train.MAE,cs_train.Coverage
				};


				spreadsheetMap.put("BBB" + String.valueOf(i), row_test);
				spreadsheetMap.put("BBB" + String.valueOf(i) + "Z", row_train);



			}
		}

		if (isBinary == false) {
			// adds the consensus row
			ContinuousStats cs_test = new ContinuousStats();
			// printmap(consensusStatisticsMap);
			cs_test.splitting = "Test";
			cs_test.R2 = consensusStatisticsMap.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TEST);
			cs_test.MAE = consensusStatisticsMap.get(DevQsarConstants.MAE +  DevQsarConstants.TAG_TEST);
			cs_test.RMSE = consensusStatisticsMap.get(DevQsarConstants.RMSE + DevQsarConstants.TAG_TEST);
			cs_test.Q2 = consensusStatisticsMap.get(DevQsarConstants.Q2_TEST);
			cs_test.Coverage = consensusStatisticsMap.get(DevQsarConstants.COVERAGE+ DevQsarConstants.TAG_TEST);;

			Object[] consensusrow_test = new Object[] { predictionReport.predictionReportMetadata.datasetName,
					"Consensus", cs_test.splitting,
					cs_test.R2,cs_test.Q2,cs_test.RMSE,cs_test.MAE,cs_test.Coverage
			};
			spreadsheetMap.put("CCC", consensusrow_test);

			ContinuousStats cs_train = new ContinuousStats();
			cs_train.splitting = "Training";
			// printmap(consensusStatisticsMap);

			cs_train.R2 = consensusStatisticsMapTrain.get(DevQsarConstants.PEARSON_RSQ + DevQsarConstants.TAG_TRAINING);
			cs_train.MAE = consensusStatisticsMapTrain.get(DevQsarConstants.MAE +  DevQsarConstants.TAG_TRAINING);
			cs_train.RMSE = consensusStatisticsMapTrain.get(DevQsarConstants.RMSE + DevQsarConstants.TAG_TRAINING);
			cs_train.Q2 = null;
			cs_train.Coverage = consensusStatisticsMapTrain.get(DevQsarConstants.COVERAGE+ DevQsarConstants.TAG_TRAINING);

			Object[] consensusrow_train = new Object[] { predictionReport.predictionReportMetadata.datasetName,
					"Consensus", cs_train.splitting,
					cs_train.R2,cs_train.Q2,cs_train.RMSE,cs_train.MAE,cs_train.Coverage
			};
			spreadsheetMap.put("CCB", consensusrow_train);

			/*
	        this should be for adding consensus stats
			 */
	
	/*
			populateSheet(spreadsheetMap, "Summary", true, wb, isBinary);
		} else if (isBinary) {
			BinaryStats bs = new BinaryStats();
			bs.BA = consensusStatisticsMap.get(DevQsarConstants.BALANCED_ACCURACY  + DevQsarConstants.TAG_TEST);
			bs.SN = consensusStatisticsMap.get(DevQsarConstants.SENSITIVITY+  DevQsarConstants.TAG_TEST);
			bs.SP = consensusStatisticsMap.get(DevQsarConstants.SPECIFICITY + DevQsarConstants.TAG_TEST);
			bs.Coverage = consensusStatisticsMap.get(DevQsarConstants.COVERAGE + DevQsarConstants.TAG_TEST);

			Object[] consensusrow = new Object[] { predictionReport.predictionReportMetadata.datasetName,
					"Consensus",
					bs.BA,bs.SN,bs.SP,bs.Coverage
			};
			spreadsheetMap.put("CCC", consensusrow);
			
			
			
			

			populateSheet(spreadsheetMap, "Summary", true, wb, isBinary);

		}


	}	
	*/

	private static double findExperimentalAverage(List<ModelPrediction> modelPredictions) {
		Double sum = 0.0;
		for (int i = 0; i < modelPredictions.size(); i++) {			
			sum += modelPredictions.get(i).exp;
		}
		return modelPredictions.size() > 0 ? sum / modelPredictions.size() : 0.0d;
	}

	private static Map < String, Object[] > generateConsensusSheet(ArrayList<modelHashTables> manyModelHashTables, modelHashTables consensusHashTables, List<ModelPrediction> modelPredictionsTest, List<ModelPrediction> modelPredictionsTraining) {
		final Hashtable<String,Double> predConsensusHash = new Hashtable<String,Double>();
		final Hashtable<String,ArrayList<Double>> predTable = new Hashtable<String,ArrayList<Double>>();
		final Hashtable<String,Double> expConsensusHash = manyModelHashTables.get(0).expHash;
		Set<String> keysTEST = manyModelHashTables.get(0).getPredictionHash().keySet();
		Set<String> keysTrain = manyModelHashTables.get(0).getExpHash().keySet();
		ArrayList<String> presentInAllModelKeys = new ArrayList<String>();
		presentInAllModelKeys.addAll(keysTEST);
		ArrayList<String> presentinTraining = new ArrayList<String>();
		presentinTraining.addAll(keysTrain);
		// ensures predictions are only collected for id's predicted for in all models
		for (int i = 0; i < manyModelHashTables.size(); i++) {
			for (String key:keysTEST) {
				if (!(manyModelHashTables.get(i).predictionHash.containsKey(key))) {
					presentInAllModelKeys.remove(key);
					expConsensusHash.remove(key);
				}
			}
		}
		

		for (int i = 0; i < manyModelHashTables.size(); i++) {
			for(String key: presentInAllModelKeys){

				if (!predTable.containsKey(key)) {
					ArrayList<Double> preds = new ArrayList<Double>();
					preds.add(manyModelHashTables.get(i).getPredictionHash().get(key));
					predTable.put(key,preds);
				} else {
					ArrayList<Double> preds = predTable.get(key);
					preds.add(manyModelHashTables.get(i).getPredictionHash().get(key));
					predTable.put(key,preds);
				}
			}

		}

		for (String key: presentInAllModelKeys) {
			ArrayList<Double> preds = predTable.get(key);
			Double sum = 0.0;
			for (Double pred : preds) {
				sum += pred;
			}

			Double avg = preds.size() > 0 ? sum / preds.size() : 0.0d;
			predConsensusHash.put(key, avg);
		}


		Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
		spreadsheetMap.put( "AAA", new Object[] { "ID","Exp", "Pred", "Error" });
		for(String key: presentInAllModelKeys){
			ModelPrediction modelPrediction = new ModelPrediction(key, expConsensusHash.get(key), predConsensusHash.get(key));
			modelPredictionsTraining.add(modelPrediction);
			spreadsheetMap.put(key, new Object[] { key, expConsensusHash.get(key) , predConsensusHash.get(key), Math.abs(expConsensusHash.get(key) - predConsensusHash.get(key)) });
		}
		
		for(String key: presentinTraining) {
			ModelPrediction modelPrediction = new ModelPrediction(key, expConsensusHash.get(key), predConsensusHash.get(key));
			modelPredictionsTest.add(modelPrediction);
		}



		return spreadsheetMap;
	}



	private void buildSpreadSheet(Map < String, Object[] > map) {

	}

	private static Map < String, Object[] > generatePredictionSheet(List<PredictionReportDataPoint> predictionReportDataPoints, int methodID, modelHashTables modelHashTable) {

		final Hashtable<String,Double> expHash = new Hashtable<String,Double>();
		final Hashtable<String,Double> predictionHash = new Hashtable<String,Double>();


		for (int i = 0; i < predictionReportDataPoints.size(); i++) {

			String compoundIdentifier = predictionReportDataPoints.get(i).canonQsarSmiles;

			if (hasPrediction(predictionReportDataPoints.get(i), methodID)) {

				expHash.put(compoundIdentifier, predictionReportDataPoints.get(i).experimentalPropertyValue);
				if (predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue != null) {
					predictionHash.put(compoundIdentifier, predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue);
				}
			}

		}
		modelHashTable.modelName = predictionReportDataPoints.get(0).qsarPredictedValues.get(methodID).qsarMethodName;
		modelHashTable.expHash = expHash;
		modelHashTable.predictionHash = predictionHash;

		Map < String, Object[] > spreadsheetMap = new TreeMap < String, Object[] >();
		spreadsheetMap.put( "AAA", new Object[] { "Canonical QSAR Ready Smiles","Exp", "Pred", "Error" });


		Set<String> keys = predictionHash.keySet();
		for(String key: keys){
			spreadsheetMap.put(key, new Object[] { key, expHash.get(key) , predictionHash.get(key), Math.abs(expHash.get(key) - predictionHash.get(key)) });
		}

		return spreadsheetMap;




	}
	
	
	private void generatePredictionSheet2(List<PredictionReportDataPoint> predictionReportDataPoints, int methodID, Workbook wb, Boolean isBinary) {

		final Hashtable<String,Double> expHash = new Hashtable<String,Double>();
		final Hashtable<String,Double> predictionHash = new Hashtable<String,Double>();

		String methodName = predictionReportDataPoints.get(0).qsarPredictedValues.get(methodID).qsarMethodName;
		
		for (int i = 0; i < predictionReportDataPoints.size(); i++) {

			String compoundIdentifier = predictionReportDataPoints.get(i).canonQsarSmiles;

			if (hasPrediction(predictionReportDataPoints.get(i), methodID)) {

				expHash.put(compoundIdentifier, predictionReportDataPoints.get(i).experimentalPropertyValue);
				if (predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue != null) {
					predictionHash.put(compoundIdentifier, predictionReportDataPoints.get(i).qsarPredictedValues.get(methodID).qsarPredictedValue);
				}
			}

		}

		Map < Integer, Object[] > spreadsheetMap = new TreeMap < Integer, Object[] >();
		spreadsheetMap.put( 0, new Object[] { "Canonical QSAR Ready Smiles","Exp", "Pred", "Error" });


		Set<String> keys = predictionHash.keySet();
		ArrayList<String> keyList = new ArrayList<String>();
		for (String key:keys) {
			keyList.add(key);
		}
		
		for(int i = 0; i < keyList.size(); i++) {
			spreadsheetMap.put(i + 1, new Object[] { keyList.get(i), expHash.get(keyList.get(i)) , predictionHash.get(keyList.get(i)), Math.abs(expHash.get(keyList.get(i)) - predictionHash.get(keyList.get(i))) });
		}

		populateSheet2(spreadsheetMap, wb, isBinary, methodName);




	}

	

	private static boolean hasPrediction(PredictionReportDataPoint prdp, int methodID) {
		if (prdp.qsarPredictedValues.get(methodID).splitNum == 1) {
			return true;
		}
		else {
			return false;
		}
	}

	private void columnResizing(Workbook wb) {

		String[] trainTest = {"Training", "Test"};
		for (String s:trainTest) {
			XSSFSheet sheet = (XSSFSheet) wb.getSheet(s);
			sheet.setColumnWidth(2, 50 * 256);
			sheet.setColumnWidth(3, 50 * 256);
			sheet.setColumnWidth(5, 50 * 256);
		}
		
		
		

	}
	
	private void populateSheet2(Map < Integer, Object[] > sheetMap, Workbook wb, boolean isBinary, String sheetName) {
		ExcelUtilities eu = new ExcelUtilities();
		System.out.println(sheetName);
		if (wb.getSheet(sheetName) != null) {
			return;
		}
		XSSFSheet sheet = (XSSFSheet) wb.createSheet(sheetName);
		// 2 decimal center aligned numeric cell style
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(wb.createDataFormat().getFormat("0.00"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		// boldstyle
		CellStyle boldstyle = wb.createCellStyle();//Create style
		Font font = wb.createFont();;//Create font
		font.setBold(true);//Make font bold
		boldstyle.setFont(font);//set it to bold

		XSSFRow row;
		Set < Integer > keyid = sheetMap.keySet();



		int rowid = 0;
		for (Integer key : keyid)
		{
			row = sheet.createRow(rowid++);
			Object [] objectArr = sheetMap.get(key);
			int cellid = 0;
			for (Object obj : objectArr)
			{
				Cell cell = row.createCell(cellid++);

				if (obj instanceof Number) {
					cell.setCellValue((Double)obj);
					
					/*TODO : come up with a sensible way of handling binary uniformally
					 * 
					 */
					if (rowid > 1) {
						
							cell.setCellStyle(cellStyle);
						
					}


				} else if (obj instanceof String) {
					cell.setCellValue((String)obj);


					// logic that handles bolding of left hand column of coversheet
					if (sheetName.equals("Cover sheet") && (cellid == 1)) {
							cell.setCellStyle(boldstyle);
					}
					
					if (sheetName.equals("Summary sheet")) {
						
						if (rowid == 1) { 
							cell.setCellStyle(boldstyle);
						} 
						
						if (isBinary) {
							sheet.setAutoFilter(CellRangeAddress.valueOf("A1:H1"));
						} else {
							sheet.setAutoFilter(CellRangeAddress.valueOf("A1:I1"));

						}
					}
					
					
					if (sheetName.equals("Test") || sheetName.equals("Training")) {
						
						if (rowid == 1) { 
							cell.setCellStyle(boldstyle);
						} 
						
						sheet.setAutoFilter(CellRangeAddress.valueOf("A1:G1"));

					}
					
					if (!(sheetName.equals("Cover sheet") || sheetName.equals("Summary sheet") || sheetName.equals("Training") || sheetName.equals("Test"))) {
						sheet.setAutoFilter(CellRangeAddress.valueOf("A1:D1"));
						
						if (rowid == 1) { 
							cell.setCellStyle(boldstyle);
						} 

					}

					
					

				}
				
				

			}
		}
		if (!(isBinary)) {
		if (!(sheetName.equals("Cover sheet") || sheetName.equals("Summary sheet") || sheetName.equals("Training") || sheetName.equals("Test"))) {
			eu.GenerateChart(sheet,"Exp","Pred",sheetName,"");
		}
		}


		autoSizeColumns(wb);
		


	}


	private void populateSheet(Map < String, Object[] > spreadsheetMap, String methodName, boolean summary,Workbook wb, boolean isBinary) {


		if (wb.getSheet(methodName) != null) {
			return;
		}
		XSSFSheet sheet = (XSSFSheet) wb.createSheet(methodName);
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setDataFormat(wb.createDataFormat().getFormat("0.00"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		CellStyle cellStyle2 = wb.createCellStyle();
		cellStyle2.setDataFormat(wb.createDataFormat().getFormat("0"));
		cellStyle2.setAlignment(CellStyle.ALIGN_CENTER);

		XSSFRow row;
		Set < String > keyid = spreadsheetMap.keySet();


		CellStyle boldstyle = wb.createCellStyle();//Create style
		Font font = wb.createFont();;//Create font
		font.setBold(true);//Make font bold
		boldstyle.setFont(font);//set it to bold


		int rowid = 0;
		for (String key : keyid)
		{
			row = sheet.createRow(rowid++);
			Object [] objectArr = spreadsheetMap.get(key);
			int cellid = 0;
			for (Object obj : objectArr)
			{
				Cell cell = row.createCell(cellid++);

				if (obj instanceof Number) {
					cell.setCellValue((Double)obj);



					if (!(methodName.equals("Cover sheet")) && (cellid != 1)) {
						cell.setCellStyle(cellStyle);
					}


					if (isBinary==true) {
						// sheet.setAutoFilter(CellRangeAddress.valueOf("A1:J1"));
						// autoSizeColumns(wb);

					} else if (isBinary == false) {
						sheet.setAutoFilter(CellRangeAddress.valueOf("A1:I1"));
					} else if (methodName.equals("Test") || methodName.equals("Training")) {
						sheet.setAutoFilter(CellRangeAddress.valueOf("A1:G1"));


					} 


				} else if (obj instanceof String) {
					cell.setCellValue((String)obj);


					// logic that handles bolding
					if (!methodName.equals("Cover sheet")) {
						if (rowid == 1) {
							cell.setCellStyle(boldstyle);
						}
					} else {
						if (cellid == 1) {
							cell.setCellStyle(boldstyle);
						}
					}




					//
					/*
		            // gives non-ID columns 3 decimal places
		            if ((cellid != 1) && summary == true) {cell.setCellStyle(cellStyle);}
		            else { cell.setCellStyle(cellStyle);}

		            } else {
		            cell.setCellValue((String)obj);

					 */

				}
			}
		}

		if (methodName.equals("Summary")) {
			if (isBinary==true) {
				sheet.setAutoFilter(CellRangeAddress.valueOf("A1:G1"));
				// autoSizeColumns(wb);

			} else if (isBinary == false) {
				sheet.setAutoFilter(CellRangeAddress.valueOf("A1:H1"));
			} 

		} else if (methodName.equals("Test") || methodName.equals("Training")) {
			sheet.setAutoFilter(CellRangeAddress.valueOf("A1:G1"));



		} else if (methodName.equals("Cover sheet")) {
		} else {

			if (isBinary == false) {
				ExcelUtilities eu = new ExcelUtilities();
				eu.GenerateChart(sheet,"Exp","Pred",methodName,"");

				sheet.setAutoFilter(CellRangeAddress.valueOf("A1:D1"));
			} else if (isBinary == true) {

				sheet.setAutoFilter(CellRangeAddress.valueOf("A1:D1"));

			}
		}
		autoSizeColumns(wb);



	}



	public void autoSizeColumns(Workbook workbook) {
		int numberOfSheets = workbook.getNumberOfSheets();
		for (int i = 0; i < numberOfSheets; i++) {
			XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
			if (sheet.getPhysicalNumberOfRows() > 0) {
				XSSFRow row = sheet.getRow(sheet.getFirstRowNum());
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					int columnIndex = cell.getColumnIndex();
					sheet.autoSizeColumn(columnIndex);
					int currentColumnWidth = sheet.getColumnWidth(columnIndex);
					sheet.setColumnWidth(columnIndex, (currentColumnWidth + 200));
				}
			}
		}
	}

	static class modelHashTables {
		String modelName;
		Hashtable<String,Double> expHash;
		Hashtable<String,Double> predictionHash;

		Hashtable<String,Double> getPredictionHash(){
			return predictionHash;
		}

		Hashtable<String,Double> getExpHash(){
			return expHash;
		}

	}

	class ExcelUtilities {

		public void GenerateChart(XSSFSheet sheet,String source1,String source2,String property,String units) {
			XSSFDrawing drawing = sheet.createDrawingPatriarch();
			XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 0, 16, 24);

			XSSFChart chart = drawing.createChart(anchor);

			if (chart instanceof XSSFChart) ((XSSFChart)chart).setTitleText(property+": "+source1+" vs. "+source2);


			XSSFValueAxis bottomAxis = chart.createValueAxis(AxisPosition.BOTTOM);
			XSSFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
			leftAxis.setCrosses(AxisCrosses.MIN);
			bottomAxis.setCrosses(AxisCrosses.MIN);


			CellRangeAddress crXData = new CellRangeAddress(1, sheet.getLastRowNum(), 1, 1);
			CellRangeAddress crYData = new CellRangeAddress(1, sheet.getLastRowNum(), 2, 2);
			CellReference crTitle = new CellReference(0,1);
			//			    Cell cell = sheet.getRow(crTitle.getRow()).getCell(crTitle.getCol());

			ChartDataSource<Number> dsXData = DataSources.fromNumericCellRange(sheet, crXData);
			ChartDataSource<Number> dsYData = DataSources.fromNumericCellRange(sheet, crYData);

			XSSFScatterChartData data = chart.getChartDataFactory().createScatterChartData();

			ScatterChartSeries series1 = data.addSerie(dsXData, dsYData);
			ScatterChartSeries series2 = data.addSerie(dsXData, dsXData);

			series1.setTitle("Exp. Data");
			series2.setTitle("Y=X");
			chart.plot(data, bottomAxis, leftAxis);

			//Set axis titles:
			CTValAx valAx = chart.getCTChart().getPlotArea().getValAxArray(0);
			CTValAx valAy = chart.getCTChart().getPlotArea().getValAxArray(1);
			setAxisTitle(source1+" "+units, valAx);
			setAxisTitle(source2+" "+units, valAy);
			// *******

			//set properties of first scatter chart data series to not smooth the line:
			((XSSFChart)chart).getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
			.addNewSmooth().setVal(false);

			//			    System.out.println(chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0).getSpPr());

			//Set series line to no fill: 
			chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0)
			.addNewSpPr().addNewLn().addNewNoFill();

			chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(1)
			.addNewMarker().addNewSymbol().setVal(STMarkerStyle.NONE);




			//Add linear trend line:
			chart.getCTChart().getPlotArea().getScatterChartArray(0).getSerArray(0).addNewTrendline().addNewTrendlineType().setVal(org.openxmlformats.schemas.drawingml.x2006.chart.STTrendlineType.LINEAR);
			// *******

			XSSFChartLegend legend = chart.getOrCreateLegend();
			legend.setPosition(LegendPosition.BOTTOM);



			//set properties of first scatter chart series to not vary the colors:
			((XSSFChart)chart).getCTChart().getPlotArea().getScatterChartArray(0)
			.addNewVaryColors().setVal(false);



		}


		private void setAxisTitle(String source1, CTValAx valAx) {
			CTTitle ctTitle = valAx.addNewTitle();
			ctTitle.addNewLayout();
			ctTitle.addNewOverlay().setVal(false);
			CTTextBody rich = ctTitle.addNewTx().addNewRich();
			rich.addNewBodyPr();
			rich.addNewLstStyle();
			CTTextParagraph p = rich.addNewP();
			p.addNewPPr().addNewDefRPr();
			p.addNewR().setT(source1);
			p.addNewEndParaRPr();
		}

	}



}

