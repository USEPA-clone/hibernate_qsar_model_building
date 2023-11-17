package gov.epa.run_from_java.scripts.DSSTOX_Loading;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.json.CDL;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVParser;

import gov.epa.databases.dsstox.entity.DsstoxCompound;
import gov.epa.databases.dsstox.entity.GenericSubstance;
import gov.epa.databases.dsstox.entity.GenericSubstanceCompound;
import gov.epa.run_from_java.scripts.SqlUtilities;
import gov.epa.run_from_java.scripts.DSSTOX_Loading.DSSTOX_Name_Script.DSSTOX_Name;
import gov.epa.run_from_java.scripts.GetExpPropInfo.Utilities;

/**
* @author TMARTI02
*/
public class ToxPrintsScript {
	Connection conn=SqlUtilities.getConnectionDSSTOX();
	
	String lanId="tmarti02";
	
	List<DsstoxCompound> getCompoundsBySQL(int offset,int limit) {

		List<DsstoxCompound>compounds=new ArrayList<>();

		String sql="SELECT c.id, dsstox_compound_id, gs.dsstox_substance_id\n";  
		sql+="FROM compounds c\n";
		sql+="left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n";
		sql+="left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"; 
		//		sql+="where (mol_weight is not null and mol_weight !=0) and (indigo_inchi_key is not null or jchem_inchi_key is not null) and gs.dsstox_substance_id is not null\n";
		sql+="ORDER BY dsstox_compound_id\n";
//		sql+="LIMIT "+limit+" OFFSET "+offset;

		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DsstoxCompound compound=new DsstoxCompound();				

				compound.setId(rs.getLong(1));

				compound.setDsstoxCompoundId(rs.getString(2));

				if (rs.getString(3)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(3));


				}

				compounds.add(compound);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;
	}
	
	
	static class DsstoxCompoundToxprint extends DsstoxCompound implements Comparable<DsstoxCompoundToxprint> {

		String toxPrints;
		
		public void setToxPrints(String descriptorString) {
			this.toxPrints=descriptorString;
		}

		public static String getHeader() {
			return "DTXSID,PREFERRED_NAME,TOXPRINTS_FINGERPRINT,CASRN,INCHIKEY,SMILES,atom:element_main_group,atom:element_metal_group_I_II,atom:element_metal_group_III,atom:element_metal_metalloid,atom:element_metal_poor_metal,atom:element_metal_transistion_metal,atom:element_noble_gas,bond:C#N_cyano_acylcyanide,bond:C#N_cyano_cyanamide,bond:C#N_cyano_cyanohydrin,bond:C#N_nitrile_ab-acetylenic,bond:C#N_nitrile_ab-unsaturated,bond:C#N_nitrile_generic,bond:C#N_nitrile_isonitrile,bond:C#N_nitrile,bond:C(~Z)~C~Q_a-haloalcohol,bond:C(~Z)~C~Q_a-halocarbonyl,bond:C(~Z)~C~Q_a-haloether,bond:C(~Z)~C~Q_a-haloketone_perhalo,bond:C(~Z)~C~Q_b-halocarbonyl,bond:C(~Z)~C~Q_haloamine_haloethyl_(N-mustard),bond:C(~Z)~C~Q_halocarbonyl_dichloro_quinone_(1_2-),bond:C(~Z)~C~Q_halocarbonyl_dichloro_quinone_(1_4-),bond:C(~Z)~C~Q_halocarbonyl_dichloro_quinone_(1_5-),bond:C(~Z)~C~Q_haloether_dibenzodioxin_1-halo,bond:C(~Z)~C~Q_haloether_dibenzodioxin_2-halo,bond:C(~Z)~C~Q_haloether_dibenzodioxin_dichloro_(2_7-),bond:C(~Z)~C~Q_haloether_dibenzodioxin_tetrachloro_(2_3_7_8-),bond:C(~Z)~C~Q_haloether_pyrimidine_2-halo-,bond:C(=O)N_carbamate_dithio,bond:C(=O)N_carbamate_thio,bond:C(=O)N_carbamate_thio_generic,bond:C(=O)N_carbamate,bond:C(=O)N_carboxamide_(NH2),bond:C(=O)N_carboxamide_(NHR),bond:C(=O)N_carboxamide_(NR2),bond:C(=O)N_carboxamide_generic,bond:C(=O)N_carboxamide_thio,bond:C(=O)N_dicarboxamide_N-hydroxy,bond:C(=O)O_acidAnhydride,bond:C(=O)O_carboxylicAcid_alkenyl,bond:C(=O)O_carboxylicAcid_alkyl,bond:C(=O)O_carboxylicAcid_aromatic,bond:C(=O)O_carboxylicAcid_generic,bond:C(=O)O_carboxylicEster_4-nitrophenol,bond:C(=O)O_carboxylicEster_acyclic,bond:C(=O)O_carboxylicEster_aliphatic,bond:C(=O)O_carboxylicEster_alkenyl,bond:C(=O)O_carboxylicEster_alkyl,bond:C(=O)O_carboxylicEster_aromatic,bond:C(=O)O_carboxylicEster_cyclic_b-propiolactone,bond:C(=O)O_carboxylicEster_N-hydroxytriazole_(aromatic),bond:C(=O)O_carboxylicEster_O-pentafluorophenoxy,bond:C(=O)O_carboxylicEster_thio,bond:C=N_carbodiimide,bond:C=N_carboxamidine_generic,bond:C=N_guanidine_generic,bond:C=N_imine_C(connect_H_gt_0),bond:C=N_imine_N(connect_noZ),bond:C=N_imine_oxy,bond:C=O_acyl_halide,bond:C=O_acyl_hydrazide,bond:C=O_aldehyde_alkyl,bond:C=O_aldehyde_aromatic,bond:C=O_aldehyde_generic,bond:C=O_carbonyl_1_2-di,bond:C=O_carbonyl_ab-acetylenic,bond:C=O_carbonyl_ab-unsaturated_aliphatic_(michael_acceptors),bond:C=O_carbonyl_ab-unsaturated_generic,bond:C=O_carbonyl_azido,bond:C=O_carbonyl_generic,bond:C=S_acyl_thio_halide,bond:C=S_carbonyl_thio_generic,bond:CC(=O)C_ketone_aliphatic_acyclic,bond:CC(=O)C_ketone_aliphatic_generic,bond:CC(=O)C_ketone_alkane_cyclic,bond:CC(=O)C_ketone_alkane_cyclic_(C4),bond:CC(=O)C_ketone_alkane_cyclic_(C5),bond:CC(=O)C_ketone_alkane_cyclic_(C6),bond:CC(=O)C_ketone_alkene_cyclic_(C6),bond:CC(=O)C_ketone_alkene_cyclic_(C7),bond:CC(=O)C_ketone_alkene_cyclic_2-en-1-one,bond:CC(=O)C_ketone_alkene_cyclic_2-en-1-one_generic,bond:CC(=O)C_ketone_alkene_cyclic_3-en-1-one,bond:CC(=O)C_ketone_alkene_generic,bond:CC(=O)C_ketone_aromatic_aliphatic,bond:CC(=O)C_ketone_generic,bond:CC(=O)C_ketone_methyl_aliphatic,bond:CC(=O)C_quinone_1_2-benzo,bond:CC(=O)C_quinone_1_2-naphtho,bond:CC(=O)C_quinone_1_4-benzo,bond:CC(=O)C_quinone_1_4-naphtho,bond:CN_amine_alicyclic_generic,bond:CN_amine_aliphatic_generic,bond:CN_amine_alkyl_ethanolamine,bond:CN_amine_alkyl_methanolamine,bond:CN_amine_aromatic_benzidine,bond:CN_amine_aromatic_generic,bond:CN_amine_aromatic_N-hydroxy,bond:CN_amine_pri-NH2_alkyl,bond:CN_amine_pri-NH2_aromatic,bond:CN_amine_pri-NH2_generic,bond:CN_amine_sec-NH_alkyl,bond:CN_amine_sec-NH_aromatic,bond:CN_amine_sec-NH_aromatic_aliphatic,bond:CN_amine_sec-NH_generic,bond:CN_amine_ter-N_aliphatic,bond:CN_amine_ter-N_aromatic,bond:CN_amine_ter-N_aromatic_aliphatic,bond:CN_amine_ter-N_generic,bond:CNO_amineOxide_aromatic,bond:CNO_amineOxide_dimethyl_alkyl,bond:CNO_amineOxide_generic,bond:COC_ether_aliphatic,bond:COC_ether_aliphatic__aromatic,bond:COC_ether_alkenyl,bond:COC_ether_aromatic,bond:COH_alcohol_aliphatic_generic,bond:COH_alcohol_alkene,bond:COH_alcohol_alkene_acyclic,bond:COH_alcohol_alkene_cyclic,bond:COH_alcohol_allyl,bond:COH_alcohol_aromatic,bond:COH_alcohol_aromatic_phenol,bond:COH_alcohol_benzyl,bond:COH_alcohol_diol_(1_1-),bond:COH_alcohol_diol_(1_2-),bond:COH_alcohol_diol_(1_3-),bond:COH_alcohol_generic,bond:COH_alcohol_pri-alkyl,bond:COH_alcohol_sec-alkyl,bond:COH_alcohol_ter-alkyl,bond:CS_sulfide_di-,bond:CS_sulfide_dialkyl,bond:CS_sulfide,bond:CX_halide_alkenyl-Cl_acyclic,bond:CX_halide_alkenyl-Cl_dichloro_(1_1-),bond:CX_halide_alkenyl-X_acyclic,bond:CX_halide_alkenyl-X_acyclic_generic,bond:CX_halide_alkenyl-X_dihalo_(1_1-),bond:CX_halide_alkenyl-X_dihalo_(1_2-),bond:CX_halide_alkenyl-X_generic,bond:CX_halide_alkenyl-X_trihalo_(1_1_2-),bond:CX_halide_alkyl-Cl_dichloro_(1_1-),bond:CX_halide_alkyl-Cl_ethyl,bond:CX_halide_alkyl-Cl_trichloro_(1_1_1-),bond:CX_halide_alkyl-F_perfluoro_butyl,bond:CX_halide_alkyl-F_perfluoro_ethyl,bond:CX_halide_alkyl-F_perfluoro_hexyl,bond:CX_halide_alkyl-F_perfluoro_octyl,bond:CX_halide_alkyl-F_tetrafluoro_(1_1_1_2-),bond:CX_halide_alkyl-F_trifluoro_(1_1_1-),bond:CX_halide_alkyl-X_aromatic_alkane,bond:CX_halide_alkyl-X_aromatic_generic,bond:CX_halide_alkyl-X_benzyl_alkane,bond:CX_halide_alkyl-X_benzyl_generic,bond:CX_halide_alkyl-X_bicyclo[2_2_1]heptane,bond:CX_halide_alkyl-X_bicyclo[2_2_1]heptene,bond:CX_halide_alkyl-X_dihalo_(1_1-),bond:CX_halide_alkyl-X_dihalo_(1_2-),bond:CX_halide_alkyl-X_dihalo_(1_3),bond:CX_halide_alkyl-X_ethyl,bond:CX_halide_alkyl-X_ethyl_generic,bond:CX_halide_alkyl-X_generic,bond:CX_halide_alkyl-X_primary,bond:CX_halide_alkyl-X_secondary,bond:CX_halide_alkyl-X_tertiary,bond:CX_halide_alkyl-X_tetrahalo_(1_1_2_2-),bond:CX_halide_alkyl-X_trihalo_(1_1_1-),bond:CX_halide_alkyl-X_trihalo_(1_1_2-),bond:CX_halide_alkyl-X_trihalo_(1_2_3-),bond:CX_halide_allyl-Cl_acyclic,bond:CX_halide_allyl-X_acyclic,bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_2-),bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_3-),bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_4-),bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_5-),bond:CX_halide_aromatic-Cl_trihalo_benzene_(1_2_4-),bond:CX_halide_aromatic-X_biphenyl,bond:CX_halide_aromatic-X_dihalo_benzene_(1_2-),bond:CX_halide_aromatic-X_dihalo_benzene_(1_3-),bond:CX_halide_aromatic-X_dihalo_benzene_(1_4-),bond:CX_halide_aromatic-X_ether_aromatic_(Ph-O-Ph),bond:CX_halide_aromatic-X_ether_aromatic_(Ph-O-Ph)_generic,bond:CX_halide_aromatic-X_generic,bond:CX_halide_aromatic-X_halo_phenol,bond:CX_halide_aromatic-X_halo_phenol_meta,bond:CX_halide_aromatic-X_halo_phenol_ortho,bond:CX_halide_aromatic-X_halo_phenol_para,bond:CX_halide_aromatic-X_trihalo_benzene_(1_2_3-),bond:CX_halide_aromatic-X_trihalo_benzene_(1_3_5-),bond:CX_halide_generic-X_dihalo_(1_2-),bond:N(=O)_nitrate_generic,bond:N(=O)_nitro_ab-acetylenic,bond:N(=O)_nitro_ab-unsaturated,bond:N(=O)_nitro_aromatic,bond:N(=O)_nitro_C,bond:N(=O)_nitro_N,bond:N[!C]_amino,bond:N=[N+]=[N-]_azide_aromatic,bond:N=[N+]=[N-]_azide_generic,bond:N=C=O_isocyanate_[O_S],bond:N=C=O_isocyanate_generic,bond:N=C=O_isocyanate_thio,bond:N=N_azo_aliphatic_acyclic,bond:N=N_azo_aromatic,bond:N=N_azo_cyanamide,bond:N=N_azo_generic,bond:N=N_azo_oxy,bond:N=O_nitrite_neutral,bond:N=O_N-nitroso_alkyl_mono,bond:N=O_N-nitroso_dialkyl,bond:N=O_N-nitroso_generic,bond:N=O_N-nitroso,bond:NC=O_aminocarbonyl_generic,bond:NC=O_urea_generic,bond:NC=O_urea_thio,bond:NN_hydrazine_acyclic_(connect_noZ),bond:NN_hydrazine_alkyl_generic,bond:NN_hydrazine_alkyl_H,bond:NN_hydrazine_alkyl_H2,bond:NN_hydrazine_alkyl_HH,bond:NN_hydrazine_alkyl_HH2,bond:NN_hydrazine_alkyl_N(connect_Z=1),bond:NN=N_triazene,bond:NO_amine_hyrdroxyl,bond:NO_amino_oxy_generic,bond:OZ_oxide_hyroxy,bond:OZ_oxide_peroxy,bond:OZ_oxide,bond:P(=O)N_phosphonamide,bond:P(=O)N_phosphoramide_diamidophosphate,bond:P(=O)N_phosphoramide_monoamidophosphate,bond:P(=O)N_phosphoramide_phosphotriamide,bond:P=C_phosphorane_generic,bond:P=O_phosphate_alkyl_ester,bond:P=O_phosphate_dithio,bond:P=O_phosphate_thio,bond:P=O_phosphate_thioate,bond:P=O_phosphate_trithio_phosphorothioate,bond:P=O_phosphate,bond:P=O_phosphonate_acid,bond:P=O_phosphonate_aliphatic_ester,bond:P=O_phosphonate_alkyl_ester,bond:P=O_phosphonate_cyano,bond:P=O_phosphonate_ester,bond:P=O_phosphonate_thio_dimethyl_methylphosphonothionate,bond:P=O_phosphonate_thio_acid,bond:P=O_phosphonate_thio_O_S-dimethyl_methylphosphonothioate,bond:P=O_phosphonate_thio_phosphonotrithioate,bond:P=O_phosphonate,bond:P=O_phosphorus_oxo,bond:PC_phosphine_organo_generic,bond:PC_phosphorus_organo_generic,bond:PO_phosphine_oxy,bond:PO_phosphine_oxy_generic,bond:PO_phosphite_generic,bond:PO_phosphite,bond:P~N_generic,bond:P~S_generic,bond:QQ(Q~O_S)_sulfhydride,bond:QQ(Q~O_S)_sulfide_di-,bond:QQ(Q~O_S)_sulfur_oxide,bond:quatN_alkyl_acyclic,bond:quatN_ammonium_inorganic,bond:quatN_b-carbonyl,bond:quatN_generic,bond:quatN_trimethyl_alkyl_acyclic,bond:quatP_phosphonium,bond:quatS,bond:S(=O)N_sulfonamide_ab-acetylenic,bond:S(=O)N_sulfonamide_ab-unsaturated,bond:S(=O)N_sulfonamide,bond:S(=O)N_sulfonylamide,bond:S(=O)O_sulfonate,bond:S(=O)O_sulfonicAcid_acyclic_(chain),bond:S(=O)O_sulfonicAcid_anion,bond:S(=O)O_sulfonicAcid_cyclic_(ring),bond:S(=O)O_sulfonicAcid_generic,bond:S(=O)O_sulfonicEster_acyclic_(S-C(ring)),bond:S(=O)O_sulfonicEster_acyclic_S-C_(chain),bond:S(=O)O_sulfonicEster_aliphatic_(S-C),bond:S(=O)O_sulfonicEster_alkyl_O-C_(H=0),bond:S(=O)O_sulfonicEster_alkyl_S-C,bond:S(=O)O_sulfonicEster_cyclic_S-(any_in_ring),bond:S(=O)O_sulfonyl_triflate,bond:S(=O)O_sulfuricAcid_generic,bond:S(=O)X_sulfonylhalide_fluoride,bond:S(=O)X_sulfonylhalide,bond:S=O_sulfonyl_a_b-acetylenic,bond:S=O_sulfonyl_a_b-unsaturated,bond:S=O_sulfonyl_cyanide,bond:S=O_sulfonyl_generic,bond:S=O_sulfonyl_S_(connect_Z=2),bond:S=O_sulfoxide,bond:S~N_generic,bond:Se~Q_selenium_oxo,bond:Se~Q_selenium_thio,bond:Se~Q_selenium_thioxo,bond:Se~Q_selenocarbon,bond:Se~Q_selenohalide,bond:X[any]_halide,bond:X[any_!C]_halide_inorganic,bond:X~Z_halide-[N_P]_heteroatom,bond:X~Z_halide-[N_P]_heteroatom_N,bond:X~Z_halide-[N_P]_heteroatom_N_generic,bond:X~Z_halide-[O_S]_heteroatom,bond:X~Z_halide_oxo,bond:metal_group_I_II_Ca_oxy_oxo,bond:metal_group_I_II_oxo,bond:metal_group_I_II_oxy,bond:metal_group_III_other_Al_generic,bond:metal_group_III_other_Al_halide,bond:metal_group_III_other_Al_organo,bond:metal_group_III_other_Al_oxo,bond:metal_group_III_other_Al_oxy,bond:metal_group_III_other_Bi_generic,bond:metal_group_III_other_Bi_halide,bond:metal_group_III_other_Bi_organo,bond:metal_group_III_other_Bi_oxo,bond:metal_group_III_other_Bi_oxy,bond:metal_group_III_other_Bi_sulfide,bond:metal_group_III_other_Bi_sulfide(II),bond:metal_group_III_other_generic,bond:metal_group_III_other_generic_oxo,bond:metal_group_III_other_generic_oxy,bond:metal_group_III_other_In_generic,bond:metal_group_III_other_In_halide,bond:metal_group_III_other_In_oxy,bond:metal_group_III_other_In_phosphide_arsenide,bond:metal_group_III_other_Pb_generic,bond:metal_group_III_other_Pb_halide,bond:metal_group_III_other_Pb_organo,bond:metal_group_III_other_Pb_oxo,bond:metal_group_III_other_Pb_oxy,bond:metal_group_III_other_Pb_sulfide,bond:metal_group_III_other_Pb_sulfide(II),bond:metal_group_III_other_Sn_generic,bond:metal_group_III_other_Sn_halide,bond:metal_group_III_other_Sn_organo,bond:metal_group_III_other_Sn_oxo,bond:metal_group_III_other_Sn_oxy,bond:metal_group_III_other_Sn_sulfide,bond:metal_group_III_other_Sn_sulfide(II),bond:metal_group_III_other_Th_generic,bond:metal_group_III_other_Th_halide,bond:metal_group_III_other_Th_oxo,bond:metal_metalloid_alkylSiloxane,bond:metal_metalloid_As_generic,bond:metal_metalloid_As_halide,bond:metal_metalloid_As_organo,bond:metal_metalloid_As_oxo,bond:metal_metalloid_As_oxy,bond:metal_metalloid_As_sulfide,bond:metal_metalloid_As_sulfide(II),bond:metal_metalloid_B_generic,bond:metal_metalloid_B_halide,bond:metal_metalloid_B_organo,bond:metal_metalloid_B_oxo,bond:metal_metalloid_B_oxy,bond:metal_metalloid_oxo,bond:metal_metalloid_oxy,bond:metal_metalloid_Sb_generic,bond:metal_metalloid_Sb_halide,bond:metal_metalloid_Sb_organo,bond:metal_metalloid_Sb_oxo,bond:metal_metalloid_Sb_oxy,bond:metal_metalloid_Sb_sulfide,bond:metal_metalloid_Sb_sulfide(II),bond:metal_metalloid_Si_generic,bond:metal_metalloid_Si_halide,bond:metal_metalloid_Si_organo,bond:metal_metalloid_Si_oxo,bond:metal_metalloid_Si_oxy,bond:metal_metalloid_Te_generic,bond:metal_metalloid_Te_halide,bond:metal_metalloid_Te_organo,bond:metal_metalloid_Te_oxo,bond:metal_metalloid_Te_oxy,bond:metal_metalloid_Te_sulfide,bond:metal_metalloid_Te_sulfide(II),bond:metal_metalloid_trimethylsilane,bond:metal_transition_Ag_oxy_oxo,bond:metal_transition_Cd_generic,bond:metal_transition_Cd_halide,bond:metal_transition_Cr_generic,bond:metal_transition_Cr_oxo,bond:metal_transition_Cr_oxy,bond:metal_transition_Cu_generic,bond:metal_transition_Cu_oxy_oxo,bond:metal_transition_Fe_generic,bond:metal_transition_Hg_generic,bond:metal_transition_Hg_halide,bond:metal_transition_Hg_organo,bond:metal_transition_Hg_oxo,bond:metal_transition_Hg_oxy,bond:metal_transition_Hg_sulfide,bond:metal_transition_Hg_sulfide(II),bond:metal_transition_Mn_generic,bond:metal_transition_Mn_oxy_oxo,bond:metal_transition_Mo_oxy_oxo,bond:metal_transition_Mo_sulfide,bond:metal_transition_oxo,bond:metal_transition_oxy,bond:metal_transition_Pt_generic,bond:metal_transition_Pt_halide,bond:metal_transition_Pt_nitrogen,bond:metal_transition_Pt_organo,bond:metal_transition_Pt_oxy,bond:metal_transition_Ti_generic,bond:metal_transition_Ti_organo,bond:metal_transition_Ti_oxo,bond:metal_transition_Ti_oxy,bond:metal_transition_Tl_halide,bond:metal_transition_V_generic,bond:metal_transition_V_oxo,bond:metal_transition_V_oxy,bond:metal_transition_W_generic,bond:metal_transition_W_oxo,bond:metal_transition_Zn_generic,bond:metal_transition_Zn_phosphide,chain:alkaneBranch_isopropyl_C3,chain:alkaneBranch_t-butyl_C4,chain:alkaneBranch_neopentyl_C5,chain:alkaneBranch_isohexyl_pentyl_3-methyl,chain:alkaneBranch_isooctyl_heptyl_3-methyl,chain:alkaneBranch_isooctyl_hexyl_2-ethyl,chain:alkaneBranch_isooctyl_hexyl_2-methyl,chain:alkaneBranch_isononyl_heptyl_2_5-methyl,chain:alkaneBranch_isononyl_pentyl_1_1_1_3-metyl,chain:alkaneBranch_isodecyl_octyl_1_2-methyl,chain:alkaneCyclic_ethyl_C2_(connect_noZ),chain:alkaneCyclic_propyl_C3,chain:alkaneCyclic_butyl_C4,chain:alkaneCyclic_pentyl_C5,chain:alkaneCyclic_hexyl_C6,chain:alkaneLinear_ethyl_C2(H_gt_1),chain:alkaneLinear_ethyl_C2_(connect_noZ_CN=4),chain:alkaneLinear_propyl_C3,chain:alkaneLinear_butyl_C4,chain:alkaneLinear_hexyl_C6,chain:alkaneLinear_octyl_C8,chain:alkaneLinear_decyl_C10,chain:alkaneLinear_dodedyl_C12,chain:alkaneLinear_tetradecyl_C14,chain:alkaneLinear_hexadecyl_C16,chain:alkaneLinear_stearyl_C18,chain:alkeneBranch_diene_2_6-octadiene,chain:alkeneBranch_diene_2_7-octadiene_(linalyl),chain:alkeneBranch_mono-ene_2-butene,chain:alkeneBranch_mono-ene_2-butene_2-propyl_(tiglate),chain:alkeneCyclic_diene_1_3-cyclohexadiene_C6,chain:alkeneCyclic_diene_1_5-cyclooctadiene,chain:alkeneCyclic_diene_cyclohexene,chain:alkeneCyclic_diene_cyclopentadiene,chain:alkeneCyclic_ethene_C_(connect_noZ),chain:alkeneCyclic_ethene_generic,chain:alkeneCyclic_triene_tropilidine,chain:alkeneLinear_diene_1_2-butene,chain:alkeneLinear_diene_1_3-butene,chain:alkeneLinear_diene_1_4-diene,chain:alkeneLinear_diene_linoleic_(C18),chain:alkeneLinear_mono-ene_2-hexene,chain:alkeneLinear_mono-ene_allyl,chain:alkeneLinear_mono-ene_ehtylene_terminal,chain:alkeneLinear_mono-ene_ethylene,chain:alkeneLinear_mono-ene_ethylene_generic,chain:alkeneLinear_mono-ene_oleic_(C18),chain:alkeneLinear_mono-ene_vinyl,chain:alkeneLinear_triene_linolenic_(C18),chain:alkyne_ethyne_generic,chain:aromaticAlkane_Ar-C_meta,chain:aromaticAlkane_Ar-C_ortho,chain:aromaticAlkane_Ar-C-Ar,chain:aromaticAlkane_Ph-C1_acyclic_connect_H_gt_1,chain:aromaticAlkane_Ph-C1_acyclic_connect_noDblBd,chain:aromaticAlkane_Ph-C1_acyclic_generic,chain:aromaticAlkane_Ph-1_4-C1_acyclic,chain:aromaticAlkane_Ph-C1-Ph,chain:aromaticAlkane_Ph-C2,chain:aromaticAlkane_Ph-C4,chain:aromaticAlkane_Ph-C6,chain:aromaticAlkane_Ph-C8,chain:aromaticAlkane_Ph-C9_nonylphenyl,chain:aromaticAlkane_Ph-C10,chain:aromaticAlkane_Ph-C12,chain:aromaticAlkane_Ph-C1_cyclic,chain:aromaticAlkene_Ph-C2_acyclic_generic,chain:aromaticAlkene_Ph-C2_styrene,chain:aromaticAlkene_Ph-C2,chain:aromaticAlkene_Ph-C3,chain:aromaticAlkene_Ph-C4_isocrotylbenzene,chain:aromaticAlkene_Ph-C4_phenylbutadiene,chain:aromaticAlkene_Ph-C2_cyclic,chain:oxy-alkaneLinear_ethyleneOxide_EO1,chain:oxy-alkaneLinear_ethylenOxide_EO1(O),chain:oxy-alkaneLinear_ethyleneOxide_EO2,chain:oxy-alkaneLinear_ethyleneOxide_EO3,chain:oxy-alkaneLinear_ethyleneOxide_EO4,chain:oxy-alkaneLinear_ethyleneOxide_EO6,chain:oxy-alkaneLinear_ethyleneOxide_EO8,chain:oxy-alkaneLinear_ethyleneOxide_EO10,chain:oxy-alkaneLinear_ethyleneOxide_EO12,chain:oxy-alkaneLinear_ethyleneOxide_EO14,chain:oxy-alkaneLinear_ethyleneOxide_EO16,chain:oxy-alkaneLinear_ethyleneOxide_EO18,chain:oxy-alkaneLinear_ethyleneOxide_EO20,chain:oxy-alkaneBranch_propyleneoxide_PO1,chain:oxy-alkaneBranch_propyleneoxide_PO2,chain:oxy-alkaneBranch_propyleneoxide_PO3,chain:oxy-alkaneBranch_propyleneoxide_PO4,chain:oxy-alkaneBranch_propyleneoxide_PO6,chain:oxy-alkaneBranch_propyleneoxide_PO8,chain:oxy-alkaneBranch_propyleneoxide_PO10,chain:oxy-alkaneLinear_carboxylicEster_AEOC,chain:oxy-alkaneLinear_sulfuricEster_AEOS,group:aminoAcid_aminoAcid_generic,group:aminoAcid_alanine,group:aminoAcid_arginine,group:aminoAcid_asparagine,group:aminoAcid_aspartic_acid,group:aminoAcid_cysteine,group:aminoAcid_glutamic_acid,group:aminoAcid_glutamine,group:aminoAcid_glycine,group:aminoAcid_histidine,group:aminoAcid_isoleucine,group:aminoAcid_leucine,group:aminoAcid_lysine,group:aminoAcid_methionine,group:aminoAcid_phenylalanine,group:aminoAcid_proline,group:aminoAcid_serine,group:aminoAcid_threonine,group:aminoAcid_tryptophan,group:aminoAcid_tyrosine,group:aminoAcid_valine,group:carbohydrate_aldohexose,group:carbohydrate_aldopentose,group:carbohydrate_hexofuranose_hexulose,group:carbohydrate_hexofuranose,group:carbohydrate_hexopyranose_2-deoxy,group:carbohydrate_hexopyranose_fructose,group:carbohydrate_hexopyranose_generic,group:carbohydrate_hexopyranose_glucose,group:carbohydrate_hexopyranose_maltose,group:carbohydrate_inositol,group:carbohydrate_ketohexose,group:carbohydrate_ketopentose,group:carbohydrate_pentofuranose_2-deoxy,group:carbohydrate_pentofuranose,group:carbohydrate_pentopyranose,group:ligand_path_4_bidentate_aminoacetaldehyde,group:ligand_path_4_bidentate_aminoacetate,group:ligand_path_4_bidentate_aminoethanol,group:ligand_path_4_bidentate_bipyridyl,group:ligand_path_4_bidentate_ethylenediamine,group:ligand_path_4_macrocycle_tetrazacyclododecane,group:ligand_path_4_macrocycle_triethylenetriamine,group:ligand_path_4_polydentate,group:ligand_path_4_polydentate_EDTA,group:ligand_path_4_polydentate_NTA,group:ligand_path_4_tridentate,group:ligand_path_4-5_macrocycle_tetrazacyclotetradecane,group:ligand_path_4-5_tridentate,group:ligand_path_5_bidentate_ACAC,group:ligand_path_5_bidentate_aminopropanal,group:ligand_path_5_bidentate_bipyridylmethyl,group:ligand_path_5_bidentate_bipyrrolidilmethyl,group:ligand_path_5_bidentate_diformamide,group:ligand_path_5_bidentate_malonate,group:ligand_path_5_bidentate_propandiamine,group:ligand_path_5_bidentate_propanolamine,group:ligand_path_5_macrocycle,group:ligand_path_5_tridentate,group:ligand_path_5_tridentate_3-hydroxycadaverine,group:ligand_path_5-7_bidentate,group:nucleobase_adenine,group:nucleobase_cytosine,group:nucleobase_guanine,group:nucleobase_guanine_7-methyl,group:nucleobase_thymine,group:nucleobase_uracil,group:nucleobase_hypoxanthine,group:nucleobase_xanthine_purine-2_6-dione,ring:aromatic_benzene,ring:aromatic_biphenyl,ring:aromatic_phenyl,ring:fused_[5_6]_indane,ring:fused_[5_6]_indene,ring:fused_[5_7]_azulene,ring:fused_[6_6]_naphthalene,ring:fused_[6_6]_tetralin,ring:fused_PAH_acenaphthylene,ring:fused_PAH_anthanthrene,ring:fused_PAH_anthracene,ring:fused_PAH_benz(a)anthracene,ring:fused_PAH_benzophenanthrene,ring:fused_PAH_fluorene,ring:fused_PAH_phenanthrene,ring:fused_PAH_pyrene,ring:fused_steroid_generic_[5_6_6_6],ring:hetero_[3]_N_aziridine,ring:hetero_[3]_O_epoxide,ring:hetero_[3]_Z_generic,ring:hetero_[4]_N_azetidine,ring:hetero_[4]_N_beta_lactam,ring:hetero_[4]_O_oxetane,ring:hetero_[4]_Z_generic,ring:hetero_[5]_N_imidazole,ring:hetero_[5]_N_pyrazole,ring:hetero_[5]_N_pyrrole,ring:hetero_[5]_N_pyrrole_generic,ring:hetero_[5]_N_pyrrolidone_(2-),ring:hetero_[5]_N_tetrazole,ring:hetero_[5]_N_triazole_(1_2_3-),ring:hetero_[5]_N_triazole_(1_2_4-),ring:hetero_[5]_N_triazole_(1_3_4-),ring:hetero_[5]_N_O_isoxazole,ring:hetero_[5]_N_O_oxazole,ring:hetero_[5]_N_S_isothiazole,ring:hetero_[5]_N_S_thiadiazole_(1_3_4-),ring:hetero_[5]_N_S_thiazole,ring:hetero_[5]_O_dioxolane_(1_3-),ring:hetero_[5]_O_furan,ring:hetero_[5]_O_furan_a-nitro,ring:hetero_[5]_O_oxolane,ring:hetero_[5]_S_thiophene,ring:hetero_[5]_Z_1_2_3_4-Z,ring:hetero_[5]_Z_1_2_3-Z,ring:hetero_[5]_Z_1_2_4_1_3_4-Z,ring:hetero_[5]_Z_1_2-Z,ring:hetero_[5]_Z_1_3-Z,ring:hetero_[5]_Z_1-Z,ring:hetero_[5_5]_N_pyrrolizidine,ring:hetero_[5_5]_Z_generic,ring:hetero_[5_5_6]_O_aflatoxin_generic,ring:hetero_[5_6]_N_benzimidazole,ring:hetero_[5_6]_N_indazole,ring:hetero_[5_6]_N_indole,ring:hetero_[5_6]_N_isoindole_1_3-dione,ring:hetero_[5_6]_N_isoindole_1-one,ring:hetero_[5_6]_N_purine,ring:hetero_[5_6]_N_S_benzothiazole_(1_3-),ring:hetero_[5_6]_O_benzodioxole_(1_3-),ring:hetero_[5_6]_O_benzofuran,ring:hetero_[5_6]_Z_generic,ring:hetero_[5_7]_Z_generic,ring:hetero_[6]_N_diazine_(1_2-)_generic,ring:hetero_[6]_N_diazine_(1_3-)_generic,ring:hetero_[6]_N_piperazine,ring:hetero_[6]_N_piperidine,ring:hetero_[6]_N_pyrazine,ring:hetero_[6]_N_pyridazine,ring:hetero_[6]_N_pyridine,ring:hetero_[6]_N_pyridine_generic,ring:hetero_[6]_N_pyrimidine,ring:hetero_[6]_N_pyrimidine_2_4-dione,ring:hetero_[6]_N_tetrazine_(1_2_3_4-),ring:hetero_[6]_N_tetrazine_generic,ring:hetero_[6]_N_triazine_(1_2_3-),ring:hetero_[6]_N_triazine_(1_2_4-),ring:hetero_[6]_N_triazine_(1_3_5-),ring:hetero_[6]_N_triazine_generic,ring:hetero_[6]_N_O_1_4-oxazine_generic,ring:hetero_[6]_N_O_1_4-oxazine_morpholine,ring:hetero_[6]_O_dioxane_(1_4-)_generic,ring:hetero_[6]_O_pyran_generic,ring:hetero_[6]_Z_1-,ring:hetero_[6]_Z_1_2-,ring:hetero_[6]_Z_1_2_3-,ring:hetero_[6]_Z_1_2_3_4-,ring:hetero_[6]_Z_1_2_3_5-,ring:hetero_[6]_Z_1_2_4-,ring:hetero_[6]_Z_1_2_4_5-,ring:hetero_[6]_Z_1_3-,ring:hetero_[6]_Z_1_3_5-,ring:hetero_[6]_Z_1_4-,ring:hetero_[6]_Z_generic,ring:hetero_[6_5_6]_N_carbazole,ring:hetero_[6_5_6]_O_benzofuran_dibenzo,ring:hetero_[6_6]_N_isoquinoline,ring:hetero_[6_6]_N_pteridine,ring:hetero_[6_6]_N_pteridine_generic,ring:hetero_[6_6]_N_quinazoline,ring:hetero_[6_6]_N_quinoline,ring:hetero_[6_6]_N_quinoxaline,ring:hetero_[6_6]_O_benzodioxin_(1_4-),ring:hetero_[6_6]_O_benzopyran,ring:hetero_[6_6]_O_benzopyrone_(1_2-),ring:hetero_[6_6]_O_benzopyrone_(1_4-),ring:hetero_[6_6]_Z_generic,ring:hetero_[6_6_6]_N_acridine,ring:hetero_[6_6_6]_N_pteridine_flavin_generic,ring:hetero_[6_6_6]_N_S_phenothiazine,ring:hetero_[6_6_6]_O_benzopyran_dibenzo[b_d],ring:hetero_[6_6_6]_O_benzopyran_dibenzo[b_e],ring:hetero_[6_7]_N_benzodiazepine_(1_4-),ring:hetero_[7]_generic_1_2-Z,ring:hetero_[7]_generic_1_3-Z,ring:hetero_[7]_generic_1_4-Z,ring:hetero_[7]_generic_1-Z,ring:hetero_[7]_N_azepine_generic,ring:hetero_[7]_N_diazepine_(1_4-),ring:hetero_[7]_O_oxepin,ring:polycycle_bicyclo_[2.1.1]heptane,ring:polycycle_bicyclo_[2.1.1]hexane,ring:polycycle_bicyclo_[2.1.1]hexane_5-oxabicyclo,ring:polycycle_bicyclo_[2.2.2]octane,ring:polycycle_bicyclo_[2.2.2]octatriene,ring:polycycle_bicyclo_[3.2.1]octane,ring:polycycle_bicyclo_[3.2.2]nonane,ring:polycycle_bicyclo_[3.3.1]nonane,ring:polycycle_bicyclo_[3.3.2]decane,ring:polycycle_bicyclo_[4.2.0]octadiene,ring:polycycle_bicyclo_[4.3.1]decane,ring:polycycle_bicyclo_[4.4.1]undecane,ring:polycycle_bicyclo_[5.1.0]octadiene,ring:polycycle_bicyclo_[5.4.1]dodecane,ring:polycycle_bicyclo_propene,ring:polycycle_spiro_[2.2]pentane,ring:polycycle_spiro_[2.5]octane,ring:polycycle_spiro_[4.5]decane,ring:polycycle_spiro_1_4-dioxaspiro[4.5]decane,ring:polycycle_tricyclo_[3.5.5]_cyclopropa[cd]pentalene,ring:polycycle_tricyclo_[3.7.7]bullvalene,ring:polycycle_tricyclo_[3.7.7]semibullvalene,ring:polycycle_tricyclo_adamantane,ring:polycycle_tricyclo_benzvalene";
		}
		
		public String toCsv() {
			// TODO Auto-generated method stub
			String line=this.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId()+",";
			line+="\""+this.getGenericSubstanceCompound().getGenericSubstance().getPreferredName()+"\",";
			
			if (this.toxPrints!=null) {
				line+="\""+this.toxPrints+"\",";	
			} else {
				line+="NA,";
			}
			
			line+=this.getGenericSubstanceCompound().getGenericSubstance().getCasrn()+",";
			
			if (this.getJchemInchi()==null) {
				if(this.getIndigoInchikey()!=null) {
					line+=this.getIndigoInchikey()+",";
				} else {
					line+="NA,";
				}
			} else {
				line+=this.getJchemInchikey()+",";
			}

			if (this.getSmiles()==null) {
				line+="\"NA\",";
			} else {
				line+="\""+this.getSmiles()+"\",";	
			}
			
			if (this.toxPrints!=null) {
				line+=this.toxPrints.replace("\t", ",");	
			} else {
				System.out.println("toxprints null");//doesnt happen
				for (int i=1;i<=728;i++) {
					line+=",";	
				}
			}
			return line;
		}

		@Override
		public int compareTo(DsstoxCompoundToxprint c) {
			return this.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId().compareTo(c.getGenericSubstanceCompound().getGenericSubstance().getDsstoxSubstanceId());
		}
		
	}
	
	
	List<DsstoxCompoundToxprint> getToxPrintsBySQL(int offset,int limit) {

		List<DsstoxCompoundToxprint>compounds=new ArrayList<>();

		String sql="select gs.dsstox_substance_id,gs.preferred_name,gs.casrn,c.jchem_inchi_key,c.indigo_inchi_key,c.smiles,"
				+ "cds.descriptor_string_tsv from compound_descriptor_sets cds\n"+
				"join compounds c on c.id=cds.efk_dsstox_compound_id\n"+
				"left join generic_substance_compounds gsc on gsc.fk_compound_id =c.id\n"+//could use inner join to only get ones with SID but a lot slower
				"left join generic_substances gs on gs.id=gsc.fk_generic_substance_id\n"+
				"where cds.fk_descriptor_set_id=1500\n"+
				"limit "+limit+" offset "+offset+";";
		
		
//		DTXSID,PREFERRED_NAME,TOXPRINTS_FINGERPRINT,CASRN,INCHIKEY,SMILES,atom:element_main_group,atom:element_metal_group_I_II,atom:element_metal_group_III,atom:element_metal_metalloid,atom:element_metal_poor_metal,atom:element_metal_transistion_metal,atom:element_noble_gas,bond:C#N_cyano_acylcyanide,bond:C#N_cyano_cyanamide,bond:C#N_cyano_cyanohydrin,bond:C#N_nitrile_ab-acetylenic,bond:C#N_nitrile_ab-unsaturated,bond:C#N_nitrile_generic,bond:C#N_nitrile_isonitrile,bond:C#N_nitrile,bond:C(~Z)~C~Q_a-haloalcohol,bond:C(~Z)~C~Q_a-halocarbonyl,bond:C(~Z)~C~Q_a-haloether,bond:C(~Z)~C~Q_a-haloketone_perhalo,bond:C(~Z)~C~Q_b-halocarbonyl,bond:C(~Z)~C~Q_haloamine_haloethyl_(N-mustard),bond:C(~Z)~C~Q_halocarbonyl_dichloro_quinone_(1_2-),bond:C(~Z)~C~Q_halocarbonyl_dichloro_quinone_(1_4-),bond:C(~Z)~C~Q_halocarbonyl_dichloro_quinone_(1_5-),bond:C(~Z)~C~Q_haloether_dibenzodioxin_1-halo,bond:C(~Z)~C~Q_haloether_dibenzodioxin_2-halo,bond:C(~Z)~C~Q_haloether_dibenzodioxin_dichloro_(2_7-),bond:C(~Z)~C~Q_haloether_dibenzodioxin_tetrachloro_(2_3_7_8-),bond:C(~Z)~C~Q_haloether_pyrimidine_2-halo-,bond:C(=O)N_carbamate_dithio,bond:C(=O)N_carbamate_thio,bond:C(=O)N_carbamate_thio_generic,bond:C(=O)N_carbamate,bond:C(=O)N_carboxamide_(NH2),bond:C(=O)N_carboxamide_(NHR),bond:C(=O)N_carboxamide_(NR2),bond:C(=O)N_carboxamide_generic,bond:C(=O)N_carboxamide_thio,bond:C(=O)N_dicarboxamide_N-hydroxy,bond:C(=O)O_acidAnhydride,bond:C(=O)O_carboxylicAcid_alkenyl,bond:C(=O)O_carboxylicAcid_alkyl,bond:C(=O)O_carboxylicAcid_aromatic,bond:C(=O)O_carboxylicAcid_generic,bond:C(=O)O_carboxylicEster_4-nitrophenol,bond:C(=O)O_carboxylicEster_acyclic,bond:C(=O)O_carboxylicEster_aliphatic,bond:C(=O)O_carboxylicEster_alkenyl,bond:C(=O)O_carboxylicEster_alkyl,bond:C(=O)O_carboxylicEster_aromatic,bond:C(=O)O_carboxylicEster_cyclic_b-propiolactone,bond:C(=O)O_carboxylicEster_N-hydroxytriazole_(aromatic),bond:C(=O)O_carboxylicEster_O-pentafluorophenoxy,bond:C(=O)O_carboxylicEster_thio,bond:C=N_carbodiimide,bond:C=N_carboxamidine_generic,bond:C=N_guanidine_generic,bond:C=N_imine_C(connect_H_gt_0),bond:C=N_imine_N(connect_noZ),bond:C=N_imine_oxy,bond:C=O_acyl_halide,bond:C=O_acyl_hydrazide,bond:C=O_aldehyde_alkyl,bond:C=O_aldehyde_aromatic,bond:C=O_aldehyde_generic,bond:C=O_carbonyl_1_2-di,bond:C=O_carbonyl_ab-acetylenic,bond:C=O_carbonyl_ab-unsaturated_aliphatic_(michael_acceptors),bond:C=O_carbonyl_ab-unsaturated_generic,bond:C=O_carbonyl_azido,bond:C=O_carbonyl_generic,bond:C=S_acyl_thio_halide,bond:C=S_carbonyl_thio_generic,bond:CC(=O)C_ketone_aliphatic_acyclic,bond:CC(=O)C_ketone_aliphatic_generic,bond:CC(=O)C_ketone_alkane_cyclic,bond:CC(=O)C_ketone_alkane_cyclic_(C4),bond:CC(=O)C_ketone_alkane_cyclic_(C5),bond:CC(=O)C_ketone_alkane_cyclic_(C6),bond:CC(=O)C_ketone_alkene_cyclic_(C6),bond:CC(=O)C_ketone_alkene_cyclic_(C7),bond:CC(=O)C_ketone_alkene_cyclic_2-en-1-one,bond:CC(=O)C_ketone_alkene_cyclic_2-en-1-one_generic,bond:CC(=O)C_ketone_alkene_cyclic_3-en-1-one,bond:CC(=O)C_ketone_alkene_generic,bond:CC(=O)C_ketone_aromatic_aliphatic,bond:CC(=O)C_ketone_generic,bond:CC(=O)C_ketone_methyl_aliphatic,bond:CC(=O)C_quinone_1_2-benzo,bond:CC(=O)C_quinone_1_2-naphtho,bond:CC(=O)C_quinone_1_4-benzo,bond:CC(=O)C_quinone_1_4-naphtho,bond:CN_amine_alicyclic_generic,bond:CN_amine_aliphatic_generic,bond:CN_amine_alkyl_ethanolamine,bond:CN_amine_alkyl_methanolamine,bond:CN_amine_aromatic_benzidine,bond:CN_amine_aromatic_generic,bond:CN_amine_aromatic_N-hydroxy,bond:CN_amine_pri-NH2_alkyl,bond:CN_amine_pri-NH2_aromatic,bond:CN_amine_pri-NH2_generic,bond:CN_amine_sec-NH_alkyl,bond:CN_amine_sec-NH_aromatic,bond:CN_amine_sec-NH_aromatic_aliphatic,bond:CN_amine_sec-NH_generic,bond:CN_amine_ter-N_aliphatic,bond:CN_amine_ter-N_aromatic,bond:CN_amine_ter-N_aromatic_aliphatic,bond:CN_amine_ter-N_generic,bond:CNO_amineOxide_aromatic,bond:CNO_amineOxide_dimethyl_alkyl,bond:CNO_amineOxide_generic,bond:COC_ether_aliphatic,bond:COC_ether_aliphatic__aromatic,bond:COC_ether_alkenyl,bond:COC_ether_aromatic,bond:COH_alcohol_aliphatic_generic,bond:COH_alcohol_alkene,bond:COH_alcohol_alkene_acyclic,bond:COH_alcohol_alkene_cyclic,bond:COH_alcohol_allyl,bond:COH_alcohol_aromatic,bond:COH_alcohol_aromatic_phenol,bond:COH_alcohol_benzyl,bond:COH_alcohol_diol_(1_1-),bond:COH_alcohol_diol_(1_2-),bond:COH_alcohol_diol_(1_3-),bond:COH_alcohol_generic,bond:COH_alcohol_pri-alkyl,bond:COH_alcohol_sec-alkyl,bond:COH_alcohol_ter-alkyl,bond:CS_sulfide_di-,bond:CS_sulfide_dialkyl,bond:CS_sulfide,bond:CX_halide_alkenyl-Cl_acyclic,bond:CX_halide_alkenyl-Cl_dichloro_(1_1-),bond:CX_halide_alkenyl-X_acyclic,bond:CX_halide_alkenyl-X_acyclic_generic,bond:CX_halide_alkenyl-X_dihalo_(1_1-),bond:CX_halide_alkenyl-X_dihalo_(1_2-),bond:CX_halide_alkenyl-X_generic,bond:CX_halide_alkenyl-X_trihalo_(1_1_2-),bond:CX_halide_alkyl-Cl_dichloro_(1_1-),bond:CX_halide_alkyl-Cl_ethyl,bond:CX_halide_alkyl-Cl_trichloro_(1_1_1-),bond:CX_halide_alkyl-F_perfluoro_butyl,bond:CX_halide_alkyl-F_perfluoro_ethyl,bond:CX_halide_alkyl-F_perfluoro_hexyl,bond:CX_halide_alkyl-F_perfluoro_octyl,bond:CX_halide_alkyl-F_tetrafluoro_(1_1_1_2-),bond:CX_halide_alkyl-F_trifluoro_(1_1_1-),bond:CX_halide_alkyl-X_aromatic_alkane,bond:CX_halide_alkyl-X_aromatic_generic,bond:CX_halide_alkyl-X_benzyl_alkane,bond:CX_halide_alkyl-X_benzyl_generic,bond:CX_halide_alkyl-X_bicyclo[2_2_1]heptane,bond:CX_halide_alkyl-X_bicyclo[2_2_1]heptene,bond:CX_halide_alkyl-X_dihalo_(1_1-),bond:CX_halide_alkyl-X_dihalo_(1_2-),bond:CX_halide_alkyl-X_dihalo_(1_3),bond:CX_halide_alkyl-X_ethyl,bond:CX_halide_alkyl-X_ethyl_generic,bond:CX_halide_alkyl-X_generic,bond:CX_halide_alkyl-X_primary,bond:CX_halide_alkyl-X_secondary,bond:CX_halide_alkyl-X_tertiary,bond:CX_halide_alkyl-X_tetrahalo_(1_1_2_2-),bond:CX_halide_alkyl-X_trihalo_(1_1_1-),bond:CX_halide_alkyl-X_trihalo_(1_1_2-),bond:CX_halide_alkyl-X_trihalo_(1_2_3-),bond:CX_halide_allyl-Cl_acyclic,bond:CX_halide_allyl-X_acyclic,bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_2-),bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_3-),bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_4-),bond:CX_halide_aromatic-Cl_dichloro_pyridine_(1_5-),bond:CX_halide_aromatic-Cl_trihalo_benzene_(1_2_4-),bond:CX_halide_aromatic-X_biphenyl,bond:CX_halide_aromatic-X_dihalo_benzene_(1_2-),bond:CX_halide_aromatic-X_dihalo_benzene_(1_3-),bond:CX_halide_aromatic-X_dihalo_benzene_(1_4-),bond:CX_halide_aromatic-X_ether_aromatic_(Ph-O-Ph),bond:CX_halide_aromatic-X_ether_aromatic_(Ph-O-Ph)_generic,bond:CX_halide_aromatic-X_generic,bond:CX_halide_aromatic-X_halo_phenol,bond:CX_halide_aromatic-X_halo_phenol_meta,bond:CX_halide_aromatic-X_halo_phenol_ortho,bond:CX_halide_aromatic-X_halo_phenol_para,bond:CX_halide_aromatic-X_trihalo_benzene_(1_2_3-),bond:CX_halide_aromatic-X_trihalo_benzene_(1_3_5-),bond:CX_halide_generic-X_dihalo_(1_2-),bond:N(=O)_nitrate_generic,bond:N(=O)_nitro_ab-acetylenic,bond:N(=O)_nitro_ab-unsaturated,bond:N(=O)_nitro_aromatic,bond:N(=O)_nitro_C,bond:N(=O)_nitro_N,bond:N[!C]_amino,bond:N=[N+]=[N-]_azide_aromatic,bond:N=[N+]=[N-]_azide_generic,bond:N=C=O_isocyanate_[O_S],bond:N=C=O_isocyanate_generic,bond:N=C=O_isocyanate_thio,bond:N=N_azo_aliphatic_acyclic,bond:N=N_azo_aromatic,bond:N=N_azo_cyanamide,bond:N=N_azo_generic,bond:N=N_azo_oxy,bond:N=O_nitrite_neutral,bond:N=O_N-nitroso_alkyl_mono,bond:N=O_N-nitroso_dialkyl,bond:N=O_N-nitroso_generic,bond:N=O_N-nitroso,bond:NC=O_aminocarbonyl_generic,bond:NC=O_urea_generic,bond:NC=O_urea_thio,bond:NN_hydrazine_acyclic_(connect_noZ),bond:NN_hydrazine_alkyl_generic,bond:NN_hydrazine_alkyl_H,bond:NN_hydrazine_alkyl_H2,bond:NN_hydrazine_alkyl_HH,bond:NN_hydrazine_alkyl_HH2,bond:NN_hydrazine_alkyl_N(connect_Z=1),bond:NN=N_triazene,bond:NO_amine_hyrdroxyl,bond:NO_amino_oxy_generic,bond:OZ_oxide_hyroxy,bond:OZ_oxide_peroxy,bond:OZ_oxide,bond:P(=O)N_phosphonamide,bond:P(=O)N_phosphoramide_diamidophosphate,bond:P(=O)N_phosphoramide_monoamidophosphate,bond:P(=O)N_phosphoramide_phosphotriamide,bond:P=C_phosphorane_generic,bond:P=O_phosphate_alkyl_ester,bond:P=O_phosphate_dithio,bond:P=O_phosphate_thio,bond:P=O_phosphate_thioate,bond:P=O_phosphate_trithio_phosphorothioate,bond:P=O_phosphate,bond:P=O_phosphonate_acid,bond:P=O_phosphonate_aliphatic_ester,bond:P=O_phosphonate_alkyl_ester,bond:P=O_phosphonate_cyano,bond:P=O_phosphonate_ester,bond:P=O_phosphonate_thio_dimethyl_methylphosphonothionate,bond:P=O_phosphonate_thio_acid,bond:P=O_phosphonate_thio_O_S-dimethyl_methylphosphonothioate,bond:P=O_phosphonate_thio_phosphonotrithioate,bond:P=O_phosphonate,bond:P=O_phosphorus_oxo,bond:PC_phosphine_organo_generic,bond:PC_phosphorus_organo_generic,bond:PO_phosphine_oxy,bond:PO_phosphine_oxy_generic,bond:PO_phosphite_generic,bond:PO_phosphite,bond:P~N_generic,bond:P~S_generic,bond:QQ(Q~O_S)_sulfhydride,bond:QQ(Q~O_S)_sulfide_di-,bond:QQ(Q~O_S)_sulfur_oxide,bond:quatN_alkyl_acyclic,bond:quatN_ammonium_inorganic,bond:quatN_b-carbonyl,bond:quatN_generic,bond:quatN_trimethyl_alkyl_acyclic,bond:quatP_phosphonium,bond:quatS,bond:S(=O)N_sulfonamide_ab-acetylenic,bond:S(=O)N_sulfonamide_ab-unsaturated,bond:S(=O)N_sulfonamide,bond:S(=O)N_sulfonylamide,bond:S(=O)O_sulfonate,bond:S(=O)O_sulfonicAcid_acyclic_(chain),bond:S(=O)O_sulfonicAcid_anion,bond:S(=O)O_sulfonicAcid_cyclic_(ring),bond:S(=O)O_sulfonicAcid_generic,bond:S(=O)O_sulfonicEster_acyclic_(S-C(ring)),bond:S(=O)O_sulfonicEster_acyclic_S-C_(chain),bond:S(=O)O_sulfonicEster_aliphatic_(S-C),bond:S(=O)O_sulfonicEster_alkyl_O-C_(H=0),bond:S(=O)O_sulfonicEster_alkyl_S-C,bond:S(=O)O_sulfonicEster_cyclic_S-(any_in_ring),bond:S(=O)O_sulfonyl_triflate,bond:S(=O)O_sulfuricAcid_generic,bond:S(=O)X_sulfonylhalide_fluoride,bond:S(=O)X_sulfonylhalide,bond:S=O_sulfonyl_a_b-acetylenic,bond:S=O_sulfonyl_a_b-unsaturated,bond:S=O_sulfonyl_cyanide,bond:S=O_sulfonyl_generic,bond:S=O_sulfonyl_S_(connect_Z=2),bond:S=O_sulfoxide,bond:S~N_generic,bond:Se~Q_selenium_oxo,bond:Se~Q_selenium_thio,bond:Se~Q_selenium_thioxo,bond:Se~Q_selenocarbon,bond:Se~Q_selenohalide,bond:X[any]_halide,bond:X[any_!C]_halide_inorganic,bond:X~Z_halide-[N_P]_heteroatom,bond:X~Z_halide-[N_P]_heteroatom_N,bond:X~Z_halide-[N_P]_heteroatom_N_generic,bond:X~Z_halide-[O_S]_heteroatom,bond:X~Z_halide_oxo,bond:metal_group_I_II_Ca_oxy_oxo,bond:metal_group_I_II_oxo,bond:metal_group_I_II_oxy,bond:metal_group_III_other_Al_generic,bond:metal_group_III_other_Al_halide,bond:metal_group_III_other_Al_organo,bond:metal_group_III_other_Al_oxo,bond:metal_group_III_other_Al_oxy,bond:metal_group_III_other_Bi_generic,bond:metal_group_III_other_Bi_halide,bond:metal_group_III_other_Bi_organo,bond:metal_group_III_other_Bi_oxo,bond:metal_group_III_other_Bi_oxy,bond:metal_group_III_other_Bi_sulfide,bond:metal_group_III_other_Bi_sulfide(II),bond:metal_group_III_other_generic,bond:metal_group_III_other_generic_oxo,bond:metal_group_III_other_generic_oxy,bond:metal_group_III_other_In_generic,bond:metal_group_III_other_In_halide,bond:metal_group_III_other_In_oxy,bond:metal_group_III_other_In_phosphide_arsenide,bond:metal_group_III_other_Pb_generic,bond:metal_group_III_other_Pb_halide,bond:metal_group_III_other_Pb_organo,bond:metal_group_III_other_Pb_oxo,bond:metal_group_III_other_Pb_oxy,bond:metal_group_III_other_Pb_sulfide,bond:metal_group_III_other_Pb_sulfide(II),bond:metal_group_III_other_Sn_generic,bond:metal_group_III_other_Sn_halide,bond:metal_group_III_other_Sn_organo,bond:metal_group_III_other_Sn_oxo,bond:metal_group_III_other_Sn_oxy,bond:metal_group_III_other_Sn_sulfide,bond:metal_group_III_other_Sn_sulfide(II),bond:metal_group_III_other_Th_generic,bond:metal_group_III_other_Th_halide,bond:metal_group_III_other_Th_oxo,bond:metal_metalloid_alkylSiloxane,bond:metal_metalloid_As_generic,bond:metal_metalloid_As_halide,bond:metal_metalloid_As_organo,bond:metal_metalloid_As_oxo,bond:metal_metalloid_As_oxy,bond:metal_metalloid_As_sulfide,bond:metal_metalloid_As_sulfide(II),bond:metal_metalloid_B_generic,bond:metal_metalloid_B_halide,bond:metal_metalloid_B_organo,bond:metal_metalloid_B_oxo,bond:metal_metalloid_B_oxy,bond:metal_metalloid_oxo,bond:metal_metalloid_oxy,bond:metal_metalloid_Sb_generic,bond:metal_metalloid_Sb_halide,bond:metal_metalloid_Sb_organo,bond:metal_metalloid_Sb_oxo,bond:metal_metalloid_Sb_oxy,bond:metal_metalloid_Sb_sulfide,bond:metal_metalloid_Sb_sulfide(II),bond:metal_metalloid_Si_generic,bond:metal_metalloid_Si_halide,bond:metal_metalloid_Si_organo,bond:metal_metalloid_Si_oxo,bond:metal_metalloid_Si_oxy,bond:metal_metalloid_Te_generic,bond:metal_metalloid_Te_halide,bond:metal_metalloid_Te_organo,bond:metal_metalloid_Te_oxo,bond:metal_metalloid_Te_oxy,bond:metal_metalloid_Te_sulfide,bond:metal_metalloid_Te_sulfide(II),bond:metal_metalloid_trimethylsilane,bond:metal_transition_Ag_oxy_oxo,bond:metal_transition_Cd_generic,bond:metal_transition_Cd_halide,bond:metal_transition_Cr_generic,bond:metal_transition_Cr_oxo,bond:metal_transition_Cr_oxy,bond:metal_transition_Cu_generic,bond:metal_transition_Cu_oxy_oxo,bond:metal_transition_Fe_generic,bond:metal_transition_Hg_generic,bond:metal_transition_Hg_halide,bond:metal_transition_Hg_organo,bond:metal_transition_Hg_oxo,bond:metal_transition_Hg_oxy,bond:metal_transition_Hg_sulfide,bond:metal_transition_Hg_sulfide(II),bond:metal_transition_Mn_generic,bond:metal_transition_Mn_oxy_oxo,bond:metal_transition_Mo_oxy_oxo,bond:metal_transition_Mo_sulfide,bond:metal_transition_oxo,bond:metal_transition_oxy,bond:metal_transition_Pt_generic,bond:metal_transition_Pt_halide,bond:metal_transition_Pt_nitrogen,bond:metal_transition_Pt_organo,bond:metal_transition_Pt_oxy,bond:metal_transition_Ti_generic,bond:metal_transition_Ti_organo,bond:metal_transition_Ti_oxo,bond:metal_transition_Ti_oxy,bond:metal_transition_Tl_halide,bond:metal_transition_V_generic,bond:metal_transition_V_oxo,bond:metal_transition_V_oxy,bond:metal_transition_W_generic,bond:metal_transition_W_oxo,bond:metal_transition_Zn_generic,bond:metal_transition_Zn_phosphide,chain:alkaneBranch_isopropyl_C3,chain:alkaneBranch_t-butyl_C4,chain:alkaneBranch_neopentyl_C5,chain:alkaneBranch_isohexyl_pentyl_3-methyl,chain:alkaneBranch_isooctyl_heptyl_3-methyl,chain:alkaneBranch_isooctyl_hexyl_2-ethyl,chain:alkaneBranch_isooctyl_hexyl_2-methyl,chain:alkaneBranch_isononyl_heptyl_2_5-methyl,chain:alkaneBranch_isononyl_pentyl_1_1_1_3-metyl,chain:alkaneBranch_isodecyl_octyl_1_2-methyl,chain:alkaneCyclic_ethyl_C2_(connect_noZ),chain:alkaneCyclic_propyl_C3,chain:alkaneCyclic_butyl_C4,chain:alkaneCyclic_pentyl_C5,chain:alkaneCyclic_hexyl_C6,chain:alkaneLinear_ethyl_C2(H_gt_1),chain:alkaneLinear_ethyl_C2_(connect_noZ_CN=4),chain:alkaneLinear_propyl_C3,chain:alkaneLinear_butyl_C4,chain:alkaneLinear_hexyl_C6,chain:alkaneLinear_octyl_C8,chain:alkaneLinear_decyl_C10,chain:alkaneLinear_dodedyl_C12,chain:alkaneLinear_tetradecyl_C14,chain:alkaneLinear_hexadecyl_C16,chain:alkaneLinear_stearyl_C18,chain:alkeneBranch_diene_2_6-octadiene,chain:alkeneBranch_diene_2_7-octadiene_(linalyl),chain:alkeneBranch_mono-ene_2-butene,chain:alkeneBranch_mono-ene_2-butene_2-propyl_(tiglate),chain:alkeneCyclic_diene_1_3-cyclohexadiene_C6,chain:alkeneCyclic_diene_1_5-cyclooctadiene,chain:alkeneCyclic_diene_cyclohexene,chain:alkeneCyclic_diene_cyclopentadiene,chain:alkeneCyclic_ethene_C_(connect_noZ),chain:alkeneCyclic_ethene_generic,chain:alkeneCyclic_triene_tropilidine,chain:alkeneLinear_diene_1_2-butene,chain:alkeneLinear_diene_1_3-butene,chain:alkeneLinear_diene_1_4-diene,chain:alkeneLinear_diene_linoleic_(C18),chain:alkeneLinear_mono-ene_2-hexene,chain:alkeneLinear_mono-ene_allyl,chain:alkeneLinear_mono-ene_ehtylene_terminal,chain:alkeneLinear_mono-ene_ethylene,chain:alkeneLinear_mono-ene_ethylene_generic,chain:alkeneLinear_mono-ene_oleic_(C18),chain:alkeneLinear_mono-ene_vinyl,chain:alkeneLinear_triene_linolenic_(C18),chain:alkyne_ethyne_generic,chain:aromaticAlkane_Ar-C_meta,chain:aromaticAlkane_Ar-C_ortho,chain:aromaticAlkane_Ar-C-Ar,chain:aromaticAlkane_Ph-C1_acyclic_connect_H_gt_1,chain:aromaticAlkane_Ph-C1_acyclic_connect_noDblBd,chain:aromaticAlkane_Ph-C1_acyclic_generic,chain:aromaticAlkane_Ph-1_4-C1_acyclic,chain:aromaticAlkane_Ph-C1-Ph,chain:aromaticAlkane_Ph-C2,chain:aromaticAlkane_Ph-C4,chain:aromaticAlkane_Ph-C6,chain:aromaticAlkane_Ph-C8,chain:aromaticAlkane_Ph-C9_nonylphenyl,chain:aromaticAlkane_Ph-C10,chain:aromaticAlkane_Ph-C12,chain:aromaticAlkane_Ph-C1_cyclic,chain:aromaticAlkene_Ph-C2_acyclic_generic,chain:aromaticAlkene_Ph-C2_styrene,chain:aromaticAlkene_Ph-C2,chain:aromaticAlkene_Ph-C3,chain:aromaticAlkene_Ph-C4_isocrotylbenzene,chain:aromaticAlkene_Ph-C4_phenylbutadiene,chain:aromaticAlkene_Ph-C2_cyclic,chain:oxy-alkaneLinear_ethyleneOxide_EO1,chain:oxy-alkaneLinear_ethylenOxide_EO1(O),chain:oxy-alkaneLinear_ethyleneOxide_EO2,chain:oxy-alkaneLinear_ethyleneOxide_EO3,chain:oxy-alkaneLinear_ethyleneOxide_EO4,chain:oxy-alkaneLinear_ethyleneOxide_EO6,chain:oxy-alkaneLinear_ethyleneOxide_EO8,chain:oxy-alkaneLinear_ethyleneOxide_EO10,chain:oxy-alkaneLinear_ethyleneOxide_EO12,chain:oxy-alkaneLinear_ethyleneOxide_EO14,chain:oxy-alkaneLinear_ethyleneOxide_EO16,chain:oxy-alkaneLinear_ethyleneOxide_EO18,chain:oxy-alkaneLinear_ethyleneOxide_EO20,chain:oxy-alkaneBranch_propyleneoxide_PO1,chain:oxy-alkaneBranch_propyleneoxide_PO2,chain:oxy-alkaneBranch_propyleneoxide_PO3,chain:oxy-alkaneBranch_propyleneoxide_PO4,chain:oxy-alkaneBranch_propyleneoxide_PO6,chain:oxy-alkaneBranch_propyleneoxide_PO8,chain:oxy-alkaneBranch_propyleneoxide_PO10,chain:oxy-alkaneLinear_carboxylicEster_AEOC,chain:oxy-alkaneLinear_sulfuricEster_AEOS,group:aminoAcid_aminoAcid_generic,group:aminoAcid_alanine,group:aminoAcid_arginine,group:aminoAcid_asparagine,group:aminoAcid_aspartic_acid,group:aminoAcid_cysteine,group:aminoAcid_glutamic_acid,group:aminoAcid_glutamine,group:aminoAcid_glycine,group:aminoAcid_histidine,group:aminoAcid_isoleucine,group:aminoAcid_leucine,group:aminoAcid_lysine,group:aminoAcid_methionine,group:aminoAcid_phenylalanine,group:aminoAcid_proline,group:aminoAcid_serine,group:aminoAcid_threonine,group:aminoAcid_tryptophan,group:aminoAcid_tyrosine,group:aminoAcid_valine,group:carbohydrate_aldohexose,group:carbohydrate_aldopentose,group:carbohydrate_hexofuranose_hexulose,group:carbohydrate_hexofuranose,group:carbohydrate_hexopyranose_2-deoxy,group:carbohydrate_hexopyranose_fructose,group:carbohydrate_hexopyranose_generic,group:carbohydrate_hexopyranose_glucose,group:carbohydrate_hexopyranose_maltose,group:carbohydrate_inositol,group:carbohydrate_ketohexose,group:carbohydrate_ketopentose,group:carbohydrate_pentofuranose_2-deoxy,group:carbohydrate_pentofuranose,group:carbohydrate_pentopyranose,group:ligand_path_4_bidentate_aminoacetaldehyde,group:ligand_path_4_bidentate_aminoacetate,group:ligand_path_4_bidentate_aminoethanol,group:ligand_path_4_bidentate_bipyridyl,group:ligand_path_4_bidentate_ethylenediamine,group:ligand_path_4_macrocycle_tetrazacyclododecane,group:ligand_path_4_macrocycle_triethylenetriamine,group:ligand_path_4_polydentate,group:ligand_path_4_polydentate_EDTA,group:ligand_path_4_polydentate_NTA,group:ligand_path_4_tridentate,group:ligand_path_4-5_macrocycle_tetrazacyclotetradecane,group:ligand_path_4-5_tridentate,group:ligand_path_5_bidentate_ACAC,group:ligand_path_5_bidentate_aminopropanal,group:ligand_path_5_bidentate_bipyridylmethyl,group:ligand_path_5_bidentate_bipyrrolidilmethyl,group:ligand_path_5_bidentate_diformamide,group:ligand_path_5_bidentate_malonate,group:ligand_path_5_bidentate_propandiamine,group:ligand_path_5_bidentate_propanolamine,group:ligand_path_5_macrocycle,group:ligand_path_5_tridentate,group:ligand_path_5_tridentate_3-hydroxycadaverine,group:ligand_path_5-7_bidentate,group:nucleobase_adenine,group:nucleobase_cytosine,group:nucleobase_guanine,group:nucleobase_guanine_7-methyl,group:nucleobase_thymine,group:nucleobase_uracil,group:nucleobase_hypoxanthine,group:nucleobase_xanthine_purine-2_6-dione,ring:aromatic_benzene,ring:aromatic_biphenyl,ring:aromatic_phenyl,ring:fused_[5_6]_indane,ring:fused_[5_6]_indene,ring:fused_[5_7]_azulene,ring:fused_[6_6]_naphthalene,ring:fused_[6_6]_tetralin,ring:fused_PAH_acenaphthylene,ring:fused_PAH_anthanthrene,ring:fused_PAH_anthracene,ring:fused_PAH_benz(a)anthracene,ring:fused_PAH_benzophenanthrene,ring:fused_PAH_fluorene,ring:fused_PAH_phenanthrene,ring:fused_PAH_pyrene,ring:fused_steroid_generic_[5_6_6_6],ring:hetero_[3]_N_aziridine,ring:hetero_[3]_O_epoxide,ring:hetero_[3]_Z_generic,ring:hetero_[4]_N_azetidine,ring:hetero_[4]_N_beta_lactam,ring:hetero_[4]_O_oxetane,ring:hetero_[4]_Z_generic,ring:hetero_[5]_N_imidazole,ring:hetero_[5]_N_pyrazole,ring:hetero_[5]_N_pyrrole,ring:hetero_[5]_N_pyrrole_generic,ring:hetero_[5]_N_pyrrolidone_(2-),ring:hetero_[5]_N_tetrazole,ring:hetero_[5]_N_triazole_(1_2_3-),ring:hetero_[5]_N_triazole_(1_2_4-),ring:hetero_[5]_N_triazole_(1_3_4-),ring:hetero_[5]_N_O_isoxazole,ring:hetero_[5]_N_O_oxazole,ring:hetero_[5]_N_S_isothiazole,ring:hetero_[5]_N_S_thiadiazole_(1_3_4-),ring:hetero_[5]_N_S_thiazole,ring:hetero_[5]_O_dioxolane_(1_3-),ring:hetero_[5]_O_furan,ring:hetero_[5]_O_furan_a-nitro,ring:hetero_[5]_O_oxolane,ring:hetero_[5]_S_thiophene,ring:hetero_[5]_Z_1_2_3_4-Z,ring:hetero_[5]_Z_1_2_3-Z,ring:hetero_[5]_Z_1_2_4_1_3_4-Z,ring:hetero_[5]_Z_1_2-Z,ring:hetero_[5]_Z_1_3-Z,ring:hetero_[5]_Z_1-Z,ring:hetero_[5_5]_N_pyrrolizidine,ring:hetero_[5_5]_Z_generic,ring:hetero_[5_5_6]_O_aflatoxin_generic,ring:hetero_[5_6]_N_benzimidazole,ring:hetero_[5_6]_N_indazole,ring:hetero_[5_6]_N_indole,ring:hetero_[5_6]_N_isoindole_1_3-dione,ring:hetero_[5_6]_N_isoindole_1-one,ring:hetero_[5_6]_N_purine,ring:hetero_[5_6]_N_S_benzothiazole_(1_3-),ring:hetero_[5_6]_O_benzodioxole_(1_3-),ring:hetero_[5_6]_O_benzofuran,ring:hetero_[5_6]_Z_generic,ring:hetero_[5_7]_Z_generic,ring:hetero_[6]_N_diazine_(1_2-)_generic,ring:hetero_[6]_N_diazine_(1_3-)_generic,ring:hetero_[6]_N_piperazine,ring:hetero_[6]_N_piperidine,ring:hetero_[6]_N_pyrazine,ring:hetero_[6]_N_pyridazine,ring:hetero_[6]_N_pyridine,ring:hetero_[6]_N_pyridine_generic,ring:hetero_[6]_N_pyrimidine,ring:hetero_[6]_N_pyrimidine_2_4-dione,ring:hetero_[6]_N_tetrazine_(1_2_3_4-),ring:hetero_[6]_N_tetrazine_generic,ring:hetero_[6]_N_triazine_(1_2_3-),ring:hetero_[6]_N_triazine_(1_2_4-),ring:hetero_[6]_N_triazine_(1_3_5-),ring:hetero_[6]_N_triazine_generic,ring:hetero_[6]_N_O_1_4-oxazine_generic,ring:hetero_[6]_N_O_1_4-oxazine_morpholine,ring:hetero_[6]_O_dioxane_(1_4-)_generic,ring:hetero_[6]_O_pyran_generic,ring:hetero_[6]_Z_1-,ring:hetero_[6]_Z_1_2-,ring:hetero_[6]_Z_1_2_3-,ring:hetero_[6]_Z_1_2_3_4-,ring:hetero_[6]_Z_1_2_3_5-,ring:hetero_[6]_Z_1_2_4-,ring:hetero_[6]_Z_1_2_4_5-,ring:hetero_[6]_Z_1_3-,ring:hetero_[6]_Z_1_3_5-,ring:hetero_[6]_Z_1_4-,ring:hetero_[6]_Z_generic,ring:hetero_[6_5_6]_N_carbazole,ring:hetero_[6_5_6]_O_benzofuran_dibenzo,ring:hetero_[6_6]_N_isoquinoline,ring:hetero_[6_6]_N_pteridine,ring:hetero_[6_6]_N_pteridine_generic,ring:hetero_[6_6]_N_quinazoline,ring:hetero_[6_6]_N_quinoline,ring:hetero_[6_6]_N_quinoxaline,ring:hetero_[6_6]_O_benzodioxin_(1_4-),ring:hetero_[6_6]_O_benzopyran,ring:hetero_[6_6]_O_benzopyrone_(1_2-),ring:hetero_[6_6]_O_benzopyrone_(1_4-),ring:hetero_[6_6]_Z_generic,ring:hetero_[6_6_6]_N_acridine,ring:hetero_[6_6_6]_N_pteridine_flavin_generic,ring:hetero_[6_6_6]_N_S_phenothiazine,ring:hetero_[6_6_6]_O_benzopyran_dibenzo[b_d],ring:hetero_[6_6_6]_O_benzopyran_dibenzo[b_e],ring:hetero_[6_7]_N_benzodiazepine_(1_4-),ring:hetero_[7]_generic_1_2-Z,ring:hetero_[7]_generic_1_3-Z,ring:hetero_[7]_generic_1_4-Z,ring:hetero_[7]_generic_1-Z,ring:hetero_[7]_N_azepine_generic,ring:hetero_[7]_N_diazepine_(1_4-),ring:hetero_[7]_O_oxepin,ring:polycycle_bicyclo_[2.1.1]heptane,ring:polycycle_bicyclo_[2.1.1]hexane,ring:polycycle_bicyclo_[2.1.1]hexane_5-oxabicyclo,ring:polycycle_bicyclo_[2.2.2]octane,ring:polycycle_bicyclo_[2.2.2]octatriene,ring:polycycle_bicyclo_[3.2.1]octane,ring:polycycle_bicyclo_[3.2.2]nonane,ring:polycycle_bicyclo_[3.3.1]nonane,ring:polycycle_bicyclo_[3.3.2]decane,ring:polycycle_bicyclo_[4.2.0]octadiene,ring:polycycle_bicyclo_[4.3.1]decane,ring:polycycle_bicyclo_[4.4.1]undecane,ring:polycycle_bicyclo_[5.1.0]octadiene,ring:polycycle_bicyclo_[5.4.1]dodecane,ring:polycycle_bicyclo_propene,ring:polycycle_spiro_[2.2]pentane,ring:polycycle_spiro_[2.5]octane,ring:polycycle_spiro_[4.5]decane,ring:polycycle_spiro_1_4-dioxaspiro[4.5]decane,ring:polycycle_tricyclo_[3.5.5]_cyclopropa[cd]pentalene,ring:polycycle_tricyclo_[3.7.7]bullvalene,ring:polycycle_tricyclo_[3.7.7]semibullvalene,ring:polycycle_tricyclo_adamantane,ring:polycycle_tricyclo_benzvalene

		
//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				DsstoxCompoundToxprint compound=new DsstoxCompoundToxprint();				

				if (rs.getString(1)!=null) {
					GenericSubstanceCompound gsc=new GenericSubstanceCompound();
					GenericSubstance gs=new GenericSubstance(); 
					compound.setGenericSubstanceCompound(gsc);
					gsc.setGenericSubstance(gs);
					gs.setDsstoxSubstanceId(rs.getString(1));
					gs.setPreferredName(rs.getString(2));
					gs.setCasrn(rs.getString(3));
					compound.setJchemInchikey(rs.getString(4));
					compound.setIndigoInchikey(rs.getString(5));
					compound.setSmiles(rs.getString(6));
					compound.setToxPrints(rs.getString(7));
					compounds.add(compound);
				}

				

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//		System.out.println(compounds.size());
		return compounds;
	}

	
	HashSet<Long> getLoadedRecordsHashtable(int fk_descriptor_set_id) {

		HashSet<Long>ids=new HashSet<>();

		String sql="SELECT efk_dsstox_compound_id FROM compound_descriptor_sets c\n"+
		"where fk_descriptor_set_id="+fk_descriptor_set_id+";";
		
//		sql+="LIMIT "+limit+" OFFSET "+offset;

//		System.out.println(sql);

		ResultSet rs=SqlUtilities.runSQL2(conn, sql);

		try {
			while (rs.next()) {
				ids.add(rs.getLong(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ids;
	}
	
	void compoundsToJsonFiles() {

		//		DsstoxCompoundServiceImpl compoundService=new DsstoxCompoundServiceImpl();
		//		List<DsstoxCompound>compounds=compoundService.findAll();

		File file=new File("data/dsstox/json/snapshot_compounds_for_toxprints.json");

		List<DsstoxCompound>compounds=getCompoundsBySQL(0,-1);
		
		for (int i=0;i<compounds.size();i++) {
			
			DsstoxCompound compound=compounds.get(i);
			
			if(compound.getGenericSubstanceCompound()==null) {
				compounds.remove(i--);
			}
		}
		
		System.out.println(compounds.size());

		Utilities.saveJson(compounds, file.getAbsolutePath());

	}
	
	void goThroughCSV() {
		String filepath="data/dsstox/snapshot_compounds_curated_toxprints.csv";
//		String filepath="O:\\Public\\CharlieLowe\\snapshot_compounds_curated_toxprints.csv";
		
		try {
			int fk_descriptor_set_id=1500;
			
			
			HashSet<Long> efksLoaded=getLoadedRecordsHashtable(fk_descriptor_set_id);//toxprints already in the db
			
			Type listOfMyClassObject = new TypeToken<List<DsstoxCompound>>() {}.getType();
			File fileJson=new File("data/dsstox/json/snapshot_compounds_for_toxprints.json");
			List<DsstoxCompound>compounds=Utilities.gson.fromJson(new FileReader(fileJson), listOfMyClassObject);
			
			System.out.println("loaded json file for dsstox lookup");
			
			Hashtable<String,DsstoxCompound>htCompoundsByDTXCID=new Hashtable<>();
			
			for (DsstoxCompound compound:compounds) {
				htCompoundsByDTXCID.put(compound.getDsstoxCompoundId(), compound);
			}
			
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			
			CSVParser parser = new CSVParser();
			
			String header=br.readLine();

			String[] hvals = parser.parseLine(header);
			
			String htoxprints="";
			for (int i=3;i<hvals.length-2;i++) {
				htoxprints+=hvals[i];
				if(i<hvals.length-3) htoxprints+="\t";
			}

			int batchSize=50000;
			
			List<CompoundDescriptors>compoundDescriptors=new ArrayList<>();
			
			int counter=0;
			
			while (true) {
				

				String line=br.readLine();
				
				counter++;
				
				if(counter%100000==0) System.out.println("\n"+counter);
				
				if(line==null) break;
				
				String[] vals = parser.parseLine(line);
				
				String smiles=vals[0];
				String sid=vals[1];
				String cid=vals[2];
				
				if(efksLoaded.contains(htCompoundsByDTXCID.get(cid).getId())) continue;
				
				String toxprints="";
				
				for (int i=3;i<vals.length-2;i++) {
					toxprints+=vals[i];
					if(i<vals.length-3) toxprints+="\t";
				}
				
				if(htoxprints.split("\t").length!=toxprints.split("\t").length) {
					System.out.println("vals mismatch for "+cid);
					break;
				}

				CompoundDescriptors cd=new CompoundDescriptors();
				cd.efk_dsstox_compound_id=htCompoundsByDTXCID.get(cid).getId();
				cd.fk_descriptor_set_id=fk_descriptor_set_id;
				cd.descriptor_string_tsv=toxprints;
				cd.created_by=lanId;
				
				compoundDescriptors.add(cd);
				
				if (compoundDescriptors.size()==batchSize) {
					createSQL(compoundDescriptors,1000);
					compoundDescriptors.clear();
//					break;
				}
			}
			
			
			for (CompoundDescriptors cd:compoundDescriptors) {
				System.out.println("\t"+cd.efk_dsstox_compound_id);
			}
			
			//Do the rest:
			createSQL(compoundDescriptors,1000);

			
			br.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	class CompoundDescriptors {
		long efk_dsstox_compound_id;
		int fk_descriptor_set_id;
		String descriptor_string_tsv;
		String created_by;
		
	}
	
	public void createSQL (List<CompoundDescriptors> compoundDescriptors,int batchSize) {

		Connection conn=SqlUtilities.getConnectionDSSTOX();
		
		String [] fieldNames= {"efk_dsstox_compound_id","fk_descriptor_set_id","descriptor_string_tsv","created_by","created_at"};
		
		
		String sql="INSERT INTO compound_descriptor_sets (";
		
		for (int i=0;i<fieldNames.length;i++) {
			
			if (fieldNames[i].contains(" ")) {
				sql+="\""+fieldNames[i]+"\"";	
			} else {
				sql+=fieldNames[i];
			}
			
			if (i<fieldNames.length-1)sql+=",";
			else sql+=") VALUES (";
		}
		
		for (int i=0;i<fieldNames.length-1;i++) {
			sql+="?";
			if (i<fieldNames.length-1)sql+=",";			 		
		}
		sql+="current_timestamp)";	
//		System.out.println(sql);
		
		try {
			conn.setAutoCommit(false);
			PreparedStatement prep = conn.prepareStatement(sql);
			
			long t1=System.currentTimeMillis();

			for (int counter = 0; counter < compoundDescriptors.size(); counter++) {
				CompoundDescriptors cd=compoundDescriptors.get(counter);
				prep.setLong(1, cd.efk_dsstox_compound_id);
				prep.setInt(2, cd.fk_descriptor_set_id);
				prep.setString(3, cd.descriptor_string_tsv);
				prep.setString(4, cd.created_by);
				prep.addBatch();
				
				if (counter % batchSize == 0 && counter!=0) {
					 System.out.println(counter+"\t"+cd.efk_dsstox_compound_id);
					prep.executeBatch();
					conn.commit();
				}
			}

			int[] count = prep.executeBatch();// do what's left
			long t2=System.currentTimeMillis();
			System.out.println("time to post "+compoundDescriptors.size()+" records using batchsize=" +batchSize+":\t"+(t2-t1)/1000.0+" seconds");
			conn.commit();
//			conn.setAutoCommit(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void createFigShareCSV() {

		int batchSize=50000;
		int i=0;

		List<DsstoxCompoundToxprint>compoundsAll=new ArrayList<>();

		try {

			while(true) {
				List<DsstoxCompoundToxprint>compounds=getToxPrintsBySQL(i*batchSize, batchSize);
				if(compounds.size()==0) break;
				compoundsAll.addAll(compounds);
				i++;
				
				System.out.println(i+"\t"+compounds.size()+"\t"+compoundsAll.size());
			}
			
			Collections.sort(compoundsAll);

			i=0;
			while(compoundsAll.size()>0) {

				File file=new File("data/dsstox/csv/toxprints_figshare_"+(i+1)+".csv");
				FileWriter fw=new FileWriter(file);
				fw.write(DsstoxCompoundToxprint.getHeader()+"\r\n");
				
				for (int j=0;j<batchSize;j++) {					
					DsstoxCompoundToxprint compound=compoundsAll.remove(0);
					fw.write(compound.toCsv()+"\r\n");
					if(compoundsAll.size()==0) break;
				}
				
				fw.flush();
				fw.close();
				i++;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}


	}
	
	

	public static void main(String[] args) {
		ToxPrintsScript t=new ToxPrintsScript();
//		t.compoundsToJsonFiles();
//		t.goThroughCSV();
		t.createFigShareCSV();
	}		
	
}
