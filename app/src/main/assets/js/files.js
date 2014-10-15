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

    app.directive('fileUpload', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/file_upload.html'
        };
    });

    app.controller('FileController', function ($scope, $http) {
        var main = this;
        main.files = [];
        main.breadcrumbs = [];
        main.currentLocation = {name: 'Home', path: '/files'};

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
                    main.currentLocation = {name: entry, path: tmp};
                    main.breadcrumbs.push(main.currentLocation);
                }
            });
        };

        addEventListener('load', this.load, false);

        this.load = function () {
            console.log('load');
            'use strict';

            var dropZone = angular.element(document.querySelector('#drop-zone'));
            var uploadForm = angular.element(document.querySelector('#js-upload-form'));

            var startUpload = function (files) {
                console.log(files)
            };

            uploadForm.addEventListener('submit', function (e) {
                var uploadFiles = document.getElementById('js-upload-files').files;
                e.preventDefault();

                startUpload(uploadFiles);
            });

            dropZone.ondrop = function (e) {
                e.preventDefault();
                this.className = 'upload-drop-zone';

                startUpload(e.dataTransfer.files);
            };

            dropZone.ondragover = function () {
                this.className = 'upload-drop-zone drop';
                return false;
            };

            dropZone.ondragleave = function () {
                this.className = 'upload-drop-zone';
                return false;
            };

            alert('loaded!');
        }
    });
})(jQuery);
