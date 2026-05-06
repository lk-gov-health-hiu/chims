/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.facade;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.ItemType;
import lk.gov.health.phsp.enums.SelectionDataType;

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Stateless
public class ItemFacade extends AbstractFacade<Item> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ItemFacade() {
        super(Item.class);
    }

    /**
     * Imports a single dictionary item within one JTA transaction.
     * All entity lookups (parent, existing item) happen inside this transaction,
     * avoiding detached-entity + CascadeType.PERSIST conflicts that cause
     * "Transaction aborted" when the import loop calls create/edit across
     * separate transactions with a cached (detached) parent reference.
     */
    public Item importItem(String name, String code, SelectionDataType dataType,
                           String parentCode, WebUser createdBy) {
        Item parentItem = null;
        if (parentCode != null && !parentCode.trim().isEmpty()) {
            Map<String, Object> p = new HashMap<>();
            p.put("code", parentCode.trim().toLowerCase());
            // Prefer non-retired parents; fall back to retired only if nothing active exists
            parentItem = findFirstByJpql(
                    "select i from Item i where lower(i.code)=:code and i.retired=false order by i.id desc", p);
            if (parentItem == null) {
                p.clear();
                p.put("name", parentCode.trim().toLowerCase());
                parentItem = findFirstByJpql(
                        "select i from Item i where lower(i.name)=:name and i.retired=false order by i.id desc", p);
            }
            if (parentItem == null) {
                // Last resort: accept a retired item (will be un-retired so the hierarchy is visible)
                p.clear();
                p.put("code", parentCode.trim().toLowerCase());
                parentItem = findFirstByJpql(
                        "select i from Item i where lower(i.code)=:code order by i.id desc", p);
                if (parentItem == null) {
                    p.clear();
                    p.put("name", parentCode.trim().toLowerCase());
                    parentItem = findFirstByJpql(
                            "select i from Item i where lower(i.name)=:name order by i.id desc", p);
                }
                if (parentItem != null && parentItem.isRetired()) {
                    parentItem.setRetired(false);
                    edit(parentItem);
                }
            }
        }

        Map<String, Object> p2 = new HashMap<>();
        p2.put("code", code.trim().toLowerCase());
        Item importingItem = findFirstByJpql(
                "select i from Item i where lower(i.code)=:code order by i.id desc", p2);

        SelectionDataType resolvedType = (dataType != null) ? dataType : SelectionDataType.Short_Text;

        if (importingItem == null) {
            importingItem = new Item();
            importingItem.setItemType(ItemType.Dictionary_Item);
            importingItem.setParent(parentItem);
            importingItem.setName(name);
            importingItem.setCode(code.trim().toLowerCase());
            importingItem.setOrderNo(0);
            importingItem.setDataType(resolvedType);
            importingItem.setCreatedAt(new Date());
            importingItem.setCreatedBy(createdBy);
            create(importingItem);
        } else {
            importingItem.setParent(parentItem);
            importingItem.setName(name);
            importingItem.setDataType(resolvedType);
            importingItem.setRetired(false);
            importingItem.setEditedAt(new Date());
            importingItem.setEditedBy(createdBy);
            edit(importingItem);
        }
        return importingItem;
    }
}
