var client;

var init = function () {

  var iwcCallback = function (intent) {
    // define your reactions on incoming iwc events here
    console.log(intent);
  };
  client = new Las2peerWidgetLibrary("http://localhost:8080/", iwcCallback);
}

function getVideos() {
    client.sendRequest("GET",
    "videos",
    "",
    "application/json",
    {},
    function(data,type) {
      
      // add table rows
      var videoDetails = [];
      $.each(data, function(index, value) {
        videoDetails.push( "<tr><td>" + value.videoId + "</td><td><img src='" + value.thumbnail + "' alt= '" + value.thumbnail + "' style='width:64px;height:64px'></td><td>" + value.community + "</td><td>" + value.uploader + "</td><td class='visible-lg-block'>" + value.url + "</td></tr>" );
      });
      $("#videoTable").html(videoDetails);
      
      // make table rows "clickable"
      $("#videoTable").find("tr").click(function() {
      var videoDetails = [];
      // fill array with id, thumbnail and link to video (rest is not needed for playback)
      videoDetails[0] = $(this).find("td").get(0).innerHTML; // id
      videoDetails[1] = $(this).find("img").attr("src"); // thumbnail
      videoDetails[2] = $(this).find("td").get(4).innerHTML; // videoLink

      sendCreateNodeRequest(videoDetails);

      });
    },
        function(error) {
      // this is the error callback
      console.log(error);
      $("#videoTable").html(error);
    }
)};

var sendCreateNodeRequest = function (videoDetails) {
  // convert to JSON (one cannot sent JS-arrays via intents)
  videoDetails= JSON.stringify(videoDetails);
  client.sendIntent("CREATE_NODE", videoDetails);
}

$(document).ready(function () {
  init();
  getVideos();
});
