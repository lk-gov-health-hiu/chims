select distinct(name), count(name)
from component 
WHERE `DTYPE`='ClientEncounterComponentItem'
group by `name`
order by count(name) desc
limit 10;