select count(*), name, `ITEM_ID`
from component
where `DTYPE` = "ClientEncounterComponentItem"
and `BOOLEANVALUE` =true
group by name,`ITEM_ID`
order by count(*) desc;