package me.giskard.dust.sandbox.db;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import me.giskard.dust.core.Dust;
import me.giskard.dust.core.DustConsts;
import me.giskard.dust.core.utils.DustUtils;

public class DustSandboxSQLAgent implements DustConsts {

	public static final String TEST_URL = "jdbc:sqlite:mhData/sandbox01/mh.db";

	public static final String DEF_TABLE = "tap_stream";
	public static final String[] DEF_COLS = new String[] { "streamtype:TEXT(20):not null", "format:TEXT(20):not null", "url:TEXT(2000)", "content:BLOB",
			"srcunit:TEXT(20):not null", "srcid:TEXT(20):not null" };

	String url;
	String[] colNames;

	String sqlList;
	String sqlInsert;
	String sqlUpdate;

	Set<String> dbHandles = new HashSet<>();

	public static void main(String[] args) throws Exception {
		DustSandboxSQLAgent sqla = new DustSandboxSQLAgent();

		sqla.initSql(TEST_URL, DEF_TABLE, DEF_COLS, 2);

		Object[] values = new Object[sqla.colNames.length + 1];

		String fName = "localStore/res/0381a42d.jpg";
		File f = new File(fName);

		values[1] = "image";
		values[2] = "png";
		values[3] = "file://" + f.getCanonicalPath();
		values[5] = "streams.1";
		values[6] = "testImg01";

		try (Connection conn = DriverManager.getConnection(sqla.url)) {
			sqla.doUpdate(conn, values);
		}
	}

	public void update(Collection<DustHandle> streamColl) throws Exception {
		Object[] values = new Object[colNames.length + 1];

		values[1] = "image";
		values[2] = "png";
		values[5] = "streams.1";

		try (Connection conn = DriverManager.getConnection(url)) {
			for (DustHandle hStream : streamColl) {
				String id = hStream.getId();
				values[5] = DustUtils.getPrefix(id, DUST_SEP_TOKEN);
				values[6] = DustUtils.getPostfix(id, DUST_SEP_TOKEN);

				String fName = Dust.access(DustAccess.Peek, null, hStream, TOKEN_PATH);
				File f = new File("localStore/" + fName);

				values[3] = "file://" + f.getCanonicalPath();
				doUpdate(conn, values);
			}
		}
	}

	public void doUpdate(Connection conn, Object[] values) throws Exception {
		URI fUri = new URI((String) values[3]);

		File f = new File(fUri);
		if (f.isFile()) {
			ByteArrayOutputStream bos = null;
			try (FileInputStream fis = new FileInputStream(f)) {
				byte[] buffer = new byte[1024];
				bos = new ByteArrayOutputStream();
				for (int len; (len = fis.read(buffer)) != -1;) {
					bos.write(buffer, 0, len);
				}
			}

			byte[] data = bos.toByteArray();
			values[4] = data;

			String id = values[5] + DUST_SEP_TOKEN + values[6];

			boolean insert = dbHandles.add(id);
			String sql = insert ? sqlInsert : sqlUpdate;

			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				for (int i = 1; i < values.length; ++i) {
					pstmt.setObject(i, values[i]);
				}

				pstmt.executeUpdate();

				values[4] = data.length + " bytes";
				Dust.log(TOKEN_LEVEL_TRACE, insert ? "Inserted" : "Updated", id, values);
			}
		} else {
			Dust.log(TOKEN_LEVEL_ERROR, "Missing file", f.getCanonicalPath());
		}
	}

	public void initSql(String url, String tblName, String[] colDefs, int idCount) throws Exception {
		this.url = url;
		int cl = colDefs.length;
		this.colNames = new String[cl];

		StringBuilder sbAll = null;
		StringBuilder sbVal = null;
		StringBuilder sbUpdate = null;
		StringBuilder sbWhere = null;
		StringBuilder sbId = null;
		StringBuilder sbCols = null;

		int idStart = cl - idCount;

		for (int i = 0; i < cl; ++i) {
			Object[] cd = colDefs[i].split(":");
			if (null != sbCols) {
				sbCols.append(", ");
			}
			sbCols = DustUtils.sbAppend(sbCols, " ", true, cd);

			String c = (String) cd[0];
			colNames[i] = c;

			sbAll = DustUtils.sbAppend(sbAll, ", ", true, c);
			sbVal = DustUtils.sbAppend(sbVal, ", ", true, "?");

			if (i < idStart) {
				sbUpdate = DustUtils.sbAppend(sbUpdate, ", ", true, c + " = ?");
			} else {
				sbWhere = DustUtils.sbAppend(sbWhere, " AND ", true, c + " = ?");
				sbId = DustUtils.sbAppend(sbId, ", ", true, c);
			}
		}

		sqlList = DustUtils.sbAppend(null, " ", true, "SELECT", sbId, "FROM", tblName).toString();
		sqlInsert = DustUtils.sbAppend(null, " ", true, "INSERT INTO", tblName, "(", sbAll, ") VALUES (", sbVal, ")").toString();
		sqlUpdate = DustUtils.sbAppend(null, " ", true, "UPDATE", tblName, "SET", sbUpdate, "WHERE", sbWhere).toString();

		try (Connection conn = DriverManager.getConnection(url)) {
			boolean tExists = false;
			try (ResultSet rs = conn.getMetaData().getTables(null, null, tblName, null)) {
				while (rs.next()) {
					String tName = rs.getString("TABLE_NAME");
					if (DustUtils.isEqual(tName, tblName)) {
						tExists = true;
						break;
					}
				}
			}
			if (tExists) {
				try (PreparedStatement pstmt = conn.prepareStatement(sqlList)) {
					Dust.log(TOKEN_LEVEL_TRACE, "Running query", sqlList);
					ResultSet rs = pstmt.executeQuery();

					while (rs.next()) {
						String id = rs.getString("srcunit") + DUST_SEP_TOKEN + rs.getString("srcid");
						dbHandles.add(id);
						Dust.log(TOKEN_LEVEL_TRACE, rs.getString("srcunit"), rs.getString("srcid"));
					}
				}
			} else {
				String sql;

				try (Statement stmt = conn.createStatement()) {
					sql = DustUtils.sbAppend(null, " ", true, "CREATE TABLE", tblName, "(", sbCols, ")").toString();
					stmt.executeUpdate(sql);

					sql = DustUtils.sbAppend(null, " ", true, "CREATE UNIQUE INDEX", tblName + "_idx", "ON", tblName, "(", sbId, ")").toString();
					stmt.executeUpdate(sql);
				}
			}
		}
	}
}
