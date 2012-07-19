package org.jbei.ice.lib.entry.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.config.ConfigurationDAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.server.dao.hibernate.HibernateRepository;
import org.jbei.ice.shared.dto.EntryType;

/**
 * Manager to manipulate {@link Storage} objects in the database.
 * 
 * @author Timothy Ham, Hector Plahar
 */
public class StorageDAO extends HibernateRepository<Storage> {

    /**
     * Retrieve {@link Storage} object from the database by its id. Optionally, retrieve children at
     * this time.
     * 
     * @param id
     * @param fetchChildren
     *            True if children are to be fetched.
     * @return Storage object.
     * @throws DAOException
     */
    public Storage get(long id, boolean fetchChildren) throws DAOException {
        Storage result = null;
        Session session = newSession();
        try {
            Query query = session
                    .createQuery("from " + Storage.class.getName() + " where id = :id");
            query.setLong("id", id);
            result = (Storage) query.uniqueResult();
            if (fetchChildren && result != null) {
                result.getChildren().size();
            }
        } catch (Exception e) {
            String msg = "Could not get Location by id: " + id + " " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return result;
    }

    /**
     * Retrieve {@link Storage} object by its uuid.
     * 
     * @param uuid
     *            universally unique identifier
     * @return Storage object.
     * @throws DAOException
     */
    public Storage get(String uuid) throws DAOException {
        return (Storage) super.getByUUID(Storage.class, uuid);
    }

    /**
     * Retrieves {@link Storage} object representing a tube. The 2Dbarcode for a tube is unique
     * across plates so this method is expected to return a single results. Compare to wells in 96
     * well plate that have same type and index across multiple plates
     * 
     * @param barcode
     *            unique identifier for storage tube
     * @return retrieved Storage
     * @throws ManagerException
     *             on exception
     */
    public Storage retrieveStorageTube(String barcode) throws DAOException {
        List<Storage> results = retrieveStorageByIndex(barcode, StorageType.TUBE);

        if (results == null || results.isEmpty()) {
            return null;
        }

        if (results.size() > 1)
            throw new DAOException("Expecting single result, received \"" + results.size()
                    + "\" for index " + barcode);

        return results.get(0);
    }

    /**
     * Retrieve a {@link Storage} object by its index and {@link StorageType} fields.
     * 
     * @param index
     * @param type
     * @return List of Storage objects.
     * @throws ManagerException
     */
    @SuppressWarnings("unchecked")
    public List<Storage> retrieveStorageByIndex(String index, StorageType type) throws DAOException {
        List<Storage> result = null;
        Session session = newSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                    + " where index = :index and storage_type = :type");
            query.setString("index", index);
            query.setString("type", type.name());

            List<Storage> list = query.list();
            if (list != null) {
                result = list;
            }
        } catch (Exception e) {
            String msg = "Could not get Storage by index: " + index + " " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;

    }

    /**
     * Save the given {@link Storage} object in the database.
     * 
     * @param location
     * @return Saved Storage object.
     * @throws ManagerException
     */
    public Storage update(Storage location) throws DAOException {
        if (location == null) {
            return null;
        }

        if (location.getUuid() == null) {
            location.setUuid(Utils.generateUUID());
        }

        super.saveOrUpdate(location);
        return location;
    }

    /**
     * Save the given {@link Storage} object in the database.
     * 
     * @param location
     * @return Saved Storage object.
     * @throws DAOException
     */
    public Storage save(Storage location) throws DAOException {
        return (Storage) super.saveOrUpdate(location);
    }

    /**
     * Delete the given {@link Storage} object in the database.
     * 
     * @param location
     * @throws ManagerException
     */
    public void delete(Storage location) throws DAOException {
        super.delete(location);
    }

    /**
     * Retrieve all {@link Storage} objects with non-empty schemes.
     * 
     * @return List of Storage objects with schemes.
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public List<Storage> getAllStorageSchemes() throws DAOException {
        ArrayList<Storage> result = null;
        Session session = newSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                    + " storage where storage.storageType = :storageType");
            query.setParameter("storageType", StorageType.SCHEME);

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null) {
                result = (ArrayList<Storage>) list;
            }
        } catch (Exception e) {
            String msg = "Could not get all schemes " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    /**
     * Retrieve all {@link Storage} objects with schemes for a given entryType.n
     * 
     * @return List of Storage objects with schemes.
     */
    @SuppressWarnings("unchecked")
    public List<Storage> getAllStorageRoot() throws DAOException {
        ArrayList<Storage> result = null;
        Session session = newSession();
        try {
            Query query = session.createQuery("FROM " + Storage.class.getName()
                    + " storage WHERE storage.storageType = :storageType");
            query.setParameter("storageType", StorageType.GENERIC);

            @SuppressWarnings("rawtypes")
            List list = query.list();
            if (list != null)
                result = (ArrayList<Storage>) list;

        } catch (Exception e) {
            String msg = "Could not get all generic types: " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<Storage> getStorageSchemesForEntryType(String entryType) throws DAOException {
        ArrayList<Storage> result = new ArrayList<Storage>();
        Session session = newSession();
        String uuid = null;
        EntryType type = EntryType.nameToType(entryType);
        if (type == null)
            return null;

        ConfigurationDAO configurationDAO = new ConfigurationDAO();
        switch (type) {
        case STRAIN:
            uuid = configurationDAO.get(ConfigurationKey.STRAIN_STORAGE_ROOT).getValue();
            break;

        case PLASMID:
            uuid = configurationDAO.get(ConfigurationKey.PLASMID_STORAGE_ROOT).getValue();
            break;

        case PART:
            uuid = configurationDAO.get(ConfigurationKey.PART_STORAGE_ROOT).getValue();
            break;

        case ARABIDOPSIS:
            uuid = configurationDAO.get(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT).getValue();
            break;
        }

        if (uuid != null) {
            Storage parent = get(uuid);
            Query query = session.createQuery("from " + Storage.class.getName()
                    + " storage where storage.parent = :parent AND storage.storageType = "
                    + ":storageType");
            query.setParameter("parent", parent);
            query.setParameter("storageType", StorageType.SCHEME);
            result.addAll(query.list());

        }
        return result;
    }

    /**
     * Retrieve or create {@link Storage} object with parent hierarchy as specified in the template
     * scheme as specified inside the given {@link Storage} object, with given labels, ordered from
     * parent to child.
     * 
     * @param scheme
     *            {@link Storage} object with the template scheme.
     * @param labels
     *            Text labels, ordered from parent to child.
     * @return Storage object in the database.
     * @throws ManagerException
     */
    public Storage getLocation(Storage scheme, String[] labels) throws DAOException {
        Storage result = null;
        Storage parent = scheme;
        List<Storage> schemes = scheme.getSchemes();

        if (schemes.size() != labels.length) {
            throw new DAOException("Storage Scheme and label lengths are not equal");
        }
        try {
            for (int i = 0; i < labels.length; i++) {
                result = getOrCreateChildLocation(schemes.get(i), labels[i], parent);
                parent = result;
            }
        } catch (DAOException e) {
            String msg = "Could not retrieve child ";
            Logger.error(msg, e);
            // ok to return null on fail
        }

        return result;
    }

    /**
     * Retrieve or create a {@link Storage} object as a child of the given {@link Storage} parent
     * object, with the name from the template object, and index itemLabel.
     * 
     * @param template
     * @param itemLabel
     * @param parent
     * @return Storage object.
     * @throws ManagerException
     */
    private Storage getOrCreateChildLocation(Storage template, String itemLabel, Storage parent)
            throws DAOException {

        Storage result = retrieveStorageBy(template.getName(), itemLabel,
            template.getStorageType(), parent.getId());

        if (result == null) {
            result = new Storage();
            result.setName(template.getName());
            result.setIndex(itemLabel);
            result.setParent(parent);
            result.setStorageType(template.getStorageType());
            result.setOwnerEmail(parent.getOwnerEmail());
            result = save(result);
        }
        return result;
    }

    /**
     * Retrieve {@link Storage} object by its name, index, {@link StorageType} and parent id.
     * 
     * @param name
     * @param index
     * @param type
     * @param parentId
     * @return Storage object.
     * @throws ManagerException
     */
    public Storage retrieveStorageBy(String name, String index, StorageType type, long parentId)
            throws DAOException {
        Session session = newSession();
        try {
            Query query = session.createQuery("from " + Storage.class.getName()
                    + " storage where storage.name = :name and storage.index = :index and "
                    + "storage.storageType = :storageType and parent_id = :parentId");
            query.setString("index", index);
            query.setString("name", name);
            query.setParameter("storageType", type);
            query.setLong("parentId", parentId);

            Storage result = (Storage) query.uniqueResult();

            return result;
        } catch (Exception e) {
            String msg = "Could not retrieve storage " + e.toString();
            Logger.error(msg, e);
            throw new DAOException(msg);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Retrieve the root level {@link Storage} object with the scheme that is used by the given
     * {@link Storage} object.
     * 
     * @param storage
     * @return Root level Storage object.
     */
    public static Storage getSchemeContainingParentStorage(Storage storage) {
        if (storage == null) {
            return null;
        }
        Storage result = null;
        Storage current = storage;
        while (true) {
            if (current.getStorageType() == StorageType.SCHEME) {
                result = current;
                break;
            } else {
                current = current.getParent();
                if (current == null) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Retrieve the parent {@link Storage} objects of a given {@link Storage} object, up to, but
     * excluding the Storage object containing the scheme.
     * <p/>
     * Useful for getting all the parents of a Storage object, except the scheme containing root
     * object.
     * 
     * @param storage
     * @return parent storage object.
     */
    public static List<Storage> getStoragesUptoScheme(Storage storage) {
        if (storage == null) {
            return null;
        }
        ArrayList<Storage> result = new ArrayList<Storage>();
        Storage current = storage;
        while (current != null && current.getStorageType() != StorageType.SCHEME) {
            result.add(current);
            current = current.getParent();
        }
        return result;
    }

    /**
     * Check if the {@link Storage} object given agrees with the scheme as specified by its parents.
     * 
     * @param storage
     * @return True if the scheme is in agreement with the hierarchy.
     */
    public boolean isStorageSchemeInAgreement(Storage storage) {
        boolean result = true;
        if (storage.getStorageType() == StorageType.SCHEME) {
            // should not compare scheme to itself
            return false;
        }
        Storage supposedScheme = getSchemeContainingParentStorage(storage);

        ArrayList<String> schemeNames = new ArrayList<String>();
        for (Storage item : supposedScheme.getSchemes()) {
            schemeNames.add(item.getName());
        }

        ArrayList<String> actualNames = new ArrayList<String>();
        Storage currentStorage = storage;
        while (currentStorage.getId() != supposedScheme.getId()) {
            actualNames.add(currentStorage.getName());
            currentStorage = currentStorage.getParent();
        }
        Collections.reverse(actualNames);

        String schemeName = null;
        String actualName = null;
        if (schemeNames.size() == actualNames.size()) {
            int index = 0;
            while (index < actualNames.size()) {
                schemeName = schemeNames.get(index);
                actualName = actualNames.get(index);
                if (actualName.equals(schemeName)) {
                    index++;
                } else {
                    // name doesn't match
                    result = false;
                    break;
                }
            }
        } else { // sizes don't match
            result = false;
        }
        return result;
    }
}