select  encounter.`CREATEDAT`,  encounter.`ENCOUNTERDATE`, encounter.`COMPLETED`,
hospital.`NAME`, district.`NAME`, province.`NAME`
from encounter
join institution on encounter.`INSTITUTION_ID` = institution.`ID`
join institution as hospital on institution.`PARENT_ID` = hospital.`ID`
join client on encounter.`CLIENT_ID`=client.`ID`
join area province on hospital.`PROVINCE_ID` = province.`ID`
join area district on hospital.`DISTRICT_ID`=district.`ID`
join person on client.`PERSON_ID`=person.`ID`
join item sex on person.`SEX_ID` = sex.`ID`
where encounter.`ENCOUNTERTYPE` = 'Clinic_Visit'
limit 100
;