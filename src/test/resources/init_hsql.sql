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

INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (1, 'Jack', NULL, 1);
INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (2, 'Joe', NULL, 2);
INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (3, 'John', NULL, 3);
INSERT INTO t_person (id, c_name, d_birth_date, f_education) VALUES (4, 'James', NULL, 4);

SET DATABASE SQL SYNTAX ORA TRUE;
