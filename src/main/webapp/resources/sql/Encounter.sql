select  encounter.`CREATEDAT`, encounter.`ENCOUNTERTYPE` , encounter.`ENCOUNTERDATE`, encounter.`COMPLETED`,
institution.`NAME`, district.`NAME`, province.`NAME`
from encounter
join institution on encounter.`INSTITUTION_ID` = institution.`ID`
join client on encounter.`CLIENT_ID`=client.`ID`
join area province on institution.`PROVINCE_ID` = province.`ID` 
join area district on institution.`DISTRICT_ID`=district.`ID`
join person on client.`PERSON_ID`=person.`ID`
join item sex on person.`SEX_ID` = sex.`ID`
;