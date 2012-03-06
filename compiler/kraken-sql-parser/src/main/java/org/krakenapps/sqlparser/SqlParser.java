/*
 * Copyright 2010 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.sqlparser;

import java.text.ParseException;
import java.util.Date;

import org.krakenapps.bnf.Syntax;
import org.krakenapps.sqlparser.syntax.SqlSyntax;

public class SqlParser {
	private Syntax syntax;

	public SqlParser() {
		syntax = SqlSyntax.create();
	}

	public Object eval(String sql) throws ParseException {
		return syntax.eval(sql);
	}

	public static void main(String[] args) throws ParseException {
		SqlParser parser = new SqlParser();

		Date begin = new Date();
		runTests(parser);
		Date end = new Date();
		System.out.println((end.getTime() - begin.getTime()) + " milliseconds");
	}

	private static void runTests(SqlParser parser) throws ParseException {
		String createTable = "CREATE TABLE test ( id INT NOT NULL PRIMARY KEY, name CHAR(20), created_at DATE, FOREIGN KEY (name) REFERENCES test2(name), PRIMARY KEY (id,name,qoo) )";
		test(parser, createTable);
		test(parser, "ALTER TABLE test ADD name char(60)");
		test(parser, "ALTER TABLE test ADD created_at date");
		test(parser, "ALTER TABLE test ADD name character varying(60)");
		test(parser, "ALTER TABLE test DROP COLUMN name");
		test(parser, "DROP TABLE test");
		test(parser, "DESC test");
		test(parser, "SHOW TABLES");
		test(parser, "INSERT INTO test DEFAULT VALUES");
		test(parser, "INSERT INTO test VALUES (NULL, NULL)");
		test(parser, "INSERT INTO test (id, name) VALUES (NULL, NULL)");
		test(parser, "INSERT INTO test (id, name) VALUES (DEFAULT, DEFAULT)");
		test(parser, "INSERT INTO test (id, name) VALUES (-1, +2)");
		test(parser, "INSERT INTO test (id, name) VALUES (-1, 2 + 3 + 4)");
		test(parser, "INSERT INTO test (id, name) VALUES (-1, 2 * 3 + 4)");
		test(parser, "INSERT INTO test (id, name) VALUES (-1, 'hello ''xeraph''')");
		test(parser, "DELETE FROM test");
		test(parser, "DELETE FROM test WHERE 1 = 1");
		test(parser, "DELETE FROM test WHERE 1 = 'xeraph'");
		test(parser, "DELETE FROM test WHERE 1 = 1 OR 2 <> 3");
		test(parser, "DELETE FROM test WHERE 1 = 1 AND 2 <> 3 AND id = 1");
		test(parser, "DELETE FROM test WHERE id = 1");
		test(parser, "DELETE FROM test WHERE id IS NULL");
		test(parser, "DELETE FROM test WHERE id IS NOT NULL");
		test(parser, "DELETE FROM test WHERE test.id IS NOT NULL");
		test(parser, "DELETE FROM test WHERE test.\"number id\" IS NOT NULL");
		test(parser, "SELECT * FROM test");
		test(parser, "SELECT id, name FROM test");
		test(parser, "SELECT * FROM test WHERE id = 1");
		test(parser, "SELECT p.productid, p.name, s.inventory FROM products p INNER JOIN stock s ON p.productid = s.productid WHERE (p.productid = 1)");
		test(parser, "SELECT BridgeOrganizationStructure.ParentOrganizationCode, DimEmployeeOrganization.Emp_FName, DimEmployeeOrganization.Emp_LName, BridgeOrganizationStructure.SubsidiaryOrganizationCode, BridgeOrganizationStructure.LevelFromParent, FactAttempt.ActivityFK, FactAttempt.CurrentAttemptInd, FactAttempt.AttemptStartDt, DimActivity.Activity_PK FROM DimActivity INNER JOIN FactAttempt ON DimActivity.Activity_PK = FactAttempt.ActivityFK AND (DimActivity.Activity_PK = 55) AND (FactAttempt.CurrentAttemptInd = 1 ) RIGHT OUTER JOIN BridgeOrganizationStructure INNER JOIN DimEmployeeOrganization ON BridgeOrganizationStructure.SubsidiaryOrganizationID = DimEmployeeOrganization.EmpOrg_OrgFK ON FactAttempt.EmpJob_EmpFK = DimEmployeeOrganization.EmpOrg_EmpFK WHERE (BridgeOrganizationStructure.ParentOrganizationCode = '10525')");
		test(parser, "SELECT Merchandise.Description, Users.Name FROM Users INNER JOIN (Sales INNER JOIN (Merchandise INNER JOIN Types ON Merchandise.TypeID = Types.TypeID) ON Sales.MercID = Merchandise.MercID) ON Users.UserID = Sales.BuyerID WHERE Types.TypeID = 1");
		test(parser, "SELECT Sales FROM Store_Information WHERE Store_name IN ('Seoul', 'Incheon')");
		test(parser, "SELECT Sales FROM Store_Information WHERE Store_name IN (SELECT store_name FROM Geography WHERE region_name = 'West')");
		test(parser, "UPDATE test SET id = 1");
		test(parser, "UPDATE test SET name = '' WHERE name is null");
		
//		test(parser, "ALTER DOMAIN zipcode ADD CONSTRAINT zipchk CHECK (char_length = 5)");
//		test(parser, "COMMIT");
//		test(parser, "CONNECT TO :postgresql AS myconnection");
//		test(parser, "CREATE CHARACTER SET a.e.en_US.UTF8  AS GET en.UTF");
		
		// //TODO
		// test(parser, "create collation lal for  test from memem"); //TODO
		// test(parser, "CREATE DOMAIN SSN_TYPE AS CHAR(9)");
		// test(parser, "CLOSE my_curser");
		// test(parser, "commit work and chain");
		// test(parser, "connect to krakenapps.org as db_conn USER merius");
		// test(parser,
		// "create assertion first_constraint check ( abc < 2 ) not deferable");

	}

	private static void test(SqlParser parser, String sql) throws ParseException {
		Object result = parser.eval(sql);
		System.out.println("result: " + result);
	}
}
