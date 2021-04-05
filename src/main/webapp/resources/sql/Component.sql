select id, name, `DTYPE` , `CREATEDAT`, `REFERENCECOMPONENT_ID`,`PARENTCOMPONENT_ID`
from component
order by id desc
limit 100;