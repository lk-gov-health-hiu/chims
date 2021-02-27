select count(*), dtype from component where `REFERENCECOMPONENT_ID` is null group by dtype;
select count(*), dtype from component where `REFERENCECOMPONENT_ID` is not null group by dtype;