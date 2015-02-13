/**
 * Created by alex on 14.10.14.
 */
(function () {
    var app = angular.module('device', []);

    app.controller('DeviceController', function ($http) {
        var main = this;
        main.device = null;

        $http.get('/api/device').success(function (data) {
            main.device = data;
        });

    });
})();
