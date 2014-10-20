/**
 * Created by alex on 13.10.14.
 */
(function () {
    var app = angular.module('interface', ['device', 'files']);

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

    app.controller('NavigationBarController', function () {
        this.mainTab = 1;

        this.setTab = function (selectedTab) {
            this.mainTab = selectedTab;
        };

        this.isSelected = function (checkTab) {
            return this.mainTab === checkTab;
        };
    });

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

});
