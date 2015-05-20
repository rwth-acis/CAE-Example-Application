--
-- Database:  commedit 
--
-- --------------------------------------------------------

--
-- Table structure for table videodetails 
--

CREATE TABLE IF NOT EXISTS commedit.videodetails (
   videoId varchar(250) NOT NULL,
   url varchar(250) NOT NULL,
   thumbnail varchar(250) NOT NULL,
   uploader varchar(50) NOT NULL,
   tool varchar(50) NOT NULL,
   community varchar(50) NOT NULL,
   time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   description varchar(250) NOT NULL,
   PRIMARY KEY (videoId)
);

--
-- Dumping data for table videodetails 
--

INSERT INTO commedit.videodetails ( videoId,  url,  thumbnail,  uploader,  tool,  community,  time,  description ) VALUES
('1', 'http://cdn.clipcanvas.com/sample/clipcanvas_14348_H264_640x360.mp4', 'http://i.imgur.com/HngIsi8.jpg', 'Filibuster Chowder', 'restservice', 'ACIS', '2014-12-10 16:14:32', 'This is a test video'),
('2', 'http://clips.vorwaerts-gmbh.de/VfE_html5.mp4', 'http://i.imgur.com/xxAqUpz.jpg', 'Snorkel Polenta', 'restservice', 'ACIS', '2014-12-04 15:30:39', 'This is another test video'),
('3', 'http://techslides.com/demos/sample-videos/small.mp4', 'http://i.imgur.com/UucIiQc.jpg', 'Folderol Rice', 'achso', 'i5', '2014-12-12 10:35:43', 'This is a great video'),
('4', 'http://playground.html5rocks.com/samples/html5_misc/chrome_japan.ogv', 'http://i.imgur.com/ThZhD6E.jpg', 'Rigmarole Lard', 'another tool', 'i5', '2014-12-17 11:22:08', 'Some interesting description'),
('5', 'http://downloads.4ksamples.com/[2160p]%204K-HD.Club-2013-Taipei%20101%20Fireworks%20Trailer%20(4ksamples.com).mp4', 'http://i.imgur.com/CclmTGh.jpg', 'Bumblebee Oatmeal', 'unknown', 'RWTH', '2014-12-11 14:09:13', 'And a last description');
