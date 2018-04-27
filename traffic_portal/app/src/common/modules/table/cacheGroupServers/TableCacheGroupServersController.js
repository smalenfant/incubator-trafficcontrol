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

var TableCacheGroupsServersController = function(cacheGroup, servers, $scope, $state, $uibModal, cacheGroupService, locationUtils, serverUtils, propertiesModel) {

	$scope.cacheGroup = cacheGroup;

	$scope.servers = servers;

	var queueServerUpdates = function(cacheGroup, cdnId) {
		cacheGroupService.queueServerUpdates(cacheGroup.id, cdnId)
			.then(
				function() {
					$scope.refresh();
				}
			);
	};

	var clearServerUpdates = function(cacheGroup, cdnId) {
		cacheGroupService.clearServerUpdates(cacheGroup.id, cdnId)
			.then(
				function() {
					$scope.refresh();
				}
			);
	};

	$scope.editServer = function(id) {
		locationUtils.navigateToPath('/servers/' + id);
	};

	$scope.refresh = function() {
		$state.reload(); // reloads all the resolves for the view
	};

	$scope.confirmQueueServerUpdates = function(cacheGroup) {
		var params = {
			title: 'Queue Server Updates: ' + cacheGroup.name,
			message: "Please select a CDN"
		};
		var modalInstance = $uibModal.open({
			templateUrl: 'common/modules/dialog/select/dialog.select.tpl.html',
			controller: 'DialogSelectController',
			size: 'md',
			resolve: {
				params: function () {
					return params;
				},
				collection: function(cdnService) {
					return cdnService.getCDNs();
				}
			}
		});
		modalInstance.result.then(function(cdn) {
			queueServerUpdates(cacheGroup, cdn.id);
		}, function () {
			// do nothing
		});
	};

	$scope.confirmClearServerUpdates = function(cacheGroup) {
		var params = {
			title: 'Clear Server Updates: ' + cacheGroup.name,
			message: "Please select a CDN"
		};
		var modalInstance = $uibModal.open({
			templateUrl: 'common/modules/dialog/select/dialog.select.tpl.html',
			controller: 'DialogSelectController',
			size: 'md',
			resolve: {
				params: function () {
					return params;
				},
				collection: function(cdnService) {
					return cdnService.getCDNs();
				}
			}
		});
		modalInstance.result.then(function(cdn) {
			clearServerUpdates(cacheGroup, cdn.id);
		}, function () {
			// do nothing
		});
	};

	$scope.showChartsButton = propertiesModel.properties.servers.charts.show;

	$scope.navigateToPath = locationUtils.navigateToPath;

	$scope.ssh = serverUtils.ssh;

	$scope.gotoMonitor = serverUtils.gotoMonitor;

	$scope.openCharts = serverUtils.openCharts;

	$scope.isOffline = serverUtils.isOffline;

	$scope.offlineReason = serverUtils.offlineReason;

	angular.element(document).ready(function () {
		$('#serversTable').dataTable({
			"aLengthMenu": [[25, 50, 100, -1], [25, 50, 100, "All"]],
			"iDisplayLength": 25,
			"columnDefs": [
				{ 'orderable': false, 'targets': 11 }
			],
			"aaSorting": []
		});
	});

};

TableCacheGroupsServersController.$inject = ['cacheGroup', 'servers', '$scope', '$state', '$uibModal', 'cacheGroupService', 'locationUtils', 'serverUtils', 'propertiesModel'];
module.exports = TableCacheGroupsServersController;
