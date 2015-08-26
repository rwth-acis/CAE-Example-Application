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
  };
  
  client = new Las2peerWidgetLibrary("http://localhost:8080/videos", iwcCallback);
  

  // event with function call is in "getVideos"
}


// sendCreateNode
var sendCreateNode = function(videoDetails){
  var videoDetails = JSON.stringify(videoDetails);
  client.sendIntent("CREATE_NODE", videoDetails);
}


// getVideos
var getVideos = function(){
  client.sendRequest("GET", "", "", "", {},
  function(data, type) {
    // create table rows
    var videoDetails = [];
    $.each(data, function(index, value) {
      videoDetails.push(
        "<tr><td>" + value.videoId
        + "</td><td><img src='" + value.thumbnail + "' alt= '"
        + value.thumbnail
        + "' style='width:64px;height:64px'></td><td>"
        + value.community + "</td><td>" + value.uploader
        + "</td><td class='visible-lg-block'>" + value.url
        + "</td></tr>"
      );
    });
    // video table update
    $("#videoTable").html(videoDetails);
    // make table rows "clickable" (event)
    $("#videoTable").find("tr").click(function() {
      var videoDetails = [];
      // fill array with id, thumbnail and link to video (rest is not
      // needed for playback)
      videoDetails[0] = $(this).find("td").get(0).innerHTML; // id
      videoDetails[1] = $(this).find("img").attr("src"); // thumbnail
      videoDetails[2] = $(this).find("td").get(4).innerHTML; // videoLink
      // event function call
      sendCreateNode(videoDetails);
    });
  },
  function(error) {
    console.log(error);
  });
}


$(document).ready(function() {
  init();
  getVideos(); // getVideos call at startup
});
