/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *  
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/

CREATE TABLE athz_role_permission (
  scope_id             		BIGINT(21) 	  UNSIGNED NOT NULL,
  id                     	BIGINT(21) 	  UNSIGNED NOT NULL,
  
  created_on             	TIMESTAMP(3)  NOT NULL,
  created_by_type			VARCHAR(64)   NOT NULL,
  created_by_id            	BIGINT(21)    UNSIGNED NOT NULL,
  
  role_id             	    BIGINT(21) 	  UNSIGNED,
  
  domain					VARCHAR(64),
  action					VARCHAR(64),
  target_scope_id		    BIGINT(21)	  UNSIGNED,
  group_id             	    BIGINT(21) 	  UNSIGNED,
  
  PRIMARY KEY (id),
--  FOREIGN KEY (role_id) REFERENCES athz_role(id) ON DELETE CASCADE
) DEFAULT CHARSET=utf8;

CREATE UNIQUE INDEX idx_role_permission_scope_id ON athz_role_permission (role_id, domain, action, target_scope_id, group_id);
