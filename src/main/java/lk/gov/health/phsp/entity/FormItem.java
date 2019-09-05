/*
 * To change this license header, choose License Headers in Client Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author sunila_soft
 */
@Entity
public class FormItem extends Encounter  implements Serializable {

    @ManyToOne
    private Client project;

    
}
