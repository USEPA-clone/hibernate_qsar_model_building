package gov.epa.databases.dev_qsar;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DevQsarConstants {
	
	// Numerical constants for processing and modeling
	// Fraction agreement required to map a DSSTox conflict
	public static final Double CONFLICT_FRAC_AGREE = 1.0;
	// Fraction agreement required to merge binary data points
	public static final Double BINARY_FRAC_AGREE = 0.8;
	// Cutoff for binary classification
	public static final Double BINARY_CUTOFF = 0.5;
	
	// Multiple of dataset stdev required to exclude a property value based on its stdev
	public static final Double STDEV_WIDTH_TOLERANCE = 3.0;
	// Range tolerance values
	public static final Double LOG_RANGE_TOLERANCE = 1.0;
	public static final Double TEMP_RANGE_TOLERANCE = 10.0;
	public static final Double DENSITY_RANGE_TOLERANCE = 0.1;
	public static final Double ZERO_TOLERANCE = Math.pow(10.0, -6.0);
	
	// Max realistic water solubility (in g/L)
	public static final Double MAX_WATER_SOLUBILITY = 990.0;
	
	// Data folder path
	public static final String OUTPUT_FOLDER_PATH = "data" + File.separator + "dev_qsar" + File.separator + "output";
	
	// Servers for other web services
	public static final String SERVER_819 = "http://v2626umcth819.rtord.epa.gov";
	public static final String SERVER_LOCAL = "http://localhost";
	
	// Ports for other web services
	public static final int PORT_DEFAULT = 5000;
	public static final int PORT_STANDARDIZER_OPERA = 5001;
	public static final int PORT_TEST_DESCRIPTORS = 5002;
	public static final int PORT_JAVA_MODEL_BUILDING = 5003;
	public static final int PORT_PYTHON_MODEL_BUILDING = 5004;
	public static final int PORT_REPRESENTATIVE_SPLIT = 5005;
	public static final int PORT_OUTLIER_DETECTION = 5006;
	public static final int PORT_STANDARDIZER_JAVA = 5010;
	
	// DSSTox mapping strategies
	public static final String MAPPING_BY_CASRN = "CASRN";
	public static final String MAPPING_BY_DTXCID = "DTXCID";
	public static final String MAPPING_BY_DTXSID = "DTXSID";
	public static final String MAPPING_BY_LIST = "LIST";
	
	// Standardizer types
	public static final String QSAR_READY = "QSAR_READY";
	public static final String MS_READY = "MS_READY";
	
	// Standardizers
	public static final String STANDARDIZER_NONE = "NONE"; // Default QSAR-ready SMILES from DSSTox
	public static final String STANDARDIZER_OPERA = "OPERA";
	public static final String STANDARDIZER_SCI_DATA_EXPERTS = "SCI_DATA_EXPERTS";
	
	// Descriptor set names
	public static final String DESCRIPTOR_SET_TEST = "T.E.S.T. 5.1";
	public static final String DESCRIPTOR_SET_PADEL_SINGLE = "Padelpy webservice single";
	public static final String DESCRIPTOR_SET_PADEL_BATCH = "Padelpy_batch";
	
	// Property names
	public static final String WATER_SOLUBILITY = "Water solubility";
	public static final String HENRYS_LAW_CONSTANT = "Henry's law constant";
	public static final String MELTING_POINT = "Melting point";
	public static final String LOG_KOW = "Octanol water partition coefficient";
	public static final String VAPOR_PRESSURE = "Vapor pressure";
	public static final String DENSITY = "Density";
	public static final String BOILING_POINT = "Boiling point";
	public static final String FLASH_POINT = "Flash point";
	public static final String PKA = "pKA";
	public static final String PKA_A = "pKAa";
	public static final String PKA_B = "pKAb";
	public static final String LOG_BCF = "LogBCF";
	public static final String LOG_OH = "LogOH";
	public static final String LOG_KOC = "LogKOC";
	public static final String LOG_HALF_LIFE = "LogHalfLife";
	public static final String LOG_KM_HL = "LogKmHL";
	public static final String LOG_KOA = "LogKOA";
	
	public static final String DEV_TOX ="DevTox";
	public static final String IGC50 ="IGC50";
	public static final String LC50 ="LC50";
	public static final String LC50DM ="LC50DM";
	public static final String LD50="LD50";
	public static final String LLNA ="LLNA";
	public static final String MUTAGENICITY ="Mutagenicity";
	
	// Unit names
	public static final String BINARY = "Binary";
	public static final String G_L = "g/L";
	public static final String MG_L = "mg/L";
	public static final String MG_KG = "mg/kg";
	public static final String L_KG = "L/kg";
	public static final String DEG_C = "C";
	public static final String LOG_UNITS = "Log units";
	public static final String MOLAR = "M";
	public static final String LOG_M = "log10(M)";
	public static final String NEG_LOG_M = "-log10(M)";
	public static final String NEG_LOG_MOL_KG = "-log10(mol/kg)";
	public static final String LOG_L_KG = "log10(L/kg)";
	public static final String G_CM3 = "g/cm3";
	public static final String ATM_M3_MOL = "atm-m3/mol";
	public static final String NEG_LOG_ATM_M3_MOL = "-log10(atm-m3/mol)";
	public static final String LOG_ATM_M3_MOL = "log10(atm-m3/mol)";
	public static final String MMHG = "mmHg";
	public static final String NEG_LOG_MMHG = "-log10(mmHg)";
	public static final String LOG_MMHG = "log10(mmHg)";
	public static final String LOG_HR = "log10(hr)";
	public static final String LOG_DAYS = "log10(days)";
	public static final String DAYS = "days";
	public static final String LOG_CM3_MOLECULE_SEC="log10(cm3/molecule-sec)";
	public static final String CM3_MOLECULE_SEC="cm3/molecule-sec";
	
	// Integer codes for train/test splitting
	public static final Integer TRAIN_SPLIT_NUM = 0;
	public static final Integer TEST_SPLIT_NUM = 1;
	
	// Splitting names
	public static final String SPLITTING_RND_REPRESENTATIVE = "RND_REPRESENTATIVE";
	public static final String SPLITTING_OPERA = "OPERA";
	public static final String SPLITTING_TEST = "TEST";
	
	// Input types for DSSTox queries
	public static final String INPUT_CASRN = "CASRN";
	public static final String INPUT_OTHER_CASRN = "OTHER_CASRN";
	public static final String INPUT_PREFERRED_NAME = "PREFERRED_NAME";
	public static final String INPUT_SYNONYM = "SYNONYM";
	public static final String INPUT_NAME2STRUCTURE = "NAME2STRUCTURE";
	public static final String INPUT_MAPPED_IDENTIFIER = "MAPPED_IDENTIFIER";
	public static final String INPUT_DTXCID = "DTXCID";
	public static final String INPUT_DTXSID = "DTXSID";
	public static final String INPUT_SMILES = "SMILES";
	public static final String INPUT_INCHIKEY = "INCHIKEY";
	
	// QSAR method codes for modeling web services
	public static final String KNN = "knn";
	public static final String XGB = "xgb";
	public static final String SVM = "svm";
	public static final String RF = "rf";
	public static final String DNN = "dnn";
	public static final String CONSENSUS = "consensus";
	
	// Statistic names in qsar_models database
	public static final String Q2_TEST = "Q2_Test";
	public static final String R2_TRAINING = "R2_Training";
	public static final String COVERAGE = "Coverage";
	public static final String MAE = "MAE";
	public static final String RMSE = "RMSE";
	public static final String BALANCED_ACCURACY = "BA";
	public static final String SENSITIVITY = "SN";
	public static final String SPECIFICITY = "SP";
	public static final String CONCORDANCE = "Concordance";
	public static final String POS_CONCORDANCE = "PosConcordance";
	public static final String NEG_CONCORDANCE = "NegConcordance";
	public static final String PEARSON_RSQ = "PearsonRSQ";
	public static final String TAG_TEST = "_Test";
	public static final String TAG_TRAINING = "_Training";
	
	// Acceptable atoms in structures for modeling
	public static HashSet<String> getAcceptableAtomsSet() {
		List<String> list = Arrays.asList("C", "H", "O", "N", "F", "Cl", "Br", "I", "S", "P", "Si", "As", "Hg", "Sn");
		return new HashSet<String>(list);
	}
	
	// Final modeling unit names for each property name
	public static HashMap<String, String> getFinalUnitsMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(MELTING_POINT, DEG_C);
		map.put(BOILING_POINT, DEG_C);
		map.put(FLASH_POINT, DEG_C);
		map.put(LOG_KOW, LOG_UNITS);
		map.put(PKA, LOG_UNITS);
		map.put(PKA_A, LOG_UNITS);
		map.put(PKA_B, LOG_UNITS);
		map.put(WATER_SOLUBILITY, NEG_LOG_M);
		map.put(HENRYS_LAW_CONSTANT, NEG_LOG_ATM_M3_MOL);
		map.put(VAPOR_PRESSURE, NEG_LOG_MMHG);
		map.put(DENSITY, G_CM3);
		map.put(LLNA, BINARY);
		return map;
	}
}
