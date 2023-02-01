package gov.epa.run_from_java.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.databases.dev_qsar.qsar_models.entity.DescriptorEmbedding;
import gov.epa.databases.dev_qsar.qsar_models.service.DescriptorEmbeddingServiceImpl;
import gov.epa.endpoints.models.ModelData;
import gov.epa.endpoints.reports.predictions.PredictionReport;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;
import gov.epa.web_services.ModelWebService;
import gov.epa.web_services.embedding_service.CalculationInfo;

public class ApplicabilityDomainScript {
	
//	static String lanId="cramslan";
	static String lanId = "tmarti02";
	
	static int portModelBuilding=DevQsarConstants.PORT_PYTHON_MODEL_BUILDING;
	static String serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
//	static String serverModelBuilding=DevQsarConstants.SERVER_819;

	static String descriptorSetName = DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
	
	static String qsarMethodGA = DevQsarConstants.KNN;
	
	
	static String strSampleResponse="{\"idTest\":\"OC(=O)C(F)(F)C(F)(F)F\",\"idNeighbor1\":\"OCC(F)(F)C(F)(F)F\",\"idNeighbor2\":\"OCC(F)(F)C(F)F\",\"idNeighbor3\":\"FC(F)(Cl)C(F)(F)C(F)Cl\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"OC(=O)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F\",\"idNeighbor1\":\"OCCC(F)(F)C(F)(F)C(F)(F)C(F)(F)F\",\"idNeighbor2\":\"OC(=O)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)C(F)(F)F\",\"idNeighbor3\":\"OC(=O)C1C=CC=CC=1\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FC(F)(Cl)C(F)(F)Cl\",\"idNeighbor1\":\"FC(F)(F)C(F)(Cl)Cl\",\"idNeighbor2\":\"FC(F)(Br)C(F)(F)Br\",\"idNeighbor3\":\"O=C(F)C(F)(F)F\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FCC(F)(F)F\",\"idNeighbor1\":\"FC(F)(F)C(F)(F)F\",\"idNeighbor2\":\"O=C(F)C(F)(F)F\",\"idNeighbor3\":\"FC(Cl)C(F)(F)F\",\"AD\":false}\r\n"
	+ "{\"idTest\":\"FC(F)(F)C(F)(F)C(Cl)Cl\",\"idNeighbor1\":\"FC(F)(Cl)C(F)(F)C(F)Cl\",\"idNeighbor2\":\"FC(F)OC(F)(F)C(F)Cl\",\"idNeighbor3\":\"FC(F)(C(F)(F)F)C(F)(F)F\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FC(F)C(F)(F)F\",\"idNeighbor1\":\"FC(F)(F)C(F)(F)F\",\"idNeighbor2\":\"O=C(F)C(F)(F)F\",\"idNeighbor3\":\"FC(Cl)C(F)(F)F\",\"AD\":false}\r\n"
	+ "{\"idTest\":\"CC(C)(CS(C)(=O)=O)NC(=O)C1=C(I)C=CC=C1C(=O)NC1=CC=C(C=C1C)C(F)(C(F)(F)F)C(F)(F)F\",\"idNeighbor1\":\"CC1(C)C(C1C=CC(=O)OC(C(F)(F)F)C(F)(F)F)C(=O)OC(C#N)C1=CC(=CC=C1)OC1C=CC=CC=1\",\"idNeighbor2\":\"O=C(NC(=O)NC1C=C(Cl)C(OC(F)(F)C(F)F)=C(Cl)C=1)C1C(F)=CC=CC=1F\",\"idNeighbor3\":\"O=C(NC1C=C(Cl)C(=CC=1Cl)OC(F)(F)C(F)C(F)(F)F)NC(=O)C1C(F)=CC=CC=1F\",\"AD\":true}\r\n"
	+ "{\"idTest\":\"FC1(F)C(F)(F)C(F)(F)C1(F)F\",\"idNeighbor1\":\"FC(F)OC(F)(F)C(F)Cl\",\"idNeighbor2\":\"OC(C(F)(F)F)C(F)(F)F\",\"idNeighbor3\":\"FC(F)(Cl)C(F)(F)C(F)Cl\",\"AD\":true}";

	
	class ApplicabilityDomainPrediction {
		String id;
		List<String>idNeighbors;
		Boolean AD;
	}
	
	DescriptorEmbeddingServiceImpl descriptorEmbeddingService = new DescriptorEmbeddingServiceImpl();

	
	
	public void runCaseStudyExpProp_All_Endpoints() {
		
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Cosine;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_Embedding_Euclidean;
//		String applicability_domain=DevQsarConstants.Applicability_Domain_OPERA_local_index;
		
		//Need to use following for 2.0 models:
		String applicability_domain=DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine; 
		
		boolean storeNeighbors=false;
//		serverModelBuilding=DevQsarConstants.SERVER_819;
		serverModelBuilding=DevQsarConstants.SERVER_LOCAL;
		portModelBuilding=5004;
		
		ModelWebService mws=new ModelWebService(serverModelBuilding, portModelBuilding);

		
		String listName="PFASSTRUCTV4";		
		String folder="data/dev_qsar/dataset_files/";
		String filePath=folder+listName+"_qsar_ready_smiles.txt";
		ArrayList<String>smilesArray=SplittingGeneratorPFAS.getPFASSmiles(filePath);

		List<String>datasetNames=new ArrayList<>();

		datasetNames.add("HLC from exp_prop and chemprop");
//		datasetNames.add("WS from exp_prop and chemprop");
//		datasetNames.add("VP from exp_prop and chemprop");
//		datasetNames.add("LogP from exp_prop and chemprop");
//		datasetNames.add("MP from exp_prop and chemprop");
//		datasetNames.add("BP from exp_prop and chemprop");
		
		String splitting =DevQsarConstants.SPLITTING_RND_REPRESENTATIVE;
//		String splitting ="T=PFAS only, P=PFAS";
//		String splitting = "T=all but PFAS, P=PFAS";

		String descriptorSetName=DevQsarConstants.DESCRIPTOR_SET_WEBTEST;
		
		String modelSetName="WebTEST2.0";
		
		if (modelSetName.contains("2.0") && !applicability_domain.equals(DevQsarConstants.Applicability_Domain_TEST_All_Descriptors_Cosine)) {
			System.out.println("Invalid AD!");
			return;			
		}
				
		for (String datasetName:datasetNames) {
						
			boolean remove_log_p = false;
			if(datasetName.contains("LogP")) remove_log_p=true;
			
			CalculationInfo ci = new CalculationInfo();
			ci.num_generations = 100;			
			if (datasetName.contains("BP") || splitting.equals("T=all but PFAS, P=PFAS")) ci.num_generations=10;//takes too long to do 100			

			ci.remove_log_p = remove_log_p;
			ci.qsarMethodGA = qsarMethodGA;
			ci.datasetName=datasetName;
			ci.descriptorSetName=descriptorSetName;
			ci.splittingName=splitting;
						
			DescriptorEmbedding de=null;
			
			if (modelSetName.contains("2.1")) {
				de=getEmbedding(ci);
				if (de==null) {
					continue;
				}
			} else {
				de=new DescriptorEmbedding();
				de.setEmbeddingTsv("N/A");//dummy value not used by All descriptors AD but need it for API call to work
			}			

			ModelData data = ModelData.initModelData(ci,false);

			//Run AD calculations using webservice:
			String strResponse=mws.callPredictionApplicabilityDomain(data.trainingSetInstances,data.predictionSetInstances,
					remove_log_p,de.getEmbeddingTsv(),applicability_domain).getBody();

//			System.out.println(strResponse);
//			String strResponse=strSampleResponse;

			List<ApplicabilityDomainPrediction>adPredictions=convertResponse(strResponse,storeNeighbors);

			System.out.println("AD="+applicability_domain);

//			for (ApplicabilityDomainPrediction pred:adPredictions) {
//				if (!pred.AD)
//					System.out.println(pred.id+"\t"+pred.AD);
//			}
			
//			System.out.println("Results="+Utilities.gson.toJson(adPredictions)+"\n");
			
			PredictionReport predictionReport=SampleReportWriter.getReport(modelSetName, datasetName, splitting);
			
			System.out.println(Utilities.gson.toJson(predictionReport.predictionReportModelMetadata.get(0)));
			if (splitting.equals(DevQsarConstants.SPLITTING_RND_REPRESENTATIVE)) {
				PredictionStatisticsScript.getStatsInsideAD(predictionReport, adPredictions,null);
			} else {
				PredictionStatisticsScript.getStatsInsideAD(predictionReport, adPredictions,smilesArray);	
			}
			System.out.println(Utilities.gson.toJson(predictionReport.predictionReportModelMetadata.get(0)));
		}
	}
	
	DescriptorEmbedding getEmbedding(CalculationInfo ci) {
		
		DescriptorEmbedding descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);
		
		if (descriptorEmbedding==null) {//look for one of the ones made using offline python run:			
			ci.num_jobs=2;//just takes slighter longer
			ci.n_threads=16;//doesnt impact knn
			descriptorEmbedding = descriptorEmbeddingService.findByGASettings(ci);				
		}			

		if (descriptorEmbedding == null) {
//			descriptorEmbedding = ews2.generateEmbedding(serverModelBuilding, portModelBuilding, lanId,ci);
//			System.out.println("New embedding from web service:"+descriptorEmbedding.getEmbeddingTsv());
			System.out.println("Dont have existing embedding:"+ci.toString());
			return null;
			
		} else {
			System.out.println("Have embedding from db:"+descriptorEmbedding.getEmbeddingTsv());
			return descriptorEmbedding;
		}
	}
	
	public List<ApplicabilityDomainPrediction>convertResponse(String response,boolean storeNeighbors) {

		List<ApplicabilityDomainPrediction>preds=new ArrayList<>();
		
		String [] lines=response.split("\n");
		
		int counter=1;
		for (String line:lines) {
			
//			System.out.println(counter+"\t"+line);
		
			ApplicabilityDomainPrediction pred=new ApplicabilityDomainPrediction();
			
			JsonObject jo=Utilities.gson.fromJson(line, JsonObject.class);
			
			pred.id=jo.get("idTest").getAsString();
			pred.AD=jo.get("AD").getAsBoolean();
			
			if (storeNeighbors) {
				pred.idNeighbors=new ArrayList<>();

				List<String> keys = jo.entrySet()
						.stream()
						.map(i -> i.getKey())
						.collect(Collectors.toCollection(ArrayList::new));

				for (String key:keys) {
					if(key.contains("Neighbor")) {
						pred.idNeighbors.add(jo.get(key).getAsString());
					}
				}

			}
			
			preds.add(pred);
						
//			System.out.println(counter+"\t"+Utilities.gson.toJson(pred)+"\n");
			counter++;
		}
		
		return preds;
	}
	
	public static void main(String[] args) {
		ApplicabilityDomainScript ads=new ApplicabilityDomainScript();
		ads.runCaseStudyExpProp_All_Endpoints();		
	}

}
