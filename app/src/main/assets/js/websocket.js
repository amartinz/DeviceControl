$(document).ready(function() {
  if ("WebSocket" in window) {
     console.log("WebSocket is supported!");
     var url = "ws://" + $(location).attr('hostname') + ":" + $(location).attr('port') + "/live";
     console.log("Connecting to: " + url);
     var ws = new WebSocket(url);
     ws.onopen = function() {
        ws.send("---CONNECTED---");
     };
     ws.onmessage = function(evt) {
        var received_msg = evt.data;
        console.log(received_msg);
        if (received_msg.indexOf("batteryLevel|") >= 0) {
            received_msg = received_msg.replace("batteryLevel|", "");
            $("#batteryLevel").html("&nbsp; " + received_msg + " %");
        } else if (received_msg.indexOf("batteryCharging|") >= 0) {
            received_msg = received_msg.replace("batteryCharging|", "");
            if (received_msg === "0") {
                $("#batteryCharging").addClass("fa-bolt");
                $("#batteryCharging").removeClass("fa-circle-o-notch");
                $("#batteryCharging").removeClass("fa-spin");
            } else {
                $("#batteryCharging").addClass("fa-circle-o-notch");
                $("#batteryCharging").addClass("fa-spin");
                $("#batteryCharging").removeClass("fa-bolt");
            }
        }
     };
     ws.onclose = function() {
        console.log("Connection is closed...");
     };
  } else {
     alert("WebSocket is NOT supported by your browser! Some features may be disabled!");
  }
});
