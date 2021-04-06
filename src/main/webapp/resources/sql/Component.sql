select id, name, `DTYPE` , `CREATEDAT`, `REFERENCECOMPONENT_ID`,`PARENTCOMPONENT_ID`
from component
order by id desc
limit 100;
select id, name, `DTYPE` , `CREATEDAT`, `REFERENCECOMPONENT_ID`,`PARENTCOMPONENT_ID`
from component
where id =14202864;