package gov.epa.run_from_java.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSetReport;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetReportServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.reports.WebTEST.GenerateWebTestReport;
import gov.epa.endpoints.reports.model_sets.ModelSetTable;
import gov.epa.endpoints.reports.model_sets.ModelSetTable.ModelSetTableRow;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.endpoints.reports.predictions.ExcelReports.ExcelPredictionReportGenerator;

public class SampleReportWriter {


	ModelSetReportServiceImpl msrs = new ModelSetReportServiceImpl();
	QsarModelsScript qms = new QsarModelsScript("tmarti02");		
	ModelSetServiceImpl mss = new ModelSetServiceImpl();
	ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();

	public PredictionReport createPredictionReport(String modelSetName, String datasetName, 
			String splittingName,boolean overWriteJsonReport) {
		
		PredictionReport predictionReport = null;

		String filepathReport = "data/reports/" + modelSetName + "_" + datasetName + "_PredictionReport.json";
		File reportFile = new File(filepathReport);

		if (reportFile.exists() && !overWriteJsonReport) {
			System.out.println("JSON report exists:" + filepathReport);
			predictionReport = GenerateWebTestReport.loadDataSetFromJson(filepathReport);
		} else {
			System.out.println("JSON report doesnt exist, creating it at:\n" + filepathReport + "\n");
			predictionReport = ReportGenerationScript.reportAllPredictions(datasetName, splittingName, modelSetName,
					true);
		}

		return predictionReport;
		
	}
	
	
	public PredictionReport getReport(String modelSetName, String datasetName, 
			String splittingName) {
		
		String filepathReport = "data/reports/" + modelSetName + "_" + datasetName + "_PredictionReport.json";
		File reportFile = new File(filepathReport);

		if (reportFile.exists()) {
			return GenerateWebTestReport.loadDataSetFromJson(filepathReport);
		} else {
			System.out.println("JSON report doesnt exist at "+filepathReport); 
			return null;
		}
	}
	
	public PredictionReport createPredictionReport(long modelSetID, String datasetName, 
			String splittingName,boolean overWriteJsonReport) {
		ModelSetServiceImpl m = new ModelSetServiceImpl();
		String modelSetName = m.findById(modelSetID).getName();		
		return createPredictionReport(modelSetName, datasetName, splittingName,overWriteJsonReport);
	}
	
	public void generateSamplePredictionReports(long modelSetID, boolean upload, boolean deleteExistingReportInDatabase) {

		ModelSetTable table = SampleModelQmrfWriter.getModelsInModelSet(modelSetID);
		
		for (ModelSetTableRow msRow : table.modelSetTableRows) {
			
			generateSamplePredictionReport(modelSetID, msRow.datasetName, msRow.splittingName, upload, deleteExistingReportInDatabase, false);
		}
	}
	
	public String createExcelReport (long modelSetId,String datasetName, String splittingName,PredictionReport predictionReport,boolean overwrite) {

		String modelSetName = mss.findById(modelSetId).getName();
		
		String outputFolder = "data/reports/prediction reports upload";
		
		File f = new File(outputFolder);
		if (!f.exists())
			f.mkdirs();

		String filepath = outputFolder + File.separator + String.join("_", modelSetName, datasetName, splittingName)
		+ ".xlsx";
		
		File excelFile=new File(filepath);
		
		if (overwrite || !excelFile.exists()) {
			ExcelPredictionReportGenerator eprg = new ExcelPredictionReportGenerator();
			eprg.generate(predictionReport, filepath);
			System.out.println("Excel report created at"+filepath);
		} else {
			System.out.println("Excel report already exists at"+filepath);
		}
		
		return filepath;
	}

		


	public void generateSamplePredictionReport(long modelSetID, String datasetName, String splittingName,
			boolean upload,boolean deleteExistingReportInDatabase,boolean overWriteReportFiles) {
		
		PredictionReport predictionReport=createPredictionReport(modelSetID, datasetName, splittingName,overWriteReportFiles);
				
		String excelFilePath=createExcelReport(modelSetID, datasetName, splittingName, predictionReport,overWriteReportFiles);
						
		ModelSetReport msr = msrs.findByModelSetIdAndModelData(modelSetID, datasetName, splittingName);

		if (msr != null) {
			if (deleteExistingReportInDatabase) {
				msrs.delete(msr);
			
			} else {
				if (upload) {
					System.out.println(datasetName + " exists skipping!");
					return;// skip it we already did it						
				}
			}
		}

		if(!upload)return;
		
		try {
			qms.uploadModelSetReport(modelSetID, datasetName, splittingName, excelFilePath);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) {

		SampleReportWriter g = new SampleReportWriter();

		boolean upload=false;
		boolean deleteExistingReportInDatabase=false;
		boolean overWriteReportFiles=false;

//		g.generateSamplePredictionReports(4L,false,false);
//		g.generateSamplePredictionReports(1L,true,true);
//		g.generateSamplePredictionReports(2L,true,true);
//		g.generateSamplePredictionReports(14L,false,false);
		
		// **************************************************************
		String splitting="T=PFAS only, P=PFAS";
		long modelSetId=15L;
		
		List<String>datasetNames=new ArrayList<>();
		datasetNames.add("HLC from exp_prop and chemprop");
		datasetNames.add("WS from exp_prop and chemprop");
		datasetNames.add("VP from exp_prop and chemprop");
		datasetNames.add("LogP from exp_prop and chemprop");
		datasetNames.add("MP from exp_prop and chemprop");
		datasetNames.add("BP from exp_prop and chemprop");
		
		for (String datasetName:datasetNames) 
			g.generateSamplePredictionReport(modelSetId, datasetName, splitting,upload,deleteExistingReportInDatabase,overWriteReportFiles);

		
		// **************************************************************
//		 QsarModelsScript q=new QsarModelsScript("tmarti02");
//		 String datasetName="LC50DM TEST";
//		 String splittingName="TEST";
//		 q.downloadModelSetReport(1L, datasetName, splittingName,
//		 "data/reports/prediction reports download");

		// **************************************************************
//		 QsarModelsScript q=new QsarModelsScript("tmarti02");
//		// String datasetName="Standard Henry's law constant from exp_prop";
//		 String datasetName="Standard Water solubility from exp_prop";
//		 String splittingName="RND_REPRESENTATIVE";
//		 q.downloadModelSetReport(2L, datasetName, splittingName,
//		 "data/reports/prediction reports download");

	}

}
