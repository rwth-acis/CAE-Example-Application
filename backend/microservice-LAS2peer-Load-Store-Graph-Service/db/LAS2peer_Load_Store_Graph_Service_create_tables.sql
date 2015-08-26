--
-- Database Schema:  commedit 
-- Automatically generated sql script for the service LAS2peer Load Store Graph Service, created by the CAE.
-- --------------------------------------------------------

--
-- Table structure for table graphs.
--
CREATE TABLE commedit.graphs (
  links text,
  description varchar(250),
  nodes text,
  graphId int,
  CONSTRAINT graphId_PK PRIMARY KEY (graphId)
);

