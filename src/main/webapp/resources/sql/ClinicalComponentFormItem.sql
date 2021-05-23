select * from apirequest where `ID`=27164902;
select `ID` , `ITEM_ID` , `ITEMVALUE_ID`, `ENCOUNTER_ID`, `ITEMENCOUNTER_ID`,
 `PARENTCOMPONENT_ID`,`SHORTTEXTVALUE`, `INSTITUTIONVALUE_ID`
from component
where `DTYPE`="ClientEncounterComponentItem"
and `ID`=27141294
order by id desc
limit 10;