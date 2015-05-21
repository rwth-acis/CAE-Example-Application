var client;

var init = function () {

  var iwcCallback = function (intent) {
    // define your reactions on incoming iwc events here
    console.log(intent);
    // create a node
    if(intent.action=="CREATE_NODE"){
      var videoDetails = $.parseJSON(intent.data);
      createNode(videoDetails);
    }
    // send the current graph via IWC so that the load store widget can process it
    if(intent.action=="STORE_GRAPH"){
      sendGraph(id, description, nodes, links);
    }
    // receive the graph and replace it with the current one
    if(intent.action=="LOAD_GRAPH"){
      var graph = $.parseJSON(intent.data);
      id = parseInt(graph.graphId);
      description = graph.description;
      nodes = $.parseJSON(graph.nodes);
      lastNodeId = nodes.length - 1;
      links = $.parseJSON(graph.links);
      restart();
    }
    // clear nodes and links
      if(intent.action=="NEW_GRAPH"){
      id = -1
      description = "none";
      nodes = []
      lastNodeId = -1;
      links = [];
      restart();
    }
  };
  client = new Las2peerWidgetLibrary(null, iwcCallback);

}

var sendPlaybackVideoRequest = function (videoDetails) {
  // convert to JSON (one cannot sent JS-arrays via intents)
  videoDetails = JSON.stringify(videoDetails);
  client.sendIntent("PLAYBACK_VIDEO", videoDetails);
}

var sendGraph = function (id, description, nodes, links) {
  var graph = {
    "id": id,
    "description": description,
    "nodes": nodes,
    "links": links
  };
  // convert to JSON (one cannot sent JS-objects via intents)
  graph = JSON.stringify(graph);
  client.sendIntent("RETURN_GRAPH", graph);
}


$(document).ready(function () {
  init();
});
