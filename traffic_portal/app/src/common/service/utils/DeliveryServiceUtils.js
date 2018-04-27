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

var DeliveryServiceUtils = function($window, propertiesModel) {

	this.protocols = {
		0: "HTTP",
		1: "HTTPS",
		2: "HTTP AND HTTPS",
		3: "HTTP TO HTTPS"
	};

	this.qstrings = {
		0: "USE",
		1: "IGNORE",
		2: "DROP"
	};

	this.openCharts = function(ds, $event) {
		if ($event) {
			$event.stopPropagation(); // this kills the click event so it doesn't trigger anything else
		}
		$window.open(
			propertiesModel.properties.deliveryServices.charts.baseUrl + ds.xmlId,
			'_blank'
		);
	};

};

DeliveryServiceUtils.$inject = ['$window', 'propertiesModel'];
module.exports = DeliveryServiceUtils;
