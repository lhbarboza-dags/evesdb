package org.sibol.eves.api.study;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.sibol.eves.api.common.Ref;

/**
 * One graph shape for reading and writing a study (studysrc → site → dataset). Ids are null for new rows.
 * Validation rules are the ZK textbox constraints from _study.zul / editstudy.zul / AddSiteDialog.zul /
 * AddSiteValuationDialog.zul / EditSiteValuationDialog.zul.
 */
public final class StudyDtos {
	private StudyDtos() {}

	public record StudyDto(
			Integer id,
			@NotBlank(message = "Title is required.") @Size(max = 255) String title,
			@Size(max = 2000) String abstractText,
			@NotBlank(message = "Author is required.") @Size(max = 255) String author,
			Ref docType,
			@Pattern(regexp = "^$|^(19|20)\\d{2}$", message = "Year must be 19xx or 20xx.") String pubYear,
			@Size(max = 255) String reference,
			String externalLink,
			Integer status,
			String statusName,
			String codedBy,
			String codedDate,
			String reviewedBy,
			String reviewedDate,
			@Valid List<SiteDto> sites) {}

	public record SiteDto(
			Integer id,
			boolean deleted,
			@NotBlank(message = "Site name is required.") @Size(max = 255) String name,
			@NotBlank(message = "Latitude is required.") @Size(max = 24) @Pattern(regexp = "^-?(\\d*\\.)?\\d+$", message = "Latitude must be a decimal number.") String latitude,
			@NotBlank(message = "Longitude is required.") @Size(max = 24) @Pattern(regexp = "^-?(\\d*\\.)?\\d+$", message = "Longitude must be a decimal number.") String longitude,
			String area,
			String areaUnit,
			Ref scale,
			@Valid LocationDto location,
			List<ClimateDto> climates,
			List<SiteAttrDto> attributes,
			@Valid List<DatasetDto> datasets) {}

	public record LocationDto(Integer id, String name, CountryInfoDto countryInfo, Ref protectedStatus, ProtectedAreaDto protectedArea) {}

	public record CountryInfoDto(Integer id, Integer countryId, String countryCode, String countryName, Integer year,
			String wbIncome, String gdpPerCapita, String popDensity) {}

	public record ProtectedAreaDto(Integer id, String name, String area, String established, String location) {}

	public record ClimateDto(Integer id, boolean deleted, Ref climateType, Ref climateClass, String description) {}

	public record SiteAttrDto(Integer id, boolean deleted, Ref attribute, String otherValue) {}

	public record DatasetDto(
			Integer id,
			boolean deleted,
			@Valid EcosystemDto ecosystem,
			@Valid ServiceDto service,
			@Valid MethodDto method,
			@Valid ValueDto value) {}

	public record EcosystemDto(Integer id, Ref realm, @Size(max = 1000) String realmDesc, Ref biome,
			@Size(max = 1000) String biomeDesc, Ref efg, @Size(max = 1000) String efgDesc) {}

	public record ServiceDto(Integer id, Ref eservice, @Size(max = 1000) String serviceDesc, Ref esubservice,
			@Size(max = 1000) String subserviceDesc) {}

	public record MethodDto(Integer id, Ref method, String valuedChange, @Size(max = 1500) String valuedEq,
			Ref economicMeasure, @Size(max = 1500) String surveyStudyInfo, List<AvailInfoDto> availStudyInfo) {}

	public record AvailInfoDto(Integer id, boolean deleted, Ref term) {}

	public record ValueDto(Integer id, Ref valueType, @Size(max = 25) String valuePoint, BigDecimal aggregate,
			BigDecimal rangeLow, BigDecimal rangeHigh, Ref currency, Ref unit,
			@Pattern(regexp = "^$|^[1-2]\\d{3}$", message = "Value year must be a 4-digit year.") String year,
			@Size(max = 25) String pvYears, @Size(max = 10) String pvDiscountRate, Ref beneficiaryType,
			BigDecimal beneficiaryNumber, Ref beneficiaryUnit) {}

	/** One row of the View/Edit Study grid (study_view.zul + ViewStudyRenderer). */
	public record StudyListRow(int id, Integer pubYear, String title, String submittedBy, String codedDate,
			String reviewedBy, String reviewedDate, int status, String statusName) {}

	public record StatusRequest(List<Integer> ids, int status) {}

	public record IdsRequest(List<Integer> ids) {}
}
