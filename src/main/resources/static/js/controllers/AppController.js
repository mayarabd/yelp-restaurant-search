var app = angular.module('searchApp', ['ngRoute', 'ngResource', 'angular-loading-bar']);

app.config(['$routeProvider', '$locationProvider', 'cfpLoadingBarProvider',
    function ($routeProvider, $locationProvider, $cfpLoadingBarProvider) {
    $cfpLoadingBarProvider.latencyThreshold = 100;

    $locationProvider.html5Mode(true);
    $routeProvider
        .when("/", {
            templateUrl: '/templates/search.html',
            controller : 'SearchController'
        });
}]);
