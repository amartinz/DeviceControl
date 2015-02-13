/**
 * Created by alex on 13.10.14.
 */
(function () {
    var app = angular.module('interface', ['device', 'files', 'ngRoute', 'pascalprecht.translate']);

    app.config(function ($routeProvider, $locationProvider) {
        $locationProvider.hashPrefix('!');
        $routeProvider.
            when("/index", {templateUrl: "snippets/index.html"}).
            // Environment
            when("/device", {templateUrl: "snippets/device.html"}).
            when("/files", {templateUrl: "snippets/files.html"}).
            when("/license", {templateUrl: "license.html"}).
            otherwise({redirectTo: "/index"});
    });

    app.directive('navigationBar', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/directives/navigation_bar.html'
        };
    });

    app.directive('awesomeFooter', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/directives/awesome_footer.html'
        }
    });

    app.controller('NavigationBarController', ['$translate', '$scope', function ($translate, $scope) {
        $scope.changeLanguage = function (langKey) {
            console.log(langKey);
            $translate.use(langKey);
        };

        this.mainTab = 1;

        this.setTab = function (selectedTab) {
            this.mainTab = selectedTab;
        };

        this.isSelected = function (checkTab) {
            return this.mainTab === checkTab;
        };
    }]);

    app.config(['$translateProvider', function ($translateProvider) {
        $translateProvider.useStaticFilesLoader({
            prefix: 'lang/locale-',
            suffix: '.json'
        });
        //$translateProvider.determinePreferredLanguage();
        $translateProvider.preferredLanguage('en');
        $translateProvider.fallbackLanguage('en');
    }]);
})();

$(document).ready(function () {
    var backToTop = $('#back-to-top');

    $(window).scroll(function () {
        if ($(this).scrollTop() > 50) {
            backToTop.fadeIn();
        } else {
            backToTop.fadeOut();
        }
    });
    backToTop.click(function () {
        $('body,html').animate({
            scrollTop: 0
        }, 800);
        return false;
    });

    $(".dropdown").hover(
        function () {
            $('.dropdown-menu', this).stop(true, true).slideDown("fast");
            $(this).toggleClass('open');
        },
        function () {
            $('.dropdown-menu', this).stop(true, true).slideUp("fast");
            $(this).toggleClass('open');
        }
    );

});
