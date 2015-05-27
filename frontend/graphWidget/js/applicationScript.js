var client;

var init = function() {

  var iwcCallback = function(intent) {
    // define your reactions on incoming iwc events here
    console.log(intent);
    // create a node
    if (intent.action == "CREATE_NODE") {
      var videoDetails = $.parseJSON(intent.data);
      createNode(videoDetails);
    }
    // send the current graph via IWC so that the load store widget can process
    // it
    if (intent.action == "STORE_GRAPH") {
      var graph = getGraph();
      sendGraph(graph);
    }
    // receive the graph, parse the JSON and send it to the graph script
    if (intent.action == "LOAD_GRAPH") {
      var graph = $.parseJSON(intent.data);
      graph.nodes = $.parseJSON(graph.nodes);
      graph.links = $.parseJSON(graph.links);
      setGraph(graph);
    }
  };
  client = new Las2peerWidgetLibrary(null, iwcCallback);

}

var sendPlaybackVideoRequest = function(videoDetails) {
  // convert to JSON (one cannot sent JS-arrays via intents)
  videoDetails = JSON.stringify(videoDetails);
  client.sendIntent("PLAYBACK_VIDEO", videoDetails);
}

var sendGraph = function(graph) {
  // convert to JSON (one cannot sent JS-objects via intents)
  graph = JSON.stringify(graph);
  client.sendIntent("RETURN_GRAPH", graph);
}

$(document).ready(function() {
  init();
});
