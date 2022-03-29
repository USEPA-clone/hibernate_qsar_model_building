package gov.epa.endpoints.datasets.descriptor_values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.DataPoint;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorSet;
import gov.epa.databases.dev_qsar.qsar_descriptors.entity.DescriptorValues;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsChemical;
import gov.epa.web_services.descriptors.SciDataExpertsDescriptorWebService.SciDataExpertsDescriptorResponse;

public class SciDataExpertsDescriptorValuesCalculator extends DescriptorValuesCalculator {
	
	private SciDataExpertsDescriptorWebService descriptorWebService;
	private Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public SciDataExpertsDescriptorValuesCalculator(String sciDataExpertsUrl, String lanId) {
		super(lanId);
		this.descriptorWebService = new SciDataExpertsDescriptorWebService(sciDataExpertsUrl);
	}
	
	@Override
	public String calculateDescriptors(String datasetName, String descriptorSetName,boolean writeToDatabase) {
		List<DataPoint> dataPoints = dataPointService.findByDatasetName(datasetName);
		List<String> canonQsarSmilesToCalculate = new ArrayList<String>();
		for (DataPoint dp:dataPoints) {
			String canonQsarSmiles = dp.getCanonQsarSmiles();
			if (dp.getCanonQsarSmiles()==null) {
				// Don't calculate descriptors on compounds without standardization
				continue;
			} 
			
			DescriptorValues descriptorValues = descriptorValuesService
					.findByCanonQsarSmilesAndDescriptorSetName(canonQsarSmiles, descriptorSetName);
			if (descriptorValues==null) {
				canonQsarSmilesToCalculate.add(canonQsarSmiles);
			} else {
				//TODO- will need to store the descriptors already in the database in the tsv that gets returned.
			}
		}
		
		return calculateDescriptors(canonQsarSmilesToCalculate, descriptorSetName,writeToDatabase);
	}
	
	public void calculateDescriptors(List<String> canonQsarSmilesToCalculate, String descriptorSetName) {
		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}
		
		String descriptorService = descriptorSet.getDescriptorService();
		String descriptorServiceOptionsStr = descriptorSet.getDescriptorServiceOptions();
		Map<String, Object> descriptorServiceOptions = new HashMap<String, Object>();
		if (descriptorSet.getDescriptorServiceOptions()!=null) {
			JsonObject jo = gson.fromJson(descriptorServiceOptionsStr, JsonObject.class);
			for (String key:jo.keySet()) {
				descriptorServiceOptions.put(key, jo.get(key));
			}
		}
		
		// Calculate descriptors
		SciDataExpertsDescriptorResponse response = null;
		if (descriptorServiceOptions.isEmpty()) {
			response = descriptorWebService.calculateDescriptors(canonQsarSmilesToCalculate, descriptorService).getBody();
		} else {
			if (descriptorSet.getHeadersTsv()==null && descriptorServiceOptions.containsKey("headers")) {
				Map<String, Object> descriptorServiceOptionsWithHeaders = new HashMap<String, Object>(descriptorServiceOptions);
				descriptorServiceOptionsWithHeaders.put("headers", true);
				
				response = descriptorWebService
						.calculateDescriptorsWithOptions(canonQsarSmilesToCalculate, descriptorService, 
								descriptorServiceOptionsWithHeaders).getBody();
				
				if (response.headers!=null) {
					String headersTsv = String.join("\t", response.headers);
//					System.out.println(headersTsv);
					descriptorSet.setHeadersTsv(headersTsv);
					descriptorSet = descriptorSetService.update(descriptorSet);
				}
			} else {
				response = descriptorWebService
						.calculateDescriptorsWithOptions(canonQsarSmilesToCalculate, descriptorService, descriptorServiceOptions).getBody();
			}
		}
		
//		System.out.println(gson.toJson(response));
		
		// Store descriptors
		// Store null or failed descriptors so we don't keep trying to calculate them every time
		if (response!=null) {
			List<SciDataExpertsChemical> chemicals = response.chemicals;
			if (chemicals!=null) {
				for (SciDataExpertsChemical chemical:chemicals) {
					String valuesTsv = null;
					if (chemical.descriptors!=null) {
						valuesTsv = String.join("\t", chemical.descriptors.stream()
								.map(d -> String.valueOf(d))
								.collect(Collectors.toList()));
					}
					
					// If descriptor calculation failed, set null so we can check easily when we try to use them later
					if (valuesTsv.contains("Error")) {
						valuesTsv = null;
					}
					
					DescriptorValues descriptorValues = new DescriptorValues(chemical.smiles, descriptorSet, valuesTsv, lanId);
					try {
						descriptorValuesService.create(descriptorValues);
					} catch (ConstraintViolationException e) {
						System.out.println(e.getMessage());
					}
				}
			}
		}
	}
	
	/**
	 * Creating separate method to add boolean saveToDatabase and return a tsv String to not break code
	 * 
	 * @param canonQsarSmilesToCalculate
	 * @param descriptorSetName
	 * @param saveToDatabase
	 * @return
	 */
	public String calculateDescriptors(List<String> canonQsarSmilesToCalculate, String descriptorSetName,boolean saveToDatabase) {
		StringBuilder sbOverall = new StringBuilder();

		DescriptorSet descriptorSet = descriptorSetService.findByName(descriptorSetName);
		if (descriptorSet==null) {
			System.out.println("No such descriptor set: " + descriptorSetName);
		}
		
		String descriptorService = descriptorSet.getDescriptorService();
		String descriptorServiceOptionsStr = descriptorSet.getDescriptorServiceOptions();
		Map<String, Object> descriptorServiceOptions = new HashMap<String, Object>();
		if (descriptorSet.getDescriptorServiceOptions()!=null) {
			JsonObject jo = gson.fromJson(descriptorServiceOptionsStr, JsonObject.class);
			for (String key:jo.keySet()) {
				descriptorServiceOptions.put(key, jo.get(key));
			}
		}
		
		System.out.println("here");
		
		// Calculate descriptors
		SciDataExpertsDescriptorResponse response = null;
		if (descriptorServiceOptions.isEmpty()) {
			response = descriptorWebService.calculateDescriptors(canonQsarSmilesToCalculate, descriptorService).getBody();
//			System.out.println(response);
//			System.out.println("options empty");
			
		} else {
			if (descriptorSet.getHeadersTsv()==null && descriptorServiceOptions.containsKey("headers")) {
			
//				System.out.println("options not empty");
				
				Map<String, Object> descriptorServiceOptionsWithHeaders = new HashMap<String, Object>(descriptorServiceOptions);
				descriptorServiceOptionsWithHeaders.put("headers", true);
				
				response = descriptorWebService
						.calculateDescriptorsWithOptions(canonQsarSmilesToCalculate, descriptorService, 
								descriptorServiceOptionsWithHeaders).getBody();
				
				if (response.headers!=null) {
					String headersTsv = String.join("\t", response.headers);
					System.out.println(headersTsv);
					descriptorSet.setHeadersTsv(headersTsv);
					descriptorSet = descriptorSetService.update(descriptorSet);
					sbOverall.append(headersTsv);
				}
			} else {
				response = descriptorWebService
						.calculateDescriptorsWithOptions(canonQsarSmilesToCalculate, descriptorService, descriptorServiceOptions).getBody();
				System.out.println("here2");
			}
		}
		
		
		System.out.println(gson.toJson(response));
		
		// Store descriptors
		// Store null or failed descriptors so we don't keep trying to calculate them every time
		if (response!=null) {
			List<SciDataExpertsChemical> chemicals = response.chemicals;
			if (chemicals!=null) {
				for (SciDataExpertsChemical chemical:chemicals) {
					String valuesTsv = null;
					if (chemical.descriptors!=null) {
						valuesTsv = String.join("\t", chemical.descriptors.stream()
								.map(d -> String.valueOf(d))
								.collect(Collectors.toList()));
					}
					
					// If descriptor calculation failed, set null so we can check easily when we try to use them later
					if (valuesTsv.contains("Error")) {
						valuesTsv = null;
					}
					
					DescriptorValues descriptorValues = new DescriptorValues(chemical.smiles, descriptorSet, valuesTsv, lanId);
					sbOverall.append(descriptorValues.getValuesTsv());
					
//					System.out.println(descriptorValues.getValuesTsv());
					
					if(saveToDatabase) {
						try {
							descriptorValuesService.create(descriptorValues);
						} catch (ConstraintViolationException e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
		}
	
		return sbOverall.toString();
	}
	
	
}
