select `ID`, `RETIRED`, `ENCOUNTER_ID`, `REFERENCECOMPONENT_ID`,`CLIENT_ID`, `COMPLETED`,`CLIENT_ID`, `INSTITUTION_ID`, `CREATEDAT`, `COMPLETED` from component
where `DTYPE` = "ClientEncounterComponentFormSet"
 order by id desc limit 10;
select id, `RETIRED`, `COMPLETED`, `CLIENT_ID` from encounter where `ID` = 53201;
SELECT * from institution where `ID`=14865;
SELECT `ID`,`PHN` from client where `ID`=151;