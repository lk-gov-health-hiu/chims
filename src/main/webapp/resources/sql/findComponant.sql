-- select id, `NAME`, `CLIENT_ID`, `DTYPE`, `ITEM_ID`, `ITEMVALUE_ID`, `PARENTCOMPONENT_ID`, `INTEGERNUMBERVALUE`, `SHORTTEXTVALUE`, `LONGNUMBERVALUE` from component where `ITEM_ID`=14960;
-- select id, `NAME`, `DTYPE`, `ITEM_ID`, `ITEMVALUE_ID`, `PARENTCOMPONENT_ID` from component where `ID`=17950;
-- 
-- select id,`NAME`,code from item where id=17651 or id=17652;

select `ID`,`DTYPE`, `ITEMCLIENT_ID`, `ITEM_ID`,`ITEMVALUE_ID` from component WHERE `ITEM_ID`=16859