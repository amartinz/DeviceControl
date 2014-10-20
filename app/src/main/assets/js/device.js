/**
 * Created by alex on 14.10.14.
 */
(function () {
    var app = angular.module('device', []);

    app.directive('deviceInformation', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/device_information.html'
        }
    });

    app.controller('DeviceController', function ($http) {
        var main = this;
        main.device = null;

        $http.get('/api/device').success(function (data) {
            main.device = data;
        });

    });
})();
