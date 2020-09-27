select `ID` , `ITEM_ID` , `ITEMVALUE_ID`, `ENCOUNTER_ID`, `ITEMENCOUNTER_ID`, `PARENTCOMPONENT_ID`,`SHORTTEXTVALUE`
from component
where `DTYPE`="ClientEncounterComponentItem"
and `SHORTTEXTVALUE` = "30-40%"
-- and `ITEM_ID`=26192

order by id desc
limit 10;