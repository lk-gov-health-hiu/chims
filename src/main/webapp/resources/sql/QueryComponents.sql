select id, `BYTEARRAYVALUE` , name, code,`PARENTCOMPONENT_ID` from component 
where `DTYPE` = "QueryComponent"
and code is not null
and `PARENTCOMPONENT_ID` = 1070853
order by id desc limit 20;
select name from component where id=1070853;