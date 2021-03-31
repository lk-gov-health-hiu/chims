select  encounter.`CREATEDAT`,  encounter.`ENCOUNTERDATE`,
hosp.`NAME` as "Institution_Name", district.`NAME` as "District", province.`NAME` as "Province",  itemc.`SHORTTEXTVALUE` as "CVD Risk"
from component as itemc
join component as formc on itemc.parentComponent_id = formc.id 
join component as setc on formc.parentComponent_id = setc.id 
join encounter on formc.`ENCOUNTER_ID` = encounter.`ID`
join client on encounter.`CLIENT_ID` = client.`ID`
join person on client.`PERSON_ID` = person.`ID`
join item sex on person.`SEX_ID` = sex.`ID`
join institution as dept on encounter.`INSTITUTION_ID` = dept.`ID`
join institution as hosp on dept.`PARENT_ID` = hosp.`ID`
join area province on hosp.`PROVINCE_ID` = province.`ID`
join area district on hosp.`DISTRICT_ID`=district.`ID`
where itemc.`ITEM_ID` = 16885
and itemc.`DTYPE` = "ClientEncounterComponentItem"
order by itemc.id desc;