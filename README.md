# Adroit
========
The `Adroit` project is a collection of utility classes, specially used in Demeter project. Some of important classes
are as follows:

- **CalendarUtil**: Handling date conversion between `Gregorian` and `Jalali (Persian)` calendar, using
[icu4j](http://userguide.icu-project.org/datetime/calendar) library.
- **ExcelExporter**: Converting a list of data to an Excel file.
- **ObjectBuilder**: A wrapper class around classes using `method chaining` to add/modify elements easily.
- **ConfigUil**: A utility class to get application configuration from `config.properties` file located in the root
of classpath. The `IConfigKey` interface, used in `ConfigUtil` as config key, is provided to be implemented by an enum class to
apply its literals as config keys.
- **NamedParameterStatement**: Like Java's prepared statement, except using named parameters instead of `?`
- **StringEncryptorUtil**: A utility class providing `encryption/decryption`, `encoding/decoding` and `hashing` functions of strings

Some samples are provided in the test class, `TestAdroit`. For testing `NamedParameterStatement`, the sample schema is
provided in the `test/resources/init_mysql.sql` file for MySQL database.