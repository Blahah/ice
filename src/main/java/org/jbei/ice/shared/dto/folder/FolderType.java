package org.jbei.ice.shared.dto.folder;

import org.jbei.ice.shared.dto.IDTOModel;

/**
 * Type of folder sharing, which indicates where it will be displayed
 * and the kinds of operations that are permitted
 *
 * @author Hector Plahar
 */
public enum FolderType implements IDTOModel {

    PUBLIC,
    PRIVATE,
    SHARED;
}
