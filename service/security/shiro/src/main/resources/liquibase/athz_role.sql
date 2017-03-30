-- *******************************************************************************
-- Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
--
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Eurotech - initial API and implementation
-- *******************************************************************************

--liquibase formatted sql

--changeset role:1

CREATE TABLE athz_role (
  scope_id             		BIGINT(21) 	  UNSIGNED NOT NULL,
  id                     	BIGINT(21) 	  UNSIGNED NOT NULL,
  created_on             	TIMESTAMP(3)  NOT NULL,
  created_by             	BIGINT(21)    UNSIGNED NOT NULL,
  modified_on               TIMESTAMP(3)  NOT NULL,
  modified_by               BIGINT(21) 	  UNSIGNED NOT NULL,

  name 						VARCHAR(255)  NOT NULL,
  
  optlock                   INT UNSIGNED,
  attributes				TEXT,
  properties                TEXT,
  
  PRIMARY KEY (id)

) DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX idx_role_name ON athz_role (scope_id, name);

INSERT INTO athz_role
	VALUES
		(1, 1, NOW(), 1, NOW(), 1, 'admin', 0, '','');
		
--changeset role:2

  -- WARNING: to be kept in sync with kapua/commons/src/main/resources/liquibase/configuration.sql
  CREATE TABLE IF NOT EXISTS sys_configuration (
  scope_id          		 BIGINT(21) 	  UNSIGNED,
  id                         BIGINT(21) 	  UNSIGNED NOT NULL,
  pid						 VARCHAR(255) 	  NOT NULL,
  configurations			 TEXT,
  created_on                 TIMESTAMP(3) 	  DEFAULT 0,
  created_by                 BIGINT(21) 	  UNSIGNED NOT NULL,
  modified_on                TIMESTAMP(3) 	  NOT NULL,
  modified_by                BIGINT(21) 	  UNSIGNED NOT NULL,
  optlock                    INT UNSIGNED,
  attributes				 TEXT,
  properties                 TEXT,
  PRIMARY KEY  (id),
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX IF NOT EXISTS idx_configurationScopeId ON sys_configuration (scope_id);

INSERT INTO sys_configuration (
  SCOPE_ID,
  ID,
  PID,
  CONFIGURATIONS,
  CREATED_ON,
  CREATED_BY,
  MODIFIED_ON,
  MODIFIED_BY,
  OPTLOCK,
  ATTRIBUTES,
  PROPERTIES)
VALUES (1,
        5,
        'org.eclipse.kapua.service.authorization.role.RoleService',
        CONCAT('#', CURRENT_TIMESTAMP(), CHAR(13), CHAR(10),
        'maxNumberChildEntities=0', CHAR(13), CHAR(10),
        'infiniteChildEntities=true'),
  CURRENT_TIMESTAMP(),
  1,
  CURRENT_TIMESTAMP(),
  1,
  0,
  null,
  null);