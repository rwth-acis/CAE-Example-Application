--
-- Database Schema:  commedit 
-- Automatically generated sql script for the service LAS2peer Video List Service, created by the CAE.
-- --------------------------------------------------------

--
-- Table structure for table videodetails.
--
CREATE TABLE commedit.videodetails (
  description varchar(250),
  url varchar(250),
  tool varchar(50),
  uploader varchar(50),
  time timestamp,
  community varchar(50),
  thumbnail varchar(250),
  videoId varchar(250),
  CONSTRAINT videoId_PK PRIMARY KEY (videoId)
);

