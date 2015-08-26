/*
 * Copyright (c) 2015 Advanced Community Information Systems (ACIS) Group, Chair
 * of Computer Science 5 (Databases & Information Systems), RWTH Aachen
 * University, Germany All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of the ACIS Group nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

var client;

var init = function() {
  
  var iwcCallback = function(intent) {
    // define your reactions on incoming iwc events here
    console.log(intent);
    if (intent.action == "LOAD_GRAPH") {
      setGraph(intent.data);
    }
    if (intent.action == "STORE_GRAPH") {
      sendGraph(intent.data);
    }
    if (intent.action == "CREATE_NODE") {
      createNode(intent.data);
    }
  };
  
  client = new Las2peerWidgetLibrary("null", iwcCallback);
  

}


// setGraph
var setGraph = function(graph){
  graph = $.parseJSON(graph);
  graph.nodes = $.parseJSON(graph.nodes);
  graph.links = $.parseJSON(graph.links);
  links = graph.links;
  nodes = graph.nodes;
  lastNodeId = nodes.length - 1;
  initGraph();
  restart();
}


// sendGraph
var sendGraph = function(graph){
  graph = getGraph(); // we don't process intent data
  graph = JSON.stringify(graph);
  client.sendIntent("RETURN_GRAPH", graph);
}


// createNode
var createNode = function(videoDetails){
  videoDetails = $.parseJSON(videoDetails);
  node = {id: ++lastNodeId, description: videoDetails[0], thumbnail: videoDetails[1], url: videoDetails[2]};
  // not the nicest appearance idea but works for the moment
  node.x = 100;
  node.y = 100;
  nodes.push(node);
  // restart graph drawing
  restart();
}


// sendPlaybackVideoRequest
var sendPlaybackVideoRequest = function(videoDetails){
  videoDetails = JSON.stringify(videoDetails);
  client.sendIntent("PLAYBACK_VIDEO", videoDetails);
}


$(document).ready(function() {
  init();
  initGraph(); // initialize at startup
});
