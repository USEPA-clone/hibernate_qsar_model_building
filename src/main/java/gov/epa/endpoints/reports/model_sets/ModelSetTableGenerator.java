package gov.epa.endpoints.reports.model_sets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.epa.databases.dev_qsar.qsar_datasets.entity.Dataset;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetService;
import gov.epa.databases.dev_qsar.qsar_datasets.service.DatasetServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.entity.Method;
import gov.epa.databases.dev_qsar.qsar_models.entity.Model;
import gov.epa.databases.dev_qsar.qsar_models.entity.ModelSet;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelServiceImpl;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetService;
import gov.epa.databases.dev_qsar.qsar_models.service.ModelSetServiceImpl;
import gov.epa.endpoints.reports.ModelMetadata;
import gov.epa.endpoints.reports.model_sets.ModelSetTable.ModelSetTableRow;

public class ModelSetTableGenerator {
	private ModelSetService modelSetService;
	private ModelService modelService;
	private DatasetService datasetService;

	private ModelSetTable modelSetTable;
	
	public ModelSetTableGenerator() {
		this.modelSetService = new ModelSetServiceImpl();
		this.modelService = new ModelServiceImpl();
		this.datasetService = new DatasetServiceImpl();
	}
	
	private ModelSetTable generate(ModelSet modelSet) {
		modelSetTable = new ModelSetTable(modelSet.getName(), modelSet.getDescription());
		
		List<Model> models = modelService.findByModelSetId(modelSet.getId());
		Map<String, ModelSetTableRow> rowMap = new HashMap<String, ModelSetTableRow>();
		for (Model model:models) {
			String datasetName = model.getDatasetName();
			String descriptorSetName = model.getDescriptorSetName();
			String splittingName = model.getSplittingName();
			String mapId = String.join("_", datasetName, descriptorSetName, splittingName);
			ModelSetTableRow row = rowMap.get(mapId);
			
			if (row==null) {
				Dataset dataset = datasetService.findByName(datasetName);
				row = new ModelSetTableRow(dataset, descriptorSetName, splittingName);
			}
			
			Method method = model.getMethod();
			row.modelMetadata.add(new ModelMetadata(model.getId(), method.getName(), method.getDescription()));
			rowMap.put(mapId, row);
		}
		
		for (String mapId:rowMap.keySet()) {
			modelSetTable.modelSetTableRows.add(rowMap.get(mapId));
		}
		
		return modelSetTable;
	}
	
	public ModelSetTable generate(String modelSetName) {
		ModelSet modelSet = modelSetService.findByName(modelSetName);
		return generate(modelSet);
	}
	
	public ModelSetTable generate(Long modelSetId) {
		ModelSet modelSet = modelSetService.findById(modelSetId);
		return generate(modelSet);
	}
}
