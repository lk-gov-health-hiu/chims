SELECT `ID`, `CREATEDAT`,`PROCESSSTARTEDAT`,`PROCESSCOMPLETEDAT`,`PROCESSCOMPLETED`,`CREATER_ID`, `PROCESSFAILED`
FROM storedqueryresult 
order by id desc LIMIT 10;
