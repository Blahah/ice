package org.jbei.ice.lib.permissions.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;

/**
 * Give an {@link Account} write permission to an {@link Entry}.
 * <p/>
 * If WriteUser object exists for a given Entry:Account pair, then the Account has write permission
 * to the Entry.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 * @deprecated this has been replaced by Permission
 */
@Entity
@Table(name = "permission_write_users")
@SequenceGenerator(name = "sequence", sequenceName = "permission_write_users_id_seq", allocationSize = 1)
public class WriteUser implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private int id;

    @ManyToOne
    @JoinColumn(name = "entry_id")
    private Entry entry;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public WriteUser() {

    }

    public WriteUser(Entry entry, Account account) {
        setEntry(entry);
        setAccount(account);
    }

    //getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

}