package org.devocative.adroit.sql.mig;

import org.devocative.adroit.obuilder.ObjectBuilder;
import org.devocative.adroit.sql.NamedParameterStatement;
import org.devocative.adroit.sql.SqlHelper;
import org.devocative.adroit.sql.XQuery;
import org.devocative.adroit.sql.result.QueryVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PKMigrate {
	private static final Logger logger = LoggerFactory.getLogger(PKMigrate.class);

	private final Connection connection;
	private final SqlHelper helper;
	private final List<String> tables = new ArrayList<>();

	// ------------------------------

	public PKMigrate(Connection connection, String... tables) {
		this.connection = connection;
		helper = new SqlHelper(connection);
		helper.setXMLQueryFile(PKMigrate.class.getResourceAsStream("/oracle_pk_mig.xml"));

		if (tables != null) {
			for (String table : tables) {
				this.tables.add(table.toUpperCase());
			}
		} // else all tables
	}

	// ------------------------------

	public void migrate() throws SQLException {
		QueryVO pk = helper.selectAll("pkList",
			ObjectBuilder.<String, Object>createDefaultMap().put("tables", tables).get());
		List<TablePkFkVO> tablePkFkVOs = pk.toBeans(TablePkFkVO.class);

		logger.info("TablePkFkVO size = {}", tablePkFkVOs.size());

		for (TablePkFkVO table : tablePkFkVOs) {
			QueryVO fk = helper.selectAll("fkList",
				ObjectBuilder.<String, Object>createDefaultMap().put("table", table.getName()).get());
			table.setReferencedBy(fk.toBeans(RefConsVO.class));
		}

		logger.info("TablePkFkVO size = {}", tablePkFkVOs.size());

		for (TablePkFkVO tablePkFkVO : tablePkFkVOs) {
			migrateTable(tablePkFkVO);
		}
	}

	public void migrateTable(TablePkFkVO table) throws SQLException {
		logger.info("Migrating Table: {}", table.getName());

		helper.executeDDL("renameColumn",
			ObjectBuilder.<String, Object>createDefaultMap()
				.put("table", table.getName())
				.put("col_cur_name", table.getPkColumn())
				.put("col_new_name", "old_" + table.getPkColumn())
				.get()
		);
		logger.info("\tRename PK column: {}", table.getPkColumn());

		helper.executeDDL("addColumn",
			ObjectBuilder.<String, Object>createDefaultMap()
				.put("table", table.getName())
				.put("col_name", table.getPkColumn())
				.put("col_type", "varchar2(255 char)") //TODO
				.get()
		);
		logger.info("\tAdd new PK column: {}", table.getPkColumn());

		for (RefConsVO refConsVO : table.getReferencedBy()) {
			helper.executeDDL("renameColumn",
				ObjectBuilder.<String, Object>createDefaultMap()
					.put("table", refConsVO.getTableName())
					.put("col_cur_name", refConsVO.getColumnName())
					.put("col_new_name", "old_" + refConsVO.getColumnName())
					.get()
			);

			helper.executeDDL("addColumn",
				ObjectBuilder.<String, Object>createDefaultMap()
					.put("table", refConsVO.getTableName())
					.put("col_name", refConsVO.getColumnName())
					.put("col_type", "varchar2(255 char)") //TODO
					.get()
			);
		}
		logger.info("\tRename & Add FK columns");

		connection.setAutoCommit(false);

		try {
			String updateIdSql = String.format("update %1$s set %2$s = :new_id where old_%2$s = :cur_id",
				table.getName(), table.getPkColumn());
			NamedParameterStatement updateIdNps = helper.createNPS(XQuery.sql(updateIdSql));

			String selectIdsSql = String.format("select old_%s from %s", table.getPkColumn(), table.getName());
			List<Object> ids = helper.firstColAsList(XQuery.sql(selectIdsSql));
			for (Object id : ids) {
				Object newId = UUID.randomUUID().toString(); //TODO

				updateIdNps
					.setParameters(ObjectBuilder.<String, Object>createDefaultMap()
						.put("new_id", newId)
						.put("cur_id", id)
						.get())
					.executeUpdate();

				for (RefConsVO refConsVO : table.getReferencedBy()) {
					String updateFkSql = String.format("update %1$s set %2$s = :new_id where old_%2$s = :cur_id",
						refConsVO.getTableName(), refConsVO.getColumnName());
					NamedParameterStatement updateFkNps = helper.createNPS(XQuery.sql(updateFkSql));

					updateFkNps
						.setParameters(ObjectBuilder.<String, Object>createDefaultMap()
							.put("new_id", newId)
							.put("cur_id", id)
							.get())
						.executeUpdate();
				}
			}

			connection.commit();
		} catch (SQLException e) {
			logger.error("Data Insert", e);
			connection.rollback();
			throw e;
		}

		for (RefConsVO refConsVO : table.getReferencedBy()) {
			helper.executeDDL("dropCons", ObjectBuilder.<String, Object>createDefaultMap()
				.put("table", refConsVO.getTableName())
				.put("cons", refConsVO.getConsName())
				.get());
		}

		helper.executeDDL("dropCons", ObjectBuilder.<String, Object>createDefaultMap()
			.put("table", table.getName())
			.put("cons", table.getPkConstraint())
			.get());

		helper.executeDDL("addPkCons", ObjectBuilder.<String, Object>createDefaultMap()
			.put("table", table.getName())
			.put("cons", table.getPkConstraint())
			.put("col", table.getPkColumn())
			.get());

		for (RefConsVO refConsVO : table.getReferencedBy()) {
			helper.executeDDL("addFkCons", ObjectBuilder.<String, Object>createDefaultMap()
				.put("table", refConsVO.getTableName())
				.put("cons", refConsVO.getConsName())
				.put("col", refConsVO.getColumnName())
				.put("dest_table", table.getName())
				.get());
		}
	}
}
