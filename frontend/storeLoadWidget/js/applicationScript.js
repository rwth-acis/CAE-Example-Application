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

// global variable stores the current graph id
var currentGraphId = -1; // -1 means "new/unsaved"

var init = function() {

  var iwcCallback = function(intent) {
    // define your reactions on incoming iwc events here
    console.log(intent);
    // received answer from graph widget
    if (intent.action == "RETURN_GRAPH") {
      storeGraph(intent.data);
    }
  };

  client = new Las2peerWidgetLibrary("http://localhost:8081/graphs",
          iwcCallback);

  $('#storeButton').on('click', function() {
    sendStoreGraphIntent();
  })

  $('#newButton').on('click', function() {
    // construct empty graph
    currentGraphId = -1;
    $('#descriptionInput').val("");
    var graph = {
      "nodes": "[]",
      "links": "[]"
    };
    sendLoadGraphIntent(graph);
  })
}

function storeGraph(graph) {

  graph = $.parseJSON(graph);
  graph.graphId = currentGraphId;
  graph.description = $('#descriptionInput').val();

  // cannot send JSON directly to LAS2peer microservice, has to be stringified
  graph = JSON.stringify(graph);
  client.sendRequest("POST", "", graph, "application/json", {}, function(data,
          type) {
    // update current graph id (in case that a new graph was stored)
    currentGraphId = parseInt(data);
    // update list
    getGraphs();
  }, function(error) {
    // this is the error callback
    console.log(error);
  })
};

function loadGraph(id) {
  client.sendRequest("GET", id, "", "application/json", {},
          function(data, type) {
            // store id and update the description input field
            currentGraphId = parseInt(data.graphId);
            $('#descriptionInput').val(data.description);
            var graph = {
              "nodes": data.nodes,
              "links": data.links
            };
            sendLoadGraphIntent(graph);
          }, function(error) {
            // this is the error callback
            console.log(error);
          })
};

function getGraphs() {
  client.sendRequest("GET", "", "", "application/json", {},
          function(data, type) {
            // add table rows
            var graphDetails = [];
            $.each(data, function(index, value) {
              graphDetails.push("<tr><td>" + value.graphId + "</td><td>"
                      + value.description + "</td></tr>");
            });
            $("#graphTable").html(graphDetails);

            // make table rows "clickable"
            $("#graphTable").find("tr").click(function() {
              // get the id
              var id = $(this).find("td").get(0).innerHTML;
              loadGraph(id);
            });
          }, function(error) {
            // this is the error callback
            console.log(error);
            $("#graphTable").html(error);
          })
};

var sendLoadGraphIntent = function(graph) {
  // convert to JSON (one cannot sent JS-objects via intents)
  graph = JSON.stringify(graph);
  client.sendIntent("LOAD_GRAPH", graph);
}

var sendStoreGraphIntent = function() {
  client.sendIntent("STORE_GRAPH", "no data");
}

$(document).ready(function() {
  init();
  getGraphs();
});
