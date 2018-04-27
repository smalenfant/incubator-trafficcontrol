package v13

import (
	"encoding/json"

	tc "github.com/apache/incubator-trafficcontrol/lib/go-tc"
)

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// ParametersResponse ...
type ParametersResponse struct {
	Response []Parameter `json:"response"`
}

// Parameter ...
type Parameter struct {
	ConfigFile  string          `json:"configFile" db:"config_file"`
	ID          int             `json:"id" db:"id"`
	LastUpdated tc.TimeNoMod    `json:"lastUpdated" db:"last_updated"`
	Name        string          `json:"name" db:"name"`
	Profiles    json.RawMessage `json:"profiles" db:"profiles"`
	Secure      bool            `json:"secure" db:"secure"`
	Value       string          `json:"value" db:"value"`
}

// ParameterNullable - a struct version that allows for all fields to be null, mostly used by the API side
type ParameterNullable struct {
	//
	// NOTE: the db: struct tags are used for testing to map to their equivalent database column (if there is one)
	//
	ConfigFile  *string         `json:"configFile" db:"config_file"`
	ID          *int            `json:"id" db:"id"`
	LastUpdated *tc.TimeNoMod   `json:"lastUpdated" db:"last_updated"`
	Name        *string         `json:"name" db:"name"`
	Profiles    json.RawMessage `json:"profiles" db:"profiles"`
	Secure      *bool           `json:"secure" db:"secure"`
	Value       *string         `json:"value" db:"value"`
}
