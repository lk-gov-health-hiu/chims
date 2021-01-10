select `ID`,`NAME`,`CREATEDAT`,`CREATER_ID`,`EDITEDAT`
from institution
where `NAME` = "Institution"
;
select `ID`,`NAME`,`CREATEDAT`,`CREATER_ID`,`INSTITUTION_ID`
from webuser
where `INSTITUTION_ID` in (select ID
from institution
where `NAME` = "Institution");
delete 
from institution
where `NAME` = "Institution"
;
select  count(*) 
from component;