set foreign_key_checks=0;
drop table APIREQUEST;
drop table APROCEDURE;
drop table CLIENT;
drop table CONSOLIDATEDQUERYRESULT;
drop table ENCOUNTER;
drop TABLE INDIVIDUALQUERYRESULT;
drop table INDIVIDUALQUERYRESULT;
drop table PHN;
drop table USERTRANSACTION;
delete from COMPONENT where DTYPE in 'ClientEncounterComponentForm' ,
'ClientEncounterComponentFormSet',
'ClientEncounterComponentItem';
delete from person where id > 2;
delete from USERAREA where WEBUSER_ID > 101;
DELETE from USERPRIVILEGE where WEBUSER_ID > 101;
delete from WEBUSER where ID > 101;
set foreign_key_checks=1;
