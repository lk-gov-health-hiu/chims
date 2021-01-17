select distinct(`QUERYTYPE`), count(*) from component 
where `DTYPE` = "QueryComponent"
group by `QUERYTYPE`;
update component
set `QUERYTYPE` = 'Encounter_Count'
where `QUERYTYPE` = 'Client'
limit 10;