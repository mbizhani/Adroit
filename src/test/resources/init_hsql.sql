-- The schema needed for testing NamedParameterStatement in TestAdroit class. This schema is provided for Oracle database.
DROP TABLE IF EXISTS t_person;
DROP TABLE IF EXISTS t_education;

CREATE TABLE t_person (
  id           INT,
  c_name       VARCHAR(255),
  d_birth_date TIMESTAMP,
  f_education  INT,

  CONSTRAINT t_person_pk PRIMARY KEY (id)
);

CREATE TABLE t_education (
  id     INT,
  c_name VARCHAR(255),

  CONSTRAINT t_education_pk PRIMARY KEY (id)
);

INSERT INTO t_education (id, c_name) VALUES (1, 'Diploma');
INSERT INTO t_education (id, c_name) VALUES (2, 'BS');
INSERT INTO t_education (id, c_name) VALUES (3, 'MS');
INSERT INTO t_education (id, c_name) VALUES (4, 'PhD');

INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (1, 'Jack', TIMESTAMP '2009-08-01 20:08:08', 1);
INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (2, 'Joe', TIMESTAMP '2000-04-08 20:08:08', 2);
INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (3, 'John', TIMESTAMP '1995-09-18 20:08:08', 3);
INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (4, 'James', TIMESTAMP '2008-11-28 20:08:08', 4);

SET DATABASE SQL SYNTAX ORA TRUE;
