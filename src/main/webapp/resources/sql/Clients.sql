select `ID`, `CREATEDAT` ,`CREATEINSTITUTION_ID`, `CREATEDON`
from client 
where `CREATEDON` is not null
order by id desc
limit 100;