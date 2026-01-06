package me.giskard.dust.stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import me.giskard.dust.Dust;
import me.giskard.dust.DustAgent;
import me.giskard.dust.kb.DustKBConsts;
import me.giskard.dust.kb.DustKBUtils;
import me.giskard.dust.ldap.DustLDAPConsts;
import me.giskard.dust.utils.DustUtils;
import me.giskard.dust.utils.DustUtilsFactory;
import me.giskard.dust.utils.DustUtilsFile;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DustStreamExcelAgent extends DustAgent implements DustStreamConsts, DustKBConsts, DustLDAPConsts {

	@Override
	protected Object process(DustAccess access) throws Exception {
		String unitId = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_UNIT);

		if (null == unitId) {
			unitId = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_CMD);
		}

		KBStore kb = Dust.getAgent(DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_KB_KNOWLEDGEBASE));
		KBUnit unit = kb.getUnit(unitId, false);

		DustUtilsFactory<String, Set<String>> meta = new DustUtilsFactory.Simple<String, Set<String>>(true, (Class<? extends Set<String>>) TreeSet.class);

		int lc = 0;

		Dust.log(TOKEN_LEVEL_TRACE, "Reading meta", unitId);
		for (KBObject o : unit.objects()) {
			if (0 == (++lc % 10000)) {
				Dust.log(TOKEN_LEVEL_TRACE, "line", lc);
			}

			Set<String> flds = meta.get(o.getType());
			for (String a : (Iterable<String>) DustKBUtils.access(DustAccess.Peek, Collections.EMPTY_LIST, o, KEY_MAP_KEYS)) {
				flds.add(a);
			}
		}

		String fName = DustKBUtils.access(DustAccess.Peek, null, null, TOKEN_PATH);
		if (null == fName) {
			fName = unitId + ".xlsx";
		}

		Workbook wb = fName.toLowerCase().endsWith(".xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();

		CellStyle cs = wb.createCellStyle();
		cs.setWrapText(true);

		Map<String, Sheet> sheets = new HashMap<String, Sheet>();

		float rowHeight = 1;

		for (String t : meta.keys()) {
			Sheet sheet = wb.createSheet(t);
			sheets.put(t, sheet);

			int rc = sheet.getPhysicalNumberOfRows();
			Row row = sheet.createRow(rc);

			int cc = 0;
			for (String f : meta.peek(t)) {
				Cell c = row.createCell(cc++);
				c.setCellValue(f);
			}

			rowHeight = row.getHeightInPoints();
		}

		Dust.log(TOKEN_LEVEL_TRACE, "Generating Excel");

		lc = 0;
		for (KBObject o : unit.objects()) {
			if (0 == (++lc % 10000)) {
				Dust.log(TOKEN_LEVEL_TRACE, "line", lc);
			}

			String t = o.getType();

			Sheet sheet = sheets.get(t);

			int rc = sheet.getPhysicalNumberOfRows();
			Row row = sheet.createRow(rc);
			row.setRowStyle(cs);

			Set<String> flds = meta.peek(t);
			int cc = 0;
			int rlc = 1;
			int clc = 1;
			for (String a : flds) {
				Cell c = row.createCell(cc++);
				c.setCellStyle(cs);

				Object v = DustKBUtils.access(DustAccess.Peek, "", o, a);
				StringBuilder sb = null;
				if (v instanceof Collection) {
					clc = 0;
					for (Object vv : (Collection) v) {
						sb = DustUtils.sbAppend(sb, "\n", false, DustUtils.toString(vv));
						++clc;
					}
					v = sb;
				} else if (v instanceof Map) {
					clc = 0;
					for (Map.Entry<Object, Object> vv : ((Map<Object, Object>) v).entrySet()) {
						sb = DustUtils.sbAppend(sb, "\n", false, DustUtils.toString(vv.getKey()) + " = " + DustUtils.toString(vv.getValue()));
						++clc;
					}
					v = sb;

				}

				c.setCellValue(DustUtils.toString(v));
				if (clc > rlc) {
					rlc = clc;
				}
			}

			row.setHeightInPoints(rowHeight * rlc);
		}

		for (String sn : meta.keys()) {
			Sheet sheet = sheets.get(sn);
			int cc = meta.peek(sn).size();
			for (int ci = 0; ci < cc; ++ci) {
				Dust.log(TOKEN_LEVEL_TRACE, "Column sizing", ci);
				sheet.autoSizeColumn(ci);
			}
		}

		Dust.log(TOKEN_LEVEL_TRACE, "Saving Excel", fName);

		File f = new File(fName);
		DustUtilsFile.ensureDir(f.getAbsoluteFile().getParentFile());
		OutputStream fileOut = new FileOutputStream(f);

		wb.write(fileOut);
		fileOut.flush();
		fileOut.close();

		return null;
	}
}
