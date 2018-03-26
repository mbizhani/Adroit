# Adroit

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.devocative/adroit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.devocative/adroit)

The `Adroit` project is a collection of utility classes, specially used in Demeter project. Some of important classes
are as follows:

- **CalendarUtil**: Handling date conversion between `Gregorian` and `Jalali (Persian)` calendar, using
[icu4j](http://userguide.icu-project.org/datetime/calendar) library.
- **ExcelExporter**: Converting a list of data to an Excel file.
- **ObjectBuilder**: A wrapper class around classes using `method chaining` to add/modify elements easily.
- **ConfigUil**: A utility class to get application configuration from `config.properties` file located in the root
of classpath. The `IConfigKey` interface, used in `ConfigUtil`, is provided to be implemented by an enum class where
its literals are used as config keys.
- **NamedParameterStatement**: Like Java's prepared statement, except using named parameters instead of `?`
	- **Using a plugin architecture to alter default behavior and modify query**
	- **FilterPlugin**: replace `%FILTER%` value in the query string with appropriate sequential `and` clauses based on a passed `Map` object as filter
	- **PaginationPlugin**: alter query for proper pagination clause for HSQLDB, MySQL, and Oracle databases
	- **SchemaPlugin**: alter query and prepend tables with the passed schema name
- **StringEncryptorUtil**: A utility class providing `encryption/decryption`, `encoding/decoding` and `hashing` functions of strings
- **ObjectUtil**: A utility class providing `bean introspection` and other general operation regarding objects
- **AdroitXStream**: A subclass of `XStream` with two features
	- Using CDATA for tag with string body
	- A boolean converter which ignores false value.
- **LRUCache**: Using `LinkedHashMap` as its core for caching objects with more utilities.
- **SQL Helper Classes**: `ExportImportHelper` and `SqlHelper` classes are wrapper around `NamedParameterStatement`
	- **ExportImportHelper**: a class to export/import SQL result to/from XML
	- **SqlHelp**: it uses an XML file based on `XQuery` class as the SQL queries reference
	(it is hard to embed SQL queries) in Java code and lots of other helper methods for result conversion to List, Map, or List of Beans

Some samples are provided in the test class, `TestAdroit`. For testing `NamedParameterStatement`, the sample schema is
provided in the `test/resources/init_hsql.sql` file for HSQLDB database and imported on test startup using `InitDB` class.
