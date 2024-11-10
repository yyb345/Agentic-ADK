/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.gpt.nl2sql.db;

import com.alibaba.langengine.gpt.nl2sql.db.meta.DatasourceConfig;
import com.alibaba.langengine.gpt.nl2sql.db.meta.Table;
import lombok.Data;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * sql database
 *
 * @author xiaoxuan.lp
 */
@Data
public class SQLDatabase {

    /**
     * sql engine
     */
    private SQLEngine engine;

    /**
     * sample条数
     */
    private int sampleRowsSize = 3;

    /**
     * we include only one table to save tokens in the prompt.
     */
    private List<String> includeTables;

    /**
     * 包括的表字段，key为表名称
     */
    private Map<String, List<String>> includeColumns;

    /**
     * custom table info
     * eq: custom_table_info = {
     *     "Track": """CREATE TABLE Track (
     *     "TrackId" INTEGER NOT NULL,
     *     "Name" NVARCHAR(200) NOT NULL,
     *     "Composer" NVARCHAR(220),
     *     PRIMARY KEY ("TrackId")
     * )
     *
     * /
     * 3 rows from Track table:
     * TrackId Name    Composer
     * 1   For Those About To Rock (We Salute You) Angus Young, Malcolm Young, Brian Johnson
     * 2   Balls to the Wall   None
     * 3   My favorite song ever   The coolest composer of all time
     * /
     */
    private Map<String, String> customTableInfo;


    /**
     * Get information about specified tables.
     *
     * @return
     */
    public String getTableInfo() {
        StringBuilder builder = new StringBuilder();
        if(customTableInfo != null && customTableInfo.size() > 0) {
            for (Map.Entry<String, String> entry : customTableInfo.entrySet()) {
                builder.append(entry.getValue() + "\n");
            }
            if(includeTables != null &&  includeTables.size() > 0) {
                builder.append(getExistTableInfo());
            }
        } else {
            builder.append(getExistTableInfo());
        }
        return builder.toString();
    }

    public ResultSet executeQuery(String sqlCmd) {
        return getEngine().executeQuery(sqlCmd);
    }

    public static SQLDatabase fromUri(String databaseUri, String dialect) {
        SQLDatabase sqlDatabase = new SQLDatabase();
        sqlDatabase.setEngine(SQLEngine.createEngine(databaseUri, dialect));
        return sqlDatabase;
    }

    public static SQLDatabase fromDatasourceConfig(DatasourceConfig datasourceConfig) {
        SQLDatabase sqlDatabase = new SQLDatabase();
        sqlDatabase.setEngine(SQLEngine.createEngine(datasourceConfig));
        return sqlDatabase;
    }

    private String getExistTableInfo() {
        StringBuilder builder = new StringBuilder();
        List<Table> allTables = getUsableTables();
        List<String> allTableNames = allTables.stream()
                .map(e -> e.getName())
                .collect(Collectors.toList());
        if (includeTables != null && includeTables.size() > 0 && includeTables.stream().allMatch(allTableNames::contains)) {
            allTables = allTables.stream()
                    .filter(e -> includeTables.contains(e.getName()))
                    .collect(Collectors.toList());
        }
        for (Table table : allTables) {
            builder.append(getCreateTable(table) + "\n");
            builder.append(getSampleRows(table, sampleRowsSize) + "\n");
        }
        return builder.toString();
    }

    private List<Table> getUsableTables() {
        return engine.getAllTables();
    }

    private String getCreateTable(Table table) {
        List<String> columnNames = null;
        if(includeColumns != null && includeColumns.containsKey(table.getName())) {
            columnNames = includeColumns.get(table.getName());
        }
        return engine.getCreateTable(table, columnNames);
    }

    private String getSampleRows(Table table, Integer limit) {
        List<String> columnNames = null;
        if(includeColumns != null && includeColumns.containsKey(table.getName())) {
            columnNames = includeColumns.get(table.getName());
        }
        return engine.getSampleRows(table, columnNames, limit);
    }
}
