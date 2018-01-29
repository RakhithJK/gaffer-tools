/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

angular.module('app').component('query', query());

function query() {

    return {
        templateUrl: 'app/query/query.html',
        controller: QueryController,
        controllerAs: 'ctrl'
    };
}

function QueryController(queryPage, operationService, types, graph, config, settings, query, functions, results, navigation, $mdDialog, loading, time, view) {

    var vm = this;
    vm.timeConfig;

    vm.$onInit = function() {
        config.get().then(function(conf) {
            vm.timeConfig = conf.time;
        });

        settings.getOpOptionKeys().then(function(keys) {
            opOptionKeys = keys;
        });
    }
    var opOptionKeys;

    vm.getSelectedOp = function() {
        return queryPage.getSelectedOperation();
    }

    vm.canExecute = function() {
        return vm.queryForm.$valid && !loading.isLoading();
    }

    vm.hasOpOptions = function() {
        return opOptionKeys && Object.keys(opOptionKeys).length > 0;
    }

    vm.execute = function() {
        var operation = createOperation();
        query.addOperation(operation);
        loading.load()
        query.execute(JSON.stringify({
            class: "uk.gov.gchq.gaffer.operation.OperationChain",
            operations: [operation, operationService.createLimitOperation(operation['options']), operationService.createDeduplicateOperation(operation['options'])],
            options: operation['options']
        }), function(data) {
            loading.finish()
            if (data.length === settings.getResultLimit()) {
                prompt(data);
            } else {
                submitResults(data);
            }
        }, function(err) {
            loading.finish();
            var errorString = 'Error executing operation';
            if (err && err !== "") {
                alert(errorString  + ": " + err.simpleMessage);
                console.log(err);
            } else {
                alert(errorString);
            }
        });
    }


    var prompt = function(data) {
        $mdDialog.show({
            template: '<result-count-warning aria-label="Result Count Warning"></result-count-warning>',
            parent: angular.element(document.body),
            clickOutsideToClose: false
        })
        .then(function(command) {
            if(command === 'results') {
                submitResults(data);
            }
        });
    }

    var submitResults = function(data) {
        graph.deselectAll();
        results.update(data);
        navigation.goTo('graph');
        queryPage.reset();
        time.resetDateRange();
        view.reset();
    }

    var createOpInput = function() {
        var opInput = [];
        var jsonVertex;
        for(var vertex in graph.getSelectedEntities()) {
            try {
               jsonVertex = JSON.parse(vertex);
            } catch(err) {
               jsonVertex = vertex;
            }
            opInput.push({
              "class": "uk.gov.gchq.gaffer.operation.data.EntitySeed",
              "vertex": jsonVertex
            });
        }
        return opInput;
    }

    var createOperation = function() {
        var selectedOp = vm.getSelectedOp()
        var op = {
             class: selectedOp.class
        };

        if(selectedOp.namedOp) {
            op.operationName = selectedOp.name;
        }

        if (selectedOp.input) {
            op.input = createOpInput();
        }

        if (selectedOp.parameters) {
            var opParams = {};
            for(name in selectedOp.parameters) {
                var valueClass = selectedOp.parameters[name].valueClass;
                var value = types.createValue(valueClass, selectedOp.parameters[name].parts);
                if (selectedOp.parameters[name].required || (value !== "" && value !== null)) {
                    opParams[name] = value;
                }
            }
            op.parameters = opParams;
        }

        if (selectedOp.view) {
            var viewEdges = view.getViewEdges();
            var viewEntities = view.getViewEntities();
            var edgeFilters = view.getEdgeFilters();
            var entityFilters = view.getEntityFilters();

            op.view = {
                globalElements: [{
                    groupBy: []
                }],
                entities: {},
                edges: {}
            };


            for(var i in viewEntities) {
                var entity = viewEntities[i];
                op.view.entities[entity] = {};

                var filterFunctions = entityFilters[entity];
                if (filterFunctions) {
                    op.view.entities[entity] = filterFunctions;
                }
            }

            for(var i in viewEdges) {
                var edge = viewEdges[i];
                op.view.edges[edge] = {};

                var filterFunctions = edgeFilters[edge];
                if (filterFunctions) {
                    op.view.edges[edge] = filterFunctions;
                }
            }

            if (time.getStartDate() !== undefined && time.getStartDate() !== null) {
                op.view.globalElements.push({
                    "preAggregationFilterFunctions": [ {
                        "predicate": {
                            "class": "uk.gov.gchq.koryphe.impl.predicate.IsMoreThan",
                            "orEqualTo": true,
                            "value": types.createJsonValue(vm.timeConfig.filter.class, time.getStartDate())
                        },
                        "selection": [ vm.timeConfig.filter.startProperty ]
                    }]
                });
            }

            if (time.getEndDate() !== undefined && time.getEndDate() !== null) {
                op.view.globalElements.push({
                    "preAggregationFilterFunctions": [ {
                        "predicate": {
                            "class": "uk.gov.gchq.koryphe.impl.predicate.IsLessThan",
                            "orEqualTo": true,
                            "value": types.createJsonValue(vm.timeConfig.filter.class, time.getEndDate())
                        },
                        "selection": [ vm.timeConfig.filter.endProperty ]
                    }]
                });
            }
        }

        if (selectedOp.inOutFlag) {
            op.includeIncomingOutGoing = queryPage.getInOutFlag();
        }

        if(queryPage.getOpOptions()) {
            op.options = queryPage.getOpOptions();
        }

        return op;
    }
}