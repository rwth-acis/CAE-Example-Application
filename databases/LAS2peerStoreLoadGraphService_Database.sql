--
-- Database:  commedit 
--
-- --------------------------------------------------------

--
-- Table structure for table graphs 
--

CREATE TABLE IF NOT EXISTS commedit.graphs (
   graphId int NOT NULL,
   description varchar(250) NOT NULL,
   nodes text NOT NULL,
   links text NOT NULL,
   PRIMARY KEY (graphId)
);


--
-- Dumping data for table graphs 
--

--INSERT INTO commedit.graphs ( graphId, description, nodes, links ) VALUES
