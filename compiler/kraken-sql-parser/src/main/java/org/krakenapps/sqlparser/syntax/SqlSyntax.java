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
package org.krakenapps.sqlparser.syntax;

import static org.krakenapps.bnf.Syntax.*;

import org.krakenapps.bnf.Rule;
import org.krakenapps.bnf.SequenceRule;
import org.krakenapps.bnf.Syntax;
import org.krakenapps.sqlparser.parser.*;

public class SqlSyntax {
	private SqlSyntax() {
	}

	public static Syntax create() {
		Syntax s = new Syntax();

		// Root syntaxes
		s.addRoot("query_specification");
		s.addRoot("update_statement_searched");
		s.addRoot("insert_statement");
		s.addRoot("delete_statement_searched");
		s.addRoot("desc_table_statement");
		s.addRoot("table_definition");
		s.addRoot("drop_table_statement");
		s.addRoot("alter_table_statement");
		s.addRoot("show_tables");
		
		//merius - start
        //ALTER DOMAIN statement -- 1 test
        s.addRoot("alter_domain_statement");
        s.add("alter_domain_statement", new AlterDomainStatementParser(),
                k("ALTER"), k("DOMAIN"), ref("domain_name"), ref("alter_domain_action"));
        s.add("alter_domain_action", null, choice(
                ref("set_domain_default_clause"),
                ref("drop_domain_default_clause"),
                ref("add_domain_constraint_definition"),
                ref("drop_domain_constraint_definition")));
        s.add("set_domain_default_clause", new SetDomainDefaultClauseParser(), 
        		k("SET"), ref("default_clause"));
        s.add("drop_domain_default_clause", new DropDomainDefaultClauseParser(), 
        		k("DROP"), k("DEFAULT"));
        s.add("add_domain_constraint_definition", new AddDomainConstraintDefinitionParser(),
                k("ADD"), ref("domain_constraint") );
        s.add("drop_domain_constraint_definition", new DropDomainConstraintDefinitionParser(),
                k("DROP"), k("CONSTRAINT"), ref("constraint_name"));
        s.add("default_clause", new DefaultClauseParser(), 
        		k("DEFAULT"), ref("default_option"));
        s.add("default_option", new DefaultOptionParser(), choice(
                ref("literal"),
                ref("datetime_value_function"),
                k("USER"),
                k("CURRENT_USER"),
                k("CURRENT_ROLE"),
                k("SESSION_USER"),
                k("SYSTEN_USER"),
                k("CURRENT_PATH"),
                ref("implicitly_typed_value_specification")));
        s.add("datetime_value_function", new DatetimeValueFunctionParser(), choice(
                ref("current_date_value_function"),
                ref("current_time_value_function"),
                ref("current_timestamp_value_function"),
                ref("current_local_time_value_function"),
                ref("current_local_timestamp_value_function")));
        s.add("current_date_value_function", null, k("CURRENT_DATE"));
        s.add("current_time_value_function", new CurrentTimeValueFunctionParser(),
        		k("CURRENT_TIME"), option( rule( k("("), ref("time_precision"), k(")"))));
        s.add("current_timestamp_value_function", new CurrentTimestampValueFunctionParser(), 
        		k("CURRENT_TIMESTAMP"), option( rule( k("("), ref("time_precision"), k(")"))));
        s.add("current_local_time_value_function", new CurrentLocalTimeValueFunctionParser(),
        		k("LOCALTIME"), option( rule( k("("), ref("time_precision"), k(")"))));
        s.add("current_local_timestamp_value_function", new CurrentLocalTimestampValueFunctionParser(), 
        		k("LOCALTIMESTAMP"), option( rule( k("("), ref("time_precision"), k(")"))));
        s.add("time_precison", null, ref("time_fractional_seconds_precision"));
        s.add("time_fractional_seconds_precision", null, ref("unsigned_integer"));
        s.add("domain_name", null, ref("schema_qualified_name"));
        s.add("domain_constraint", new DomainConstraintParser(), 
        		option(ref("constraint_name_definition")),
        		ref("check_constraint_definition"),
        		option(ref("constraint_characteristics")));

        //CLOSE statement
        s.addRoot("close_statement"); 
        s.add("close_statement", new CloseStatementParser(), k("CLOSE"), ref("cursor_name"));
        s.add("cursor_name", null, ref("identifier"));
        
        s.add("local_qualified_name", new LocalQualifiedNameParser(), 
        		option( rule( ref("local_qualifier"), k("."))), ref("qualifier_identifier"));
        s.add("local_qualifier", null, k("MODULE"));
        s.add("qualifier_identifier", null, k("identifier"));
        s.add("actual_identifier", new ActualIdentifierParser(), 
        		choice( ref("regular_identifier"), ref("delimited_identifier")));
        s.add("regular_identifier", null, ref("identifier_body"));
        s.add("identifier_body", new IdentifierBodyParser(), 
        		ref("identifier"), option(repeat(ref("identifier_part"))));
        s.add("identifier_part", null, choice(
                ref("alphabetic_character"),
                ref("ideographic_character"),
                ref("decimal_digit_character"),
                ref("identifier_combining_character"),
                k("_"),
                ref("alternate_underscore"),
                ref("extender_character"),
                ref("identifier_ignorable_character"),
                ref("connector_character")
                ));

        s.add("identifier_start", new IdentifierStartParser(), 
        		choice( ref("initial_alphabetic"), ref("ideographic_character")));
        //s.add("initial_alphabetic_character",/**/,/**/); //TODO - initial_alphabetic_character���먯떇��
        //s.add("ideographic_character",/**/,/**/); //TODO - ideographic_character���먯떇��
        s.add("delimited_identifier", new DelimitedIdentifierParser(), 
        		k("\""), doubleQuotedString(), k("\""));
        //s.add("delimited_identifier_body", /**/,/**/); //TODO
        
        //COMMIT statement
        s.addRoot("commit_statement");
        s.add( "commit_statement", new CommitStatementParser(), 
        		k("COMMIT"), option( k("WORK")), option( rule( k("AND"), option(k("NO")), k("CHAIN"))));

        //CONNECT statement
        s.addRoot("connect_statement");
        s.add("connect_statement", new ConnectStatementParser(), 
        		k("CONNECT"), k("TO"), option( ref("connection_target")));
        s.add("connection_target", new ConnectionTargetParser(), choice( 
        		rule( ref("sql_server_name"), option( rule( k("AS"), ref("connection_name"))), option(rule(k("USER"), ref("connection_user_name")))),
        		k("DEFAULT"))); //TODO
        s.add("connection_user_name", null, ref("simple_value_specification"));
        s.add("sql_server_name", null, ref("simple_value_specification"));
        s.add("simple_value_specification", new SimpleValueSpecificationParser(), choice( 
        		ref("literal"), 
        		ref("host_parameter_name"), 
        		ref("sql_parameter_reference"), 
        		ref("sql_variable_reference"), 
        		ref("embedded_variable_name")));
        s.add("signed_numeric_literal", new SignedNumericLiteralParser(), 
        		option( k("+"), k("-")), ref("unsigned_numeric_literal"));
        s.add("unsigned_numeric_literal", new UnsignedNumericLiteralParser(), 
        		choice( ref("exact_numeric_literal"), ref("approximate_numeric_literal")));
        s.add("exact_numeric_literal", new ExactNumericLiteralParser(), choice( 
        		rule( ref("unsigned_integer"), option( rule( k("."), option( ref("unsigned_integer"))))), 
        		rule( k("."), ref("unsigned_integer"))));
        s.add("apprixmate_numeric_literal", new ApproximateNumericLiteralParser(), 
        		ref("mantissa"), k("E"), ref("exponent"));
        s.add("mantissa", null, ref("exact_numeric_literal"));
        s.add("exponent", null, ref("signed_integer"));
        s.add("signed_integer", new SignedIntegerParser(), 
        		option( choice(k("+"), k("-"))), ref("unsigned_integer"));
        s.add("host_parameter_name", new HostParameterNameParser(), 
        		k(":"), ref("identifier")); 
        s.add("sql_parameter_reference", null, ref("basic_identifier_chain")); 
        s.add("sql_variable_reference", null, ref("basic_identifier_chains"));
        s.add("embedded_variable_name", new EmbeddedVariableNameParser(), 
        		k(":"), ref("host_identifier"));
        s.add("host_identifier", new HostIdentifierParser(), choice( 
        		ref("ada_host_identifier"), 
        		ref("c_host_identifier"), 
        		ref("cobol_host_identifier"), 
        		ref("fortan_host_identifier"), 
        		ref("mumps_host_identifier"), 
        		ref("pascal_host_identifier"), 
        		ref("pl_l_host_identifier")));
        s.add("connection_name", null, 
        		ref("simple_value_specification")); 
        //s.add("ada_host_identifier"); //TODO
        //s.add("cobol_host_identifier"); //TODO
        //s.add("fortan_host_identifier"); //TODO
        //s.add("mumps_host_identifier"); //TODO
        //s.add("pascal_host_identifier"); //TODO
        //s.add("pl_l_host_identifier"); //TODO

        // CREATE ASSERTION statement
        s.addRoot("assertion_definition");
        s.add("assertion_definition", new AssertionStatementParser(), 
        		k("CREATE"), k("ASSERTION"), ref("constraint_name"), k("CHECK"), k("("), ref("search_condition"), k(")"), option( ref("constraint_characteristics")) );
        s.add("constraint_characteristics", new ConstraintCharacteristicsParser(), choice(
                rule( ref("constraint_check_time"), option( rule( option(k("NOT")), k("DEFERABLE")))),
                rule( option(k("NOT")), k("DEFERABLE"), option( ref("constraint_check_time")))));
        s.add("constraint_check_time", new ConstraintCheckTimeParser(), choice( 
        		rule(k("INITIALLY"), k("DEFERRED")), 
        		rule( k("INITIALLY"), k("IMMEDIATE"))));

        // CREATE CHARACTER SET statement
        s.addRoot("character_set_definition");
        s.add("character_set_definition", new CharacterSetStatementParser(),
        		k("CREATE"), k("CHARACTER"), k("SET"), 
        		ref("character_set_name"), 
        		option( k("AS")), 
        		ref("character_set_source"), 
        		option( ref("collate_clause")));
        s.add("character_set_name", new CharacterSetNameParser(), 
        		option( rule( ref("schema_name"), k("."))), ref("sql_language_identifer"));
        s.add("schema_name", new SchemaNameParser(), 
        		option( rule( ref("catalog_name"), k("."))), ref("unqualified_schema_name"));
        s.add("catalog_name", null, ref("identifier"));
        s.add("unqualified_schema_name", null, ref("identifier"));
        s.add("sql_language_identifier", new SQLLanguageIdentifierParser(),
        		ref("sql_language_identifier_start"), 
        		option( repeat(choice(k("_"), ref("sql_language_identifier_part")))));
        s.add("sql_language_identifier_start", null, ref("simple_latin_letter"));
        s.add("sql_language_identifier_part", null, 
        		choice(ref("simple_latin_letter"), ref("digit")));
        s.add("character_set_source", new CharacterSetSourceParser(), 
        		k("GET"), ref("character_set_specification"));
        s.add("collate_clause", new CollateClauseParser(), k("COLLATE"), ref("collation_name"));
        s.add("collate_name", new CollateNameParser(), ref("schema_qualified_name"));
        s.add("character_set_specification", null, choice(
        		ref("standard_character_set_name"),
        		ref("implementation_defined_character_set_name"),
        		ref("user_defined_character_set_name")
        		));
        s.add("standard_character_set_name",null, ref("character_set_name"));
        s.add("implementation_defined_character_set_name", null, ref("character_set_name"));
        s.add("user_definded_character_set_name", null, ref("character_set_name"));
        
        

        // CREATE COLLATION statement
        s.addRoot("collation_definition");
        s.add("collation_definition", new CollationStatementParser(), k("CREATE"), k("COLLATION"), ref("collation_name"), k("FOR"),
                ref("character_set_specification"), k("FROM"), ref("existing_collation_name"), option(ref("pad_characteristic")));
        s.add("collation_name", null, ref("schema_qualified_name"));
        s.add("existing_collation_name", null, ref("collation_name"));
        s.add("pad_characteristic", new PadCharacteristicParser(), choice( 
        		rule( k("NO"), k("PAD")), 
        		rule( k("PAD"), k("SPACE"))
        		));

        // CREATE DOMAIN statement
        s.addRoot("domain_definition");
        s.add("domain_definition", new DomainStatementParser(), 
        		k("CREATE"), k("DOMAIN"), ref("domain_name"), option(k("AS")),
                ref("data_type"), 
                option( ref("default_clause")), 
                option(repeat(ref("domain_constraint"))), 
                option(ref("collate_clause")));
        s.add("domain_name", null, ref("schema_qualified_name"));
        s.add("domain_constraint", new DomainConstraintParser(), 
        		option(ref("constraint_name_definition")),
        		ref("check_constraint_definition"),
        		option(ref("constraint_characteristics")));
        s.add("constraint_name_definition", new ConstraintNameDefinitionParser(), 
        		k("CONSTRAINT"), ref("constraint_name"));
	
        // CREATE FUNCTION statement
        s.addRoot("schema_definition");
        s.add("schema_function", null, k("CREATE"), ref("sql_invoked_function"));
        s.add("sql_invoked_function", new SQLInvokedFunctionParser(), choice( 
        		ref("function_specification"), 
        		ref("method_specification_designer")),
                ref("routine_body"));
        
        s.add("function_specification", new FunctionSpecificationParser(),
                k("FUNCTION"), ref("schema_qualified_routine_name"), ref("sql_parameter_declaration_list"),
                ref("returns_clause"), ref("routine_characteristics"), option(ref("dispatch_clause")));
        
        s.add("schema_qualified_routine_name", null, ref("schema_qualified_name"));
        
        s.add("sql_parameter_declaration_list", new SQLParameterDeclarationListParser(),
                k("("), ref("sql_parameter_declaration"), option(repeat(rule(k(","), ref("sql_parameter_declaration")))), k(")"));
        s.add("sql_parameter_declaration", new SQLParameterDeclarationParser(), 
        		option(ref("parameter_mode")), option(ref("sql_parameter_name")), ref("parameter_type"), option(k("RESULT")));
        s.add("parameter_mode", null, choice(
        		k("IN"), k("OUT"), k("INOUT")));
        s.add("sql_parameter_name", null, ref("identifier"));
        s.add("parameter_type", new ParameterTypeParser(), ref("data_type"), option(ref("locator_indication")));
        s.add("returns_clause", new ReturnsClauseParser(),
                k("RETURNS"), ref("returns_data_type"), option( ref("result_cast")));
        s.add("returns_data_type", new ReturnsDataTypeParser(),
                ref("data_type"), option( ref("locator_indication")));
        s.add("locator_indication", new LocatorIndicationParser(), k("AS"), k("LOCATOR"));
        s.add("routine_characteristics", new RoutineCharacteristicsParser(), 
        		option(repeat(ref("routine_characteristic")))); 
        s.add("routine_characteristic", new RoutineCharacteristicParser(),choice(
        		ref("language_clause"),
        		ref("parameter_style_clause"),
        		rule(k("SPECIFIC"), ref("specific_name")),
        		ref("deterministic_characteristic"),
        		ref("sql_data_access_indication"),
        		ref("null_call_clause"),
        		ref("dynamic_result_sets_characterisitic")
        		));
        s.add("dynamic_result_sets_characterisitic", new DynamicResultSetsCharacteristicParser(),
        		k("DYNAMIC"), k("RESULT"), k("SETS"), ref("maximum_dynamic_result_sets"));
        s.add("maximum_dynamic_result_sets", null, ref("unsigned_integer"));
        s.add("null_call_clause", new NullCallClauseParser(), choice(
        		rule(k("RETURNS"), k("NULL"), k("ON"), k("NULL"), k("INPUT")),
        		rule(k("CALLED"), k("ON"), k("NULL"), k("INPUT"))
        		));
        s.add("sql_data_access_indication", new SQLDataAccessIndicationParser(), choice(
        		rule(k("NO"), k("SQL")),
        		rule(k("CONTAINS"), k("SQL")),
        		rule(k("READS"), k("SQL"), k("DATA")),
        		rule(k("DOMIFIES"), k("SQL"), k("DATA"))
        		));
        s.add("deterministic_characteristic", new DeterministicCharacteristicParser(), choice(
        		k("DETERMINISTIC"),
        		rule(k("NOT"), k("DETERMINISTIC"))));
        s.add("specific_name", null, ref("schema_qualified_name"));
        s.add("dispatch_clause", new DispatchClauseParser(), k("STATIC"), k("DISPATCH"));
        s.add("routine_body", null, choice( ref("sql_routine_body"), ref("external_body_reference")));
        s.add("sql_routine_body", null, ref("sql_procedure_statement"));
        s.add("sql_procedure_statement", null, ref("sql_executable_statement"));
        s.add("sql_executable_statement", null, choice(
                ref("sql_schema_statement"),
                ref("sql_data_statement"),
                ref("sql_control_statement"),
                ref("sql_transaction_statement"),
                ref("sql_connection_statement"),
                ref("sql_session_statement"),
                ref("sql_diagnostics_statement"),
                ref("sql_dynamic_statement")
                ));
        s.add("sql_schema_statement", null, choice(
                ref("sql_schema_definition_statement"),
                ref("sql_schema_manipulation_statement")));
        s.add("sql_schema_definition_statement", null, choice(
                ref("schema_definition"),
                ref("table_definition"),
                ref("view_definition"),
                ref("sql_invoked_routine"),
                ref("grant_statement"),
                ref("role_definition"),
                ref("domain_definition"),
                ref("character_set_definition"),
                ref("collation_definition"),
                ref("translation_definition"),
                ref("assertion_definition"),
                ref("trigger_definition"),
                ref("user_defined_type_definition"),
                ref("user_defined_cast_definition"),
                ref("transform_definition"),
                ref("sql_server_module_definition")));
        s.add("sql_schema_manipulation_statement", null, choice(
                ref("drop_schema_statement"),
                ref("alter_table_statement"),
                ref("drop_table_statement"),
                ref("drop_view_statement"),
                ref("alter_routine_statement"),
                ref("drop_routine_statement"),
                ref("drop_user_defined_cast_statement"),
                ref("revoke_statement"),
                ref("drop_role_statement"),
                ref("alter_domain_statement"),
                ref("drop_domain_statement"),
                ref("drop_character_set_statement"),
                ref("drop_collation_statement"),
                ref("drop_translation_statement"),
                ref("drop_assertion_statement"),
                ref("drop_trigger_statement"),
                ref("alter_type_statement"),
                ref("drop_data_type_statement"),
                ref("drop_user_defined_ordering_statement"),
                ref("drop_transform_statement"),
                ref("drop_module_statement")));
        s.add("sql_data_statement", null, choice(
                ref("open_statement"),
                ref("fetch_statement"),
                ref("close_statement"),
                ref("select_statement_single_row"),
                ref("free_locator_statement"),
                ref("hold_locator_statement"),
                ref("sql_data_change_statement")));
        s.add("select_statement_single_row", new SelectStatementSingleRowParser(),
                k("SELECT"), option( ref("set_qualifier")), ref("select_list"), k("INTO"),
                ref("select_target_list"), ref("table_expression") );
        s.add("set_qualifer", null, choice( k("DISTINCT"), k("ALL")));
        s.add("select_target_list", new SelectTargetListParser(), 
        		ref("target_specification"), option(repeat(rule(k(","), ref("target_specification")))));
        s.add("sql_control_statement", null, choice(
                ref("return_statement"),
                ref("assignment_statement"),
                ref("compound_statement"),
                ref("case_statement"),
                ref("if_statement"),
                ref("iterate_statement"),
                ref("leave_statement"),
                ref("loop_statement"),
                ref("while_statement"),
                ref("repeat_statement"),
                ref("for_statement")));
        s.add("sql_transaction_statement", null, choice(
                ref("start_transaction_statement"),
                ref("set_transaction_statement"),
                ref("set_constraints_mode_statement"),
                ref("savepoint_statement"),
                ref("release_savepoint_statement"),
                ref("commit_statement"),
                ref("rollback_statement")));
        s.add("sql_connection_statement", null, choice(
                ref("connection_statement"),
                ref("set_connection_statement"),
                ref("disconnection_statement")));
        s.add("sql_session_statement", null, choice(
                ref("set_session_user_identifier_statement"),
                ref("set_role_statement"),
                ref("set_local_time_zone_statement"),
                ref("set_session_characteristics_statement"),
                ref("set_catalog_statement"),
                ref("set_schema_statement"),
                ref("set_names_statement"),
                ref("set_path_statement"),
                ref("set_transform_group_statement")));
        s.add("sql_diagnostics_statement", null, choice(
                ref("get_diagnostics_statement"),
                ref("signal_statement"),
                ref("resignal_statement")));
        s.add("sql_dynamic_statement", null, choice(
                ref("system descriptor statement"),
                ref("prepare statement"),
                ref("deallocate prepared statement"),
                ref("describe statement"),
                ref("execute statement"),
                ref("execute immediate statement"),
                ref("SQL dynamic data statement")));
        s.add("external_body_reference", new ExternalBodyReferenceParser(),
                k("EXTERNAL"),
                option( rule( k("NAME"), ref("external_routine_name"))),
                option( ref("parameter_style_clause")),
                option( ref("transform_group_specification")),
                option( ref("external_security_clause")));
        s.add("external_routine_name", null, choice(
                ref("identifer"),
                ref("character_string_literal")));
        s.add("parameter_style_clause", new ParameterStyleClauseParser(),
                k("PARAMETER"), k("STYLE"), ref("parameter_style"));
        s.add("parameter_style", null, choice(
                k("SQL"), k("GENERAL")));
        s.add("transform_group_specification", new TransformGroupSpecificationParser(),
                k("TRANSFORM"), k("GROUP"), option(
                        choice( ref("single_group_specification"), ref("multiple_group_specification"))));
        s.add("single_group_specification", null, ref("group_name"));
        s.add("group_name", null, ref("identifier"));
        s.add("multiple_group_specification", new MultipleGroupSpecificationParser(), 
        		ref("group_specification"), option(repeat(rule(k(","), ref("group_specification"))))); //TODO
        s.add("external_security_clause", null, choice(
                rule( k("EXTERNAL"), k("SECURITY"), k("DEFINER")),
                rule( k("EXTERNAL"), k("SECURITY"), k("INVOKER")),
                rule( k("EXTERNAL"), k("SECURITY"), k("IMPLEMENTATION"), k("DEFINED"))));

        // CREATE PROCEDURE statement
        s.addRoot("schema_procedure");
        s.add("schema_procedure", new SchemaProcedureParser(), k("CREATE"), ref("sql_invoked_procedure"));
        s.add("sql_invoked_procedure", new SQLInvokedProcedureParser(),
                k("PROCEDURE"), ref("schema_qualified_routine_name"), ref("sql_parameter_declaration_list"),
                ref("routine_characteristics"), ref("routine_body"));

        // CREATE SCHEMA statement
        s.addRoot("schema_definition");
        s.add("schema_definition", new SchemaDefinitionParser(),
                k("CREATE"), k("SCHEMA"), ref("schema_name_clause"), option( ref("schema_character_set_or_path")), 
                repeat(ref("schema_element"))); //TODO
        s.add("schema_character_set_or_path", null, choice(
                ref("schema_name"),
                rule( k("AUTHORIZATION"), ref("schema_authorization_identifier")),
                rule( ref("schema_name"), k("AUTHORIZATION"), ref("schema_authorization_identifier"))));
        s.add("schema_authorization_identifier", null, ref("authorization_identifer"));
        s.add("authorization_identifier", null, choice(
                ref("role_name"),
                ref("user_identifier")));
        s.add("role_name", null, ref("identifier"));
        s.add("user_identifier", null, ref("identifier"));
        s.add("schema_element", null, choice(
        		ref("table_definition"),
        		ref("view_definition"),
        		ref("domain_definition"),
        		ref("character_set_definition"),
        		ref("collation_definition"),
        		ref("translation_definition"),
        		ref("assertion_definition"),
        		ref("trigger_definition"),
        		ref("user-defined_type_definition"),
        		ref("schema_routine"),
        		ref("grant_statement"),
        		ref("role_definition"),
        		ref("user_defined_cast_definition"),
        		ref("user_defined_ordering_definition"),
        		ref("transform_definition")
        		));

        //CREATE TRANSLATION statement
        s.addRoot("translation_definition");
        s.add("translation_definition", new TranslationDefinitionParser(),
                k("CREATE"), k("TRANSLATION"), ref("translation_name"), k("FOR"),
                ref("source_character_set_specification"), k("TO"), ref("target_character_set_specification"),
                k("FROM"), ref("translation_source"));
        s.add("translation_name", null, ref("schema_qualified_name"));
        s.add("source_character_set_specification", null, ref("character_set_specification"));
        s.add("target_character_set_specification", null, ref("character_set_specification"));
        s.add("translation_source", null, choice(
                ref("existing_translation_name"),
                ref("translation_routine")));
        s.add("existing_translation_name", null, ref("translation_name"));
        s.add("translation_routine", null, ref("specific_routine_designator"));
        s.add("specific_routine_designator", null, choice(
                rule( k("SPECIFIC"), ref("routine_type"), ref("specific_name")),
                rule( ref("routine_type"), ref("member_name"), option(rule(k("FOR"), ref("user_defined_type_name"))))));

        s.add("routine_type", null, choice(
                k("ROUTINE"),
                k("FUNCTION"),
                k("PROCEDURE"),
                rule( option( choice(
                        k("INSTANCE"), k("STATIC"), k("CONSTRUCTOR"))), k("METHOD"))));
        s.add("member_name", new MemberNameParser(), 
        		ref("schema_qualified_routine_name"), option( ref("data_type_list")));
        s.add("data_type_list", new DataTypeListParser(),
                k("("), ref("data_type_list_inner"), k(")")); //TODO
        s.add("data_type_list_inner", null, 
        		choice(ref("data_type_part"), ref("data_type")));
        s.add("data_type_part", null, ref("data_type"), k(","), ref("data_type_list_inner"));
        
        s.add("user_defined_type_name", null, ref("schema_qualified_type_name"));
        s.add("schema_qualified_type_name", new SchemaQualifiedTypeNameParser(), 
        		option( rule( ref("schema_name"), k("."))), ref("qualified_identifier"));

        // CREATE TRIGGER statement
        s.addRoot("trigger_definition");
        s.add("trigger_definition", new TriggerDefinitionParser(),
                k("CREATE"), k("TRIGGER"), ref("trigger_name"), ref("trigger_action_time"), ref("trigger_event"), k("ON"), ref("table_name"),
                option( rule(k("REFERENCING"), ref("old_or_new_values_alias_list"))), ref("triggered_action"));
        s.add("trigger_name", null, ref("schema_qualified_name"));
        s.add("trigger_action_time", null, choice(
                k("BEFORE"),
                k("AFTER")));
        s.add("trigger_event", null, choice(
                k("INSERT"),
                k("DELETE"),
                rule(k("UPDATE"), option( k("OF"), ref("trigger_column_list")))));
        s.add("trigger_column_list", null, ref("column_name_list"));
        s.add("old_or_new_values_alias_list", null, repeat(ref("old_or_new_values_alias"))); //TODO
        s.add("old_or_new_values_alias", new OldOrNewValuesAliasParser(), choice(
        		rule( k("OLD"), option(k("ROW")), option(k("AS")), ref("old_values_correlation_name")),
        		rule( k("NEW"), option(k("ROW")), option(k("AS")), ref("new_values_correlation_name")),
        		rule( k("OLD"), k("ROW"), option(k("AS")), ref("old_values_table_alias")),
        		rule( k("NEW"), k("ROW"), option(k("AS")), ref("new_values_table_alias"))
        		));
        s.add("old_values_correlation_name", null, ref("correlation_name"));
        s.add("new_values_correlation_name", null, ref("correlation_name"));
        s.add("old_values_table_alias", null, ref("identifier"));
        s.add("new_values_table_alais", null, ref("identifier"));
        
        s.add("triggered_action", new TriggeredActionParser(),
                option( rule( k("FOR"), k("EACH"), choice( k("ROW"), k("STATEMENT")))),
                option( rule( k("WHEN"), k("("), ref("search_condition"), k(")"))),
                ref("triggered_sql_statement"));
        s.add("triggered_sql_statement", new TriggeredSQLStatementParser(), choice(
                ref("sql_procedure_statement"),
                rule( k("BEGIN"), k("ATOMIC"), repeat(
                		rule(ref("sql_procedure_statement"), k(";"))
                		), k("END"))));//TODO

        // CREATE VIEW statement
        s.addRoot("view_definition");
        s.add("view_definition", new ViewDefinitionParser(),
                k("CREATE"), option(k("RECURSIVE")), k("VIEW"), ref("table_name"), ref("view_specification"), k("KEY"),
                ref("query_expression"), option( rule( k("WITH"), option(ref("levels_clause")), k("CHECK"), k("OPTION"))));
        s.add("view_specification", null, choice(
                ref("regular_view_definition"),
                ref("reference_view_specification")));
        s.add("regular_view_definition", null, option( rule( k("("), ref("view_column_list"), k(")"))));
        s.add("view_column_list", null, ref("column_name_list"));
        s.add("reference_view_specification", new ReferenceViewSpecificationParser(),
                k("OF"), ref("user_defined_type"), option(ref("subview_clause")), option(ref("view_element_list")));
        s.add("user_defined_type", null, ref("user_defined_type_name"));
        s.add("subview_clause", new SubviewClauseParser(), k("UNDER"), ref("table_name"));
        s.add("view_element_list", new ViewElementListParser(),
                k("("), option(rule(ref("self_referencing_column_specification"), k(","))), ref("view_element") /*TODO*/
                ); //TODO
        s.add("view_element", null, ref("view_column_option"));
        s.add("view_column_option", new ViewColumnOptionParser(), ref("column_name"), k("WITH"), k("OPTIONS"), ref("scope_clause"));
        s.add("scope_clause", new ScopeClauseParser(), k("SCOPE"), ref("table_name"));
        s.add("levels_clause", null, choice(
                k("CASCADED"),
                k("LOCAL")));

        // DEALLOCATE PREPARE statement
        s.addRoot("deallocate_prepared_statement");
        s.add("deallocate_prepared_statement", new DeallocatePreparedStatementParser(),
                k("DEALLOCATE"), k("PREPARED"), ref("sql_statement_name"));
        s.add("sql_statement_name", null, choice(
                ref("statement_name"),
                ref("extended_statement_name")));
        s.add("statement_name", null, ref("identifier"));
        s.add("extended_statement_name", new ExtendedStatementNameParser(), option(ref("scope_option")), ref("simple_value_specification"));
        s.add("scope_option", null, choice(k("GLOBAL"), k("LOCAL")));

        // DECLARE CURSOR statement : type A
        //TODO

        // DECLARE LOCAL TEMPORARY statement
        s.addRoot("temporary_table_declaration");
        s.add("temporary_table_declaration", new TemporaryTableDeclarationParser(),
                k("DECLARE"), k("LOCAL"), k("TEMPORARY"), k("TABLE"), ref("table_name"),
                ref("table_element_list"), option( rule(k("ON"), k("COMMIT"), ref("table_commit_action"), k("ROWS"))));
        s.add("table_commit_action", null, choice(
                k("PRESERVE"),
                k("DELETE")));

        // DELETE statement positioned
        s.addRoot("delete_statement_positioned");
        s.add("delete_statement_positioned", new DeleteStatementPositionedParser(),
                k("DELETE"), k("FROM"), ref("table_name"), k("WHERE"), k("CURRENT"), k("OF"), ref("cursor_name"));

        // DELTE statement - dynamic delete statement positioned
        s.addRoot("dynamic_delete_statement_positioned");
        s.add("dynamic_delete_statement_positioned", new DynamicDeleteStatementPositionedParser(),
                k("DELETE"), k("FROM"), ref("table_name"), k("WHERE"), k("CURRENT"), k("OF"), ref("dynamic_cursor_name"));
        s.add("dynamic_cursor_name", null, choice(
                ref("cursor_name"),
                ref("extended_cursor_name")));
        s.add("extended_cursor_name", new ExtendedCursorNameParser(), option( ref("scope_option")), ref("simple_value_specification"));

        // DESCRIBE statement
        s.addRoot("describe_statement");
        s.add("describe_statement", null, choice(
                ref("describe_input_statement"),
                ref("describe_output_statement")));
        s.add("describe_input_statement", new DescribeInputStatementParser(),
                k("DESCRIBE"), k("INPUT"), ref("sql_statement_name"), ref("using_descriptor"), option( ref("nesting_option")));
        s.add("using_descriptor", new UsingDescriptorParser(),
                k("USING"), option( k("SQL")), k("DESCRIPTOR"), ref("descriptor_name"));
        s.add("descriptor_name",new DescriptorNameParser(), option( ref("scope_option")), ref("simple_value_specification"));
        s.add("nesting_option", new NestingOptionParser(), choice(
                rule(k("WITH"), k("NESTING")),
                rule(k("WITHOUT"), k("NESTING"))));
        s.add("describe_output_statement", new DescribeOutputStatementParser(),
                k("DESCRIBE"), option(k("OUTPUT")), ref("described_object"), ref("using_descriptor"), option(ref("nesting_option")));
        s.add("decribed_object", new DescribedObjectParser(), choice(
                ref("sql_statement_name"),
                rule( k("CURSOR"), ref("extended_cursor_name"), k("STRUCTURE"))));


        // DESCRIPTOR statement
        s.addRoot("system_descriptor_statement");
        s.add("system_descriptor_statement", null, choice(
                ref("allocate_descriptor_statement"),
                ref("deallocate_descriptor_statement"),
                ref("set_descriptor_statement"),
                ref("get_descriptor_statement")
                ));
        s.add("allocate_descriptor_statement", new AllocateDescriptorStatementParser(),
                k("ALLOCATE"), option(k("SQL")), k("DESCRIPTOR"), ref("descriptor_name"), option( rule( k("WITH"), k("MAX"), ref("occurrences")))
        );
        s.add("occurrences", null, ref("simple_value_specification"));
        s.add("deallocate_descriptor_statement", new DeallocateDescriptorStatementParser(),
                k("DEALLOCATE"), option(k("SQL"), k("DESCRIPTOR"), ref("descriptor_name")));
        s.add("set_descriptor_statement", new SetDescriptorStatementParser(),
                k("SET"), option(k("SQL")), k("DESCRIPTOR"), ref("descriptor_name"), ref("set_descrptor_information"));
        
        s.add("set_descriptor_information", new SetDescriptorInformationParser(), choice(
        		ref("set_header_information_list"),
        		rule(k("VALUE"), ref("item_number"), ref("set_item_information_list"))
        		));//TODO
        s.add("set_item_information_list", null, 
        		ref("set_item_information_list_inner"));
        s.add("set_item_information_list_inner", null, choice(
        		ref("set_item_information_list_part"),
        		ref("set_item_information")
        		));
        s.add("set_item_information_part", null, 
        		ref("set_item_information"), k(","), ref("set_item_information_list_inner"));
        s.add("set_item_information", null, 
        		ref("descriptor_item_name"),
        		k("="),
        		ref("simple_value_specification_2"));
        s.add("simple_value_specification_2", null, ref("simple_value_specification"));
        

        s.add("get_descriptor_statement", new GetDescriptorStatementParser(),
                k("GET"), option(k("SQL")), k("DESCRIPTOR"), ref("descriptor_name"), ref("get_descriptor_information")
        );
        s.add("get_descriptor_information", new GetDescriptorInformationParser(), choice(
        		ref("get_header_information_list"),
        		rule(k("VALUE"), ref("item_number"), ref("get_item_information_list"))
        		)); //TODO
        s.add("get_item_information_list", null, 
        		ref("get_item_information_list_inner"));
        s.add("get_item_information_list_inner", null, choice(
        		ref("get_item_information_list_part"),
        		ref("get_item_information")
        		));
        s.add("get_item_information_part", null, 
        		ref("get_item_information"), k(","), ref("get_item_information_list_inner"));
        s.add("get_item_information", null,
        		ref("simple_target_specification_1"), k("="), ref("header_item_name")
        		);
        s.add("simple_target_specification_1", null, ref("simple_target_specification"));
        s.add("header_item_name", null, choice(
        		k("COUNT"),
        		k("KEY_TYPE"),
        		k("DYNAMIC_FUNCTION"),
        		k("DUNAMIC_FUNCTION_CODE"),
        		k("TOP_LEVEL_COUNT")
        		));
        s.add("descriptor_item_name", null, choice(
                k("CARDINALITY"),
                k("CHARACTER_SET_CATALOG"),
                k("CHARACTER_SET_NAME"),
                k("CHARACTER_SET_SCHEMA"),
                k("COLLATION_CATALOG"),
                k("COLLATION_NAME"),
                k("COLLATION_SCHEMA"),
                k("DATA"),
                k("DATETIME_INTERVAL_CODE"),
                k("DATETIME_INTERVAL_PRECISION"),
                k("DEGREE"),
                k("INDICATOR"),
                k("KEY_MEMBER"),
                k("LENGTH"),
                k("LEVEL"),
                k("NAME"),
                k("NULLABLE"),
                k("OCTET_LENGTH"),
                k("PARAMETER_MODE"),
                k("PARAMETER_ORDINAL_POSITION"),
                k("PARAMETER_SPECIFIC_CATALOG"),
                k("PARAMETER_SPECIFIC_NAME"),
                k("PARAMETER_SPECIFIC_SCHEMA"),
                k("PRECISION"),
                k("RETURNED_CARDINALITY"),
                k("RETURNED_LENGTH"),
                k("RETURNED_OCTET_LENGTH"),
                k("SCALE"),
                k("SCOPE_CATALOG"),
                k("SCOPE_NAME"),
                k("SCOPE_SCHEMA"),
                k("TYPE"),
                k("UNNAMED"),
                k("USER_DEFINED_TYPE_CATALOG"),
                k("USER_DEFINED_TYPE_NAME"),
                k("USER_DEFINED_TYPE_SCHEMA")));



        // DISCOUNNECT statement
        s.addRoot("disconnect_statement");
        s.add("disconnect_statement", new DisconnectStatementParser(), k("DISCONNECT"), ref("disconnect_object"));
        s.add("disconnect_object", new DisconnectObjectParser(), choice(
                ref("connection_object"),
                k("ALL"),
                k("CURRENT")
                ));
        s.add("connection_object", null, choice(k("OBJECT"), ref("connection_name")));
        s.add("connection_name", null, ref("simple_value_specification"));

        //EXECUTE STATEMENT
        s.addRoot("execute_statement");
        s.add("execute_statement", new ExecuteStatementParser(),
                k("EXECUTE"), ref("sql_statement_name"), option(ref("result_using_clause")), option(ref("parameter_using_clause")));
        s.add("result_using_clause", null, ref("output_using_clause"));
        s.add("output_using_clause", null, choice( ref("into_arguments"), ref("into_descriptor")));
        s.add("into_arguments", new IntoArgumentsParser(),
                k("INTO"), ref("into_argument_list"));//TODO
        s.add("into_argument_list", null, choice(ref("into_argument_part"), ref("into_argument")));
        s.add("into_argument_part", null, ref("into_argument"), k(","), ref("into_argument_list"));
        s.add("into_argument", null, ref("target_specification"));
        
        s.add("into_descriptor", new IntoDescriptorParser(),
                k("INTO"), option(k("SQL")), k("DESCIRPTOR"), ref("descriptor_name"));
        s.add("parameter_using_clause", null, ref("input_using_clause"));
        
        //EXECUTE IMMEDIATE
        s.addRoot("execute_immediate_statement");
        s.add("execute_immediate_statement", new ExecuteImmediateStatementParser(),
                k("EXECUTE"), k("IMMEDIATE"), ref("sql_statement_variable"));
        s.add("sql_statement_variable", null, ref("simple_value_specification"));

        // FETCH statement
        s.addRoot("fetch_statement");
        s.add("fetch_statement", new FetchStatementParser(),
                k("FETCH"), option( rule(option(ref("fetch_orientation")),k("FROM"))), ref("cursor_name"), k("INTO"), ref("fetch_target_list") );
        s.add("fetch_orientation", new FetchOrientationParser(), choice(
                k("NEXT"),
                k("PRIOR"),
                k("FIRST"),
                k("LAST"),
                rule( rule(choice(k("ABSOLUTE"), k("RELATIVE"))), ref("simple_value_specification"))
        ));
        s.add("fetch_target_list", new FetchTargetListParser(), choice(
        		ref("fetch_target_part"), ref("target_specification")
        		));//TODO
        s.add("fetch_target_part", null, ref("target_specification"), k(","), ref("fetch_target_list"));
        s.add("target_specification",null, choice(
        		ref("host_parameter_specification"),
        		ref("sql_parameter_reference"),
        		ref("column_reference"),
        		ref("sql_variable_reference"),
        		ref("dynamic_parameter_specification"),
        		ref("embedded_variable_specification")
        		) );
        s.add("host_parameter_specification", null, 
        		ref("host_parameter_name"), option(ref("indicator_parameter")));
        s.add("indicator_parameter", new IndicatorParameterParser(), 
        		option(k("INDICATOR")), ref("host_parameter_name"));
        s.add("dynamic_parameter_specification", null, 
        		k("?"));
        s.add("embedded_variable_specification", null,
        		ref("embedded_variable_name"), option(ref("indicator_variable")));
        s.add("indicator_variable", null,
        		option(k("INDICATOR")), ref("embedded_variable_name"));
        
        
        //GET DIAGNOSTICS statement
        s.addRoot("get_diagnostics_statement");
        s.add("get_diagnostics_statement", new GetDiagnosticsStatementParser(),
                k("GET"), k("DIAGNOSTICS"), ref("sql_diagnostics_information"));
        s.add("sql_diagnostics_information", null, choice(
                ref("statement_information"),
                ref("condition_information")
                ));
        s.add("statement_information", new StatementInformationParser(), choice(
        		ref("statement_information_part"),
        		ref("statement_information_item")
        		)); //TODO
        s.add("statement_information_part", null, 
        		ref("statement_information_item"), k(","), ref("statement_information_part"));
        s.add("statement_information_item", null,
        		ref("simple_target_specification"), k("="), ref("statement_information_item_name"));
        s.add("simple_target_specification", null, choice(
                ref("host_parameter_specification"),
                ref("sql_parameter_reference"),
                ref("column_reference"),
                ref("sql_variable_reference"),
                ref("embedded_variable_name")
        		));
        s.add("statement_information_item_name", null, choice(
        		k("NUMBER"),
        		k("MORE"),
        		k("COMMAND_FUNCTION"),
        		k("COMMAND_FUNCTION_CODE"),
        		k("DYNAMIC_FUNCTION"),
        		k("DYNAMIC_FUNCTION_CODE"),
        		k("ROW_COUNT"),
        		k("TRANSACTIONS_COMMITTED"),
        		k("TRANSACTIONS_ROLLED_BACK"),
        		k("TRANSACTION_ACTIV")
        		));
        s.add("condition_information", new ConditionInformationParser(), 
        		k("EXCEPTION"), ref("condition_number"), ref("condition_information_list")); //TODO
        s.add("condition_number", null, 
        		ref("simple_value_specification"));
        s.add("condition_information_list", null, 
        		choice(ref("condition_information_part"), ref("condition_information_item")));
        s.add("condition_information_part", null, 
        		ref("condition_information_item"), k(","), ref("condition_information_list"));
        s.add("condition_information_item", null, 
        		ref("simple_target_specification"),
        		k("="),
        		ref("condition_information_item_name"));
        s.add("condition_information_item_name", null,choice(
        		k("CATALOG_NAME"),
        		k("CLASS_ORIGIN"),
        		k("COLUMN_NAME"),
        		k("CONDITION_IDENTIFIER"),
        		k("CONDITION_NUMBER"),
        		k("CONNECTION_NAME"),
        		k("CONSTRAINT_CATALOG"),
        		k("CONSTRAINT_NAME"),
        		k("CONSTRAINT_SCHEMA"),
        		k("CURSOR_NAME"),
        		k("MESSAGE_LENGTH"),
        		k("MESSAGE_OCTET_LENGTH"),
        		k("MESSAGE_TEXT"),
        		k("PARAMETER_MODE"),
        		k("PARAMETER_NAME"),
        		k("PARAMETER_ORDINAL_POSITION"),
        		k("RETURNED_SQLSTATE"),
        		k("ROUTINE_CATALOG"),
        		k("ROUTINE_NAME"),
        		k("ROUTINE_SCHEMA"),
        		k("SCHEMA_NAME"),
        		k("SERVER_NAME"),
        		k("SPECIFIC_NAME"),
        		k("SUBCLASS_ORIGIN"),
        		k("TABLE_NAME"),
        		k("TRIGGER_CATALOG"),
        		k("TRIGGER_NAME"),
        		k("TRIGGER_SCHEM")
        		));

        // GRANT statement
        s.addRoot("grant_statement");
        s.add("grant_statement", null, choice(
                ref("grant_privilege_statement"),
                ref("grant_role_statement")
                ));
        s.add("grant_privilege_statement", new GrantPrivilegeStatementParser(),
                k("GRANT"), ref("previleges"), k("TO"), ref("grantee_list"),
                option(rule(k("WITH"), k("HIERARCHY"), k("OPTION"))),
                option(rule(k("WITH"), k("GRANT"), k("OPTION"))),
                option(rule(k("GRANTED"), k("BY"), ref("grantor")))); //TODO
        s.add("grantee_list", null, 
        		choice(ref("grantee_part"), ref("grantee")));
        s.add("grantee_part", null, ref("grantee"),k(","), ref("grantee_list"));
        
        s.add("grantor", null, choice(
                k("CURRENT_USER"),
                k("CURRENT_ROLE")
                ));
        s.add("grant_role_statement", new GrantRoleStatementParser(),
                k("GRANT"), ref("role_granted_list"), k("TO"), ref("grantee"), ref("grantee_list"),
                option(rule(k("WITH"), k("ADMIN"), k("OPTION"))),
                option(rule(k("GRANTED"), k("BY"), k("grantor")))
                ); //TODO
        s.add("role_granted_list", null, choice(
        		ref("role_granted_part"),
        		ref("role_granted")
        		));
        s.add("role_granted_part", null, 
        		ref("role_granted"), k(","), ref("role_granted_list"));
        
        s.add("grantee", null, choice(
                k("PUBLIC"),
                ref("authorization_identifier")));

        // OPEN statement
        s.addRoot("open_statement");
        s.add("open_statement", new OpenStatementParser(),
                k("OPEN"), ref("cursor_name"));
        
        // ORDER BY clause
        s.add("order_by_clause", new OrderByClauseParser(),
                k("ORDER"), k("BY"), ref("sort_specification_list"));
        s.add("sort_specification_list", new SortSpecificationListParser(), choice(
        		ref("sort_specification_part"), ref("sort_specification")
        )); //TODO
        s.add("sort_specification_part", null, 
        		ref("sort_specification"), k(","), ref("sort_specification_list"));
        s.add("sort_specification", null, 
        		ref("sort_key"), option(ref("ordering_specification")));
        s.add("sort_key", null, ref("value_expression"));
        s.add("ordering_specification", null, choice(
        		k("ASC"), k("DESC")
        		));
        

        // PREPARE statement
        s.addRoot("prepare_statement");
        s.add("prepare_statement", new PrepareStatementParser(),
                k("PREPARE"), ref("sql_statement_name"), k("FROM"), k("sql_statement_variable"));

        // REVOKE statement
        s.addRoot("revoke_statement");
        s.add("revoke_statement", new RevokeStatementParser(), choice(
                ref("revoke_privilege_statement"),
                ref("revoke_role_statement")
                ));
        s.add("revoke_privilege_statement", new RevokePrivilegeStatementParser(),
                k("REVOKE"), option(ref("revoke_option_extension")), ref("privileges"), k("FROM"), ref("grantee_list"),
                option(rule(k("GRANTED"), k("BY"), k("grantor"))), ref("drop_behavior")
                ); //TODO
        s.add("revoke_option_extension", null, choice(
                rule(k("GRANT"), k("OPTION"), k("FOR")),
                rule(k("HIERARCHY"), k("OPTION"), k("FOR"))
                ));
        s.add("privileges", new PrivilegesParser(),
                ref("object_privileges"), k("ON"), ref("object_name"));
        s.add("object_privileges", new ObjectPrivilegesParser(), choice(
                rule(k("ALL"), k("PRIVILEGES")),
                ref("action_list")
                ));//TODO
        s.add("action_list", null, choice(
        		ref("action_list_part"),
        		ref("action")
        		));
        s.add("action_list_part", null, 
        		ref("action"), k(","), ref("action_list"));
        		
        s.add("object_name", new ObjectNameParser(), choice(
                rule( option(k("TABLE")), ref("table_name")),
                rule( k("DOMAIN"), ref("domain_name")),
                rule( k("COLLATION"), ref("collation_name")),
                rule( k("CHARACTER"), k("SET"), ref("character_set_name")),
                rule( k("MODULE"), ref("module_name")),
                rule( k("TRANSLATION"), ref("translation_name")),
                rule( k("TYPE"), ref("user_defined_type_name")),
                ref("specific_routine_designator")
                ));
        s.add("module_name", null, ref("identifier"));//TODO
        s.add("revoke_role_statement", new RevokeRoleStatementParser(),
                k("REVOKE"), option(rule(k("ADMIN"), k("OPTION"), k("FOR"))), ref("role_revoked_list"),
                k("FROM"), ref("grantee_list"),
                option(k("GRANTED"), k("BY"),ref("grantor")), option(ref("drop_behavior"))); //TODO
        s.add("role_revoked_list", null, choice(
        		ref("role_revoked_part"), ref("role_revoked")
        		));
        s.add("role_revoked_part", null, 
        		ref("role_revoked"), k(","), ref("role_revoked_list"));
        s.add("role_revoked", null, ref("role_name"));

        // ROLLBACK statement
        s.addRoot("rollback_statement");
        s.add("rollback_statement", new RollbackStatementParser(),
                k("ROLLBACK"), option(k("WORK")), option( rule(k("AND"), option(k("NO")), k("CHAIN"))),
                option(ref("savepoint_clause")));
        s.add("savepoint_clause", new SavepointClauseParser(), 
        		k("TO"), k("SAVEPOINT"), ref("savepoint_specifier"));
        s.add("savepoint_specifier", null, ref("savepoint_name"));
        s.add("savepoint_name", null, ref("identifier"));


        //SAVEPOINT statement
        s.addRoot("savepoint_statement");
        s.add("savepoint_statement", new SavepointStatementParser(), k("SAVEPOINT"), ref("savepoint_specifier"));
        
        // Search condition - regular expression
        s.add("regular_expression", null, choice(
        		ref("regular_expression_part"),
        		ref("regular_term")
        		)); //TODO
        s.add("regular_expression_part", null,
        		ref("regular_term"), k("|"), ref("regular_expression"));
        s.add("regular_term", null, option(ref("regular_term")), ref("regular_factor"));
        s.add("regular_factor", null, choice(
        		ref("regular_primary"),
        		rule(ref("regular_primary"), k("*")),
        		rule(ref("regular_primary"), k("+"))
        		));
        s.add("regular_primary", null, choice(
        		ref("character_specifier"),
        		ref("%"),
        		ref("regular_character_set"),
        		rule(k("("), ref("regular_expression"), k(")"))
        		));
        s.add("character_specifier", null, choice(
        		ref("non_escaped_character"),
        		ref("escaped_character")
        		));
        s.add("non_escaped_character", null, ref("identifier")); //TODO
        s.add("escaped_character", null, ref("identifier"));
        
        s.add("regular_character_set", null, choice(
        		k("_"),
        		rule(k("["), repeat(ref("character_enumeration")), k("]")),
        		rule(k("["), k("^"), repeat(ref("character_enumeration")), k("]")),
        		rule(k("["), k(":"), ref("regular_character_set_identifier"), k(":"), k("]"))
        		));
        s.add("character_enumeration", null, 
        		ref("character_specifier"), option(rule(k("-"), ref("character_specifier"))));
        s.add("character_specifier", null, ref("identifier"));

        // SET CATALOG statement
        s.addRoot("set_catalog_statement");
        s.add("set_catalog_statement", new SetCatalogStatementParser(), k("SET"), ref("catalog_name_characteristics"));
        s.add("catalog_name_characteristics", new CatalogNameCharacteristicsParser(), k("CATALOG"), ref("value_specification"));

        // SET CONNECTION statement
        s.addRoot("set_connection_statement");
        s.add("set_connection_statement", new SetConnectionStatementParser(),
                k("SET"), k("CONNECTION"), ref("connection_object"));

        // SET CONSTRAINTS MODE statement
        s.addRoot("set_constraints_mode_statement");
        s.add("set_constraints_mode_statement", new SetConstraintsModeStatementParser(),
                k("SET"), k("CONSTRAINTS"), ref("constraint_name_list"), choice( k("DEFFERRED"), k("IMMEDIATE")));
        
        s.add("constraint_name_list", new ConstraintNameListParser(), choice(
                k("ALL"),
                ref("constraint_name_list_list")
                )); //TODO
        s.add("constraint_name_list_list", null, choice(
        		ref("constraint_name_part"),
        		ref("constraint_name")
        		));
        s.add("constraint_name_part", null, 
        		ref("constraint_name"), k(","), ref("constraint_name_list_list"));
        s.add("constraint_name", null, ref("schema_qualified_name"));

        // SET NAMES statement
        s.addRoot("set_names_statement");
        s.add("set_names_statement", new SetNamesStatementParser(),
                k("SET"), ref("character_set_name_characteristic"));
        s.add("character_set_name_characteristics", new CharacterSetNameCharacteristicsParser(),
                k("NAMES"), k("value_specification"));

        //SET schema statement
        s.addRoot("set_schema_statement");
        s.add("set_schema_statement", new SetSchemaStatementParser(),
                k("SET"), ref("schema_name_characteristics"));
        s.add("schema_name_characteristics", new SchemaNameCharacteristicsParser(),
                k("SCHEMA"), ref("value_specification"));

        //SET SESSION USER IDENTIFIER statement
        s.addRoot("set_session_user_identifier_statement");
        s.add("set_session_user_identifier_statement", new SetSessionUserIdentifierStatementParser(),
                k("SET"), k("SESSION"), k("AUTHORIZATION"), ref("value_specification"));

        // SET LOCAL TIME ZONE statement
        s.addRoot("set_local_time_zone_statement");
        s.add("set_local_time_zone_statement", new SetLocalTimeZoneStatementParser(),
                k("SET"), k("TIME"), k("ZONE"), ref("set_time_zone_value"));
        s.add("set_time_zone_value", null, choice(
                ref("interval_value_expression"), k("LOCAL")));
        s.add("interval_value_expression", new IntervalValueExpressionParser(),choice(
        		ref("interval_term"),
        		rule(ref("interval_value_expression_1"), k("+"), ref("interval_term_1")),
        		rule(ref("interval_value_expression_1"), k("-"), ref("interval_term_1")),
        		rule(k("("), ref("datetime_value_expression"), k("-"), ref("datetime_term"), k(")"), ref("interval_qualifer"))   		
        )); //TODO
        s.add("interval_value_expression_1", null, ref("interval_value_expression"));
        s.add("interval_term_1", null, ref("interval_term"));
        s.add("datetime_term", null, ref("datetime_factor"));
        s.add("datetime_factor", null, ref("datetime_primary"), option(ref("time_zone")));
        s.add("datetime_primary", null, choice(
        		ref("value_expression_primary"),
        		ref("datetime_value_function")
        		));
        s.add("time_zone", null, k("AT"), ref("time_zone_specifier"));
        s.add("time_zone_specifier", null, choice(
        		k("LOCAL"),
        		rule(k("TIMEZONE"), ref("interval_primary"))));
        s.add("interval_term_2", null, ref("interval_term"));
        s.add("datetime_value_expression", new DateTimeValueExpressionParser(), choice(
        		ref("datetime_term"),
        		rule(ref("interval_value_expression"), k("+"), ref("datetime_term")),
        		rule(ref("datetime_value_expression"), k("+"), ref("interval_term")),
        		rule(ref("datetime_value_expression"), k("-"), ref("interval_term"))
        ));
        s.add("interval_term", new IntervalTermParser(), choice(
        		ref("interval_factor"),
        		rule(ref("interval_term_2"), k("*"), ref("factor")),
        		rule(ref("interval_term_2"), k("/"), ref("factor")),
        		rule(ref("term"), k("*"), ref("interval_factor"))
        		));
        s.add("interval_factor", null, option(ref("sign")), ref("interval_primary"));
        s.add("interval_primary", null, choice(
        		ref("value_expression_primary"),
        		ref("interval_value_function")
        		));
        s.add("interval_value_function", null, ref("interval_absolute_value_function"));
        s.add("interval_absolute_value_function", new IntervalAbsoluteValueFunctionParser(),
        		k("ABS"), k("("), ref("interval_value_expression"), k(")"));
        
        
        

        // SET TRANSACTION statement
        s.addRoot("set_transaction_statement");
        s.add("set_transaction_statement", new SetTransationStatementParser(),
                k("SET"), option(k("LOCAL")), ref("transaction_characteristics"));
        s.add("transaction_characteristics", new TransactionCharacteristicsParser(),
                k("TRANSACTION"), ref("transaction_mode_list")); //TODO
        s.add("transaction_mode_list", null, choice(
        		ref("transaction_mode_part"),
        		ref("transaction_mode")
        		));
        s.add("transaction_mode_part", null, 
        		ref("transaction_mode"), k(","), ref("transaction_mode_list"));
        s.add("transaction_mode", null, choice(
        		ref("isolation_level"),
        		ref("transaction_access_mode"),
        		ref("diagnostics_size")
        		));
        s.add("isolation_level", null, 
        		k("ISOLATION"), k("LEVEL"), ref("level_of_isolation"));
        s.add("level_of_isolation", null, choice(
        		rule(k("READ"), k("UNCOMMITED")),
        		rule(k("READ"), k("COMMITED")),
        		rule(k("REPEATABLE"), k("READ")),
        		k("SERIALIZABLE")
        		));
        s.add("transaction_access_mode", null, choice(
        		rule(k("READ"), k("ONLY")),
        		rule(k("READ"), k("WRITE"))
        		));
        s.add("diagnostics_size", null, 
        		k("DIAGNOSTICS"), k("SIZE"), ref("number_of_conditions"));
        s.add("number_of_conditions", null, ref("simple_value_specification"));
        
        
        // SQL-client module statement
        s.add("sql_client_module_statement", new SQLClientModuleStatementParser(),
                ref("module_name_clause"),
                ref("language_clause"),
                ref("module_authorization_clause"),
                option( ref("module_path_specification")),
                option( ref("module_transform_group_specification")),
                option( repeat(ref("temporary_table_declaration"))),
                repeat( ref("module_contents"))
        );
        s.add("module_name_clause", new ModuleNameClauseParser(),
                k("MODULE"), option( ref("sql_client_module_name")), option(ref("module_character_set_specification")));
        s.add("sql_client_module_name", null, ref("identifier"));
        s.add("module_character_set_specification", new ModuleCharacterSetSpecificationParser(),
                k("NAMES"), k("ARE"),
                ref("character_set_specification"));
        s.add("language_clause", new LanguageClauseParser(), choice(
                k("ADA"),
                k("C"),
                k("COBOL"),
                k("FORTRAN"),
                k("MUMPS"),
                k("PASCAL"),
                k("PL"),
                k("SQL")
                ));
        s.add("module_authorization_clause", new ModuleAuthorizationClauseParser(), choice(
                rule( k("SCHEMA"), ref("schema_name")),
                rule( k("AUTHORIZATION"), ref("module_authorization_identifier")),
                rule( k("SCHEMA"), ref("schema_name"), k("AUTHORIZAITON"), ref("module_authorization_identifer"))
                ));

        s.add("catalog_name", null, ref("identifier"));
        s.add("module_authorization_identifier", null, 
        		ref("authorization_identifier"));
        s.add("module_path_specification", null, 
        		ref("path_specification"));
        s.add("path_specification", new PathSpecificationParser(),
        		k("PATH"), ref("schema_name_list"));
        s.add("schema_name_list", null, choice(
        		ref("schema_name_part"),
        		ref("schema_name")
        		));//TODO
        s.add("schema_name_part", null, 
        		ref("schema_name"), k(","), ref("schema_name_list"));
        
        
        s.add("module_transform_group_specification", null, ref("transform_group_specification"));
        s.add("module_contents", null, choice(
                ref("declare_cursor"),
                ref("externally_invoked_procedure"),
                ref("dynamic_declare_cursor")
                ));
        s.add("externally_invoked_procedure", new ExternallyInvokedProcedureParser(),
                k("PROCEDURE"), ref("procedure_name"), ref("host_parameter_declaration_setup"), k(";"),
                ref("sql_procedure_statement"), k(";"));
        s.add("procedure_name", null, ref("identifier"));
        s.add("host_parameter_declaration_setup", null, ref("host_parameter_declaration_list"));
        s.add("host_parameter_declaration_list", new HostParameterDeclarationListParser(),
                k("("), ref("host_parameter_declaration_list_inner"), k(")")); //TODO
        s.add("host_parameter_declaration_list_inner", null, choice(
        		ref("host_parameter_declaration_part"),
        		ref("host_parameter_declaration")
        		));
        s.add("host_parameter_declaration_part", null, 
        		ref("host_parameter_declaration"), k(","),
        		ref("host_parameter_declaration_list_inner")
        		);
        s.add("host_parameter_declaration", new HostParameterDeclarationParser(), choice(
                rule(ref("host_parameter_name"), ref("host_parameter_data_type")),
                ref("status_parameter")));
        s.add("host_parameter_name", new HostParameterNameParser(), k(":"), ref("identifier"));
        s.add("host_parameter_data_type", new HostParameterDataTypeParser(), ref("data_type"), option(ref("host_indication")));
        s.add("status_parameter", null, k("SQLSTATE"));

        // UPDATE statement - positioned
        s.addRoot("update_statement_positioned");
        s.add("update_statement_positioned", new UpdateStatementPositionedParser(),
                k("UPDATE"), ref("taget_table"), k("SET"),
                ref("set_clause_list"), k("WHERE"),
                k("CURRENT"), k("OF"), ref("cursor_name")
                );

        // UPDATE statement - dynamic
        s.addRoot("dynamic_update_statement_positioned");
        s.add("dynamic_update_statement_positioned", new DynamicUpdateStatementPositionedParser(),
                k("UPDATE"), ref("target_table"), k("SET"), ref("set_clause_list"), k("WHERE"), k("CURRENT"),
                k("OF"), ref("dynamic_cursor_name"));

        // merius - end
		
		
		// create table
		s.add("column_name", null, ref("identifier"));
		s.add("column_name_list", new ColumnNameListParser(), ref("column_name"), option(repeat(rule(k(","),
				ref("column_name")))));

		s.add("table_definition", new TableDefinitionParser(), k("CREATE"), option(ref("table_scope")), k("TABLE"),
				ref("table_name"), ref("table_contents_source"));

		s.add("table_scope", null, ref("global_or_local"), k("TEMPORARY"));
		s.add("global_or_local", null, choice(k("GLOBAL"), k("LOCAL")));

		s.add("table_contents_source", new TableContentsSourceParser(), ref("table_element_list"));

		s.add("table_element_part", null, ref("table_element"), k(","), ref("table_element_list_inner"));
		s.add("table_element_list_inner", null, choice(ref("table_element_part"), ref("table_element")));
		s.add("table_element_list", null, k("("), ref("table_element_list_inner"), k(")"));

		s.add("table_element", null, choice(ref("column_definition"), ref("table_constraint_definition"),
				ref("like_clause"), ref("column_options")));

		s.add("column_definition", new ColumnDefinitionParser(), regularId(), ref("data_type"),
				option(repeat(ref("column_constraint_definition"))));
		s.add("column_constraint_definition", new ColumnConstraintDefinitionParser(), ref("column_constraint"));
		s.add("column_constraint", new ColumnConstraintParser(), choice(rule(k("NOT"), k("NULL")),
				ref("unique_specification"), ref("references_specification"), ref("check_constraint_definition")));

		s.add("table_constraint_definition", null, ref("table_constraint"));
		// option(ref("constraint_name_definition")), ref("table_constraint"),
		// option(ref("constraint_characteristics")));

		s.add("constraint_name_definition", new ConstraintNameDefinitionParser(), k("CONSTRAINT"),
				ref("constraint_name"));
		s.add("constraint_name", null, ref("schema_qualified_name"));

		s.add("schema_qualified_name", new SchemaQualifiedNameParser(), 
				option(rule(ref("schema_name"), k("."))),
				ref("qualified_identifier"));

		s.add("qualified_identifier", null, ref("identifier"));

		s.add("table_constraint", new TableConstraintParser(), choice(ref("unique_constraint_definition"),
				ref("referential_constraint_definition"), ref("check_constraint_definition")));

		SequenceRule uniq1 = rule(ref("unique_specification"), k("("), ref("unique_column_list"), k(")"));
		SequenceRule uniq2 = rule(k("UNIQUE"), k("("), k("VALUE"), k(")"));
		s.add("unique_constraint_definition", new UniqueConstraintDefinitionParser(), choice(uniq1, uniq2));
		s.add("unique_specification", new UniqueSpecificationParser(),
				choice(k("UNIQUE"), rule(k("PRIMARY"), k("KEY"))));

		s.add("unique_column_list", null, ref("column_name_list"));

		s.add("referential_constraint_definition", new ReferentialConstraintDefinitionParser(), k("FOREIGN"), k("KEY"),
				k("("), ref("referencing_columns"), k(")"), ref("references_specification"));
		s.add("referencing_columns", null, ref("reference_column_list"));
		s.add("reference_column_list", null, ref("column_name_list"));
		s.add("references_specification", new ReferencesSpecificationParser(), k("REFERENCES"),
				ref("referenced_table_and_columns"));
		s.add("referenced_table_and_columns", new ReferencedTableAndColumnsParser(), ref("table_name"), option(rule(
				k("("), ref("reference_column_list"), k(")"))));

		s.add("check_constraint_definition", new CheckConstraintDefinitionParser(), k("CHECK"), k("("),
				ref("search_condition"), k(")"));

		s.add("data_type", null, ref("predefined_type"));
		s.add("predefined_type", null, choice(
				rule(ref("character_string_type"), option(k("CHARACTER"), k("SET"),	ref("character_set_specification"))), 
				ref("national_character_string_type"),
				ref("binary_large_object_string_type"), 
				ref("bit_string_type"), 
				ref("numeric_type"),
				ref("boolean_type"), 
				ref("datetime_type"), 
				ref("interval_type"))
				);

		Rule dataLength = rule(k("("), ref("length"), k(")"));
		Rule largeDataLength = rule(k("("), ref("large_object_length"), k(")"));
		Rule charType1 = rule(k("CHARACTER"), k("VARYING"), k("("), ref("length"), k(")"));
		Rule charType2 = rule(k("CHAR"), k("VARYING"), k("("), ref("length"), k(")"));
		Rule charType3 = rule(k("VARCHAR"), k("("), ref("length"), k(")"));
		Rule charType4 = rule(k("CHARACTER"), k("LARGE"), k("OBJECT"), option(largeDataLength));
		Rule charType5 = rule(k("CHAR"), k("LARGE"), k("OBJECT"), option(largeDataLength));
		Rule charType6 = rule(k("CLOB"), option(largeDataLength));
		Rule charType7 = rule(k("CHARACTER"), option(dataLength));
		Rule charType8 = rule(k("CHAR"), option(dataLength));

		s.add("character_string_type", new StringDataTypeParser(), choice(
				charType1, charType2, charType3, charType4,
				charType5, charType6, charType7, charType8));
		s.add("length", null, ref("unsigned_integer"));
		s.add("large_object_length", null, choice(rule(ref("unsigned_integer"), option(ref("multiplier"))),
				ref("large_object_length_token")));

		s.add("numeric_type", null, ref("exact_numeric_type"));
		s.add("datetime_type", new DateTimeDataTypeParser(), choice(k("DATE"), rule(k("TIME"), option(k("("), ref("time_precision"), k(")")),
				option(ref("with_or_without_time_zone"))), rule(k("TIMESTAMP"), option(k("("), ref("time_precision"),
				k(")")), option(ref("with_or_without_time_zone")))));

		// INSERT
		Syntax insert = new Syntax();
		insert.add("insert_statement", null, k("INSERT"), k("INTO"), ref("insertion_target"),
				ref("insert_columns_and_source"));

		// TODO: add <from subquery> and <from default>
		insert.add("insert_columns_and_source", null, ref("from_constructor"));
		// TODO: add optional <insert column types> and <override clause>
		insert.add("from_constructor", null, ref("contextually_typed_table_value_constructor"));
		insert.add("contextually_typed_table_value_constructor", null, k("VALUES"),
				ref("contextually_typed_row_value_expression_list"));

		SequenceRule numericColumnDefinition = rule(k("NUMERIC"), option(k("("), idvar(), option(k(","), idvar()),
				k(")")));
		SequenceRule integerColumnDefinition = rule(k("INTEGER"));
		SequenceRule intColumnDefinition = rule(k("INT"));
		s.add("exact_numeric_type", new ExactNumericTypeParser(), choice(numericColumnDefinition,
				integerColumnDefinition, intColumnDefinition));

		// DROP TABLE

		s.add("drop_table_statement", new DropTableParser(), k("DROP"), k("TABLE"), ref("table_name"), option(choice(
				k("CASCADE"), k("CASCADE"))));

		// INSERT

		s.add("insert_statement", new InsertStatementParser(), k("INSERT"), k("INTO"), ref("insertion_target"),
				ref("insert_columns_and_source"));

		s.add("insertion_target", null, ref("table_name"));

		s.add("insert_columns_and_source", new InsertColumnsAndSourceParser(), choice(ref("from_subquery"),
				ref("from_constructor"), ref("from_default")));

		s.add("from_constructor", new FromConstructorParser(), option(rule(k("("), ref("insert_column_list"), k(")"))),
				ref("contextually_typed_table_value_constructor"));

		s.add("insert_column_list", null, ref("column_name_list"));

		s.add("contextually_typed_table_value_constructor", new ContextuallyTypedTableValueConstructorParser(),
				k("VALUES"), ref("contextually_typed_row_value_expression_list"));

		s.add("contextually_typed_row_value_expression_list", new ContextuallyTypedRowValueExpressionListParser(),
				ref("contextually_typed_row_value_expression"), option(repeat(rule(k(","),
						ref("contextually_typed_row_value_expression")))));

		s.add("contextually_typed_row_value_expression", null, choice(ref("row_value_special_case"),
				ref("contextually_typed_row_value_constructor")));

		s.add("row_value_special_case", null, choice(ref("value_specification"), ref("value_expression")));

		s.add("value_specification", null, choice(ref("literal"), ref("general_value_specification")));
		s.add("literal", null, choice(ref("signed_numeric_literal"), ref("general_literal")));

		s.add("contextually_typed_row_value_constructor", new RowValueConstructorParser(), choice(
				ref("contextually_typed_row_value_constructor_element"), rule(k("("),
						ref("contextually_typed_row_value_constructor_element_list"), k(")"))));

		s.add("contextually_typed_row_value_constructor_element", null, choice(ref("value_expression"),
				ref("contextually_typed_value_specification")));

		s.add("contextually_typed_value_specification", null, choice(ref("implicitly_typed_value_specification"),
				ref("default_specification")));

		// merius touched
		s.add("implicitly_typed_value_specification", null, choice(ref("null_specification"), ref("empty_specification")));
		s.add("empty_specification", new EmptySpecificationParser(), k("ARRAY"), ref("left_bracket_or_trigraph"), ref("right_bracket_or_trigraph"));
		s.add("left_bracket_or_trigraph", null, choice( k("["), ref("left_bracket_trigraph")));
		s.add("right_bracket_or_trigraph", null, choice( k("]"), ref("right_bracket_trigraph")));
		s.add("left_bracket_trigraph", new LeftBracketTrigraphParser(), k("?"), k("?"), k("("));
		s.add("right_bracket_trigraph", new RightBracketTrigraphParser(), k("?"), k("?"), k(")"));
		s.add("null_specification", new NullSpecificationParser(), option(k("NOT")), k("NULL"));
		//touch end
		
		s.add("default_specification", new DefaultSpecificationParser(), k("DEFAULT"));

		s.add("contextually_typed_row_value_constructor_element_list", null,
				ref("contextually_typed_row_value_constructor_element"), option(repeat(rule(k(","),
						ref("contextually_typed_row_value_constructor_element")))));

		s.add("from_default", null, k("DEFAULT"), k("VALUES"));

		// value expression

		s.add("value_expression", null, choice(ref("numeric_value_expression")));

		s.add("numeric_value_expression", new NumericValueExpressionParser(), ref("term"), option(repeat(rule(choice(
				k("+"), k("-")), ref("numeric_value_expression")))));

		s.add("term", null, choice(ref("factor"), rule(ref("term"), k("*"), ref("factor")), rule(ref("term"), k("/"),
				ref("factor"))));

		s.add("term", new TermParser(), ref("factor"), option(repeat(rule(choice(k("*"), k("/")), ref("factor")))));

		s.add("factor", new FactorParser(), option(ref("sign")), ref("numeric_primary"));
		s.add("sign", null, choice(k("+"), k("-")));

		s.add("numeric_primary", null, choice(ref("value_expression_primary"), ref("numeric_value_function")));
		s.add("numeric_value_function", null, choice(
				ref("position_expression"),
				ref("extract_expression"),
				ref("length_expression"),
				ref("cardinality_expression"),
				ref("absolute_value_expression"),
				ref("modulus_expression")
				));
		s.add("position_expression", null, choice(
				ref("string_position_expression"),
				ref("blob_position_expression")
				));
		s.add("string_position_expression", null, 
				k("POSITION"), k("("), ref("string_value_expression"), k("IN"), ref("string_value_expression"), k(")"));
		s.add("string_value_expression", null, choice(
				ref("character_value_expression"),
				ref("bit_value_expression"),
				ref("blob_value_expression")
				));
		s.add("character_value_exprssion", null, choice(
				ref("character_factor_part"),
				ref("character_factor")
				));
		s.add("character_factor_part", null, 
				ref("character_factor"), k("||"), ref("character_value_expression"));
		s.add("character_factor", null, ref("character_primary"), option(ref("collate_clause")));
		s.add("character_primary", null, choice(
				ref("value_expression_primary"),
				ref("string_value_function")
				));
		s.add("string_value_function", null, choice(
				ref("character_value_function"),
				ref("blob_value_function"),
				ref("bit_value_function")
				));
		s.add("character_value_function", null, choice(
				ref("character_substring_function"),
				ref("regular_expression_substring_function"),
				ref("fold"),
				ref("form_of_use_conversion"),
				ref("character_translation"),
				ref("trim_function"),
				ref("character_overlay_function"),
				ref("specific_type_method")
				));
		s.add("character_substring_function", null,
				k("SUBSTRING"), k("("), ref("character_value_expression"), k("FROM"), ref("start_position"),
				option(rule(k("FOR"), ref("string_length"))), k(")"));
		s.add("start_position", null, ref("numeric_value_expression"));
		s.add("string_length", null, ref("numeric_value_expression"));
		s.add("regular_expression_substring_function", null, 
				k("SUBSTRING"), k("("), ref("character_value_expresssion"), k("SIMILAR"), ref("character_value_expression"),
				k("ESCAPE"), ref("escape_character"), k(")"));
		s.add("escape_character", null, ref("character_value_expression"));
		s.add("fold", new FoldParser(), 
				choice(k("UPPER"), k("LOWER")), k("("), ref("character_value_expression"), k(")"));
		s.add("form_of_use_conversion", null, 
				k("CONVERT"), k("("), ref("character_value_expression"), k("USING"), ref("form_of_use_conversion_name"), k(")"));
		s.add("form_of_use_conversion_name", null, ref("schema_qualified_name"));
		s.add("character_translation", null, 
				k("TRANSLATE"), k("("), ref("character_value_expression"), k("USING"), ref("translation_name"), k(")"));
		s.add("trim_function", null, 
				k("TRIM"), k("("), ref("trim_operands"), k(")"));
		s.add("trim_operands", new TrimOperandsParser(),
				option(rule( option(ref("trim_specification")), option(ref("trim_character")), k("FROM"))),
				ref("trim_source"));
		s.add("trim_specification", null, choice(
				k("LEADING"),
				k("TRAILING"),
				k("BOTH")));
		s.add("trim_character", null, ref("character_value_expression"));
		s.add("trim_source", null, ref("character_value_expression"));
		s.add("character_overlay_function", new CharacterOverlayFunctionParser(), 
				k("OVERLAY"), k("("), ref("character_value_expression"), k("PLACING"), ref("character_value_expression"),
				k("FROM"), ref("start_position"), option(rule(k("FOR"), ref("string_length"))), k(")"));
		s.add("specific_type_method", null, 
				ref("user_defined_type_value_expression"), k("."), k("SPECIFICTYPE"));
		s.add("user_defined_type_value_expression", null, ref("value_expression_primary"));
		s.add("blob_value_function", null, choice(
				ref("blob_substring_function"),
				ref("blob_trim_function"),
				ref("blob_overlay_function")
				));
		s.add("blob_substring_function", new BlobSubstringFunctionParser(),
				k("SUBSTRING"), k("("), ref("blob_value_expression"), k("FROM"), ref("start_position"),
				option(rule(k("FOR"), ref("string_length"))), k(")"));
		s.add("blob_trim_function", null, 
				k("TRIM"), k("("), ref("blob_trim_operands"), k(")"));
		s.add("blob_trim_operands", null, 
				option(rule( option(ref("trim_specification")), option(ref("trim_octet")), k("FROM"))), ref("blob_trim_source"));
		s.add("trim_octet", null, ref("blob_value_expression"));
		s.add("blob_trim_source", null, ref("blob_value_expression"));
		s.add("blob_overlay_function", null, 
				k("OVERLAY"), k("("), ref("blob_value_expression"), k("PLACING"), ref("blob_value_expression"),
				k("FROM"), ref("start_position"), option(k("FOR"), ref("string_length")), k(")"));
		s.add("bit_value_function", null, ref("bit_substring_function"));
		s.add("bit_substring_function", null, 
				k("SUBSTING"), k("("), ref("bit_value_expression"), k("FROM"), ref("start_position"),
				option(rule(k("FOR"),ref("string_length"))), k(")"));
		s.add("bit_value_expression", null, choice(
				ref("bit_factor_part"),
				ref("bit_factor")
				));
		s.add("bit_factor_part", null, 
				ref("bit_factor"), k("||"), ref("bit_value_expression"));						
		s.add("bit_factor", null, ref("bit_primary"));
		s.add("bit_primary", null, choice(
				ref("value_expression_primary"),
				ref("string_value_function")
				));
		s.add("blob_position_expression", null,
				k("POSITION"), k("("), ref("blob_value_expression"), k("IN"), ref("blob_value_expression"), k(")"));
		s.add("extract_expression", null, 
				k("EXTRACT"), k("("), ref("extract_field"), k("FROM"), ref("extract_source"), k(")"));
		s.add("extract_field", null, choice(
				ref("primary_datetime_field"),
				ref("time_zone_filed")));
		s.add("primary_datetime_field", null, choice(
				ref("non_second_primary_datetime_field"),
				k("SECOND")
				));
		s.add("non_second_primary_datetime_field", null, choice(
				k("YEAR"),
				k("MONTH"),
				k("DAY"),
				k("HOUR"),
				k("MINUTE")
				));
		s.add("time_zone_field", null, choice(
				k("TIMEZONE_HOUR"),
				k("TIMEZONE_MINUTE")));
		s.add("extract_source", null, choice(
				ref("datetime_value_expression"),
				ref("interval_value_expression")));
		
		s.add("length_expression", null, choice(
				ref("char_length_expression"),
				ref("octet_length_expression"),
				ref("bit_length_expression")
				));
		s.add("char_length_expression", null,
				choice(k("CHAR_LENGTH"), k("CHARACTER_LENGTH")), k("("), ref("string_value_expression"), k(")"));
		s.add("octet_length_expression", null, 
				k("OCTET_LENGTH"), k("("), ref("string_value_expression"), k(")"));
		s.add("bit_length_expression", null, 
				k("BIT_LENGTH"), k("("), ref("string_value_expression"), k(")"));
		s.add("cardinality_expression", null, 
				k("CARDINALITY"), k("("), ref("collection_value_expression"), k(")"));
		s.add("absolute_value_expression", null, 
				k("ABS"), k("("), ref("numeric_value_expression"), k(")") );
		s.add("modulus_expression", null, 
				k("MOD"), k("("), ref("numeric_value_expression_dividend"), k(","), ref("numeric_value_expression_divisor"), k(")"));
		s.add("numeric_value_expression_dividend", null, ref("numeric_value_expression"));
		s.add("numeric_value_expression_divisor", null, ref("numeric_value_exression"));

		s.add("value_expression_primary", null, choice(ref("parenthesized_value_expression"),
				ref("nonparenthesized_value_expression_primary")));
		s.add("parenthesized_value_expression", null, k("("), ref("value_expression"), k(")"));
		s.add("nonparenthesized_value_expression_primary", null, choice(ref("unsigned_value_specification"),
				ref("column_reference"), ref("set_function_specification"), ref("scalar_subquery"),
				ref("case_expression"), ref("cast_specification"), ref("subtype_treatment"),
				ref("attribute_or_method_reference"), ref("reference_resolution"), ref("collection_value_constructor"),
				ref("routine_invocation"), ref("field_reference"), ref("element_reference"), ref("method_invocation"),
				ref("static_method_invocation"), ref("new_specification")));

		s.add("unsigned_value_specification", null, choice(ref("unsigned_literal"), ref("general_value_specification")));
		s.add("unsigned_literal", null, choice(ref("unsigned_numeric_literal"), ref("general_literal")));
		s.add("signed_numeric_literal", new SignedNumericLiteralParser(), option(ref("sign")),
				ref("unsigned_numeric_literal"));
		s.add("unsigned_numeric_literal", new NumericLiteralParser(), choice(ref("exact_numeric_literal"),
				ref("approximate_numeric_literal")));
		s.add("exact_numeric_literal", null, choice(ref("unsigned_integer"), rule(k("."), ref("unsigned_integer"))));
		s.add("unsigned_integer", null, uint());

		s.add("column_reference", new ColumnReferenceParser(), choice(ref("basic_identifier_chain"), rule(k("MODULE"),
				k("."), ref("qualified_identifier"), k("."), ref("column_name"))));

		s.add("basic_identifier_chain", null, ref("identifier_chain"));
		s.add("identifier_chain", new IdentifierChainParser(), ref("identifier"), option(repeat(rule(k("."),
				ref("identifier")))));
		s.add("identifier", null, ref("actual_identifier"));
		s.add("actual_identifier", null, choice(ref("regular_identifier"), ref("delimited_identifier")));
		s.add("regular_identifier", null, regularId());
		s.add("delimited_identifier", new EnclosedIdentifierParser(), k("\""), doubleQuotedString(), k("\""));

		s.add("general_literal", null, choice(ref("character_string_literal")));

		// TODO: [ introducer character set specification ] prefix
		// TODO: non-quote chars and quote support
		// TODO: separator/comment support
		s.add("character_string_literal", new CharacterStringLiteralParser(), k("'"),
				option(ref("character_representation")), k("'"));

		s.add("character_representation", null, quotedString());

		// DESC

		s.add("desc_table_statement", new DescTableParser(), k("DESC"), ref("table_name"));

		// DELETE
		s.add("delete_statement_searched", new DeleteStatementParser(), k("DELETE"), k("FROM"), ref("target_table"),
				option(rule(k("WHERE"), ref("search_condition"))));

		s.add("target_table", null, ref("table_name"));
		s.add("table_name", null, ref("local_or_schema_qualified_name"));

		// TODO: optional <local or schema qualifier>
		s.add("local_or_schema_qualified_name", null, ref("qualified_identifier"));

		s.add("search_condition", null, ref("boolean_value_expression"));

		s.add("boolean_value_expression", new BooleanValueExpressionParser(), ref("boolean_term"), option(repeat(rule(
				k("OR"), ref("boolean_term")))));
		s.add("boolean_term", new BooleanTermParser(), ref("boolean_factor"), option(repeat(rule(k("AND"),
				ref("boolean_factor")))));
		s.add("boolean_factor", new BooleanFactorParser(), option(k("NOT")), ref("boolean_test"));
		s.add("boolean_test", new BooleanTestParser(), ref("boolean_primary"), option(rule(k("IS"), option(k("NOT")),
				ref("truth_value"))));
		s.add("boolean_primary", null, choice(ref("predicate"), ref("parenthesized_boolean_value_expression"),
				ref("nonparenthesized_value_expression_primary")));
		s.add("predicate", null, choice(ref("comparison_predicate"), ref("between_predicate"), ref("in_predicate"),
				ref("like_predicate"), ref("null_predicate"), ref("quantified_comparison_predicate"),
				ref("exists_predicate"), ref("unique_predicate"), ref("match_predicate"), ref("overlaps_predicate"),
				ref("similar_predicate"), ref("distinct_predicate"), ref("type_predicate")));

		s.add("parenthesized_boolean_value_expression", new BindingSelector(1), k("("),
				ref("boolean_value_expression"), k(")"));

		s.add("comparison_predicate", new ComparisonPredicateParser(), ref("row_value_expression"), ref("comp_op"),
				ref("row_value_expression"));

		s.add("in_predicate", new InPredicateParser(), ref("row_value_expression"), option(k("NOT")), k("IN"),
				ref("in_predicate_value"));
		s.add("in_predicate_value", null, choice(ref("table_subquery"), rule(k("("), ref("in_value_list"), k(")"))));
		s.add("in_value_list", null, ref("row_value_expression"), repeat(rule(k(","), ref("row_value_expression"))));

		s.add("null_predicate", new NullPredicateParser(), ref("row_value_expression"), k("IS"), option(k("NOT")),
				k("NULL"));

		s.add("row_value_expression", null, choice(ref("row_value_special_case"), ref("row_value_constructor")));
		s.add("row_value_constructor", new RowValueConstructorParser(), choice(ref("row_value_constructor_element"),
				rule(k("("), ref("row_value_constructor_element_list"), k(")"))), ref("row_subquery"));
		s.add("row_value_constructor_element", null, ref("value_expression"));
		s.add("row_value_constructor_element_list", null, ref("row_value_constructor_element"), option(repeat(rule(
				k(","), ref("row_value_constructor_element")))));
		s.add("row_subquery", null, ref("subquery"));

		s.add("comp_op", null, choice(k("="), k("<>"), k("<"), k(">"), k("<="), k(">=")));

		// SELECT

		s.add("query_specification", new SelectStatementParser(), k("SELECT"), option(ref("set_quantifier")),
				ref("select_list"), ref("table_expression"));

		s.add("select_list", null, choice(k("*"), rule(ref("select_sublist"), option(repeat(rule(k(","),
				ref("select_sublist")))))));

		s.add("select_sublist", null, choice(ref("derived_column"), ref("qualified_asterisk")));
		s.add("derived_column", null, ref("value_expression"), option(ref("as_clause")));
		s.add("as_clause", null, option(rule(k("AS"), ref("column_name"))));
		s.add("qualified_asterisk", null, choice(rule(ref("asterisked_identifier_chain"), k("."), k("*")),
				ref("all_fields_reference")));

		s.add("asterisked_identifier_chain", null, ref("asterisked_identifier"), option(repeat(rule(k("."),
				ref("asterisked_identifier")))));

		s.add("asterisked_identifier", null, ref("identifier"));
		s.add("all_fields_reference", null, ref("value_expression_primary"), k("."), k("*"));
		s.add("table_expression", null, ref("from_clause"), option(ref("where_clause")),
				option(ref("group_by_clause")), option(ref("having_clause")));

		s.add("from_clause", null, k("FROM"), ref("table_reference_list"));
		s.add("table_reference_list", null, ref("table_reference"),
				option(repeat(rule(k(","), ref("table_reference")))));

		s.add("table_reference", null, ref("table_primary"), ref("joined_table_repeater"));

		Rule columns = rule(option(k("AS")), ref("correlation_name"),
				option(k("("), ref("derived_column_list"), k(")")));

		Rule tablePrimary1 = rule(ref("table_or_query_name"), option(columns));
		Rule tablePrimary2 = rule(ref("derived_table"), columns);
		Rule tablePrimary3 = rule(ref("lateral_derived_table"), columns);
		Rule tablePrimary4 = rule(ref("collection_derived_table"), columns);
		Rule tablePrimary5 = rule(ref("only_spec"), option(columns));
		Rule tablePrimary6 = rule(k("("), ref("table_reference"), k(")"));

		s.add("table_primary", null, choice(tablePrimary1, tablePrimary2, tablePrimary3, tablePrimary4, tablePrimary5,
				tablePrimary6));

		s.add("table_or_query_name", null, choice(ref("table_name"), ref("query_name")));
		s.add("correlation_name", null, ref("identifier"));
		s.add("derived_column_list", null, ref("column_name_list"));
		s.add("derived_table", null, ref("table_subquery"));

		s.add("lateral_derived_table", null, k("LATERAL"), k("("), ref("query_expression"), k(")"));
		s.add("collection_derived_table", null, k("UNNEST"), k("("), ref("collection_value_expression"), k(")"),
				option(k("WITH"), k("ORDINALITY")));

		s.add("collection_value_expression", null, ref("value_expression_primary"));
		s.add("only_spec", null, k("ONLY"), k("("), ref("table_or_query_name"), k(")"));

		// non left-recursive form
		s.add("joined_table_repeater", null, choice(rule(ref("joined_table2"), ref("joined_table_repeater")), empty()));

		s.add("joined_table2", null, choice(ref("cross_join2"), ref("qualified_join2"), ref("natural_join2"),
				ref("union_join2")));

		s.add("cross_join2", null, k("CROSS"), k("JOIN"), ref("table_primary"));
		s.add("qualified_join2", null, option(ref("join_type")), k("JOIN"), ref("table_reference"),
				ref("join_specification"));
		s.add("natural_join2", null, k("NATURAL"), option(ref("join_type")), k("JOIN"), ref("table_primary"));
		s.add("union_join2", null, k("UNION"), k("JOIN"), ref("table_primary"));

		// original joined table descendants

		s.add("joined_table", null, choice(ref("cross_join"), ref("qualified_join"), ref("natural_join"),
				ref("union_join")));

		s.add("cross_join", null, ref("table_reference"), k("CROSS"), k("JOIN"), ref("table_primary"));
		s.add("qualified_join", null, ref("table_reference"), option(ref("join_type")), k("JOIN"),
				ref("table_reference"), ref("join_specification"));
		s.add("natural_join", null, ref("table_reference"), k("NATURAL"), option(ref("join_type")), k("JOIN"),
				ref("table_primary"));
		s.add("union_join", null, ref("table_reference"), k("UNION"), k("JOIN"), ref("table_primary"));

		s.add("join_type", null, choice(k("INNER"), rule(ref("outer_join_type"), option(k("OUTER")))));
		s.add("outer_join_type", null, choice(k("LEFT"), k("RIGHT"), k("FULL")));
		s.add("join_specification", null, choice(ref("join_condition"), ref("named_columns_join")));
		s.add("join_condition", null, k("ON"), ref("search_condition"));
		s.add("named_columns_join", null, k("USING"), k("("), ref("join_column_list"), k(")"));
		s.add("join_column_list", null, k("column_name_list"));

		s.add("where_clause", null, k("WHERE"), ref("search_condition"));
		s.add("group_by_clause", null, k("GROUP"), k("BY"), ref("grouping_element_list"));
		s.add("grouping_element_list", null, ref("grouping_element"), option(repeat(rule(k(","),
				ref("grouping_element")))));

		s.add("grouping_element", null, ref("ordinary_grouping_set"), ref("rollup_list"), ref("cube_list"),
				ref("grouping_sets_specification"), ref("grand_total"));

		s.add("grouping_column_reference", null, ref("column_reference"), option(ref("collate_clause")));
		s.add("rollup_list", null, k("ROLLUP"), k("("), ref("grouping_column_reference_list"), k(")"));
		s.add("grouping_column_reference_list", null, ref("grouping_column_reference"), option(repeat(rule(k(","),
				ref("grouping_column_reference")))));

		s.add("cube_list", null, k("CUBE"), k("("), ref("grouping_column_reference_list"), k(")"));
		s.add("grouping_sets_specification", null, k("GROUPING"), k("SETS"), k("("), ref("grouping_set_list"), k(")"));
		s.add("grouping_set_list", null, ref("grouping_set"), option(repeat(rule(k(","), ref("grouping_set")))));
		s.add("grouping_set", null, choice(ref("ordinary_grouping_set"), ref("rollup_list"), ref("cube_list"),
				ref("grouping_sets_specification"), ref("grand_total")));

		s.add("ordinary_grouping_set", null, choice(ref("grouping_column_reference"), rule(k("("),
				ref("grouping_column_reference_list"), k(")"))));

		s.add("grand_total", null, k("("), k(")"));
		s.add("concatenated_grouping", null, ref("grouping_set"), k(","), ref("grouping_set_list"));
		s.add("having_clause", null, k("HAVING"), ref("search_condition"));
		s.add("table_value_constructor", null, k("VALUES"), ref("row_value_expression_list"));
		s.add("row_value_expression_list", null, ref("row_value_expression"), option(repeat(rule(k(","),
				ref("row_value_expression")))));

		s.add("explicit_table", null, k("TABLE"), ref("table_name"));

		// subquery
		s.add("table_subquery", null, ref("subquery"));
		s.add("subquery", null, k("("), ref("query_expression"), k(")"));
		s.add("query_expression", null, option(ref("with_clause")), ref("query_expression_body"));
		s.add("query_expression_body", null, choice(ref("non-join_query_expression"), ref("joined_table")));

		Rule nonJoinQuery1 = rule(k("UNION"), option(choice(k("ALL"), k("DISTINCT"))),
				option(ref("corresponding_spec")), ref("query_term"));
		Rule nonJoinQuery2 = rule(k("EXCEPT"), option(choice(k("ALL"), k("DISTINCT"))),
				option(ref("corresponding_spec")), ref("query_term"));

		s.add("non-join_query_expression", null, ref("non-join_query_term"),
				repeat(choice(nonJoinQuery1, nonJoinQuery2)));

		Rule nonJoinQuery3 = rule(ref("query_term"), k("INTERSECT"), option(choice(k("ALL"), k("DISTINCT"))),
				option(ref("corresponding_spec")), ref("query_primary"));
		s.add("non-join_query_term", null, choice(ref("non-join_query_primary"), nonJoinQuery3));

		s.add("non-join_query_primary", null, choice(ref("simple_table"), rule(k("("),
				ref("non-join_query_expression"), k(")"))));
		s.add("simple_table", null, choice(ref("query_specification"), ref("table_value_constructor"),
				ref("explicit_table")));
		s.add("explicit_table", null, k("TABLE"), ref("table_name"));

		// update
		s.add("update_statement_searched", new UpdateStatementParser(), k("UPDATE"), ref("target_table"), k("SET"),
				ref("set_clause_list"), option(k("WHERE"), ref("search_condition")));
		s.add("set_clause_list", null, ref("set_clause"), repeat(rule(k(","), ref("set_clause"))));
		s.add("set_clause", null, choice(rule(ref("update_target"), k("="), ref("update_source")), rule(
				ref("mutated_set_clause"), k("="), ref("update_source"))));
		s.add("update_target", null, ref("object_column"));
		s.add("object_column", null, ref("column_name"));
		s.add("update_source", null, choice(ref("value_expression"), ref("contextually_typed_value_specification")));

		// alter table
		s.add("alter_table_statement", new AlterTableStatementParser(), k("ALTER"), k("TABLE"), ref("table_name"),
				ref("alter_table_action"));
		s.add("alter_table_action", null, choice(ref("add_column_definition"), ref("alter_column_definition"),
				ref("drop_column_definition"), ref("add_table_constraint_definition"),
				ref("drop_table_constraint_definition")));

		s.add("add_column_definition", new AddColumnDefinitionParser(), k("ADD"), option(k("COLUMN")), ref("column_definition"));
		s.add("alter_column_definition", new AlterColumnDefinitionParser(), k("ALTER"), option(k("COLUMN")), ref("alter_column_action"));
		s.add("alter_column_action", null, choice(ref("set_column_default_clause"), ref("drop_column_default_clause"),
				ref("add_column_scope_clause"), ref("drop_column_scope_clause")));
		s.add("set_column_default_clause", null, k("SET"), ref("default_clause"));
		s.add("drop_column_default_clause", null, k("DROP"), k("DEFAULT"));
		s.add("add_column_scope_clause", null, k("ADD"), ref("scope_clause"));
		s.add("drop_column_scope_clause", null, k("DROP"), k("SCOPE"), ref("drop_behavior"));

		s.add("drop_column_definition", new DropColumnDefinitionParser(), k("DROP"), option(k("COLUMN")), ref("column_name"),
				option(ref("drop_behavior")));
		s.add("add_table_constraint_definition", null, k("ADD"), option(k("COLUMN")), ref("column_definition"));
		s.add("drop_table_constraint_definition", null, k("DROP"), k("CONSTRAINT"), ref("constraint_name"),
				ref("drop_behavior"));
		s.add("drop_behavior", null, choice(k("CASCADE"), k("RESTRICT")));
		
		// show tables
		s.add("show_tables", new ShowTablesParser(), k("SHOW"), k("TABLES"));

		return s;
	}

	private static EnclosedStringPlaceholder quotedString() {
		return new EnclosedStringPlaceholder('\'');
	}

	private static EnclosedStringPlaceholder doubleQuotedString() {
		return new EnclosedStringPlaceholder('"');
	}

	private static RegularIdentifierPlaceholder regularId() {
		return new RegularIdentifierPlaceholder();
	}
}
