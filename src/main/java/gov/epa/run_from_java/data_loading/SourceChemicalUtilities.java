package gov.epa.run_from_java.data_loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gov.epa.databases.dev_qsar.exp_prop.entity.SourceChemical;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalService;
import gov.epa.databases.dev_qsar.exp_prop.service.SourceChemicalServiceImpl;
import gov.epa.endpoints.datasets.dsstox_mapping.DsstoxMapper;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.util.ParseStringUtils;

/**
* @author TMARTI02
*/
public class SourceChemicalUtilities {
	Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	SourceChemicalService sourceChemicalService=new SourceChemicalServiceImpl(); 
//	Map<String, SourceChemical> sourceChemicalMap = new HashMap<String, SourceChemical>();
	Map<String, SourceChemical> sourceChemical = new HashMap<String, SourceChemical>();
	List<SourceChemical> sourceChemicals;
	
	SourceChemicalUtilities(boolean load) {
		if(load)loadSourceChemicals();
	}

	void loadSourceChemicals() {
		System.out.print("Loading sourceChemical map...");
		sourceChemicals = sourceChemicalService.findAll();
		for (SourceChemical sourceChemical:sourceChemicals) {
//			if(sourceChemical.generateSrcChemId().equals("SCH000000571295")) {
//				System.out.println("Found it in map:"+gson.toJson(sourceChemical));
//			}
		}
		System.out.println("Done");
	}
	
//	void writeChemRegFileLiteratureSources() {
//		loadSourceChemicals();
//		
//		List<SourceChemical>sourceChemicalsLS=new ArrayList<>();
//	
//		for (String key:sourceChemicalMap.keySet()) {
//			SourceChemical sc=sourceChemicalMap.get(key);
//			
//			if(sc.getLiteratureSource()==null) continue;
//			
//			sourceChemicalsLS.add(sc);
//			
//		}
//		System.out.println(sourceChemicalsLS.size());
//		
//		String filepath="data\\dev_qsar\\output\\new chemreg lists\\2024_02_02_ChemProp_Literature_Sources.txt";
//		
//		DsstoxMapper.writeChemRegImportFile(sourceChemicalsLS, filepath);
//		
//		
//	}
	
	void writeChemRegFileChemProp() {
		//129476
		
		List<SourceChemical>scList=new ArrayList<>();
	
		for (SourceChemical sc:this.sourceChemicals) {
			if (sc.getSourceDtxrid()==null) continue;
			scList.add(sc);
		}
		System.out.println(scList.size());
		
		String filepath="data\\dev_qsar\\output\\new chemreg lists\\exp_prop_2024_02_02_from_prod_chemprop.txt";
		
		DsstoxMapper.writeChemRegImportFile(scList, filepath);
		
		
	}
	
	void writeChemRegFilePublicSources() {
		
		List<SourceChemical>sourceChemicalsPS=new ArrayList<>();
		List<SourceChemical>sourceChemicalsPS_ChemProp=new ArrayList<>();
		List<SourceChemical>sourceChemicalsPS_Non_ChemProp=new ArrayList<>();

		Map<String, List<SourceChemical>> mapBySource = new HashMap<>();
		
		for (SourceChemical sc:sourceChemicals) {
			if(sc.getPublicSource()==null) continue;
			String sourceName=sc.getPublicSource().getName();
			sourceChemicalsPS.add(sc);
			
			if(sc.getSourceDtxrid()!=null) {
				sourceChemicalsPS_ChemProp.add(sc);
			} else {
				sourceChemicalsPS_Non_ChemProp.add(sc);
				
				if(mapBySource.containsKey(sourceName)) {					
					List<SourceChemical>list=mapBySource.get(sourceName);
					list.add(sc);
					
				} else {
					List<SourceChemical>list=new ArrayList<>();
					list.add(sc);
					mapBySource.put(sourceName, list);
				}				
			}

		}
		
		System.out.println("All public source:"+sourceChemicalsPS.size());
		System.out.println("Public source with dtxrid (ChemProp):"+sourceChemicalsPS_ChemProp.size());
		System.out.println("Public source without dtxrid:"+sourceChemicalsPS_Non_ChemProp.size());

		for (String sourceName:mapBySource.keySet()) {
//			if(sourceName.toLowerCase().equals("lookchem"))continue;
//			if(!sourceName.equals("ChemicalBook"))continue;
			if(!sourceName.equals("OChem"))continue;
			
			System.out.println(sourceName+"\t"+mapBySource.get(sourceName).size());
			String filepath="data\\dev_qsar\\output\\new chemreg lists\\exp_prop_2024_02_02_from_"+sourceName+".txt";

			DsstoxMapper.writeChemRegImportFile(mapBySource.get(sourceName), filepath);

//			DsstoxMapper.writeChemRegImportFile(mapBySource.get(sourceName), filepath,20000);

		}
		
		
	}
	
	void writeChemRegFileOChem() {
		
		List<SourceChemical>scList=new ArrayList<>();

		String sourceName="OChem";
		
		for (SourceChemical sc:this.sourceChemicals) {

			if(sc.getPublicSource()==null) continue;
			if(sc.getPublicSource().getName().equals(sourceName)) {
				scList.add(sc);
			}
//			if(sc.generateSrcChemId().equals("SCH000000571295")) {
//				System.out.println("Found in map again:"+gson.toJson(sc));
//			}
		}
		
		System.out.println(sourceName+":"+scList.size());
		String filepath="data\\dev_qsar\\output\\new chemreg lists\\exp_prop_2024_02_02_from_"+sourceName+".txt";
		DsstoxMapper.writeChemRegImportFile(scList, filepath);
		
		
	}

	void writeChemRegFileLiteratureSources() {
		
		Map<String, List<SourceChemical>> mapBySource = new HashMap<>();
		
		for (SourceChemical sc:sourceChemicals) {
			if(sc.getLiteratureSource()==null) continue;
			String sourceName=sc.getLiteratureSource().getName();
			
			if(sc.getSourceDtxrid()==null) {
				if(mapBySource.containsKey(sourceName)) {					
					List<SourceChemical>list=mapBySource.get(sourceName);
					list.add(sc);
				
				} else {
					List<SourceChemical>list=new ArrayList<>();
					list.add(sc);
					mapBySource.put(sourceName, list);
				}				
			}
		}
		
		System.out.println("#sources:"+mapBySource.size());

		for (String sourceName:mapBySource.keySet()) {
			System.out.println(sourceName+"\t"+mapBySource.get(sourceName).size());
			String filepath="data\\dev_qsar\\output\\new chemreg lists\\literature sources\\exp_prop_2024_02_02_from_"+sourceName+".txt";
			DsstoxMapper.writeChemRegImportFile(mapBySource.get(sourceName), filepath,40000);
		}
		
		
	}
	
	/**
	 * Compares chemicals in chemreg list with those in the import file(s). 
	 * Missing chemicals are added to a new import file
	 * 
	 * @param file
	 */
	void compareChemRegListToImportFile(File file) {
		if (!file.getName().contains(".txt")) return;
		
		String listName=file.getName().substring(0,file.getName().indexOf(".txt"));
		
		if (file.getName().contains("OPERA.txt")) return;
		if (file.getName().contains("LookChem.txt")) return;
		
		Map<String,SourceChemical>mapList=new TreeMap<>();
		
		String sourceName=file.getName();
		sourceName=sourceName.replace("exp_prop_2024_02_02_from_", "").replace(".txt", "");
		
		if (sourceName.equals("OChem")) {
			for (int i=1;i<=11;i++) {
				Map<String,SourceChemical>mapList2=getMapChemRegList(listName+"_40000_"+i);
				System.out.println(listName+"_"+i+"\t"+mapList2.size());
				mapList.putAll(mapList2);
			}
		} else {
			mapList=getMapChemRegList(listName);	
		}
		

		Map<String,SourceChemical>mapImport=getMapChemRegImportFile(file);
		
		if (mapImport.size()==mapList.size()) return;
		
		List<SourceChemical>missing=new ArrayList<>();
		
		for (String key:mapImport.keySet()) {
			if(!mapList.containsKey(key)) {
				SourceChemical sc=mapImport.get(key);
				missing.add(sc);
//				System.out.println(sc.generateSrcChemId());
			}
		}

		System.out.println(listName+"\t"+mapImport.size()+"\t"+mapList.size()+"\t"+missing.size());

		String filepath=file.getParentFile().getAbsolutePath()+File.separator+"missing"+File.separator+file.getName();
		DsstoxMapper.writeChemRegImportFile(missing, filepath);

		
	}
	
	public static Map<String,SourceChemical> getMapChemRegList(String listName) {
//		System.out.println(listName);
		
		String sql="select external_id,ssi.identifier_type,ssi.identifier from source_generic_substance_mappings sgsm\n"+
		"join source_substances ss on sgsm.fk_source_substance_id = ss.id\n"+
		"join source_substance_identifiers ssi on ss.id = ssi.fk_source_substance_id\n"+
		"join chemical_lists cl on ss.fk_chemical_list_id = cl.id\n"+
		"where cl.name='"+listName+"' and identifier_type not like '%_INCH%';";
		
		ResultSet rs=SqlUtilities.runSQL2(SqlUtilities.getConnectionDSSTOX(), sql);

		Map<String,SourceChemical> mapList=new HashMap<>();
		
		try {

			while (rs.next()) {
				String external_id=rs.getString(1);
				SourceChemical sc=null;
				
				if(mapList.get(external_id)==null) {
					sc=new SourceChemical();
					mapList.put(external_id,sc);	
				} else {
					sc=mapList.get(external_id);
				}
				
				String identifier_type=rs.getString(2);
				String identifier=rs.getString(3);
				
				if(identifier_type.equals("CASRN")) sc.setSourceCasrn(identifier);
				if(identifier_type.equals("NAME")) sc.setSourceChemicalName(identifier);
				if(identifier_type.equals("STRUCTURE")) sc.setSourceSmiles(identifier);
				if(identifier_type.equals("DTXSID")) sc.setSourceDtxsid(identifier);
//				System.out.println(sc.getKey());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapList;
	}
	
	Map<String, SourceChemical> getMapChemRegImportFile(File file) {

		Map<String,SourceChemical> mapList=new HashMap<>();
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(file));

		    
		    
		    ParseStringUtils p=new ParseStringUtils();
		    
		    String[] fieldNames = reader.readLine().split("\t");

		    // Read tsv file line by line
		    String line;
		    while ((line = reader.readLine()) != null) {
		        
		        String[] fieldValues = line.split("\t");
		        

		        SourceChemical sc=new SourceChemical();
		        
		        String external_id=null;
		        
		        for (int i=0;i<fieldValues.length;i++) {
		        	
		        	if(fieldValues[i].isBlank()) continue;
		        	
		        	fieldValues[i]=fieldValues[i].trim();
		        	
		        	if (fieldNames[i].equals("EXTERNAL_ID")) {
		        		external_id=fieldValues[i];
		        		String strId=external_id.replace("SCH", "");
		        		while (strId.substring(0,1).equals("0")) strId=strId.substring(1,strId.length());
		        		sc.setId(Long.parseLong(strId));
		        		
//		        		System.out.println(external_id+"\t"+strId);
		        		
		        	} else if (fieldNames[i].equals("SOURCE_DTXSID")) {
		        		sc.setSourceDtxsid(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_DTXCID")) {
		        		sc.setSourceDtxcid(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_DTXRID")) {
		        		sc.setSourceDtxrid(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_CASRN")) {
		        		sc.setSourceCasrn(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_CHEMICAL_NAME")) {
		        		sc.setSourceChemicalName(fieldValues[i]);
		        	} else if (fieldNames[i].equals("SOURCE_SMILES")) {
		        		sc.setSourceSmiles(fieldValues[i]);
		        	}
		        }
				mapList.put(external_id,sc);
				
				if(external_id.equals("PR")) {
					System.out.println(line);
					System.out.println(fieldValues.length);
				}
		    }

		    reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mapList;

	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		SourceChemicalUtilities scu=new SourceChemicalUtilities(true);
//		scu.writeChemRegFileChemProp();
//		scu.writeChemRegFilePublicSources();
//		scu.writeChemRegFileLiteratureSources();//dont have any without dtxrids- all from chemprop
//		scu.writeChemRegFileOChem();
		
		
		SourceChemicalUtilities scu=new SourceChemicalUtilities(false);
		String folder="data\\dev_qsar\\output\\new chemreg lists\\check\\";
//		String folder="data\\dev_qsar\\output\\new chemreg lists\\OChem 40K\\done\\";
		File FOLDER=new File(folder);
		for (File file:FOLDER.listFiles()) {
			scu.compareChemRegListToImportFile(file);
		}
		
//		scu.compareChemRegListToImportFile(new File("data\\dev_qsar\\output\\new chemreg lists\\exp_prop_2024_02_02_from_OChem.txt"));
		
	}

}
