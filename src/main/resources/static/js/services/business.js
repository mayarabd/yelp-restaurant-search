angular.module('searchApp').factory('Business', function($resource) {
    return $resource('/api/search', {}, {
        query: { method: "GET", isArray: true }
    });
});