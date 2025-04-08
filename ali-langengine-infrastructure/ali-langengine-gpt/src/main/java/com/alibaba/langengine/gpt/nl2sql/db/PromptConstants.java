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

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;

import java.util.Arrays;

/**
 * prompt常量
 *
 * @author xiaoxuan.lp
 */
public class PromptConstants {

    public static final String PROMPT_SUFFIX = "Only use the following tables:\n" +
            "{table_info}\n" +
            "\n" +
            "Question: {input}";

    public static final String DEFAULT_PROMPT = "Given an input question, first create a syntactically correct {dialect} query to run, then look at the results of the query and return the answer. Unless the user specifies in his question a specific number of examples he wishes to obtain, always limit your query to at most {top_k} results. You can order the results by a relevant column to return the most interesting examples in the database.\n" +
            "\n" +
            "Never query for all the columns from a specific table, only ask for a the few relevant columns given the question.\n" +
            "\n" +
            "Pay attention to use only the column names that you can see in the schema description. Be careful to not query for columns that do not exist. Also, pay attention to which column is in which table.\n" +
            "\n" +
            "Use the following format:\n" +
            "\n" +
            "Question: Question here\n" +
            "SQLQuery: SQL Query to run\n" +
            "SQLResult: Result of the SQLQuery\n" +
            "Answer: Final answer here(Please answer in Chinese)\n" +
            "\n";

    public static final String SQLITE_PROMPT = "You are a SQLite expert. Given an input question, first create a syntactically correct SQLite query to run, then look at the results of the query and return the answer to the input question.\n" +
            "Unless the user specifies in the question a specific number of examples to obtain, query for at most {top_k} results using the LIMIT clause as per SQLite. You can order the results to return the most informative data in the database.\n" +
            "Never query for all columns from a table. You must query only the columns that are needed to answer the question. Wrap each column name in double quotes (\") to denote them as delimited identifiers.\n" +
            "Pay attention to use only the column names you can see in the tables below. Be careful to not query for columns that do not exist. Also, pay attention to which column is in which table.\n" +
            "Pay attention to use date('now') function to get the current date, if the question involves \"today\".\n" +
            "\n" +
            "Use the following format:\n" +
            "\n" +
            "Question: Question here\n" +
            "SQLQuery: SQL Query to run\n" +
            "SQLResult: Result of the SQLQuery\n" +
            "Answer: Final answer here(Please answer in Chinese)\n" +
            "\n";

    public static final String QUERY_CHECKER_PROMPT = "\n" +
            "{query}\n" +
            "Double check the {dialect} query above for common mistakes, including:\n" +
            "- Using NOT IN with NULL values\n" +
            "- Using UNION when UNION ALL should have been used\n" +
            "- Using BETWEEN for exclusive ranges\n" +
            "- Data type mismatch in predicates\n" +
            "- Properly quoting identifiers\n" +
            "- Using the correct number of arguments for functions\n" +
            "- Casting to the correct data type\n" +
            "- Using the proper columns for joins\n" +
            "\n" +
            "If there are any of the above mistakes, rewrite the query. If there are no mistakes, just reproduce the original query.";

    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE_EN = new PromptTemplate(DEFAULT_PROMPT + PROMPT_SUFFIX, Arrays.asList(new String[]{ "input", "table_info", "dialect", "top_k" }));
    public static final PromptTemplate SQLITE_PROMPT_TEMPLATE_EN = new PromptTemplate(SQLITE_PROMPT + PROMPT_SUFFIX, Arrays.asList(new String[]{ "input", "table_info", "top_k" }));
    public static final PromptTemplate QUERY_CHECKER_PROMPT_TEMPLATE_EN = new PromptTemplate(QUERY_CHECKER_PROMPT, Arrays.asList(new String[]{ "query", "dialect" }));


    public static final String TABLE_INFO = "\n" +
            "CREATE TABLE \"Album\" (\n" +
            "        \"AlbumId\" INTEGER NOT NULL, \n" +
            "        \"Title\" NVARCHAR(160) NOT NULL, \n" +
            "        \"ArtistId\" INTEGER NOT NULL, \n" +
            "        PRIMARY KEY (\"AlbumId\"), \n" +
            "        FOREIGN KEY(\"ArtistId\") REFERENCES \"Artist\" (\"ArtistId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Album table:\n" +
            "AlbumId Title   ArtistId\n" +
            "1       For Those About To Rock We Salute You   1\n" +
            "2       Balls to the Wall       2\n" +
            "3       Restless and Wild       2\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"Artist\" (\n" +
            "        \"ArtistId\" INTEGER NOT NULL, \n" +
            "        \"Name\" NVARCHAR(120), \n" +
            "        PRIMARY KEY (\"ArtistId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Artist table:\n" +
            "ArtistId        Name\n" +
            "1       AC/DC\n" +
            "2       Accept\n" +
            "3       Aerosmith\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"Customer\" (\n" +
            "        \"CustomerId\" INTEGER NOT NULL, \n" +
            "        \"FirstName\" NVARCHAR(40) NOT NULL, \n" +
            "        \"LastName\" NVARCHAR(20) NOT NULL, \n" +
            "        \"Company\" NVARCHAR(80), \n" +
            "        \"Address\" NVARCHAR(70), \n" +
            "        \"City\" NVARCHAR(40), \n" +
            "        \"State\" NVARCHAR(40), \n" +
            "        \"Country\" NVARCHAR(40), \n" +
            "        \"PostalCode\" NVARCHAR(10), \n" +
            "        \"Phone\" NVARCHAR(24), \n" +
            "        \"Fax\" NVARCHAR(24), \n" +
            "        \"Email\" NVARCHAR(60) NOT NULL, \n" +
            "        \"SupportRepId\" INTEGER, \n" +
            "        PRIMARY KEY (\"CustomerId\"), \n" +
            "        FOREIGN KEY(\"SupportRepId\") REFERENCES \"Employee\" (\"EmployeeId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Customer table:\n" +
            "CustomerId      FirstName       LastName        Company Address City    State   Country PostalCode      Phone   Fax     Email   SupportRepId\n" +
            "1       Luís    Gonçalves       Embraer - Empresa Brasileira de Aeronáutica S.A.        Av. Brigadeiro Faria Lima, 2170 São José dos Campos     SP      Brazil  12227-000       +55 (12) 3923-5555      +55 (12) 3923-5566      luisg@embraer.com.br    3\n" +
            "2       Leonie  Köhler  None    Theodor-Heuss-Straße 34 Stuttgart       None    Germany 70174   +49 0711 2842222        None    leonekohler@surfeu.de   5\n" +
            "3       François        Tremblay        None    1498 rue Bélanger       Montréal        QC      Canada  H2G 1A7 +1 (514) 721-4711       None    ftremblay@gmail.com     3\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"Employee\" (\n" +
            "        \"EmployeeId\" INTEGER NOT NULL, \n" +
            "        \"LastName\" NVARCHAR(20) NOT NULL, \n" +
            "        \"FirstName\" NVARCHAR(20) NOT NULL, \n" +
            "        \"Title\" NVARCHAR(30), \n" +
            "        \"ReportsTo\" INTEGER, \n" +
            "        \"BirthDate\" DATETIME, \n" +
            "        \"HireDate\" DATETIME, \n" +
            "        \"Address\" NVARCHAR(70), \n" +
            "        \"City\" NVARCHAR(40), \n" +
            "        \"State\" NVARCHAR(40), \n" +
            "        \"Country\" NVARCHAR(40), \n" +
            "        \"PostalCode\" NVARCHAR(10), \n" +
            "        \"Phone\" NVARCHAR(24), \n" +
            "        \"Fax\" NVARCHAR(24), \n" +
            "        \"Email\" NVARCHAR(60), \n" +
            "        PRIMARY KEY (\"EmployeeId\"), \n" +
            "        FOREIGN KEY(\"ReportsTo\") REFERENCES \"Employee\" (\"EmployeeId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Employee table:\n" +
            "EmployeeId      LastName        FirstName       Title   ReportsTo       BirthDate       HireDate        Address City    State   Country PostalCode      Phone   Fax     Email\n" +
            "1       Adams   Andrew  General Manager None    1962-02-18 00:00:00     2002-08-14 00:00:00     11120 Jasper Ave NW     Edmonton        AB      Canada  T5K 2N1 +1 (780) 428-9482       +1 (780) 428-3457       andrew@chinookcorp.com\n" +
            "2       Edwards Nancy   Sales Manager   1       1958-12-08 00:00:00     2002-05-01 00:00:00     825 8 Ave SW    Calgary AB      Canada  T2P 2T3 +1 (403) 262-3443       +1 (403) 262-3322      nancy@chinookcorp.com\n" +
            "3       Peacock Jane    Sales Support Agent     2       1973-08-29 00:00:00     2002-04-01 00:00:00     1111 6 Ave SW   Calgary AB      Canada  T2P 5M5 +1 (403) 262-3443       +1 (403) 262-6712       jane@chinookcorp.com\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"Genre\" (\n" +
            "        \"GenreId\" INTEGER NOT NULL, \n" +
            "        \"Name\" NVARCHAR(120), \n" +
            "        PRIMARY KEY (\"GenreId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Genre table:\n" +
            "GenreId Name\n" +
            "1       Rock\n" +
            "2       Jazz\n" +
            "3       Metal\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"Invoice\" (\n" +
            "        \"InvoiceId\" INTEGER NOT NULL, \n" +
            "        \"CustomerId\" INTEGER NOT NULL, \n" +
            "        \"InvoiceDate\" DATETIME NOT NULL, \n" +
            "        \"BillingAddress\" NVARCHAR(70), \n" +
            "        \"BillingCity\" NVARCHAR(40), \n" +
            "        \"BillingState\" NVARCHAR(40), \n" +
            "        \"BillingCountry\" NVARCHAR(40), \n" +
            "        \"BillingPostalCode\" NVARCHAR(10), \n" +
            "        \"Total\" NUMERIC(10, 2) NOT NULL, \n" +
            "        PRIMARY KEY (\"InvoiceId\"), \n" +
            "        FOREIGN KEY(\"CustomerId\") REFERENCES \"Customer\" (\"CustomerId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Invoice table:\n" +
            "InvoiceId       CustomerId      InvoiceDate     BillingAddress  BillingCity     BillingState    BillingCountry  BillingPostalCode       Total\n" +
            "1       2       2009-01-01 00:00:00     Theodor-Heuss-Straße 34 Stuttgart       None    Germany 70174   1.98\n" +
            "2       4       2009-01-02 00:00:00     Ullevålsveien 14        Oslo    None    Norway  0171    3.96\n" +
            "3       8       2009-01-03 00:00:00     Grétrystraat 63 Brussels        None    Belgium 1000    5.94\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"InvoiceLine\" (\n" +
            "        \"InvoiceLineId\" INTEGER NOT NULL, \n" +
            "        \"InvoiceId\" INTEGER NOT NULL, \n" +
            "        \"TrackId\" INTEGER NOT NULL, \n" +
            "        \"UnitPrice\" NUMERIC(10, 2) NOT NULL, \n" +
            "        \"Quantity\" INTEGER NOT NULL, \n" +
            "        PRIMARY KEY (\"InvoiceLineId\"), \n" +
            "        FOREIGN KEY(\"TrackId\") REFERENCES \"Track\" (\"TrackId\"), \n" +
            "        FOREIGN KEY(\"InvoiceId\") REFERENCES \"Invoice\" (\"InvoiceId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from InvoiceLine table:\n" +
            "InvoiceLineId   InvoiceId       TrackId UnitPrice       Quantity\n" +
            "1       1       2       0.99    1\n" +
            "2       1       4       0.99    1\n" +
            "3       2       6       0.99    1\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"MediaType\" (\n" +
            "        \"MediaTypeId\" INTEGER NOT NULL, \n" +
            "        \"Name\" NVARCHAR(120), \n" +
            "        PRIMARY KEY (\"MediaTypeId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from MediaType table:\n" +
            "MediaTypeId     Name\n" +
            "1       MPEG audio file\n" +
            "2       Protected AAC audio file\n" +
            "3       Protected MPEG-4 video file\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"Playlist\" (\n" +
            "        \"PlaylistId\" INTEGER NOT NULL, \n" +
            "        \"Name\" NVARCHAR(120), \n" +
            "        PRIMARY KEY (\"PlaylistId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Playlist table:\n" +
            "PlaylistId      Name\n" +
            "1       Music\n" +
            "2       Movies\n" +
            "3       TV Shows\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"PlaylistTrack\" (\n" +
            "        \"PlaylistId\" INTEGER NOT NULL, \n" +
            "        \"TrackId\" INTEGER NOT NULL, \n" +
            "        PRIMARY KEY (\"PlaylistId\", \"TrackId\"), \n" +
            "        FOREIGN KEY(\"TrackId\") REFERENCES \"Track\" (\"TrackId\"), \n" +
            "        FOREIGN KEY(\"PlaylistId\") REFERENCES \"Playlist\" (\"PlaylistId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from PlaylistTrack table:\n" +
            "PlaylistId      TrackId\n" +
            "1       3402\n" +
            "1       3389\n" +
            "1       3390\n" +
            "*/\n" +
            "\n" +
            "\n" +
            "CREATE TABLE \"Track\" (\n" +
            "        \"TrackId\" INTEGER NOT NULL, \n" +
            "        \"Name\" NVARCHAR(200) NOT NULL, \n" +
            "        \"AlbumId\" INTEGER, \n" +
            "        \"MediaTypeId\" INTEGER NOT NULL, \n" +
            "        \"GenreId\" INTEGER, \n" +
            "        \"Composer\" NVARCHAR(220), \n" +
            "        \"Milliseconds\" INTEGER NOT NULL, \n" +
            "        \"Bytes\" INTEGER, \n" +
            "        \"UnitPrice\" NUMERIC(10, 2) NOT NULL, \n" +
            "        PRIMARY KEY (\"TrackId\"), \n" +
            "        FOREIGN KEY(\"MediaTypeId\") REFERENCES \"MediaType\" (\"MediaTypeId\"), \n" +
            "        FOREIGN KEY(\"GenreId\") REFERENCES \"Genre\" (\"GenreId\"), \n" +
            "        FOREIGN KEY(\"AlbumId\") REFERENCES \"Album\" (\"AlbumId\")\n" +
            ")\n" +
            "\n" +
            "/*\n" +
            "3 rows from Track table:\n" +
            "TrackId Name    AlbumId MediaTypeId     GenreId Composer        Milliseconds    Bytes   UnitPrice\n" +
            "1       For Those About To Rock (We Salute You) 1       1       1       Angus Young, Malcolm Young, Brian Johnson       343719  11170334        0.99\n" +
            "2       Balls to the Wall       2       2       1       None    342562  5510424 0.99\n" +
            "3       Fast As a Shark 3       2       1       F. Baltes, S. Kaufman, U. Dirkscneider & W. Hoffman     230619  3990994 0.99\n" +
            "*/";
}
