/**
 * Created by alex on 14.10.14.
 */
(function () {
    var app = angular.module('files', []);

    app.directive('fileEntry', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/file_entry.html'
        }
    });

    app.controller('FileController', function ($http) {
        var main = this;
        main.files = [];

        $http.get('/files').success(function (data) {
            main.files = data;
        });

        this.loadFiles = function (file) {
            $http.get('/files' + file).success(function (data) {
                main.files = data;
            });
        }
    });
})();
