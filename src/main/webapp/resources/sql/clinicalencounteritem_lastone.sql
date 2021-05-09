select  
component.`ID` as componant_id , 
component.`CREATEDAT`,
component.`LASTEDITEAT`, 
formset.`name` as formset_name, 
form.`name` as form_name, 
vitem.`name` as item_name, 
component.`ITEMVALUE_ID`
from component
INNER join item as vitem on component.`ITEM_ID` = vitem.`ID`
INNER join component as form on component.`PARENTCOMPONENT_ID`= form.`ID`
INNER join component as formset on form.`PARENTCOMPONENT_ID` = formset.`ID`
where 

component.`ID` = (select lc.id from component as lc where lc.`DTYPE` = 'ClientEncounterComponentItem' order by lc.id desc limit 1);