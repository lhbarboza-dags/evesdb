package org.sibol.eves.api.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * XLSX export of the "v_fullSummary" view (SearchController btn_export → AgreementDownloadDialog → inputDownloadDialog).
 *
 * Fixes vs. legacy: rows are read with JDBC (the JPA entity used studyid as @Id, so Hibernate returned the first row of
 * each study repeated); the Value_Year column is filled and later columns are no longer shifted by one; Coded/Reviewed
 * columns are filled; the file is streamed instead of written to the server's working directory.
 */
@Service
public class ExportService {

	/** AppValues.dwHeaders (the stray "Value_Type\tTemporary" header is now "Value_Type"). */
	static final List<String> HEADERS = List.of("VALUE ID LIST", "STUDY ID", "TITLE", "AUTHOR", "DOCTYPE", "YEARPUB",
			"REFERENCE", "DOWNLOAD(EXT_LINK)", "COUNTRY", "CLIMATE TYPE", "POPULATION DENSITY", "WB_INCOME",
			"GDP_PerCAPITA", "LOCATION", "LATITUDE", "LONGITUDE", "PROTECTION STATUS", "PA_NAME (If Protected)",
			"SITE SCALE", "SITE AREA", "SITE UNIT", "SITE ATTRIBUTE", "REALM CODE", "REALM", "BIOME CODE", "BIOME",
			"EFG CODE", "EFG", "ECO SERVICE CODE", "ECO SERVICE", "ECO SUB SERVICE CODE", "ECO SUB SERVICE",
			"VALUATION CODE", "VALUATION METHOD", "VALUATION METHOD DESC", "VALUED CHANGE", "VALUATION EQ FUNC INFO",
			"ECONOMIC MEASURE", "Available_Study_Info", "Survey_Study_Info", "Value_Type", "ESValue_Aggregate",
			"ESValue_RangeLow", "ESValue_RangeHigh", "Currency", "Unit", "Value_Year", "PV_Years", "PV_Discount_Rate",
			"Beneficiary_Type", "Beneficiaries_Number", "Beneficiary_Unit", "Abstract", "Coded_by", "Coded_Date",
			"Reviewed_by", "Reviewed_date", "Notes");

	/** View column for each header ("" = left empty, as in the legacy export). */
	static final List<String> COLUMNS = List.of("valueid", "studyid", "title", "author", "doctype", "pubyear",
			"reference", "downloadext", "country", "climatetype", "popdensity", "incomeyr", "gdp", "siteloc", "lat",
			"longt", "protectedstatus", "protectedarea", "scale", "sitearea", "unitarea", "areaattr", "realmcode",
			"realm", "biomecode", "biome", "efgcode", "efgname", "servicecode", "servicename", "subservicecode",
			"subservicename", "vmcode", "vmname", "", "valuechange", "valueeqfxn", "ecomeasure", "availstudyinfo",
			"surveystudyinfo", "valuetype", "valueagg", "valuerangelow", "valuerangehigh", "currency", "unit",
			"studyyear", "pvyears", "discrate", "bentype", "bennmber", "benunit", "studyabstract", "codeby", "codedate",
			"reviewid", "reviewdate", "");

	private final NamedParameterJdbcTemplate jdbc;

	public ExportService(NamedParameterJdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public byte[] export(List<Integer> studyIds) throws IOException {
		List<Map<String, Object>> rows = studyIds.isEmpty() ? List.of()
				: jdbc.queryForList("select * from \"v_fullSummary\" where studyid in (:ids) order by studyid, valueid",
						new MapSqlParameterSource("ids", studyIds));
		Map<Object, String> users = jdbc.queryForList("select userid, firstname, lastname from \"user\"", Map.of()).stream()
				.collect(Collectors.toMap(m -> m.get("userid"),
						m -> (str(m.get("firstname")) + " " + str(m.get("lastname"))).trim(), (a, b) -> a));
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = wb.createSheet("STUDY DATA");
			for (int c = 0; c < 20; c++)
				sheet.setColumnWidth(c, 6000);
			CellStyle head = wb.createCellStyle();
			head.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			XSSFFont font = wb.createFont();
			font.setFontHeightInPoints((short) 11);
			head.setFont(font);
			Row header = sheet.createRow(0);
			for (int c = 0; c < HEADERS.size(); c++) {
				header.createCell(c).setCellValue(HEADERS.get(c));
				header.getCell(c).setCellStyle(head);
			}
			int r = 1;
			for (Map<String, Object> row : rows) {
				Row x = sheet.createRow(r++);
				for (int c = 0; c < COLUMNS.size(); c++) {
					String col = COLUMNS.get(c);
					Object v = col.isEmpty() ? null : row.get(col);
					if (("codeby".equals(col) || "reviewid".equals(col)) && v != null)
						v = users.getOrDefault(v, String.valueOf(v));
					x.createCell(c).setCellValue(v == null ? "" : String.valueOf(v));
				}
			}
			wb.write(out);
			return out.toByteArray();
		}
	}

	/** inputDownloadDialog textbox#tb → a safe download file name. */
	public static String fileName(String requested) {
		String base = requested == null ? "" : requested.replaceAll("[^A-Za-z0-9 _.-]", "").trim();
		if (base.toLowerCase().endsWith(".xlsx"))
			base = base.substring(0, base.length() - 5);
		return (base.isEmpty() ? "sibol-eves-export" : base) + ".xlsx";
	}

	private static String str(Object o) {
		return o == null ? "" : o.toString();
	}
}
