select  name, `ITEM_ID`, `ITEMENCOUNTER_ID`, `ENCOUNTER_ID`, `ITEMCLIENT_ID`, `CREATEDAT`
from component
where `DTYPE` = "ClientEncounterComponentItem" and `ITEM_ID` = 16885
order by id desc
limit 100;