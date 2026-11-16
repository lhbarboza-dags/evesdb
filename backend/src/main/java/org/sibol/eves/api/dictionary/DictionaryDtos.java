package org.sibol.eves.api.dictionary;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.sibol.eves.api.common.Ref;
import org.sibol.eves.api.study.StudyDtos.ProtectedAreaDto;

/** Payloads of the dictionary dialogs. Size limits are the zul textbox constraints of each dialog. */
public final class DictionaryDtos {
	private DictionaryDtos() {}

	// ---- shared lookups for the study forms and the dictionary page
	public record CountryInfoRow(Integer id, Integer year, String wbIncome, String gdpPerCapita, String popDensity) {}

	public record CountryRow(Integer id, String code, String name, Ref currency, List<CountryInfoRow> infos) {}

	public record ClimateClassRow(Integer id, String code, String name, String desc) {}

	public record ClimateTypeRow(Integer id, String name, String desc, List<ClimateClassRow> classes) {}

	public record EfgRow(Integer id, String code, String name, String desc) {}

	public record BiomeRow(Integer id, String code, String name, String desc, List<EfgRow> efgs) {}

	public record RealmRow(Integer id, String code, String name, String desc, List<BiomeRow> biomes) {}

	public record SubServiceRow(Integer id, String code, String name, String desc) {}

	public record EServiceRow(Integer id, String code, String name, String desc, List<SubServiceRow> subservices) {}

	public record TermRow(Integer id, String cv, String code, String name, String desc) {}

	public record Lookups(List<Ref> docTypes, List<Ref> protectedStatuses, List<Ref> scales, List<Ref> siteAttributes,
			List<Ref> valuationMethods, List<Ref> economicMeasures, List<Ref> availStudyInfo, List<Ref> valueTypes,
			List<Ref> currencies, List<Ref> units, List<Ref> beneficiaryTypes, List<Ref> beneficiaryUnits,
			List<ClimateTypeRow> climateTypes, List<CountryRow> countries, List<ProtectedAreaDto> protectedAreas,
			List<RealmRow> realms, List<EServiceRow> eservices) {}

	// ---- write payloads
	/** AddCurrencyDialog / AddBeneficialDialog / AddBeneficiaryTypeDialog / AddMethodDialog / AddAvailStudyInfoDialog / AddCvTermDialog. */
	public record TermWrite(@NotBlank String cv, @NotBlank @Size(max = 55) String code, @NotBlank @Size(max = 255) String name,
			@Size(max = 255) String desc) {}

	/** AddCountryDialog: code {1,5}, name {1,25}, currency combobox. */
	public record CountryWrite(@NotBlank @Size(max = 5) String code, @NotBlank @Size(max = 25) String name, Ref currency) {}

	/** AddCountryInfoDialog / EditCountryInfoDialog: year (19|20)xx, the rest {0,55}. */
	public record CountryInfoWrite(@NotBlank @Pattern(regexp = "^(19|20)\\d{2}$", message = "Year must be 19xx or 20xx.") String year,
			@Size(max = 55) String wbIncome, @Size(max = 55) String gdpPerCapita, @Size(max = 55) String popDensity) {}

	public record CountryInfoBatch(@NotNull @Valid List<CountryInfoWrite> items) {}

	/** AddPADialog / EditPADialog: name {1,55}, area/established/location {0,55}. */
	public record ProtectedAreaWrite(@NotBlank @Size(max = 55) String name, @Size(max = 55) String area,
			@Size(max = 55) String established, @Size(max = 55) String location) {}

	public record ProtectedAreaBatch(@NotNull @Valid List<ProtectedAreaWrite> items) {}

	/** AddClimateDialog: type name {1,50} (existing or new), desc {0,255}, staged classes code {1,10}/name {1,100}/desc {0,255}. */
	public record ClimateClassWrite(@NotBlank @Size(max = 10) String code, @NotBlank @Size(max = 100) String name, @Size(max = 255) String desc) {}

	public record ClimateWrite(@NotBlank @Size(max = 50) String typeName, @Size(max = 255) String typeDesc,
			@Valid List<ClimateClassWrite> classes) {}

	/** AddRealmDialog / AddBiomeDialog / AddEfgDialog / AddEServiceDialog / AddEsubServiceDialog staged rows. */
	public record CodeNameWrite(@NotBlank @Size(max = 10) String code, @NotBlank @Size(max = 125) String name, @Size(max = 1000) String desc) {}

	public record CodeNameBatch(Integer parentId, @NotNull @Valid List<CodeNameWrite> items) {}

	/** AddEcosystemDialog: find-or-create realm → biome → EFG by code. */
	public record EcosystemChain(@NotBlank String realmCode, String realmName, String biomeCode, String biomeName,
			String efgCode, String efgName) {}

	/** AddServiceDialog: find-or-create e-service → e-sub-service by code. */
	public record ServiceChain(@NotBlank @Size(max = 10) String serviceCode, @Size(max = 125) String serviceName,
			@Size(max = 10) String subServiceCode, @Size(max = 125) String subServiceName, @Size(max = 255) String desc) {}
}
