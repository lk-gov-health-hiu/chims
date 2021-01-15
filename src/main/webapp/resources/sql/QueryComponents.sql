select id,  name, code,`PARENTCOMPONENT_ID`,`QUERYTYPE`,`QUERYLEVEL` from component 
where `DTYPE` = "QueryComponent"
and code = 'number_of_encounters_of_clients_between_20_to_34_years';