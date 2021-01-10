select id,`name`,`ITEM_ID`,`ITEMVALUE_ID`,`CREATEDAT`,`LASTEDITEAT`,`DTYPE`,
from component 
WHERE `DTYPE` = 'ClientEncounterComponentItem'
order by id desc
limit 10;