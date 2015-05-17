var client;

var init = function () {

  var iwcCallback = function (intent) {
    // define your reactions on incoming iwc events here
    console.log(intent);
    if(intent.action=="CREATE_NODE"){
      var videoDetails = $.parseJSON(intent.data);
      createNode(videoDetails);
    }
  };
  client = new Las2peerWidgetLibrary(null, iwcCallback);
}

var sendPlaybackVideoRequest = function (videoDetails) {
  // convert to JSON (one cannot sent JS-arrays via intents)
  videoDetails= JSON.stringify(videoDetails);
  client.sendIntent("PLAYBACK_VIDEO", videoDetails);
}

$(document).ready(function () {
  init();
});
