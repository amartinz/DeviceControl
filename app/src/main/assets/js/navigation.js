$(document).ready(function() {

    var newHash = '';
    $mainContent = $("#maincontent");

    $(this).delegate("a.loadAsync", "click", function(e) {
        window.location.hash = $(this).attr('href').replace(/ /g, '+');
        return false;
    });

    $(window).bind('hashchange', function() {
        newHash = window.location.hash.substr(1);
        $mainContent.load("http://" + $(location).attr('hostname') + ":" + $(location).attr('port') + newHash + " #maincontent > *");
    });

});
