var app = angular.module("searchApp");

app.controller("SearchController", function ($scope, Business) {
    resetForm();

    $scope.search = function(query) {

        // Figure out which search type was submitted
        var searchType = null;
        if ($scope.searchWithAmbiance) {
            searchType = 'AMBIANCE';
        } else if ($scope.searchWithService) {
            searchType = 'SERVICE';
        } else {
            searchType = 'DEFAULT';
        }

        Business.query({
            query: query,
            searchType: searchType
        }, function(response) {
            $scope.businesses = response;
            $scope.hasResults = true;
            $scope.displayQuery = $scope.query;

            if ($scope.searchWithAmbiance) {
                $scope.displaySearchType = 'Ambiance';
            } else if ($scope.searchWithService) {
                $scope.displaySearchType = 'Service';
            } else {
                $scope.displaySearchType = null;
            }

            resetForm();
        });
    };

    function resetForm() {
        $scope.searchWithAmbiance = false;
        $scope.searchWithService = false;
        $scope.query = null;
    }
});