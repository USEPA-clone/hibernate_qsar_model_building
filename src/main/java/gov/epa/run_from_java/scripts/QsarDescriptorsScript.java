package gov.epa.run_from_java.scripts;

import gov.epa.databases.dev_qsar.DevQsarConstants;
import gov.epa.endpoints.datasets.descriptor_values.DescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.SciDataExpertsDescriptorValuesCalculator;
import gov.epa.endpoints.datasets.descriptor_values.TestDescriptorValuesCalculator;
import kong.unirest.Unirest;

public class QsarDescriptorsScript {
	
	private static final String ML_SCI_DATA_EXPERTS_URL = "https://ml.sciencedataexperts.com";
	private static final String HCD_SCI_DATA_EXPERTS_URL = "https://hazard-dev.sciencedataexperts.com";
	
	public static String calculateDescriptorsForDataset(String datasetName, String descriptorSetName, String descriptorWebServiceUrl, 
			boolean writeToDatabase, String lanId) {
		DescriptorValuesCalculator calc = null;
		if (descriptorSetName.equals(DevQsarConstants.DESCRIPTOR_SET_TEST)) {
			calc = new TestDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		} else {
			calc = new SciDataExpertsDescriptorValuesCalculator(descriptorWebServiceUrl, lanId);
		}
		
		return calc.calculateDescriptors(datasetName, descriptorSetName, writeToDatabase);
	}
	
	public static void deleteDescriptorSetValuesForDataset(String datasetName, String descriptorSetName, String lanId) {
		DescriptorValuesCalculator calc = new DescriptorValuesCalculator(lanId);
		calc.deleteDescriptorSetValuesForDataset(datasetName, descriptorSetName);
	}
	
	public static void main(String[] args) {
//		String testDescriptorWebServiceUrl = DevQsarConstants.SERVER_819 + ":" + DevQsarConstants.PORT_TEST_DESCRIPTORS;
		
		Unirest.config().connectTimeout(0).socketTimeout(0);
		
		String datasetName = "Standard Henry's law constant from exp_prop";
		String[] sciDataExpertsDescriptorSetNames = {
				"ToxPrints-default", 
				"Mordred-default", 
				"WebTEST-default", 
				"PaDEL-default", 
				"RDKit-default"
				};
		
		for (String descriptorSetName:sciDataExpertsDescriptorSetNames) {
			String tsv = calculateDescriptorsForDataset(datasetName, descriptorSetName, ML_SCI_DATA_EXPERTS_URL, true, "gsincl01");
			System.out.println(descriptorSetName + "\t" + tsv.split("\r\n").length);
			System.out.println(tsv.substring(0, tsv.indexOf("\r")));
		}
	}

}
