select  encounter.`CREATEDAT`,  encounter.`ENCOUNTERDATE`,
institution.`NAME` as "Institution_Name", district.`NAME` as "District", province.`NAME` as "Province",  itemc.`SHORTTEXTVALUE` as "CVD Risk"
from component as itemc
join component as formc on itemc.parentComponent_id = formc.id 
join component as setc on formc.parentComponent_id = setc.id 
join encounter on formc.`ENCOUNTER_ID` = encounter.`ID`
join client on encounter.`CLIENT_ID` = client.`ID`
join person on client.`PERSON_ID` = person.`ID`
join item sex on person.`SEX_ID` = sex.`ID`
join institution on encounter.`INSTITUTION_ID` = institution.`ID`
join area province on institution.`PROVINCE_ID` = province.`ID`
join area district on institution.`DISTRICT_ID`=district.`ID`
where itemc.`ITEM_ID` = 16885
and itemc.`DTYPE` = "ClientEncounterComponentItem"
order by itemc.id desc
limit 10
;