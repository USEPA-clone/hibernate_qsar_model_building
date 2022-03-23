package gov.epa.web_services.descriptors;

import java.util.List;
import java.util.Map;

import gov.epa.web_services.WebService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

public class SciDataExpertsDescriptorWebService extends WebService {
	
	public static class SciDataExpertsDescriptorResponse {
		public List<SciDataExpertsChemical> chemicals;
		public List<String> headers;
		public SciDataExpertsDescriptorInfo info;
		public SciDataExpertsDescriptorOptions options;
	}
	
	public static class SciDataExpertsChemical {
		public Integer ordinal;
		public List<Double> descriptors;
		public String id;
		public String inchi;
		public String inchikey;
		public String smiles;
	}
	
	public static class SciDataExpertsDescriptorInfo {
		public String name;
		public String version;
	}
	
	public static class SciDataExpertsDescriptorOptions {
		public Boolean headers;
		public Boolean compute2D;
		public Boolean compute3D;
		public Boolean computeFingerprints;
		public Boolean removeSalt;
		public Boolean standardizeNitro;
		public Boolean standardizeTautomers;
		public Integer bits;
		public Integer radius;
		public String type;
	}
	
	public static class SciDataExpertsDescriptorRequest {
		public List<String> chemicals;
		public String type;
		public Map<String, Object> options;
		
		public SciDataExpertsDescriptorRequest(List<String> chemicals, String type, Map<String, Object> options) {
			this.chemicals = chemicals;
			this.type = type;
			this.options = options;
		}
	}

	public SciDataExpertsDescriptorWebService(String url) {
		super(url);
	}
	
	public HttpResponse<SciDataExpertsDescriptorResponse> calculateDescriptors(String smiles, String descriptorName) {
		HttpResponse<SciDataExpertsDescriptorResponse> response = Unirest.get(address + "/api/descriptors")
				.queryString("type", descriptorName)
				.queryString("smiles", smiles)
				.asObject(SciDataExpertsDescriptorResponse.class);
		
		return response;
	}
	
	public HttpResponse<SciDataExpertsDescriptorResponse> calculateDescriptorsWithOptions(String smiles, String descriptorName, 
			Map<String, Object> options) {
		HttpResponse<SciDataExpertsDescriptorResponse> response = Unirest.get(address + "/api/descriptors")
				.queryString("type", descriptorName)
				.queryString("smiles", smiles)
				.queryString(options)
				.asObject(SciDataExpertsDescriptorResponse.class);
		
		return response;
	}
	
	public HttpResponse<SciDataExpertsDescriptorResponse> calculateDescriptors(List<String> smiles, String descriptorName) {
		SciDataExpertsDescriptorRequest request = new SciDataExpertsDescriptorRequest(smiles, descriptorName, null);
		HttpResponse<SciDataExpertsDescriptorResponse> response = Unirest.post(address + "/api/descriptors")
				.header("Content-Type", "application/json")
				.header("Accept", "*/*")
				.header("Accept-Encoding", "gzip, deflate, br")
				.body(request)
				.asObject(SciDataExpertsDescriptorResponse.class);
		
		return response;
	}
	
	public HttpResponse<SciDataExpertsDescriptorResponse> calculateDescriptorsWithOptions(List<String> smiles, String descriptorName, 
			Map<String, Object> options) {
		SciDataExpertsDescriptorRequest request = new SciDataExpertsDescriptorRequest(smiles, descriptorName, options);
		HttpResponse<SciDataExpertsDescriptorResponse> response = Unirest.post(address + "/api/descriptors")
				.header("Content-Type", "application/json")
				.header("Accept", "*/*")
				.header("Accept-Encoding", "gzip, deflate, br")
				.body(request)
				.asObject(SciDataExpertsDescriptorResponse.class);
		
		return response;
	}

}
