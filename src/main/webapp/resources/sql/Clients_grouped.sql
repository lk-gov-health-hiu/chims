select client.`CREATEDAT`, institution.`NAME` as 'Instituion', province.`NAME` as 'Province', district.`NAME` as 'District', sex.`name` as "Sex", person.`DATEOFBIRTH`
from client 
join institution on client.`CREATEINSTITUTION_ID` = institution.`ID`
join area province on institution.`PROVINCE_ID` = province.`ID` 
join area district on institution.`DISTRICT_ID`=district.`ID`
join person on client.`PERSON_ID`=person.`ID`
join item sex on person.`SEX_ID` = sex.`ID`
where `CREATEINSTITUTION_ID` is not null
order by client.`ID` ;