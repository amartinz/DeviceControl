$(document).ready(function() {

    $(this).delegate("a.loadAsync", "click", function(e) {
        e.preventDefault();
        var href = encodeURIComponent($(this).attr("href"));
        var $wrap = $("#maincontent");
        $wrap
            .html("")
            .slideUp({duration:0})
            .load(href + " #maincontent > *", function() {
                $wrap.slideDown({duration:300});
            });
    });

});
