/**
 * Created by alex on 14.10.14.
 */
(function () {
    var app = angular.module('files', []);

    app.controller('FileController', function ($http) {
        var main = this;
        main.files = [];
        main.breadcrumbs = [];

        $http.get('/files').success(function (data) {
            main.files = data;
        });

        this.loadFiles = function (file) {
            $http.get('/files' + file.path).success(function (data) {
                main.setupBreadcrumbs(file.path);
                main.files = data;
            });
        };

        this.loadPath = function (path) {
            $http.get('/files' + path).success(function (data) {
                main.setupBreadcrumbs(path);
                main.files = data;
            });
        };

        this.setupBreadcrumbs = function (path) {
            if (path === null) {
                console.log('setupBreadcrumbs: path is null!');
                return;
            }
            main.breadcrumbs = [];

            path = path.split('/');
            var tmp = "";
            path.forEach(function (entry) {
                if (entry && entry.length !== 0) {
                    tmp += '/' + entry;
                    main.breadcrumbs.push({name: entry, path: tmp});
                }
            });
        };
    });
})();
