package gov.epa.web_services.embedding_service;

import com.google.gson.annotations.Expose;

/**
 * @author CRAMSLAN
 * TODO: add json object to devqsarconstants with these presets
 * anytime go to build model, query based on GA method, descriptor set, dataset, and (json) descriptionstandard.toString()
 * 
 *  
 *  TODO: create an easy way to make default embeddings
 *  
 *  unique embedding name: datasetname_uniquetime in milliseconds
 *  
 *  make listsplitter less annoying to use
 */
public class CalculationInfo {
	public Boolean save_to_database;
	public String tsv;
	public String qsarMethod;
	
	public Boolean remove_log_p;
	
	//Default GA params:
	@Expose
	public Integer num_generations=100;
	@Expose
	public Integer num_optimizers=10;
	@Expose
	public Integer num_jobs=4;
	@Expose
	public Integer n_threads=20;
	@Expose
	public Integer max_length=24;
	@Expose
	public Double descriptor_coefficient=0.002;
	
	public static CalculationInfo createDefault() {
		return new CalculationInfo();
	}
	
}
