/**
 * Created by alex on 13.10.14.
 */
(function () {
    var app = angular.module('interface', ['device', 'files', 'pascalprecht.translate']);

    app.directive('mainContent', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/main_content.html'
        };
    });

    app.directive('navigationBar', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/navigation_bar.html'
        };
    });

    app.directive('awesomeFooter', function () {
        return {
            restrict: 'E',
            templateUrl: 'snippets/awesome_footer.html'
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
    $(window).scroll(function () {
        if ($(this).scrollTop() > 50) {
            $('#back-to-top').fadeIn();
        } else {
            $('#back-to-top').fadeOut();
        }
    });
    $('#back-to-top').click(function () {
        $('#back-to-top').tooltip('hide');
        $('body,html').animate({
            scrollTop: 0
        }, 800);
        return false;
    });

    $('#back-to-top').tooltip('show');

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
