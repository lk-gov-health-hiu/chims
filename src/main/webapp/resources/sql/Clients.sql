select `ID`, `CREATEDAT` ,`CREATEINSTITUTION_ID`
from client 
where `CREATEINSTITUTION_ID` is not null
order by id desc
limit 100;