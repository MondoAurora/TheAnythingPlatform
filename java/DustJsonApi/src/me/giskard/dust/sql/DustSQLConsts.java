package me.giskard.dust.sql;

import me.giskard.dust.mind.DustMindConsts;
import me.giskard.dust.utils.DustUtilsConstsJson;

public interface DustSQLConsts extends DustUtilsConstsJson, DustMindConsts {
	String TOKEN_SQLMETA_SELECT = DUST_UNIT_ID + DUST_SEP_TOKEN + "Select";

	String TOKEN_SQL_TABLE_MAP = DUST_UNIT_ID + DUST_SEP_TOKEN + "tableMap";
	String TOKEN_SQL_COLUMN_MAP = DUST_UNIT_ID + DUST_SEP_TOKEN + "columnMap";
	
	String TOKEN_SQL = DUST_UNIT_ID + DUST_SEP_TOKEN + "sql";
	
}
